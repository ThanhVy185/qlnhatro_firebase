package Data;

public final class Constants {
    public static long giadien; // Thêm 'final' để không cho lớp khác kế thừa

    // Private constructor để ngăn không cho ai tạo đối tượng từ lớp này
    private Constants() {}

    public static final long GIA_DIEN = 3500; // 3,500 VNĐ / kWh
    public static final long GIA_NUOC = 15000; // 15,000 VNĐ / m³
    public static final long GIA_PHONG_CO_BAN = 2000000; // 2,000,000 VNĐ / tháng
}