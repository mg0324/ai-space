package com.keycard.dto;

import java.util.List;

public class GenerateRequest {

    private List<Long> cardIds;
    private String template;

    public List<Long> getCardIds() { return cardIds; }
    public void setCardIds(List<Long> cardIds) { this.cardIds = cardIds; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
}
