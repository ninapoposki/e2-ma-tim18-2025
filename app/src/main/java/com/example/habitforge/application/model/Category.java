package com.example.habitforge.application.model;

public class Category {
    private String id;
    private String name;
    private String color;
    //boolean isUsed-moze se menjati kad ne pripada tasku vise,tj kad se obrise?
    public Category() {}

    public Category(String id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
