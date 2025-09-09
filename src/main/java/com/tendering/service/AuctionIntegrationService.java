package com.tendering.service;

import com.tendering.dto.response.escrow.EscrowResponse;
import com.tendering.model.Auction;
import com.tendering.model.Bid;
import com.tendering.repository.AuctionRepository;
import com.tendering.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuctionIntegrationService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final EscrowService escrowService;

    /**
     * İhale bitiminde kazanan teklif için otomatik escrow oluşturur
     */
    @Transactional
    public EscrowResponse completeAuctionWithEscrow(UUID auctionPublicId, BigDecimal commissionRate) {
        log.debug("İhale tamamlanıyor ve escrow oluşturuluyor: {}", auctionPublicId);

        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new RuntimeException("İhale bulunamadı: " + auctionPublicId));

        // İhale durumunu kontrol et
        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new IllegalStateException("İhale aktif değil: " + auction.getStatus());
        }

        // En yüksek teklifi bul
        Pageable topOne = PageRequest.of(0, 1);
        List<Bid> highestBids = bidRepository.findActiveHighestBidsByAuction(auction, topOne);
        
        if (highestBids.isEmpty()) {
            throw new IllegalStateException("İhale için geçerli teklif bulunamadı");
        }

        Bid winningBid = highestBids.get(0);
        
        // Reserve price kontrolü
        if (auction.getReservePrice() != null && 
            winningBid.getAmount().compareTo(auction.getReservePrice()) < 0) {
            throw new IllegalStateException("Kazanan teklif reserve fiyatın altında");
        }

        // İhale durumunu tamamlandı olarak güncelle
        auction.setStatus("COMPLETED");
        auction.setCurrentPrice(winningBid.getAmount());
        auctionRepository.save(auction);

        // Escrow oluştur
        EscrowResponse escrow = escrowService.createEscrowForAuction(
                auctionPublicId, 
                winningBid.getPublicId(), 
                commissionRate != null ? commissionRate : new BigDecimal("0.05")
        );

        log.info("İhale tamamlandı ve escrow oluşturuldu - Açık artırma: {}, Escrow: {}, Kazanan teklif: {}", 
                auctionPublicId, escrow.getId(), winningBid.getAmount());

        return escrow;
    }

    /**
     * İhaleyi manuel olarak tamamla (escrow olmadan)
     */
    @Transactional
    public void completeAuctionWithoutEscrow(UUID auctionPublicId) {
        log.debug("İhale escrow olmadan tamamlanıyor: {}", auctionPublicId);

        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new RuntimeException("İhale bulunamadı: " + auctionPublicId));

        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new IllegalStateException("İhale aktif değil: " + auction.getStatus());
        }

        // En yüksek teklifi bul
        Pageable topOne = PageRequest.of(0, 1);
        List<Bid> highestBids = bidRepository.findActiveHighestBidsByAuction(auction, topOne);
        
        if (!highestBids.isEmpty()) {
            Bid winningBid = highestBids.get(0);
            auction.setCurrentPrice(winningBid.getAmount());
        }

        auction.setStatus("COMPLETED");
        auctionRepository.save(auction);

        log.info("İhale tamamlandı (escrow olmadan): {}", auctionPublicId);
    }

    /**
     * İhaleyi iptal et
     */
    @Transactional  
    public void cancelAuction(UUID auctionPublicId, String reason) {
        log.debug("İhale iptal ediliyor: {}", auctionPublicId);

        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new RuntimeException("İhale bulunamadı: " + auctionPublicId));

        auction.setStatus("CANCELLED");
        auctionRepository.save(auction);

        log.info("İhale iptal edildi: {}, Sebep: {}", auctionPublicId, reason);
    }

    /**
     * İhalenin kazanan teklifini getirir
     */
    public Bid getWinningBid(UUID auctionPublicId) {
        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new RuntimeException("İhale bulunamadı: " + auctionPublicId));

        Pageable topOne = PageRequest.of(0, 1);
        List<Bid> highestBids = bidRepository.findActiveHighestBidsByAuction(auction, topOne);
        
        return highestBids.isEmpty() ? null : highestBids.get(0);
    }
}