package it.uniroma3.siw_events.controller;

import it.uniroma3.siw_events.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard Amministrazione");
        return "admin/admin_dashboard";
    }

    @GetMapping("/utenti")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("pageTitle", "Gestione Utenti");
        return "admin/manage_users";
    }
}
