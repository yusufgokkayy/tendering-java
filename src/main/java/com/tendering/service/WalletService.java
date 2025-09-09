package com.tendering.service;


import com.tendering.dto.request.transaction.TransactionDTO;
import com.tendering.dto.request.wallet.DepositRequest;
import com.tendering.dto.request.wallet.WalletDTO;
import com.tendering.dto.request.wallet.WithdrawRequest;
import com.tendering.exceptionHandlers.InsufficientFundsException;
import com.tendering.exceptionHandlers.ResourceNotFoundException;
import com.tendering.exceptionHandlers.WalletLockedException;
import com.tendering.model.Transaction;
import com.tendering.model.User;
import com.tendering.model.Wallet;
import com.tendering.model.BankAccount;
import com.tendering.model.CreditCard;
import com.tendering.repository.TransactionRepository;
import com.tendering.repository.UserRepository;
import com.tendering.repository.WalletRepository;
import com.tendering.repository.BankAccountRepository;
import com.tendering.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CreditCardRepository creditCardRepository;

    public WalletDTO getWalletInfo(UUID userPublicId) {
        log.debug("Cüzdan bilgileri getiriliyor: {}", userPublicId);
        Wallet wallet = getOrCreateWallet(userPublicId);
        return convertToWalletDTO(wallet);
    }

    @Transactional
    public TransactionDTO depositFunds(UUID userPublicId, DepositRequest request) {
        log.debug("Para yatırma işlemi başlatılıyor: {}, miktar: {}", userPublicId, request.getAmount());

        Wallet wallet = getOrCreateWallet(userPublicId);

        // Cüzdan kilitli mi kontrol et
        if (Boolean.TRUE.equals(wallet.getIsLocked())) {
            throw new WalletLockedException("Cüzdan kilitli: " + wallet.getLockReason());
        }

        // Ödeme yöntemi kontrolü
        validatePaymentMethod(wallet.getUser(), request);

        // Önceki bakiye kaydet
        BigDecimal previousBalance = wallet.getBalance();

        // Bakiye güncelle
        wallet.setBalance(previousBalance.add(request.getAmount()));
        wallet.setTotalDeposited(wallet.getTotalDeposited().add(request.getAmount()));

        Wallet updatedWallet = walletRepository.save(wallet);

        // İşlem kaydı oluştur
        Transaction transaction = Transaction.builder()
                .wallet(updatedWallet)
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .previousBalance(previousBalance)
                .currentBalance(updatedWallet.getBalance())
                .description(request.getDescription())
                .referenceId(request.getReferenceId())
                .status(Transaction.TransactionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Para yatırma işlemi tamamlandı: {}, miktar: {}", userPublicId, request.getAmount());

        return convertToTransactionDTO(savedTransaction);
    }

    @Transactional
    public TransactionDTO withdrawFunds(UUID userPublicId, WithdrawRequest request) {
        log.debug("Para çekme işlemi başlatılıyor: {}, miktar: {}", userPublicId, request.getAmount());

        Wallet wallet = getOrCreateWallet(userPublicId);

        // Cüzdan kilitli mi kontrol et
        if (Boolean.TRUE.equals(wallet.getIsLocked())) {
            throw new WalletLockedException("Cüzdan kilitli: " + wallet.getLockReason());
        }

        // Yeterli bakiye var mı kontrol et
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Yetersiz bakiye. Mevcut bakiye: " + wallet.getBalance());
        }

        // Banka hesabı kontrolü
        validateBankAccountForWithdrawal(wallet.getUser(), request.getBankAccountId());

        // Önceki bakiye kaydet
        BigDecimal previousBalance = wallet.getBalance();

        // Bakiye güncelle
        wallet.setBalance(previousBalance.subtract(request.getAmount()));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(request.getAmount()));

        Wallet updatedWallet = walletRepository.save(wallet);

        // İşlem kaydı oluştur
        Transaction transaction = Transaction.builder()
                .wallet(updatedWallet)
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .previousBalance(previousBalance)
                .currentBalance(updatedWallet.getBalance())
                .description(request.getDescription())
                .referenceId(UUID.randomUUID().toString()) // Unique referans ID oluştur
                .status(Transaction.TransactionStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Para çekme işlemi tamamlandı: {}, miktar: {}", userPublicId, request.getAmount());

        return convertToTransactionDTO(savedTransaction);
    }

    public Page<TransactionDTO> getTransactionHistory(UUID userPublicId, int page, int size) {
        log.debug("İşlem geçmişi getiriliyor: {}", userPublicId);

        Wallet wallet = getOrCreateWallet(userPublicId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactions = transactionRepository.findByWalletOrderByCreatedAtDesc(wallet, pageable);

        return transactions.map(this::convertToTransactionDTO);
    }

    private Wallet getOrCreateWallet(UUID userPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        return walletRepository.findByUser(user).orElseGet(() -> {
            log.info("Kullanıcı için yeni cüzdan oluşturuluyor: {}", userPublicId);
            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .balance(BigDecimal.ZERO)
                    .pendingBalance(BigDecimal.ZERO)
                    .holdBalance(BigDecimal.ZERO)
                    .totalDeposited(BigDecimal.ZERO)
                    .totalWithdrawn(BigDecimal.ZERO)
                    .isLocked(false)
                    .build();
            return walletRepository.save(newWallet);
        });
    }

    private WalletDTO convertToWalletDTO(Wallet wallet) {
        return WalletDTO.builder()
                .id(wallet.getPublicId().toString())
                .userId(wallet.getUser().getPublicId().toString())
                .balance(wallet.getBalance())
                .pendingBalance(wallet.getPendingBalance())
                .holdBalance(wallet.getHoldBalance())
                .totalDeposited(wallet.getTotalDeposited())
                .totalWithdrawn(wallet.getTotalWithdrawn())
                .isLocked(wallet.getIsLocked())
                .lockReason(wallet.getLockReason())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private TransactionDTO convertToTransactionDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getPublicId().toString())
                .walletId(transaction.getWallet().getPublicId().toString())
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .previousBalance(transaction.getPreviousBalance())
                .currentBalance(transaction.getCurrentBalance())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .escrowId(transaction.getEscrowId())
                .status(transaction.getStatus().name())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }

    private void validatePaymentMethod(User user, DepositRequest request) {
        if (request.getPaymentMethodId() == null) {
            return; // Ödeme yöntemi ID'si yoksa atlat
        }

        if ("CREDIT_CARD".equals(request.getPaymentMethod())) {
            UUID cardId = UUID.fromString(request.getPaymentMethodId());
            CreditCard card = creditCardRepository.findByUserAndPublicId(user, cardId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kredi kartı bulunamadı"));
            
            if (!Boolean.TRUE.equals(card.getIsActive())) {
                throw new IllegalStateException("Kredi kartı aktif değil");
            }
        } else if ("BANK_TRANSFER".equals(request.getPaymentMethod())) {
            UUID accountId = UUID.fromString(request.getPaymentMethodId());
            BankAccount account = bankAccountRepository.findByUserAndPublicId(user, accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Banka hesabı bulunamadı"));
            
            if (!Boolean.TRUE.equals(account.getIsActive())) {
                throw new IllegalStateException("Banka hesabı aktif değil");
            }
        }
    }

    private void validateBankAccountForWithdrawal(User user, String bankAccountId) {
        if (bankAccountId == null) {
            // Varsayılan banka hesabını bul
            bankAccountRepository.findByUserAndIsDefaultTrueAndIsActiveTrue(user)
                    .orElseThrow(() -> new ResourceNotFoundException("Varsayılan banka hesabı bulunamadı"));
        } else {
            UUID accountId = UUID.fromString(bankAccountId);
            BankAccount account = bankAccountRepository.findByUserAndPublicId(user, accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Banka hesabı bulunamadı"));
            
            if (!Boolean.TRUE.equals(account.getIsActive())) {
                throw new IllegalStateException("Banka hesabı aktif değil");
            }
            
            if (!Boolean.TRUE.equals(account.getIsVerified())) {
                throw new IllegalStateException("Banka hesabı doğrulanmamış");
            }
        }
    }
}