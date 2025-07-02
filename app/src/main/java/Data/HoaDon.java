package Data;

import com.google.firebase.firestore.Exclude;

public class HoaDon {
    // Các trường dữ liệu sẽ được lưu trên Firestore
    private String maPhong;
    private String tenNguoi;
    private String sdt;
    private double tienPhong;
    private double tienDien;
    private double tienNuoc;
    private String ngayLapHoaDon;
    private String trangThai;

    // THÊM TRƯỜNG NÀY: Dùng để lưu ID của document khi đọc từ Firestore,
    // nhưng không lưu ngược lại vào document.
    @Exclude
    private String documentId;

    // Constructor mặc định (Bắt buộc phải có cho Firebase)
    public HoaDon() {
    }

    // Constructor có tham số (Đã sửa lại thứ tự cho đúng)
    public HoaDon(String maPhong, String tenNguoi, String sdt, double tienPhong, double tienDien, double tienNuoc, String ngayLapHoaDon, String trangThai) {
        this.maPhong = maPhong;
        this.tenNguoi = tenNguoi;
        this.sdt = sdt;
        this.tienPhong = tienPhong;
        this.tienDien = tienDien;
        this.tienNuoc = tienNuoc;
        this.ngayLapHoaDon = ngayLapHoaDon;
        this.trangThai = trangThai;
    }

    // --- THÊM GETTER VÀ SETTER CHO DOCUMENT ID ---
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // --- CÁC GETTER VÀ SETTER CÒN LẠI ---
    // (Tôi đã bỏ @Exclude khỏi maPhong)
    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenNguoi() {
        return tenNguoi;
    }

    public void setTenNguoi(String tenNguoi) {
        this.tenNguoi = tenNguoi;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public double getTienPhong() {
        return tienPhong;
    }

    public void setTienPhong(double tienPhong) {
        this.tienPhong = tienPhong;
    }

    public double getTienDien() {
        return tienDien;
    }

    public void setTienDien(double tienDien) {
        this.tienDien = tienDien;
    }

    public double getTienNuoc() {
        return tienNuoc;
    }

    public void setTienNuoc(double tienNuoc) {
        this.tienNuoc = tienNuoc;
    }

    public String getNgayLapHoaDon() {
        return ngayLapHoaDon;
    }

    public void setNgayLapHoaDon(String ngayLapHoaDon) {
        this.ngayLapHoaDon = ngayLapHoaDon;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    // Hàm tính tổng tiền
    @Exclude // Không cần lưu tổng tiền vào database vì có thể tính toán được
    public double TongTien() {
        return tienDien + tienNuoc + tienPhong;
    }
}
