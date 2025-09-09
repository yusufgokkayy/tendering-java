package com.tendering.service;

import com.tendering.dto.response.escrow.EscrowResponse;
import com.tendering.exceptionHandlers.InsufficientFundsException;
import com.tendering.exceptionHandlers.ResourceNotFoundException;
import com.tendering.model.*;
import com.tendering.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EscrowService {

    private final EscrowRepository escrowRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    /**
     * İhale bitiminde kazanan teklif için escrow oluşturur ve parayı tutar
     */
    @Transactional
    public EscrowResponse createEscrowForAuction(UUID auctionPublicId, UUID winningBidPublicId, BigDecimal commissionRate) {
        log.debug("Escrow oluşturuluyor - Auction: {}, Bid: {}", auctionPublicId, winningBidPublicId);

        // Auction ve Bid kontrolü
        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + auctionPublicId));

        Bid winningBid = bidRepository.findByPublicId(winningBidPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Teklif bulunamadı: " + winningBidPublicId));

        // Escrow zaten var mı kontrol et
        if (escrowRepository.existsByAuction(auction)) {
            throw new IllegalStateException("Bu ihale için zaten escrow mevcut");
        }

        User buyer = winningBid.getBidder();
        User seller = auction.getSeller();
        BigDecimal bidAmount = winningBid.getAmount();

        // Komisyon hesapla
        BigDecimal commissionAmount = bidAmount.multiply(commissionRate).setScale(2, BigDecimal.ROUND_HALF_UP);

        // Alıcının cüzdanından parayı tut
        Wallet buyerWallet = walletRepository.findByUser(buyer)
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı cüzdanı bulunamadı"));

        if (buyerWallet.getBalance().compareTo(bidAmount) < 0) {
            throw new InsufficientFundsException("Yetersiz bakiye. Gerekli: " + bidAmount + ", Mevcut: " + buyerWallet.getBalance());
        }

        // Escrow oluştur
        Escrow escrow = Escrow.builder()
                .auction(auction)
                .buyer(buyer)
                .seller(seller)
                .winningBid(winningBid)
                .amount(bidAmount)
                .commissionAmount(commissionAmount)
                .commissionRate(commissionRate)
                .status(Escrow.EscrowStatus.PENDING)
                .autoReleaseDate(LocalDateTime.now().plusDays(30)) // 30 gün otomatik serbest bırakma
                .build();

        Escrow savedEscrow = escrowRepository.save(escrow);

        // Parayı tut (alıcının cüzdanından düş, hold balance'a aktar)
        holdFunds(savedEscrow);

        log.info("Escrow oluşturuldu ve para tutuldu - ID: {}, Miktar: {}", savedEscrow.getPublicId(), bidAmount);

        return convertToEscrowResponse(savedEscrow);
    }

    /**
     * Escrow'daki parayı satıcıya serbest bırakır
     */
    @Transactional
    public EscrowResponse releaseEscrow(UUID escrowPublicId, String notes) {
        log.debug("Escrow serbest bırakılıyor: {}", escrowPublicId);

        Escrow escrow = escrowRepository.findByPublicId(escrowPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Escrow bulunamadı: " + escrowPublicId));

        if (escrow.getStatus() != Escrow.EscrowStatus.HELD) {
            throw new IllegalStateException("Escrow durumu serbest bırakma için uygun değil: " + escrow.getStatus());
        }

        // Satıcının cüzdanını bul veya oluştur
        Wallet sellerWallet = walletRepository.findByUser(escrow.getSeller())
                .orElseGet(() -> createWalletForUser(escrow.getSeller()));

        // Komisyon kesimi
        BigDecimal sellerAmount = escrow.getAmount().subtract(escrow.getCommissionAmount());

        // Satıcıya para aktar
        BigDecimal previousBalance = sellerWallet.getBalance();
        sellerWallet.setBalance(previousBalance.add(sellerAmount));
        walletRepository.save(sellerWallet);

        // Release transaction oluştur
        Transaction releaseTransaction = Transaction.builder()
                .wallet(sellerWallet)
                .type(Transaction.TransactionType.EARNINGS)
                .amount(sellerAmount)
                .previousBalance(previousBalance)
                .currentBalance(sellerWallet.getBalance())
                .description("İhale kazancı: " + escrow.getAuction().getTitle())
                .escrowId(escrow.getPublicId().toString())
                .status(Transaction.TransactionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        Transaction savedReleaseTransaction = transactionRepository.save(releaseTransaction);

        // Escrow durumunu güncelle
        escrow.setStatus(Escrow.EscrowStatus.RELEASED);
        escrow.setReleaseTransactionId(savedReleaseTransaction.getPublicId().toString());
        escrow.setNotes(notes);
        escrow.setCompletedAt(LocalDateTime.now());

        Escrow updatedEscrow = escrowRepository.save(escrow);

        log.info("Escrow serbest bırakıldı - ID: {}, Satıcıya aktarılan: {}", escrowPublicId, sellerAmount);

        return convertToEscrowResponse(updatedEscrow);
    }

    /**
     * Escrow'daki parayı alıcıya iade eder
     */
    @Transactional
    public EscrowResponse refundEscrow(UUID escrowPublicId, String reason) {
        log.debug("Escrow iade ediliyor: {}", escrowPublicId);

        Escrow escrow = escrowRepository.findByPublicId(escrowPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Escrow bulunamadı: " + escrowPublicId));

        if (escrow.getStatus() != Escrow.EscrowStatus.HELD) {
            throw new IllegalStateException("Escrow durumu iade için uygun değil: " + escrow.getStatus());
        }

        // Alıcının cüzdanını bul
        Wallet buyerWallet = walletRepository.findByUser(escrow.getBuyer())
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı cüzdanı bulunamadı"));

        // Parayı iade et
        BigDecimal previousBalance = buyerWallet.getBalance();
        buyerWallet.setBalance(previousBalance.add(escrow.getAmount()));
        buyerWallet.setHoldBalance(buyerWallet.getHoldBalance().subtract(escrow.getAmount()));
        walletRepository.save(buyerWallet);

        // Refund transaction oluştur
        Transaction refundTransaction = Transaction.builder()
                .wallet(buyerWallet)
                .type(Transaction.TransactionType.REFUND)
                .amount(escrow.getAmount())
                .previousBalance(previousBalance)
                .currentBalance(buyerWallet.getBalance())
                .description("İhale iadesi: " + reason)
                .escrowId(escrow.getPublicId().toString())
                .status(Transaction.TransactionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        Transaction savedRefundTransaction = transactionRepository.save(refundTransaction);

        // Escrow durumunu güncelle
        escrow.setStatus(Escrow.EscrowStatus.REFUNDED);
        escrow.setRefundTransactionId(savedRefundTransaction.getPublicId().toString());
        escrow.setNotes(reason);
        escrow.setCompletedAt(LocalDateTime.now());

        Escrow updatedEscrow = escrowRepository.save(escrow);

        log.info("Escrow iade edildi - ID: {}, İade edilen: {}", escrowPublicId, escrow.getAmount());

        return convertToEscrowResponse(updatedEscrow);
    }

    /**
     * Kullanıcının escrow'larını listeler
     */
    public List<EscrowResponse> getUserEscrows(UUID userPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        List<Escrow> escrows = escrowRepository.findByBuyerOrSeller(user);
        return escrows.stream()
                .map(this::convertToEscrowResponse)
                .collect(Collectors.toList());
    }

    /**
     * Escrow detayını getirir
     */
    public EscrowResponse getEscrowDetails(UUID escrowPublicId) {
        Escrow escrow = escrowRepository.findByPublicId(escrowPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Escrow bulunamadı: " + escrowPublicId));

        return convertToEscrowResponse(escrow);
    }

    /**
     * Parayı tutar (alıcının cüzdanından düşer, hold balance'a aktarır)
     */
    private void holdFunds(Escrow escrow) {
        Wallet buyerWallet = walletRepository.findByUser(escrow.getBuyer())
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı cüzdanı bulunamadı"));

        BigDecimal previousBalance = buyerWallet.getBalance();
        buyerWallet.setBalance(previousBalance.subtract(escrow.getAmount()));
        buyerWallet.setHoldBalance(buyerWallet.getHoldBalance().add(escrow.getAmount()));
        walletRepository.save(buyerWallet);

        // Hold transaction oluştur
        Transaction holdTransaction = Transaction.builder()
                .wallet(buyerWallet)
                .type(Transaction.TransactionType.ESCROW_HOLD)
                .amount(escrow.getAmount())
                .previousBalance(previousBalance)
                .currentBalance(buyerWallet.getBalance())
                .description("İhale ödemesi tutuldu: " + escrow.getAuction().getTitle())
                .escrowId(escrow.getPublicId().toString())
                .status(Transaction.TransactionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(holdTransaction);

        // Escrow'u HELD durumuna getir
        escrow.setStatus(Escrow.EscrowStatus.HELD);
        escrow.setHoldTransactionId(savedTransaction.getPublicId().toString());
        escrowRepository.save(escrow);
    }

    private Wallet createWalletForUser(User user) {
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .pendingBalance(BigDecimal.ZERO)
                .holdBalance(BigDecimal.ZERO)
                .totalDeposited(BigDecimal.ZERO)
                .totalWithdrawn(BigDecimal.ZERO)
                .isLocked(false)
                .build();
        return walletRepository.save(wallet);
    }

    private EscrowResponse convertToEscrowResponse(Escrow escrow) {
        return EscrowResponse.builder()
                .id(escrow.getPublicId().toString())
                .auctionId(escrow.getAuction().getPublicId().toString())
                .buyerId(escrow.getBuyer().getPublicId().toString())
                .sellerId(escrow.getSeller().getPublicId().toString())
                .winningBidId(escrow.getWinningBid() != null ? escrow.getWinningBid().getPublicId().toString() : null)
                .amount(escrow.getAmount())
                .commissionAmount(escrow.getCommissionAmount())
                .commissionRate(escrow.getCommissionRate())
                .status(escrow.getStatus().name())
                .holdTransactionId(escrow.getHoldTransactionId())
                .releaseTransactionId(escrow.getReleaseTransactionId())
                .refundTransactionId(escrow.getRefundTransactionId())
                .notes(escrow.getNotes())
                .autoReleaseDate(escrow.getAutoReleaseDate())
                .createdAt(escrow.getCreatedAt())
                .updatedAt(escrow.getUpdatedAt())
                .completedAt(escrow.getCompletedAt())
                .build();
    }
}