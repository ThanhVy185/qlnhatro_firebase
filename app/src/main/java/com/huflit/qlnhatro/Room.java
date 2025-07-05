package com.huflit.qlnhatro;

public class Room {
    private String id;
    private String number;
    private String name;
    private int maxOccupancy;

    public Room() {}

    public Room(String id, String number, String name, int maxOccupancy) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.maxOccupancy = maxOccupancy;
    }

    public String getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxOccupancy(int maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    @Override
    public String toString() {
        return number;  // dùng cho Spinner hiển thị
    }
}
