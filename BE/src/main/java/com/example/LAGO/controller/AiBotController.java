package com.example.LAGO.controller;

import com.example.LAGO.service.AiBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-bot")
@RequiredArgsConstructor
public class AiBotController {

    private final AiBotService aiBotService;

    @GetMapping("/status/{personality}")
    public ResponseEntity<?> getStatus(@PathVariable String personality) {
        return ResponseEntity.ok(aiBotService.getAiBotStatus(personality));
    }
}
