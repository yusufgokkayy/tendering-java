package com.tendering.service;

import com.tendering.model.*;
import com.tendering.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EscrowServiceTest {

    @Mock
    private EscrowRepository escrowRepository;
    
    @Mock
    private WalletRepository walletRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AuctionRepository auctionRepository;
    
    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private EscrowService escrowService;

    private User buyer;
    private User seller;
    private Auction auction;
    private Bid winningBid;
    private Wallet buyerWallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock data setup
        buyer = User.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .name("Buyer")
                .surname("Test")
                .phoneNumber("1234567890")
                .email("buyer@test.com")
                .build();

        seller = User.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .name("Seller")
                .surname("Test")
                .phoneNumber("0987654321")
                .email("seller@test.com")
                .build();

        auction = Auction.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .title("Test Auction")
                .seller(seller)
                .startPrice(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("150.00"))
                .status("ACTIVE")
                .build();

        winningBid = Bid.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .auction(auction)
                .bidder(buyer)
                .amount(new BigDecimal("150.00"))
                .status("ACTIVE")
                .build();

        buyerWallet = Wallet.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .user(buyer)
                .balance(new BigDecimal("200.00"))
                .holdBalance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void testCreateEscrowForAuction_Success() {
        // Arrange
        when(auctionRepository.findByPublicId(auction.getPublicId())).thenReturn(Optional.of(auction));
        when(bidRepository.findByPublicId(winningBid.getPublicId())).thenReturn(Optional.of(winningBid));
        when(escrowRepository.existsByAuction(auction)).thenReturn(false);
        when(walletRepository.findByUser(buyer)).thenReturn(Optional.of(buyerWallet));
        
        Escrow savedEscrow = Escrow.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .auction(auction)
                .buyer(buyer)
                .seller(seller)
                .winningBid(winningBid)
                .amount(new BigDecimal("150.00"))
                .commissionAmount(new BigDecimal("7.50"))
                .commissionRate(new BigDecimal("0.05"))
                .status(Escrow.EscrowStatus.PENDING)
                .build();
        
        when(escrowRepository.save(any(Escrow.class))).thenReturn(savedEscrow);
        when(walletRepository.save(any(Wallet.class))).thenReturn(buyerWallet);
        
        Transaction mockTransaction = Transaction.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .wallet(buyerWallet)
                .type(Transaction.TransactionType.ESCROW_HOLD)
                .amount(new BigDecimal("150.00"))
                .status(Transaction.TransactionStatus.COMPLETED)
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        // Act
        var result = escrowService.createEscrowForAuction(
                auction.getPublicId(), 
                winningBid.getPublicId(), 
                new BigDecimal("0.05")
        );

        // Assert
        assertNotNull(result);
        assertEquals("150.00", result.getAmount().toString());
        assertEquals("7.50", result.getCommissionAmount().toString());
        assertEquals("HELD", result.getStatus());
        
        verify(escrowRepository, times(2)).save(any(Escrow.class)); // Called twice: creation + hold
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testCreateEscrowForAuction_InsufficientFunds() {
        // Arrange
        buyerWallet.setBalance(new BigDecimal("100.00")); // Less than bid amount
        
        when(auctionRepository.findByPublicId(auction.getPublicId())).thenReturn(Optional.of(auction));
        when(bidRepository.findByPublicId(winningBid.getPublicId())).thenReturn(Optional.of(winningBid));
        when(escrowRepository.existsByAuction(auction)).thenReturn(false);
        when(walletRepository.findByUser(buyer)).thenReturn(Optional.of(buyerWallet));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            escrowService.createEscrowForAuction(
                    auction.getPublicId(), 
                    winningBid.getPublicId(), 
                    new BigDecimal("0.05")
            );
        });
    }

    @Test
    void testCreateEscrowForAuction_EscrowAlreadyExists() {
        // Arrange
        when(auctionRepository.findByPublicId(auction.getPublicId())).thenReturn(Optional.of(auction));
        when(bidRepository.findByPublicId(winningBid.getPublicId())).thenReturn(Optional.of(winningBid));
        when(escrowRepository.existsByAuction(auction)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            escrowService.createEscrowForAuction(
                    auction.getPublicId(), 
                    winningBid.getPublicId(), 
                    new BigDecimal("0.05")
            );
        });
    }
}