package ru.Tim.Proj.moneyAnalyzer.Models.Other;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.IncomeSource;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.MoneyHolders;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "accounts")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="login", nullable = false)
    @NotEmpty(message = "Поле имени не должно быть пустым")
    @Size(min = 3, max = 20, message = "Логин должен быть в пределах от 3 до 20 символов")
    private String login;

    @Column(name="email", nullable = false)
    @NotEmpty
    @Email
    private String email;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @NotEmpty
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Size(min = 8, max = 30, message = "Пароль должен быть не менее 8 и не более 30 символов")
    @Transient
    private String checkPass;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<MoneyHolders> holders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<IncomeSource> incomeSources;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ExpenseCategory> expenseCategories;

    @Column(name = "enabled")
    private boolean enabled = false;

    public User() {}

    public User(String login, String email, String password,
                String checkPass, Long id, LocalDate createDate) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.password = password;
        this.checkPass = checkPass;
        this.createDate = createDate;
    }

    public List<IncomeSource> getIncomeSources() {
        return incomeSources;
    }

    public void setIncomeSources(List<IncomeSource> incomeSources) {
        this.incomeSources = incomeSources;
    }

    public List<ExpenseCategory> getExpenseCategories() {
        return expenseCategories;
    }

    public void setExpenseCategories(List<ExpenseCategory> expenseCategories) {
        this.expenseCategories = expenseCategories;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<MoneyHolders> getHolders() {
        return holders;
    }

    public void setHolders(List<MoneyHolders> holders) {
        this.holders = holders;
    }

    public LocalDate getCreateDate() { return createDate;}

    public void setCreateDate(LocalDate createDate) { this.createDate = createDate; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCheckPass() {
        return checkPass;
    }

    public void setCheckPass(String checkPass) {
        this.checkPass = checkPass;
    }
}
