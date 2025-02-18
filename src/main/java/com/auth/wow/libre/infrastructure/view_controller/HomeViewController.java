package com.auth.wow.libre.infrastructure.view_controller;

import com.auth.wow.libre.domain.model.dto.*;
import com.auth.wow.libre.domain.model.dto.view.*;
import com.auth.wow.libre.domain.ports.in.account.*;
import com.auth.wow.libre.domain.ports.in.dashboard.*;
import com.auth.wow.libre.domain.ports.in.realmlist.*;
import com.auth.wow.libre.domain.ports.in.server_publications.*;
import com.auth.wow.libre.infrastructure.conf.*;
import com.auth.wow.libre.infrastructure.entities.auth.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Controller
@RequestMapping
public class HomeViewController {
    private final AccountPort accountPort;
    private final Configurations configurations;
    private final DashboardPort dashboardPort;
    private final RealmlistPort realmlistPort;
    private final ServerPublicationsPort serverPublicationsPort;

    public HomeViewController(AccountPort accountPort, Configurations configurations, DashboardPort dashboardPort,
                              RealmlistPort realmlistPort, ServerPublicationsPort serverPublicationsPort) {
        this.accountPort = accountPort;
        this.configurations = configurations;
        this.dashboardPort = dashboardPort;
        this.realmlistPort = realmlistPort;
        this.serverPublicationsPort = serverPublicationsPort;
    }

    @PostMapping("/register")
    public String saveStudent(Model model,
                              @ModelAttribute("register") AccountViewCreateDto createDto,
                              @RequestParam("g-recaptcha-response") String recaptchaResponse,
                              HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        try {
            accountPort.createLocal(createDto.getUsername(), createDto.getPassword(), createDto.getEmail(),
                    recaptchaResponse, clientIp);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("register", new AccountViewCreateDto());
            model.addAttribute("serverName", configurations.getServerWebName());
            model.addAttribute("recaptchaSiteKey", configurations.getApiKey());
            return "register";
        }
        final String realmlist = String.format("set realmlist %s",
                realmlistPort.findByAll().stream().findFirst().map(RealmlistEntity::getAddress).orElse(""));
        model.addAttribute("textToCopy", realmlist);
        return "congrats";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("register", new AccountViewCreateDto());
        model.addAttribute("serverName", configurations.getServerWebName());
        model.addAttribute("recaptchaSiteKey", configurations.getApiKey());
        return "register";
    }

    @GetMapping("/")
    public String home(Model model) {
        DashboardMetricsDto dashboard = dashboardPort.metricsCount("");


        model.addAttribute("serverName", configurations.getServerWebName());
        Map<String, Long> stats = new HashMap<>();
        stats.put("characters", dashboard.getCharacterCount());
        stats.put("accounts", dashboard.getTotalUsers());
        stats.put("online", dashboard.getOnlineUsers());
        model.addAttribute("cards", serverPublicationsPort.publications());

        model.addAttribute("stats", stats);
        return "main";
    }

    @GetMapping("/congrats")
    public String showSuccessPage(Model model) {
        final String realmlist = String.format("set realmlist %s",
                realmlistPort.findByAll().stream().findFirst().map(RealmlistEntity::getAddress).orElse(""));
        model.addAttribute("textToCopy", realmlist);
        model.addAttribute("serverName", configurations.getServerWebName());

        return "congrats";
    }
}
