package ru.Tim.Proj.moneyAnalyzer.Controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.Tim.Proj.moneyAnalyzer.Config.MyUserDetails;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.AccountDataService;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

@Controller
@RequestMapping("/profile")
public class AccountController {

    private final AccountDataService accountDataService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountController(AccountDataService accountDataService, PasswordEncoder passwordEncoder) {
        this.accountDataService = accountDataService;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping()
    public String viewProfile(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = accountDataService.findUserByLogin(username);
        model.addAttribute("user", user);
        return "accountpages/profile";
    }

    @PatchMapping("/edit-login")
    public String userUpdateLogin(@RequestParam("login") String login,
                                  @AuthenticationPrincipal MyUserDetails userDetails,
                                  RedirectAttributes redirectAttributes){
        System.out.println(login.length());
        if(login.length() <= 3 || login.length() >= 20){
            redirectAttributes.addFlashAttribute("loginError", "Логин должен быть в переделах от 3 до 20 символов.");
            return "redirect:/profile";
        }
        if (accountDataService.userExists(login)) {
            redirectAttributes.addFlashAttribute("loginError", "Логин уже занят");
            return "redirect:/profile";
        }
        User user = userDetails.getUser();
        System.out.println(user.getLogin() + user.getId() + user.getEmail() + user.getPassword());
        user.setLogin(login);
        updateSecurityContext(user);
        accountDataService.createOrUpdateUser(user);

        return "redirect:/profile";
    }

    @PatchMapping("/edit-email")
    public String userUpdateEmail(@RequestParam("email") String email,
                                  @AuthenticationPrincipal MyUserDetails userDetails,
                                  RedirectAttributes redirectAttributes){
        if(email.length() <= 3 || email.length() >= 20){
            redirectAttributes.addFlashAttribute("emailError", "Неверный формат");
            return "redirect:/profile";
        }
        if (accountDataService.emailExists(email)) {
            redirectAttributes.addFlashAttribute("emailError", "Почта уже используется");
            return "redirect:/profile";
        }

        User user = userDetails.getUser();
        user.setEmail(email);
        updateSecurityContext(user);
        accountDataService.createOrUpdateUser(user);

        return "redirect:/profile";
    }

    @PatchMapping("/edit-password")
    public String userUpdatePassword(@RequestParam("password") String password,
                                     @RequestParam("checkPass") String checkPass,
                                     @RequestParam("oldPass") String oldPass,
                                     @AuthenticationPrincipal MyUserDetails userDetails,
                                     RedirectAttributes redirectAttributes){
        User user = userDetails.getUser();
        if(passwordEncoder.matches(oldPass, user.getPassword())){
            if(password.equals(checkPass) && !password.equals(oldPass)){
                String pass = passwordEncoder.encode(password);
                user.setPassword(pass);
                System.out.println(user.getPassword());
                updateSecurityContext(user);
                accountDataService.createOrUpdateUser(user);
            }else {
                redirectAttributes.addFlashAttribute("passwordError", "Пароли должны совпадать, и не повторять старый.");
                return "redirect:/profile";
            }
        } else {
            redirectAttributes.addFlashAttribute("passwordError", "Неправильный старый пароль");
            return "redirect:/profile";
        }
        return "redirect:/profile";
    }

    @DeleteMapping("/deleteAccount")
    public String deleteAccount(@RequestParam("deleteId") Long id,
                                @RequestParam("pass") String pass,
                                RedirectAttributes redirectAttributes){
        User user = accountDataService.findUserById(id);
        if(passwordEncoder.matches(pass, user.getPassword())){
            accountDataService.deleteAccount(id);
        }else{
            redirectAttributes.addFlashAttribute("deleteError", "Не верный пароль.");
            return "redirect:/profile";
        }
        return "redirect:/login?logout";
    }

    private void updateSecurityContext(User user) {
        MyUserDetails updatedUserDetails = new MyUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        updatedUserDetails,
                        updatedUserDetails.getPassword(),
                        updatedUserDetails.getAuthorities())
        );
    }
}
