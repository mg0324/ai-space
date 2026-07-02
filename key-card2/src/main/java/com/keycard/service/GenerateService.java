package com.keycard.service;

import com.keycard.dto.CardDTO;
import com.keycard.dto.GenerateRequest;
import com.keycard.entity.Card;
import com.keycard.repository.CardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GenerateService {

    private final CardRepository cardRepository;
    private final TemplateService templateService;

    @Value("${app.output-dir}")
    private String outputDir;

    public GenerateService(CardRepository cardRepository, TemplateService templateService) {
        this.cardRepository = cardRepository;
        this.templateService = templateService;
    }

    public Map<String, String> preview(GenerateRequest request) {
        List<Card> cards = findOrderedCards(request.getCardIds());
        List<Map<String, Object>> cardMaps = toCardMaps(cards);
        String html = templateService.renderTemplate(request.getTemplate(), cardMaps);
        Map<String, String> result = new HashMap<>();
        result.put("html", html);
        return result;
    }

    public Map<String, String> export(GenerateRequest request) throws IOException {
        List<Card> cards = findOrderedCards(request.getCardIds());
        List<Map<String, Object>> cardMaps = toCardMaps(cards);
        String html = templateService.renderTemplate(request.getTemplate(), cardMaps);

        String exportName = request.getTemplate() + "_" + cards.size() + "cards";
        Path exportPath = Path.of(outputDir, exportName);
        Files.createDirectories(exportPath);
        Files.writeString(exportPath.resolve("index.html"), html, StandardCharsets.UTF_8);

        Map<String, String> result = new HashMap<>();
        result.put("path", exportPath.toString());
        result.put("message", "导出成功");
        return result;
    }

    private List<Card> findOrderedCards(List<Long> ids) {
        List<Card> cards = cardRepository.findAllById(ids);
        Map<Long, Card> map = cards.stream().collect(Collectors.toMap(Card::getId, c -> c));
        return ids.stream()
                .map(map::get)
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> toCardMaps(List<Card> cards) {
        return cards.stream().map(card -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", card.getId());
            m.put("title", card.getTitle());
            m.put("content", card.getContent());
            m.put("source", card.getSource());
            m.put("tags", card.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()));
            m.put("created_at", card.getCreatedAt() != null ? card.getCreatedAt().toString() : null);
            m.put("updated_at", card.getUpdatedAt() != null ? card.getUpdatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
    }
}
