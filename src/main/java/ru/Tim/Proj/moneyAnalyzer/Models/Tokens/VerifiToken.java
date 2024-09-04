package ru.Tim.Proj.moneyAnalyzer.Models.Tokens;


import jakarta.persistence.*;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

@Entity
@DiscriminatorValue("VERIFICATION")
public class VerifiToken extends EmailToken{


    public VerifiToken() {}

    public VerifiToken(String token, User user) {
        super(token, user);
    }

}
