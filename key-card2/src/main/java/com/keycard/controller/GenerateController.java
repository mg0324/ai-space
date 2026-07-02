package com.keycard.controller;

import com.keycard.dto.GenerateRequest;
import com.keycard.service.GenerateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/generate")
public class GenerateController {

    private final GenerateService generateService;

    public GenerateController(GenerateService generateService) {
        this.generateService = generateService;
    }

    @PostMapping("/preview")
    public Map<String, String> preview(@RequestBody GenerateRequest request) {
        if (request.getCardIds() == null || request.getCardIds().isEmpty() || request.getTemplate() == null || request.getTemplate().isBlank()) {
            return Map.of("error", "请选择卡片和模板");
        }
        return generateService.preview(request);
    }

    @PostMapping("/export")
    public ResponseEntity<?> export(@RequestBody GenerateRequest request) throws IOException {
        if (request.getCardIds() == null || request.getCardIds().isEmpty() || request.getTemplate() == null || request.getTemplate().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请选择卡片和模板"));
        }
        return ResponseEntity.ok(generateService.export(request));
    }
}
