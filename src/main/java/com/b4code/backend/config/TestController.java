package com.b4code.backend.config;

import com.b4code.backend.dao.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
// Exposing this specifically for DEV to prevent accidental production leaks
@Profile("dev") 
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        long userCount = userRepository.count();
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Database successfully connected! Backend is running properly.",
            "database_user_count", userCount
        ));
    }
}
