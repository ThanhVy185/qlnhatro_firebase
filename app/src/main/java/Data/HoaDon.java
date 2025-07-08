// File: Data/HoaDon.java
package Data;

public class HoaDon {
    private String id; // Trường này dùng để lưu ID từ Firestore

    private String maPhong;
    private String tenNguoiThue;
    private String sdt;
    private double tienPhong;
    private double tienDien;
    private double tienNuoc;
    private String ngayLap;
    private String trangThai;
    private double soDien; // kWh
    private double soNuoc; // m³

    // Constructor rỗng cần cho Firestore
    public HoaDon() {}

    // Constructor đầy đủ
    public HoaDon(String maPhong, String tenNguoiThue, String sdt, double tienPhong, double tienDien, double tienNuoc, String ngayLap, String trangThai, double soDien, double soNuoc) {
        this.maPhong = maPhong;
        this.tenNguoiThue = tenNguoiThue;
        this.sdt = sdt;
        this.tienPhong = tienPhong;
        this.tienDien = tienDien;
        this.tienNuoc = tienNuoc;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
        this.soDien = soDien;
        this.soNuoc = soNuoc;
    }

    // --- GETTERS AND SETTERS ĐẦY ĐỦ ---

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public String getTenNguoiThue() { return tenNguoiThue; }
    public void setTenNguoiThue(String tenNguoiThue) { this.tenNguoiThue = tenNguoiThue; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public double getTienPhong() { return tienPhong; }
    public void setTienPhong(double tienPhong) { this.tienPhong = tienPhong; }

    public double getTienDien() { return tienDien; }
    public void setTienDien(double tienDien) { this.tienDien = tienDien; }

    public double getTienNuoc() { return tienNuoc; }
    public void setTienNuoc(double tienNuoc) { this.tienNuoc = tienNuoc; }

    public String getNgayLap() { return ngayLap; }
    public void setNgayLap(String ngayLap) { this.ngayLap = ngayLap; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public double getSoDien() { return soDien; }
    public void setSoDien(double soDien) { this.soDien = soDien; }

    public double getSoNuoc() { return soNuoc; }
    public void setSoNuoc(double soNuoc) { this.soNuoc = soNuoc; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }





}