package com.keycard.controller;

import com.keycard.dto.TagDTO;
import com.keycard.dto.TagRequest;
import com.keycard.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<TagDTO> list() {
        return tagService.listAll();
    }

    @PostMapping
    public ResponseEntity<TagDTO> create(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.create(request));
    }

    @PutMapping("/{id}")
    public TagDTO rename(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return tagService.rename(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Map.of("message", "删除成功");
    }
}
