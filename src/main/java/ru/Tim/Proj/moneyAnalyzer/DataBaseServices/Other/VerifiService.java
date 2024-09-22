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
import ru.Tim.Proj.moneyAnalyzer.Models.Tokens.ChangeEmailToken;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other.TokenRepository;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
public class VerifiService {

    @Value("${myapp.hostUrl}")
    private String hostUrl;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public VerifiService(TokenRepository tokenRepository, JavaMailSender mailSender, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    @Async
    public void sendVerificationEmail(User user, String type, String newEmail) {
        String token = UUID.randomUUID().toString();
        EmailToken newToken;
        if(newEmail.isEmpty()){
            newToken = new VerifiToken(token, user);
        }else {
            newToken = new ChangeEmailToken(token, user, newEmail);
        }
        tokenRepository.save(newToken);

        String subject = null;
        String confirmationUrl;
        String message = null;

        if(type.equals("verifi")){
            subject = "Account Verification";
            confirmationUrl = hostUrl + "/register/confirm?token=" + token;
            message = "Перейдите по ссылке для подтверждения аккаунта: " + confirmationUrl;
        } else if(type.equals("emailChange")){
            subject = "Email change";
            confirmationUrl = hostUrl + "/profile/verification-mail?token=" + token;
            message = "Перейдите по ссылке для подтверждения почты: " + confirmationUrl;
        }

        SimpleMailMessage email = new SimpleMailMessage();
        if(!newEmail.isEmpty()){
            email.setTo(newEmail);
        } else {
            email.setTo(user.getEmail());
        }
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

    public void deleteTokens(){
        for(EmailToken token : tokenRepository.getExpiredTokens()){
            tokenRepository.delete(token);
        }
        for(User user : userRepository.findByEnabledFalse()){
            if(!tokenRepository.existsByUserId(user.getId())){
                userRepository.delete(user);
                System.out.println("пользователь удален");
            }
        }
    }

}
