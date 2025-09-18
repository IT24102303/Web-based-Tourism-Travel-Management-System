package org.example.tourmanagement.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.tourmanagement.user.User;
import org.example.tourmanagement.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String admin(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("newUser", new CreateUserForm());
        return "admin";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute("newUser") CreateUserForm form, BindingResult bindingResult) {
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            bindingResult.rejectValue("username", "required", "Username is required");
        }
        if (form.getEmail() == null || form.getEmail().isBlank()) {
            bindingResult.rejectValue("email", "required", "Email is required");
        }
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "min", "Password must be at least 6 characters");
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/admin?error";
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(form.getRole() == null || form.getRole().isBlank() ? "USER" : form.getRole().toUpperCase());
        userRepository.save(user);
        return "redirect:/admin?created";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute("user") UpdateUserForm form,
                             BindingResult bindingResult) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin?notfound";
        }
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            bindingResult.rejectValue("username", "required", "Username is required");
        }
        if (form.getEmail() == null || form.getEmail().isBlank()) {
            bindingResult.rejectValue("email", "required", "Email is required");
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/admin?error";
        }
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        if (form.getRole() != null && !form.getRole().isBlank()) {
            user.setRole(form.getRole().toUpperCase());
        }
        userRepository.save(user);
        return "redirect:/admin?updated";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
        return "redirect:/admin?deleted";
    }

    public static class CreateUserForm {
        private String username;
        private String email;
        private String password;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class UpdateUserForm {
        private String username;
        private String email;
        private String password;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}


