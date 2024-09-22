package ru.Tim.Proj.moneyAnalyzer.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.AccountDataService;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.HoldersService;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.VerifiService;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;
import ru.Tim.Proj.moneyAnalyzer.Models.Tokens.EmailToken;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class AuthController {

    private final HoldersService holdersService;
    private final AccountDataService accountDataService;
    private final PasswordEncoder passwordEncoder;
    private final VerifiService verifiService;


    @Autowired
    public AuthController(AccountDataService accountDataService, PasswordEncoder passwordEncoder,
                          HoldersService holdersService, VerifiService verifiService) {
        this.accountDataService = accountDataService;
        this.passwordEncoder = passwordEncoder;
        this.holdersService = holdersService;
        this.verifiService = verifiService;
    }

    @GetMapping("/")
    public String home(){
        return "homepages/login";
    }

    @GetMapping("/register")
    public String regPage(@ModelAttribute("user") User user, Model model) {
        return "homepages/register";
    }

    @PostMapping("/register")
    public String createAcc(@ModelAttribute("user") @Valid User user,
                            BindingResult bindingResult) {

        if (!user.getPassword().equals(user.getCheckPass())) {
            bindingResult.rejectValue("checkPass", "error.confirmPassword", "Passwords do not match");
        }
        if (accountDataService.userExists(user.getLogin())) {
            bindingResult.rejectValue("login", "error.login", "User already exists");
        }
        if(accountDataService.emailExists(user.getEmail())){
            bindingResult.rejectValue("email", "error.email", "Email already used");
        }
        if(user.getPassword().isEmpty()){
            bindingResult.rejectValue("password", "error.password", "password");
        }
        if (bindingResult.hasErrors()) {
            return "homepages/register";
        }

        user.setCreateDate(LocalDate.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        accountDataService.createOrUpdateUser(user);
        holdersService.starterHolders(user);
        verifiService.sendVerificationEmail(user, "verifi", "");

        return "redirect:/register/confirm-message";
    }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request,
                            @RequestParam(value = "message", required = false) String message,
                            Model model) {
        String errorMessage = (String) request.getSession().getAttribute("errorMessage");
        request.getSession().removeAttribute("errorMessage");
        request.setAttribute("errorMessage", errorMessage);
        model.addAttribute("message", message);

        return "homepages/login";
    }

    @GetMapping("/register/confirm")
    public String confirmRegistration(@RequestParam("token") String token){
        EmailToken verifiToken = verifiService.getToken(token);

        if(verifiToken == null){
            return "redirect:/login?message=invalidToken";
        }

        if(!LocalDateTime.now().isBefore(verifiToken.getExpiryDate())){
            Long id = verifiToken.getUser().getId();
            verifiService.deleteToken(verifiToken);
            accountDataService.deleteAccount(id);
            return "redirect:/login?message=invalidToken";
        }

        User user = verifiToken.getUser();
        user.setEnabled(true);
        accountDataService.createOrUpdateUser(user);
        verifiService.deleteToken(verifiToken);
        return "redirect:/login?success&message=accepted";
    }

    @GetMapping("/register/confirm-message")
    public String confirmMessagePage(){
        return "homepages/sender";
    }

    @GetMapping("/send-recovery")
    public String emailRecovery(@RequestParam(value = "accept", required = false) String accept,
                                Model model){
        model.addAttribute("accept", accept);
        return "homepages/sendRecovery";
    }

    @PostMapping("/send-recovery")
    public String sendRecoveryMail(@RequestParam("recoveryEmail") String email,
                                   RedirectAttributes redirectAttributes){
        if(!accountDataService.emailExists(email)) {
            redirectAttributes.addFlashAttribute("existError", "Аккаунт не существует");
            return "redirect:/send-recovery";
        }
        if(accountDataService.isEnabled(email)){
            redirectAttributes.addFlashAttribute("existError", "Аккаунт не подтвержден");
            return "redirect:/send-recovery";
        }
        User user = accountDataService.findUserByEmail(email);
        verifiService.sendRecoveryEmail(user);
        return "redirect:/send-recovery?accept=yes";
    }

    @GetMapping("/recovery")
    public String recoveryAccount(@RequestParam(value = "token") String token, Model model){
        EmailToken recoveryToken = verifiService.getToken(token);

        if(recoveryToken == null){
            return "redirect:/login?message=invalidToken";
        }

        if(!LocalDateTime.now().isBefore(recoveryToken.getExpiryDate())){
            verifiService.deleteToken(recoveryToken);
            return "redirect:/login?message=invalidToken";
        }
        model.addAttribute("token", token);
        return "homepages/recovery";
    }

    @PostMapping("/recovery")
    public String recoveryPassword(@RequestParam("newPassword") String password,
                                   @RequestParam("confirmPassword") String confPass,
                                   @RequestParam("curToken") String token,
                                   RedirectAttributes redirectAttributes){

        if(!password.equals(confPass)){
            redirectAttributes.addFlashAttribute("passwordError", "Пароли не совпадают");
            return "redirect:/recovery";
        }
        EmailToken recoveryToken = verifiService.getToken(token);
        User user = accountDataService.findUserById(recoveryToken.getUser().getId());
        user.setPassword(passwordEncoder.encode(password));
        accountDataService.createOrUpdateUser(user);
        verifiService.deleteToken(recoveryToken);
        return "redirect:/login?success&message=recovery";
    }

}