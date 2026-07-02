package com.keycard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keycard.dto.TemplateDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class TemplateService {

    private final ObjectMapper objectMapper;

    @Value("${app.templates-dir}")
    private String templatesDir;

    public TemplateService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<TemplateDTO> listTemplates() {
        List<TemplateDTO> result = new ArrayList<>();
        File dir = new File(templatesDir);
        if (!dir.isDirectory()) return result;

        File[] subDirs = dir.listFiles(File::isDirectory);
        if (subDirs == null) return result;

        for (File subDir : subDirs) {
            File metaFile = new File(subDir, "meta.json");
            String label = subDir.getName();
            String description = "";

            if (metaFile.isFile()) {
                try {
                    JsonNode meta = objectMapper.readTree(metaFile);
                    if (meta.has("label")) label = meta.get("label").asText();
                    if (meta.has("description")) description = meta.get("description").asText();
                } catch (IOException ignored) {}
            }

            result.add(new TemplateDTO(subDir.getName(), label, description));
        }

        result.sort(Comparator.comparing(TemplateDTO::getName));
        return result;
    }

    public String renderTemplate(String templateName, List<Map<String, Object>> cards) {
        Path templatePath = Path.of(templatesDir, templateName, "template.html");
        if (!Files.exists(templatePath)) {
            throw new RuntimeException("模板不存在: " + templateName);
        }

        try {
            String templateContent = Files.readString(templatePath, StandardCharsets.UTF_8);

            StringBuilder cardsHtml = new StringBuilder();
            for (Map<String, Object> card : cards) {
                cardsHtml.append(renderCardBlock(templateName, card));
            }

            return templateContent
                    .replace("{{ site_title }}", "关键卡片")
                    .replace("{{ cards_html }}", cardsHtml.toString());
        } catch (IOException e) {
            throw new RuntimeException("读取模板失败", e);
        }
    }

    private String renderCardBlock(String templateName, Map<String, Object> card) {
        String tagsHtml = "";
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) card.get("tags");
        if (tags != null && !tags.isEmpty()) {
            StringBuilder tagSb = new StringBuilder();
            for (String tag : tags) {
                tagSb.append("<span class=\"tag\">").append(escapeHtml(tag)).append("</span>");
            }
            tagsHtml = "<div>" + tagSb + "</div>";
        }

        String sourceHtml = "";
        String source = (String) card.get("source");
        if (source != null && !source.isEmpty()) {
            sourceHtml = "<div>来源：" + escapeHtml(source) + "</div>";
        }

        return switch (templateName) {
            case "timeline" -> """
                <div class="entry">
                    <h2>${title}</h2>
                    <div class="content">${content}</div>
                    <div class="meta">${tags}${source}</div>
                </div>
                """.replace("${title}", escapeHtml((String) card.get("title")))
                   .replace("${content}", escapeHtml((String) card.get("content")))
                   .replace("${tags}", tagsHtml)
                   .replace("${source}", sourceHtml);

            case "comparison" -> """
                <div class="card">
                    <h2>${title}</h2>
                    <div class="content">${content}</div>
                    ${tags}
                </div>
                """.replace("${title}", escapeHtml((String) card.get("title")))
                   .replace("${content}", escapeHtml((String) card.get("content")))
                   .replace("${tags}", tagsHtml.isEmpty() ? "" : "<div style=\"margin-top: 1rem;\">" + tagsHtml + "</div>");

            default -> """
                <div class="card">
                    <h2>${title}</h2>
                    <div class="content">${content}</div>
                    <div class="meta">${tags}${source}</div>
                </div>
                """.replace("${title}", escapeHtml((String) card.get("title")))
                   .replace("${content}", escapeHtml((String) card.get("content")))
                   .replace("${tags}", tagsHtml.isEmpty() ? "" : "标签：" + tagsHtml)
                   .replace("${source}", sourceHtml);
        };
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
