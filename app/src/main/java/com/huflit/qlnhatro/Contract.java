package com.huflit.qlnhatro;

import com.google.firebase.firestore.PropertyName;

public class Contract {
    private String id;
    private String contractCode;
    private String roomId;
    private String tenantName;
    private String identityNumber;
    private long endDate;
    private String imageUrl;
    private String invoiceId;
    private boolean isPaid;

    // Mandatory empty constructor for Firestore deserialization
    public Contract() {}

    // --- id ---
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // --- contractCode ---
    public String getContractCode() {
        return contractCode;
    }
    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    // --- roomId ---
    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    // --- tenantName ---
    public String getTenantName() {
        return tenantName;
    }
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    // --- identityNumber ---
    public String getIdentityNumber() {
        return identityNumber;
    }
    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    // --- endDate ---
    public long getEndDate() {
        return endDate;
    }
    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    // --- imageUrl ---
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // --- invoiceId ---
    public String getInvoiceId() {
        return invoiceId;
    }
    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    // --- isPaid ---
    @PropertyName("isPaid")
    public boolean isPaid() {
        return isPaid;
    }

    @PropertyName("isPaid")
    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }
}
