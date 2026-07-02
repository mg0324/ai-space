package com.keycard.dto;

import jakarta.validation.constraints.NotBlank;

public class TagRequest {

    @NotBlank(message = "标签名不能为空")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
