package com.keycard.controller;

import com.keycard.dto.*;
import com.keycard.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public List<CardDTO> list(
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String q) {
        if (tag != null && !tag.isBlank()) return cardService.findByTag(tag);
        if (q != null && !q.isBlank()) return cardService.search(q);
        return cardService.listAll();
    }

    @GetMapping("/{id}")
    public CardDTO get(@PathVariable Long id) {
        return cardService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CardDTO> create(@Valid @RequestBody CardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.create(request));
    }

    @PutMapping("/{id}")
    public CardDTO update(@PathVariable Long id, @Valid @RequestBody CardRequest request) {
        return cardService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        cardService.delete(id);
        return Map.of("message", "删除成功");
    }

    @GetMapping("/export")
    public List<CardDTO> exportAll() {
        return cardService.exportAll();
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCards(@RequestBody List<CardDTO> data) {
        int count = cardService.importCards(data);
        return ResponseEntity.ok(Map.of("message", "导入成功", "count", count));
    }
}
