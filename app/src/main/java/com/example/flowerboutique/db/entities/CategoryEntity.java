package com.example.flowerboutique.db.entities;

public class CategoryEntity {
    private String name;
    private String thumbnail;
    private boolean status;

    public CategoryEntity() {}

    public CategoryEntity(String name, String thumbnail, boolean status) {
        this.name = name;
        this.thumbnail = thumbnail;
        this.status = status;
    }

    public String getName() { return name; }
    public String getThumbnail() { return thumbnail; }
    public boolean isStatus() { return status; }
}
