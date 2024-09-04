package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.Tim.Proj.moneyAnalyzer.Models.Tokens.EmailToken;
import ru.Tim.Proj.moneyAnalyzer.Models.Tokens.VerifiToken;

public interface TokenRepository extends JpaRepository<EmailToken, Long> {
    EmailToken findByToken(String token);
}
