package com.tendering.repository;

import com.tendering.model.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {

    boolean existsByToken(String token);

    @Query("SELECT i FROM InvalidatedToken i WHERE i.expirationDate < :now")
    List<InvalidatedToken> findExpiredTokens(Date now);
}
