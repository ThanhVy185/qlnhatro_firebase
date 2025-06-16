package com.huflit.qlnhatro;

public class Room {
    private String id;
    private String number;
    private String name;
    private int maxOccupancy;

    // Firestore cần constructor không args
    public Room() {}

    // Constructor mới với maxOccupancy
    public Room(String id, String number, String name, int maxOccupancy) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.maxOccupancy = maxOccupancy;
    }

    // getters
    public String getId()            { return id; }
    public String getNumber()        { return number; }
    public String getName()          { return name; }
    public int    getMaxOccupancy()  { return maxOccupancy; }
}
