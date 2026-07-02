package com.keycard.dto;

public class TemplateDTO {

    private String name;
    private String label;
    private String description;

    public TemplateDTO() {}

    public TemplateDTO(String name, String label, String description) {
        this.name = name;
        this.label = label;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
