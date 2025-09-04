package com.tendering.service;

import com.tendering.model.InvalidatedToken;
import com.tendering.repository.InvalidatedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class TokenCleanupService {

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(cron = "0 0 * * * *") // Her saat başı çalıştır
    @Transactional
    public void cleanupExpiredTokens() {
        List<InvalidatedToken> expiredTokens = invalidatedTokenRepository.findExpiredTokens(new Date());
        invalidatedTokenRepository.deleteAll(expiredTokens);
    }
}
