package com.huflit.qlnhatro;

import com.google.firebase.firestore.PropertyName;



public class Contract {
    private String id;
    private String contractCode;
    private String roomNumber;
    private String tenantName;
    private String identityNumber;
    private String imageUrl;
    private String roomId;
    private long createDate;
    private long endDate;

    public Contract() {
        // Constructor rỗng cần thiết cho Firebase
    }

    public Contract(String id, String contractCode, String roomNumber, String tenantName, String identityNumber, String imageUrl) {
        this.id = id;
        this.contractCode = contractCode;
        this.roomNumber = roomNumber;
        this.tenantName = tenantName;
        this.identityNumber = identityNumber;
        this.imageUrl = imageUrl;
        this.roomId = roomId;
        this.createDate =createDate;
        this.endDate=endDate;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getContractCode() {
        return contractCode;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public String getRoomId() {
        return roomId;}

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public long getCreateDate() { return createDate; }
    public void setCreateDate(long createDate) { this.createDate = createDate; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }
}
