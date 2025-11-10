package com.organicnow.backend.controller;

import com.organicnow.backend.model.Admin;
import com.organicnow.backend.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    
    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        Optional<Admin> adminOpt = adminRepository.findByAdminUsername(username);
        
        if (adminOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Invalid credentials"));
        }
        
        Admin admin = adminOpt.get();
        
        // สำหรับ demo ใช้ plain text เปรียบเทียบ
        if (!password.equals("admin123")) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Invalid credentials"));
        }
        
        // ส่งข้อมูล admin กลับ
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Login successful",
            "data", Map.of(
                "token", "simple-token-" + admin.getId(),
                "admin", Map.of(
                    "id", admin.getId(),
                    "adminUsername", admin.getAdminUsername(),
                    "adminRole", admin.getAdminRole()
                )
            )
        ));
    }
}
