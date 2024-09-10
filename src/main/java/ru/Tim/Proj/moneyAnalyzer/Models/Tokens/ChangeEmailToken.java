package ru.Tim.Proj.moneyAnalyzer.Models.Tokens;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

@Entity
@DiscriminatorValue("EDITMAIL")
public class ChangeEmailToken extends EmailToken{

    @Email
    @Column(name = "new_email")
    private String email;

    public ChangeEmailToken() {}

    public ChangeEmailToken(String token, User user, String email) {
        super(token, user);
        this.email = email;
    }

    public @Email String getEmail() {
        return email;
    }

    public void setEmail(@Email String email) {
        this.email = email;
    }
}
