package com.organicnow.backend.controller;

import com.organicnow.backend.dto.DashboardDto;
import com.organicnow.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://app.localtest.me"}, allowCredentials = "true")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardDto getDashboardData() {
        return dashboardService.getDashboardData();
    }
}

