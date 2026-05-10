package com.myandroid.nariguard;

import java.util.List;

public class SafetyModel {

    private String title;
    private List<String> tips;

    public SafetyModel(String title, List<String> tips) {
        this.title = title;
        this.tips = tips;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getTips() {
        return tips;
    }
}