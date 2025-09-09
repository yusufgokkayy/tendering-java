package com.tendering.controller;

import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.request.transaction.TransactionDTO;
import com.tendering.dto.request.wallet.DepositRequest;
import com.tendering.dto.request.wallet.WalletDTO;
import com.tendering.dto.request.wallet.WithdrawRequest;
import com.tendering.exceptionHandlers.InsufficientFundsException;
import com.tendering.exceptionHandlers.WalletLockedException;
import com.tendering.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{userPublicId}")
    public ResponseEntity<ApiResponse<WalletDTO>> getWalletInfo(
            @PathVariable UUID userPublicId) {
        log.debug("Cüzdan bilgisi istendi: {}", userPublicId);
        WalletDTO wallet = walletService.getWalletInfo(userPublicId);
        return ResponseEntity.ok(ApiResponse.success("Cüzdan bilgisi başarıyla getirildi", wallet));
    }

    @PostMapping("/{userPublicId}/deposit")
    public ResponseEntity<ApiResponse<TransactionDTO>> depositFunds(
            @PathVariable UUID userPublicId,
            @Valid @RequestBody DepositRequest request) {
        log.debug("Para yatırma isteği: {}, miktar: {}", userPublicId, request.getAmount());
        try {
            TransactionDTO transaction = walletService.depositFunds(userPublicId, request);
            return new ResponseEntity<>(
                    ApiResponse.success("Para yatırma işlemi başarıyla tamamlandı", transaction),
                    HttpStatus.CREATED);
        } catch (WalletLockedException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Para yatırma işlemi başarısız: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Para yatırma işlemi sırasında bir hata oluştu: " + e.getMessage()));
        }
    }

    @PostMapping("/{userPublicId}/withdraw")
    public ResponseEntity<ApiResponse<TransactionDTO>> withdrawFunds(
            @PathVariable UUID userPublicId,
            @Valid @RequestBody WithdrawRequest request) {
        log.debug("Para çekme isteği: {}, miktar: {}", userPublicId, request.getAmount());
        try {
            TransactionDTO transaction = walletService.withdrawFunds(userPublicId, request);
            return ResponseEntity.ok(ApiResponse.success("Para çekme işlemi başarıyla tamamlandı", transaction));
        } catch (WalletLockedException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (InsufficientFundsException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Para çekme işlemi başarısız: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Para çekme işlemi sırasında bir hata oluştu: " + e.getMessage()));
        }
    }

    @GetMapping("/{userPublicId}/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getTransactionHistory(
            @PathVariable UUID userPublicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.debug("İşlem geçmişi istendi: {}", userPublicId);
        Page<TransactionDTO> transactions = walletService.getTransactionHistory(userPublicId, page, size);
        return ResponseEntity.ok(ApiResponse.success("İşlem geçmişi başarıyla getirildi", transactions));
    }
}