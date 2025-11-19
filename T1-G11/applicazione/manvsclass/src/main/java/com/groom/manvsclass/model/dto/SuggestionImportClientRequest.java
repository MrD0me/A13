package com.groom.manvsclass.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SuggestionImportClientRequest {

    private String className;

    private List<Item> suggestions = new ArrayList<>();

    @Data
    public static class Item {
        private String difficulty;
        private String text;
    }
}
