package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;
import ru.Tim.Proj.moneyAnalyzer.Models.Tokens.EmailToken;
import ru.Tim.Proj.moneyAnalyzer.Models.Tokens.RecoveryToken;
import ru.Tim.Proj.moneyAnalyzer.Models.Tokens.VerifiToken;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other.TokenRepository;

import java.util.UUID;

@Service
public class VerifiService {

    @Value("${myapp.hostUrl}")
    private String hostUrl;

    private final TokenRepository tokenRepository;
    private final JavaMailSender mailSender;

    public VerifiService(TokenRepository tokenRepository, JavaMailSender mailSender) {
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        VerifiToken verificationToken = new VerifiToken(token, user);
        tokenRepository.save(verificationToken);

        String subject = "Account Verification";
        String confirmationUrl = hostUrl + "/register/confirm?token=" + token;
        String message = "Перейдите по ссылке для подтверждения аккаунта: " + confirmationUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }

    @Async
    public void sendRecoveryEmail(User user){
        String token = UUID.randomUUID().toString();
        RecoveryToken recoveryToken = new RecoveryToken(token, user);
        tokenRepository.save(recoveryToken);

        String subject = "Account Recovery";
        String recoveryUrl = hostUrl + "/recovery?token=" + token;
        String message = "Для восстановления пароля перейдите по данной ссылке: " + recoveryUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }

    public EmailToken getToken(String token){
        return tokenRepository.findByToken(token);
    }

    public void deleteToken(EmailToken token){
        tokenRepository.delete(token);
    }
}
