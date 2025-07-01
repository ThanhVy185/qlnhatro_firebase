package com.huflit.qlnhatro;

public class Contract {
    private String id;
    private String roomId;
    private String contractCode;
    private String tenantName;
    private String identityNumber;
    private String imageUrl;
    private long electricityFee;
    private long waterFee;
    private long serviceFee;
    private long parkingFee;
    private String invoiceId;
    private long dueDate;
    private boolean isPaid;

    // Firestore cần constructor không args
    public Contract() {}

    public Contract(String id,
                    String roomId,
                    String contractCode,
                    String tenantName,
                    String identityNumber,
                    String imageUrl,
                    long electricityFee,
                    long waterFee,
                    long serviceFee,
                    long parkingFee,
                    String invoiceId,
                    long dueDate,
                    boolean isPaid) {
        this.id = id;
        this.roomId = roomId;
        this.contractCode = contractCode;
        this.tenantName = tenantName;
        this.identityNumber = identityNumber;
        this.imageUrl = imageUrl;
        this.electricityFee = electricityFee;
        this.waterFee = waterFee;
        this.serviceFee = serviceFee;
        this.parkingFee = parkingFee;
        this.invoiceId = invoiceId;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
    }

    // === Getters & Setters ===

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getContractCode() {
        return contractCode;
    }
    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public String getTenantName() {
        return tenantName;
    }
    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }
    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getElectricityFee() {
        return electricityFee;
    }
    public void setElectricityFee(long electricityFee) {
        this.electricityFee = electricityFee;
    }

    public long getWaterFee() {
        return waterFee;
    }
    public void setWaterFee(long waterFee) {
        this.waterFee = waterFee;
    }

    public long getServiceFee() {
        return serviceFee;
    }
    public void setServiceFee(long serviceFee) {
        this.serviceFee = serviceFee;
    }

    public long getParkingFee() {
        return parkingFee;
    }
    public void setParkingFee(long parkingFee) {
        this.parkingFee = parkingFee;
    }

    public String getInvoiceId() {
        return invoiceId;
    }
    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public long getDueDate() {
        return dueDate;
    }
    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isPaid() {
        return isPaid;
    }
    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    /**
     * Tính tổng các khoản phí
     */
    public long getTotalFee() {
        return electricityFee + waterFee + serviceFee + parkingFee;
    }
}