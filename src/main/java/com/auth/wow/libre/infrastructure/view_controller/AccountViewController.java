package com.auth.wow.libre.infrastructure.view_controller;

import com.auth.wow.libre.domain.model.dto.*;
import com.auth.wow.libre.domain.model.dto.view.*;
import com.auth.wow.libre.domain.ports.in.account.*;
import com.auth.wow.libre.domain.ports.in.dashboard.*;
import com.auth.wow.libre.infrastructure.conf.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Controller
@RequestMapping
public class AccountViewController {
    private final AccountPort accountPort;
    private final Configurations configurations;
    private final DashboardPort dashboardPort;

    public AccountViewController(AccountPort accountPort, Configurations configurations, DashboardPort dashboardPort) {
        this.accountPort = accountPort;
        this.configurations = configurations;
        this.dashboardPort = dashboardPort;
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
            return "register";
        }
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
        List<Card> cards = Arrays.asList(
                new Card("https://wow.zamimg.com/uploads/screenshots/normal/1182980.jpg","Card 1", "Description for card 1"),
                new Card("https://wow.zamimg.com/uploads/screenshots/normal/1182980.jpg","Card 2", "Description for card 2"),
                new Card("https://wow.zamimg.com/uploads/screenshots/normal/1182980.jpg","Card 3", "Description for card 3")
        );

        model.addAttribute("serverName", configurations.getServerWebName());
        Map<String, Long> stats = new HashMap<>();
        stats.put("characters", dashboard.getCharacterCount());
        stats.put("accounts", dashboard.getTotalUsers());
        stats.put("online", dashboard.getOnlineUsers());
        model.addAttribute("cards", cards);

        model.addAttribute("stats", stats);
        return "main";
    }

    @GetMapping("/congrats")
    public String showSuccessPage(Model model) {
        model.addAttribute("textToCopy", "set realmlist");
        model.addAttribute("serverName", configurations.getServerWebName());

        return "congrats";
    }
}
