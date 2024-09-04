package ru.Tim.Proj.moneyAnalyzer.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class AuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "Неверный логин или пароль";
        if (exception.getMessage().contains("UserDetailsService")) {
            errorMessage = "Пользователь не найден";
        } else if (exception.getMessage().contains("Bad credentials")) {
            errorMessage = "Неверный логин или пароль";
        }

        request.getSession().setAttribute("errorMessage", errorMessage);
        response.sendRedirect("/login?error");
    }
}
