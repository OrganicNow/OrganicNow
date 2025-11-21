package com.organicnow.backend.controller;

import com.organicnow.backend.model.Admin;
import com.organicnow.backend.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api/auth", "/auth"})
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173",
        "http://localhost:3000",
        "http://localhost:4173",
        "http://app.localtest.me",
        "https://transcondylar-noncorporately-christen.ngrok-free.dev"}, allowCredentials = "true")
public class AuthController {
    
    private final AdminRepository adminRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
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
        
        // สร้าง session
        HttpSession session = request.getSession(true);
        session.setAttribute("adminId", admin.getId());
        session.setAttribute("adminUsername", admin.getAdminUsername());
        session.setAttribute("adminRole", admin.getAdminRole());
        session.setMaxInactiveInterval(24 * 60 * 60); // 24 hours
        
        System.out.println("=== LOGIN SUCCESS DEBUG ===");
        System.out.println("Created session ID: " + session.getId());
        System.out.println("Session max inactive: " + session.getMaxInactiveInterval());
        System.out.println("Admin ID stored: " + session.getAttribute("adminId"));
        System.out.println("============================");
        
        // ส่งข้อมูล admin กลับ
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Login successful",
            "data", Map.of(
                "token", "session-" + session.getId(),
                "admin", Map.of(
                    "id", admin.getId(),
                    "adminUsername", admin.getAdminUsername(),
                    "adminRole", admin.getAdminRole()
                )
            )
        ));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String authHeader = request.getHeader("Authorization");
        
        System.out.println("=== AUTH CHECK DEBUG ===");
        System.out.println("Session exists: " + (session != null));
        System.out.println("Authorization header: " + authHeader);
        
        if (session != null) {
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session max inactive: " + session.getMaxInactiveInterval());
            System.out.println("Admin ID in session: " + session.getAttribute("adminId"));
            System.out.println("Session creation time: " + new java.util.Date(session.getCreationTime()));
            System.out.println("Session last accessed: " + new java.util.Date(session.getLastAccessedTime()));
        }
        System.out.println("Request cookies: ");
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                System.out.println("  " + cookie.getName() + " = " + cookie.getValue());
            }
        }
        System.out.println("========================");
        
        // Check session first
        if (session != null && session.getAttribute("adminId") != null) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Session valid",
                "data", Map.of(
                    "adminId", session.getAttribute("adminId"),
                    "adminUsername", session.getAttribute("adminUsername"),
                    "adminRole", session.getAttribute("adminRole")
                )
            ));
        }
        
        // Check Authorization header as fallback
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Received token: " + token);
            
            // Simple token validation (ในระบบจริงควรใช้ JWT หรือ database lookup)
            if (token.startsWith("session-")) {
                // สำหรับ demo ให้ผ่านทุก token ที่ขึ้นต้นด้วย session-
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token valid",
                    "data", Map.of(
                        "adminId", 1L,
                        "adminUsername", "admin",
                        "adminRole", 1
                    )
                ));
            }
        }
        
        return ResponseEntity.status(401)
            .body(Map.of("success", false, "message", "Session expired"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            session.invalidate();
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Logged out successfully"
        ));
    }
}
