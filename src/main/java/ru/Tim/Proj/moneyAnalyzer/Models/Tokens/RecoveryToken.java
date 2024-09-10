package ru.Tim.Proj.moneyAnalyzer.Models.Tokens;

import jakarta.persistence.*;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

@Entity
@DiscriminatorValue("RECOVERY")
public class RecoveryToken extends EmailToken {

    public RecoveryToken() {}

    public RecoveryToken(String token, User user) {
        super(token, user);
    }

}
