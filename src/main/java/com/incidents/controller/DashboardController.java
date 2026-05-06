package com.incidents.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the analytics dashboard HTML page.
 * Access at: http://localhost:8080/dashboard
 *
 * The dashboard.html file is in src/main/resources/static/
 * Spring Boot automatically serves static files from /static/
 */
@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
}
