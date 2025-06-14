package com.huflit.qlnhatro;

public class Tenant {
    private String id;
    private String name;
    private String phone;
    private String roomId;

    public Tenant() {}

    public Tenant(String id, String name, String phone, String roomId) {
        this.id = id; this.name = name;
        this.phone = phone; this.roomId = roomId;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRoomId() { return roomId; }
}
