package com.huflit.qlnhatro;

/**
 * Model cho một hợp đồng.
 */
public class Contract {
    private String id;
    private String roomId;
    private String roomNumber;
    private String tenantId;
    private String tenantName;

    private long rent;
    private long deposit;
    private long electricityFee;
    private long waterFee;

    private String startDate;
    private String endDate;
    private long createdAt;

    // Firestore cần constructor không args
    public Contract() {}

    // Getters và setters
    public String getId()               { return id; }
    public void   setId(String id)      { this.id = id; }

    public String getRoomId()           { return roomId; }
    public void   setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomNumber()       { return roomNumber; }
    public void   setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getTenantId()         { return tenantId; }
    public void   setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTenantName()       { return tenantName; }
    public void   setTenantName(String tenantName) { this.tenantName = tenantName; }

    public long   getRent()             { return rent; }
    public void   setRent(long rent)    { this.rent = rent; }

    public long   getDeposit()          { return deposit; }
    public void   setDeposit(long deposit) { this.deposit = deposit; }

    public long   getElectricityFee()   { return electricityFee; }
    public void   setElectricityFee(long electricityFee) { this.electricityFee = electricityFee; }

    public long   getWaterFee()         { return waterFee; }
    public void   setWaterFee(long waterFee) { this.waterFee = waterFee; }

    public String getStartDate()        { return startDate; }
    public void   setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate()          { return endDate; }
    public void   setEndDate(String endDate) { this.endDate = endDate; }

    public long   getCreatedAt()        { return createdAt; }
    public void   setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
