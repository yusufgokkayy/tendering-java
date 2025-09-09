package com.tendering.service;

import com.tendering.dto.request.payment.BankAccountCreateRequest;
import com.tendering.dto.response.payment.BankAccountResponse;
import com.tendering.exceptionHandlers.ResourceNotFoundException;
import com.tendering.model.BankAccount;
import com.tendering.model.User;
import com.tendering.repository.BankAccountRepository;
import com.tendering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Transactional
    public BankAccountResponse createBankAccount(UUID userPublicId, BankAccountCreateRequest request) {
        log.debug("Banka hesabı oluşturuluyor - Kullanıcı: {}", userPublicId);

        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        // IBAN zaten kayıtlı mı kontrol et
        if (bankAccountRepository.existsByUserAndIban(user, request.getIban())) {
            throw new IllegalStateException("Bu IBAN zaten kayıtlı");
        }

        // Eğer default olarak işaretlenmişse, diğerlerini default olmaktan çıkar
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            resetDefaultBankAccount(user);
        }

        BankAccount bankAccount = BankAccount.builder()
                .user(user)
                .accountHolderName(request.getAccountHolderName())
                .iban(request.getIban().toUpperCase().replaceAll("\\s", ""))
                .bankName(request.getBankName())
                .branchCode(request.getBranchCode())
                .isDefault(request.getIsDefault())
                .isVerified(false)
                .isActive(true)
                .build();

        BankAccount savedAccount = bankAccountRepository.save(bankAccount);

        log.info("Banka hesabı oluşturuldu - ID: {}, IBAN: {}", savedAccount.getPublicId(), 
                maskIban(savedAccount.getIban()));

        return convertToBankAccountResponse(savedAccount);
    }

    public List<BankAccountResponse> getUserBankAccounts(UUID userPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        List<BankAccount> accounts = bankAccountRepository.findByUserAndIsActiveTrue(user);
        return accounts.stream()
                .map(this::convertToBankAccountResponse)
                .collect(Collectors.toList());
    }

    public BankAccountResponse getBankAccountDetails(UUID userPublicId, UUID accountPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        BankAccount account = bankAccountRepository.findByUserAndPublicId(user, accountPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Banka hesabı bulunamadı: " + accountPublicId));

        return convertToBankAccountResponse(account);
    }

    @Transactional
    public BankAccountResponse setDefaultBankAccount(UUID userPublicId, UUID accountPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        BankAccount account = bankAccountRepository.findByUserAndPublicId(user, accountPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Banka hesabı bulunamadı: " + accountPublicId));

        // Diğer hesapları default olmaktan çıkar
        resetDefaultBankAccount(user);

        // Bu hesabı default yap
        account.setIsDefault(true);
        BankAccount updatedAccount = bankAccountRepository.save(account);

        log.info("Default banka hesabı değiştirildi - Kullanıcı: {}, Hesap: {}", userPublicId, accountPublicId);

        return convertToBankAccountResponse(updatedAccount);
    }

    @Transactional
    public void deleteBankAccount(UUID userPublicId, UUID accountPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userPublicId));

        BankAccount account = bankAccountRepository.findByUserAndPublicId(user, accountPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Banka hesabı bulunamadı: " + accountPublicId));

        // Soft delete
        account.setIsActive(false);
        if (Boolean.TRUE.equals(account.getIsDefault())) {
            account.setIsDefault(false);
        }

        bankAccountRepository.save(account);

        log.info("Banka hesabı silindi - Kullanıcı: {}, Hesap: {}", userPublicId, accountPublicId);
    }

    @Transactional
    public BankAccountResponse verifyBankAccount(UUID accountPublicId) {
        BankAccount account = bankAccountRepository.findByPublicId(accountPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Banka hesabı bulunamadı: " + accountPublicId));

        account.setIsVerified(true);
        account.setVerificationDate(LocalDateTime.now());
        BankAccount verifiedAccount = bankAccountRepository.save(account);

        log.info("Banka hesabı doğrulandı - ID: {}", accountPublicId);

        return convertToBankAccountResponse(verifiedAccount);
    }

    private void resetDefaultBankAccount(User user) {
        bankAccountRepository.findByUserAndIsDefaultTrueAndIsActiveTrue(user)
                .ifPresent(defaultAccount -> {
                    defaultAccount.setIsDefault(false);
                    bankAccountRepository.save(defaultAccount);
                });
    }

    private String maskIban(String iban) {
        if (iban == null || iban.length() < 8) {
            return iban;
        }
        return iban.substring(0, 4) + "***" + iban.substring(iban.length() - 4);
    }

    private BankAccountResponse convertToBankAccountResponse(BankAccount account) {
        return BankAccountResponse.builder()
                .id(account.getPublicId().toString())
                .accountHolderName(account.getAccountHolderName())
                .iban(maskIban(account.getIban())) // IBAN'ı maskele
                .bankName(account.getBankName())
                .branchCode(account.getBranchCode())
                .isVerified(account.getIsVerified())
                .verificationDate(account.getVerificationDate())
                .isDefault(account.getIsDefault())
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}