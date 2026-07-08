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

            String cardsHtml;
            if ("kanban".equals(templateName)) {
                cardsHtml = renderKanban(cards);
            } else if ("mindmap".equals(templateName)) {
                cardsHtml = renderMindmap(cards);
            } else {
                StringBuilder sb = new StringBuilder();
                for (Map<String, Object> card : cards) {
                    sb.append(renderCardBlock(templateName, card));
                }
                cardsHtml = sb.toString();
            }

            return templateContent
                    .replace("{{ site_title }}", "关键卡片")
                    .replace("{{ cards_html }}", cardsHtml);
        } catch (IOException e) {
            throw new RuntimeException("读取模板失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String renderKanban(List<Map<String, Object>> cards) {
        java.util.LinkedHashMap<String, java.util.List<Map<String, Object>>> groups = new java.util.LinkedHashMap<>();
        for (Map<String, Object> card : cards) {
            List<Map<String, String>> tags = (List<Map<String, String>>) card.get("tags");
            String groupName = "未分类";
            if (tags != null && !tags.isEmpty()) {
                groupName = tags.get(0).get("name");
            }
            groups.computeIfAbsent(groupName, k -> new ArrayList<>()).add(card);
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, java.util.List<Map<String, Object>>> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            java.util.List<Map<String, Object>> groupCards = entry.getValue();

            String tagColor = "#722ed1";
            Map<String, Object> firstCard = groupCards.get(0);
            List<Map<String, String>> firstTags = (List<Map<String, String>>) firstCard.get("tags");
            if (firstTags != null && !firstTags.isEmpty()) {
                String c = firstTags.get(0).get("color");
                if (c != null && !c.isEmpty()) tagColor = c;
            }
            String borderColor = escapeHtml(tagColor);

            sb.append("<div class=\"kanban-column\">");
            sb.append("<div class=\"kanban-column-header\">");
            sb.append("<span class=\"dot\" style=\"background:").append(borderColor).append("\"></span>");
            sb.append(escapeHtml(groupName));
            sb.append("<span class=\"count\">").append(groupCards.size()).append("</span>");
            sb.append("</div>");

            for (Map<String, Object> card : groupCards) {
                String source = (String) card.get("source");
                String sourceHtml = "";
                if (source != null && !source.isEmpty()) {
                    sourceHtml = "<div>来源：" + escapeHtml(source) + "</div>";
                }
                sb.append("<div class=\"kanban-card\" style=\"border-left-color:").append(borderColor).append("\">");
                sb.append("<h3>").append(escapeHtml((String) card.get("title"))).append("</h3>");
                sb.append("<div class=\"content\">").append(escapeHtml((String) card.get("content"))).append("</div>");
                if (!sourceHtml.isEmpty()) {
                    sb.append("<div class=\"meta\">").append(sourceHtml).append("</div>");
                }
                sb.append("</div>");
            }
            sb.append("</div>");
        }
        return sb.toString();
    }

    private String renderMindmap(List<Map<String, Object>> cards) {
        if (cards.isEmpty()) return "";

        Map<String, Object> center = cards.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"mindmap-center\">");
        sb.append("<div class=\"center-node\">").append(escapeHtml((String) center.get("title"))).append("</div>");
        sb.append("</div>");
        sb.append("<div class=\"connector\"><div class=\"connector-line\"></div></div>");

        if (cards.size() > 1) {
            sb.append("<div class=\"mindmap-branches\">");
            for (int i = 1; i < cards.size(); i++) {
                sb.append(renderCardBlock("mindmap", cards.get(i)));
            }
            sb.append("</div>");
        }
        return sb.toString();
    }

    private String renderCardBlock(String templateName, Map<String, Object> card) {
        String tagsHtml = "";
        @SuppressWarnings("unchecked")
        List<Map<String, String>> tags = (List<Map<String, String>>) card.get("tags");
        if (tags != null && !tags.isEmpty()) {
            StringBuilder tagSb = new StringBuilder();
            for (Map<String, String> tag : tags) {
                String name = tag.get("name");
                String color = tag.get("color");
                String style = "";
                if (color != null && !color.isEmpty()) {
                    style = " style=\"background:" + escapeHtml(color) + ";color:#fff\"";
                }
                tagSb.append("<span class=\"tag\"").append(style).append(">").append(escapeHtml(name)).append("</span>");
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

            case "faq" -> """
                <div class="faq-item">
                    <div class="faq-question">${title} <span class="arrow">▼</span></div>
                    <div class="faq-answer"><div class="faq-answer-inner">${content}${meta}</div></div>
                </div>
                """.replace("${title}", escapeHtml((String) card.get("title")))
                   .replace("${content}", escapeHtml((String) card.get("content")))
                   .replace("${meta}", tagsHtml.isEmpty() && sourceHtml.isEmpty() ? "" : "<div class=\"faq-meta\">" + tagsHtml + sourceHtml + "</div>");

            case "gallery" -> """
                <div class="gallery-card">
                    <h2>${title}</h2>
                    <div class="content">${content}</div>
                    <div class="meta">${tags}${source}</div>
                </div>
                """.replace("${title}", escapeHtml((String) card.get("title")))
                   .replace("${content}", escapeHtml((String) card.get("content")))
                   .replace("${tags}", tagsHtml)
                   .replace("${source}", sourceHtml);

            case "mindmap" -> """
                <div class="branch">
                    <h3>${title}</h3>
                    <div class="content">${content}</div>
                    <div class="meta">${tags}${source}</div>
                </div>
                """.replace("${title}", escapeHtml((String) card.get("title")))
                   .replace("${content}", escapeHtml((String) card.get("content")))
                   .replace("${tags}", tagsHtml)
                   .replace("${source}", sourceHtml);

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
