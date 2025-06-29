package io.github.susimsek.springbootjweauthjpademo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/mfa-setup")
    public String mfaSetupPage() {
        return "mfa-setup";
    }

    @GetMapping("/mfa-management")
    public String mfaManagementPage() {
        return "mfa-management";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/verify-email")
    public String verifyEmailPage() {
        return "verify-email";
    }

    @GetMapping("/verify-totp")
    public String verifyTotpPage() {
        return "verify-totp";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "change-password";
    }

    @GetMapping("/change-email")
    public String changeEmailPage() {
        return "change-email";
    }

    @GetMapping("/confirm-email")
    public String confirmEmailPage() {
        return "confirm-email";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/403")
    public String forbiddenPage() {
        return "error/403";
    }

    @GetMapping("/admin/user-management")
    public String userManagementPage() {
        return "admin/user-management";
    }

    @GetMapping("/admin/create-user")
    public String createUserPage() {
        return "admin/create-user";
    }

    @GetMapping("/admin/edit-user/{id}")
    public String editUserPage(@PathVariable("id") String id, Model model) {
        model.addAttribute("userId", id);
        return "admin/edit-user";
    }

    @GetMapping("/admin/authority-management")
    public String authorityManagementPage() {
        return "admin/authority-management";
    }

    @GetMapping("/admin/create-authority")
    public String createAuthorityPage() {
        return "admin/create-authority";
    }

    @GetMapping("/admin/edit-authority/{id}")
    public String editAuthorityPage(@PathVariable("id") String id, Model model) {
        model.addAttribute("authorityId", id);
        return "admin/edit-authority";
    }

    @GetMapping("/admin/message-management")
    public String messageManagementPage() {
        return "admin/message-management";
    }

    @GetMapping("/admin/create-message")
    public String createMessagePage() {
        return "admin/create-message";
    }

    @GetMapping("/admin/edit-message/{id}")
    public String editMessagePage(@PathVariable("id") String id, Model model) {
        model.addAttribute("messageId", id);
        return "admin/edit-message";
    }

    @GetMapping("/admin/docs")
    public String docsPage() {
        return "admin/docs";
    }
}
