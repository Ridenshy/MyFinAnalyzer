package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other;

import org.springframework.stereotype.Service;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other.UserRepository;

@Service
public class AccountDataService {

    private final UserRepository userRepository;

    public AccountDataService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createOrUpdateUser(User user) {
        userRepository.save(user);
    }

    public boolean userExists(String username) {
        return userRepository.findByLogin(username).isPresent();
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User findUserByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    public void deleteAccount(Long id){ userRepository.deleteById(id); }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }
}
