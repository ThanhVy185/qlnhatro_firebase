package com.huflit.qlnhatro;

/**
 * Model class for Tenant.
 * Includes both roomId (to reference Firestore document) and roomNumber for display.
 */
public class Tenant {
    private String id;
    private String name;
    private String phone;
    private String roomId;
    private String roomNumber;

    // No-arg constructor required by Firestore
    public Tenant() {}

    /**
     * Full constructor
     * @param id Firestore document ID
     * @param name Tenant's name
     * @param phone Tenant's phone
     * @param roomId Firestore room document ID
     * @param roomNumber Displayable room number string
     */
    public Tenant(String id, String name, String phone, String roomId, String roomNumber) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    /**
     * The Firestore document ID of the room this tenant occupies.
     */
    public String getRoomId() {
        return roomId;
    }

    /**
     * The displayable room number (e.g., "101").
     */
    public String getRoomNumber() {
        return roomNumber;
    }


}
