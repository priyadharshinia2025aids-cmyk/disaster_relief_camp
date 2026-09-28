package com.reliefcamp.tracker.controller;

import com.reliefcamp.tracker.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }
    @GetMapping public Map<String, Long> get() { return service.get(); }
}
