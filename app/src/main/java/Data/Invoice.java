package Data;

public class Invoice {
    private String id;
    private String contractId;
    private long issuedAt;
    private long oldElectricityReading;
    private long newElectricityReading;
    private long waterFee;
    private long serviceFee;
    private long parkingFee;
    private boolean paid;

    public Invoice() {}

    public Invoice(String id,
                   String contractId,
                   long issuedAt,
                   long oldElectricityReading,
                   long newElectricityReading,
                   long waterFee,
                   long serviceFee,
                   long parkingFee,
                   boolean paid) {
        this.id = id;
        this.contractId = contractId;
        this.issuedAt = issuedAt;
        this.oldElectricityReading = oldElectricityReading;
        this.newElectricityReading = newElectricityReading;
        this.waterFee = waterFee;
        this.serviceFee = serviceFee;
        this.parkingFee = parkingFee;
        this.paid = paid;
    }

    // === Getters & Setters ===

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getContractId() {
        return contractId;
    }
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public long getIssuedAt() {
        return issuedAt;
    }
    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public long getOldElectricityReading() {
        return oldElectricityReading;
    }
    public void setOldElectricityReading(long oldElectricityReading) {
        this.oldElectricityReading = oldElectricityReading;
    }

    public long getNewElectricityReading() {
        return newElectricityReading;
    }
    public void setNewElectricityReading(long newElectricityReading) {
        this.newElectricityReading = newElectricityReading;
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

    public boolean isPaid() {
        return paid;
    }
    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    /**
     * Tính tiền điện theo công thức:
     * (số điện mới – số điện cũ) × ĐƠN GIÁ mặc định
     */
    public long getElectricityFee() {
        long diff = newElectricityReading - oldElectricityReading;
        if (diff < 0) {
            throw new IllegalArgumentException("Chỉ số mới phải ≥ chỉ số cũ");
        }
        return diff * Constants.giadien;
    }

    /**
     * Tính tổng hóa đơn
     */
    public long getTotalFee() {
        return getElectricityFee() + waterFee + serviceFee + parkingFee;
    }
}
