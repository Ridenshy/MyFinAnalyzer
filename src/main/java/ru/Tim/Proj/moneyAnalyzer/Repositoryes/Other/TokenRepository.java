package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.Tokens.EmailToken;

import java.util.List;

public interface TokenRepository extends JpaRepository<EmailToken, Long> {
    EmailToken findByToken(String token);

    @Query("SELECT et FROM EmailToken et WHERE et.expiryDate <= DATE_TRUNC('second', CURRENT_TIMESTAMP)")
    List<EmailToken> getExpiredTokens();

    boolean existsByUserId(@Param("userId") Long id);
}
