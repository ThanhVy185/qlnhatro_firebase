public class PhongTro {
    private String tenPhong;
    private long gia;
    private String trangThai;

    public PhongTro() {
        // Bắt buộc để Firestore tự map dữ liệu
    }

    public PhongTro(String tenPhong, long gia, String trangThai) {
        this.tenPhong = tenPhong;
        this.gia = gia;
        this.trangThai = trangThai;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public long getGia() {
        return gia;
    }

    public String getTrangThai() {
        return trangThai;
    }
}
