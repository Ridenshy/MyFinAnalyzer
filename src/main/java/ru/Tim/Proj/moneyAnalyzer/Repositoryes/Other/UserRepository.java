package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);

    Optional<User> findByEmail(String email);

    List<User> findByEnabledFalse();

    @Query("SELECT EXISTS (SELECT 1 FROM User u WHERE u.enabled = false AND u.email = :email)")
    boolean isEnabledFalseFindByEmail(String email);
}
