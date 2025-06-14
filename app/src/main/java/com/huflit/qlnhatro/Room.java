package com.huflit.qlnhatro;

public class Room {
    private String id;
    private String number;
    private String name;

    public Room() {}  // required for Firestore

    public Room(String id, String number, String name) {
        this.id = id;
        this.number = number;
        this.name = name;
    }

    public String getId() { return id; }
    public String getNumber() { return number; }
    public String getName() { return name; }
}
