package it.uniroma3.siw_events.controller;

import it.uniroma3.siw_events.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')") // Assicura che solo gli admin possano accedere a questo controller
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        // Aggiungere qui eventuali dati specifici per la dashboard admin
        model.addAttribute("pageTitle", "Dashboard Amministrazione");
        return "admin/admin_dashboard"; // Template per la dashboard admin
    }

    @GetMapping("/utenti")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("pageTitle", "Gestione Utenti");
        return "admin/manage_users"; // Template per la gestione utenti
    }

    // Altri metodi per la gestione utenti, eventi, ecc. verranno aggiunti qui
    // ad esempio /admin/utenti, /admin/eventi
}
