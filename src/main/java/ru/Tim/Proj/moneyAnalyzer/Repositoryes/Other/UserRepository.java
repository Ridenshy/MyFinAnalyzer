package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Optional<User> findByEmail(String email);
}
