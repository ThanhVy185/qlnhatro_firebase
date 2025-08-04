package com.huflit.qlnhatro;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import Data.HoaDon;

public class ReportActivity extends AppCompatActivity {

    // Khai báo các View cho phần thống kê tổng quan
    private TextView tvTotalRooms, tvRentedRooms, tvAvailableRooms, tvTotalInvoices;

    // Khai báo các View cho phần biểu đồ
    private Spinner spnMonth, spnYear;
    private Button btnFilterMonthly;
    private LineChart lineChartMonthly;

    private FirebaseFirestore db;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // <<< THAY ĐỔI: Sử dụng layout activity_report >>>
        setContentView(R.layout.activity_report);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();

        // Ánh xạ các View từ layout
        bindViews();

        // Tải tất cả dữ liệu thống kê khi Activity được tạo
        loadAllStatistics();

        // Cấu hình cho phần biểu đồ doanh thu
        setupMonthlyChartFilter();
    }

    /**
     * Hàm để ánh xạ các view từ file XML.
     */
    private void bindViews() {
        // Thống kê tổng quan
        tvTotalRooms = findViewById(R.id.tvTotalRooms);
        tvRentedRooms = findViewById(R.id.tvRentedRooms);
        tvAvailableRooms = findViewById(R.id.tvAvailableRooms);
        tvTotalInvoices = findViewById(R.id.tvTotalInvoices);

        // Biểu đồ doanh thu
        spnMonth = findViewById(R.id.spnMonth);
        spnYear = findViewById(R.id.spnYear);
        btnFilterMonthly = findViewById(R.id.btnFilterMonthly);
        lineChartMonthly = findViewById(R.id.lineChartMonthly);
    }

    /**
     * Hàm chính để gọi các hàm con tải từng loại dữ liệu thống kê.
     */
    private void loadAllStatistics() {
        fetchRoomStatistics();
        fetchInvoiceStatistics();
    }

    /**
     * Lấy dữ liệu về phòng: tổng số, đã thuê, còn trống.
     */
    private void fetchRoomStatistics() {
        // B1: Lấy tất cả hợp đồng để gom roomId duy nhất
        db.collection("contracts").get()
                .addOnSuccessListener(contractsSnapshot -> {
                    Set<String> rentedRoomIds = new HashSet<>();
                    for (QueryDocumentSnapshot c : contractsSnapshot) {
                        String roomId = c.getString("roomId");
                        if (roomId != null) {
                            rentedRoomIds.add(roomId); // chỉ giữ unique
                        }
                    }

                    // B2: Lấy tất cả phòng và tính toán
                    db.collection("rooms").get()
                            .addOnSuccessListener(roomsSnapshot -> {
                                int totalRooms = roomsSnapshot.size();
                                int rentedCount = 0;
                                int emptyCount = 0;

                                for (QueryDocumentSnapshot r : roomsSnapshot) {
                                    String roomId = r.getId();
                                    if (rentedRoomIds.contains(roomId)) {
                                        rentedCount++;
                                    } else {
                                        emptyCount++;
                                    }
                                }

                                // B3: Gán vào TextView
                                tvTotalRooms.setText(String.valueOf(totalRooms));
                                tvRentedRooms.setText(String.valueOf(rentedCount));
                                tvAvailableRooms.setText(String.valueOf(emptyCount));
                            })
                            .addOnFailureListener(e -> Log.e("ReportActivity", "Lỗi khi lấy phòng: ", e));
                })
                .addOnFailureListener(e -> Log.e("ReportActivity", "Lỗi khi lấy hợp đồng: ", e));
    }


    /**
     * Lấy tổng số hóa đơn đã được tạo.
     */
    private void fetchInvoiceStatistics() {
        db.collection("HoaDon").get().addOnSuccessListener(invoicesSnapshot -> {
            tvTotalInvoices.setText(String.valueOf(invoicesSnapshot.size()));
        }).addOnFailureListener(e -> Log.e("ReportActivity", "Lỗi khi lấy tổng số hóa đơn: ", e));
    }

    /**
     * Cấu hình Spinner và Button cho bộ lọc doanh thu theo tháng.
     */
    private void setupMonthlyChartFilter() {
        Calendar calendar = Calendar.getInstance();

        // Cấu hình Spinner tháng
        List<String> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) months.add("Tháng " + m);
        spnMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months));
        spnMonth.setSelection(calendar.get(Calendar.MONTH));

        // Cấu hình Spinner năm
        int currentYear = calendar.get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int y = currentYear - 5; y <= currentYear; y++) years.add(String.valueOf(y));
        spnYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years));
        spnYear.setSelection(years.indexOf(String.valueOf(currentYear)));

        // Sự kiện cho nút lọc
        btnFilterMonthly.setOnClickListener(v -> {
            // Lấy tháng (vị trí 0 -> tháng 1) và năm đã chọn
            int month = spnMonth.getSelectedItemPosition() + 1;
            int year = Integer.parseInt((String) spnYear.getSelectedItem());
            fetchMonthlyRevenue(month, year);
        });

        // Tải biểu đồ cho tháng hiện tại khi mở màn hình
        fetchMonthlyRevenue(calendar.get(Calendar.MONTH) + 1, currentYear);
    }

    /**
     * Lấy dữ liệu và vẽ biểu đồ doanh thu cho một tháng/năm cụ thể.
     * @param month Tháng cần thống kê (1-12)
     * @param year  Năm cần thống kê
     */
    private void fetchMonthlyRevenue(int month, int year) {
        Toast.makeText(this, "Đang tải doanh thu tháng " + month + "/" + year, Toast.LENGTH_SHORT).show();

        db.collection("HoaDon")
                .whereEqualTo("trangThai", "Đã thanh toán")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Map để lưu tổng doanh thu theo từng ngày của tháng (Key: ngày, Value: tổng tiền)
                    Map<Integer, Double> monthlyRevenueMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HoaDon hd = doc.toObject(HoaDon.class);
                        try {
                            Date invoiceDate = DATE_FORMAT.parse(hd.getNgayLap());
                            if (invoiceDate != null) {
                                Calendar invoiceCal = Calendar.getInstance();
                                invoiceCal.setTime(invoiceDate);

                                // Kiểm tra hóa đơn có thuộc tháng và năm đã chọn không
                                if (invoiceCal.get(Calendar.MONTH) + 1 == month && invoiceCal.get(Calendar.YEAR) == year) {
                                    double total = hd.getTienPhong() + hd.getTienDien() + hd.getTienNuoc();
                                    int dayOfMonth = invoiceCal.get(Calendar.DAY_OF_MONTH);
                                    monthlyRevenueMap.put(dayOfMonth, monthlyRevenueMap.getOrDefault(dayOfMonth, 0.0) + total);
                                }
                            }
                        } catch (ParseException e) {
                            Log.e("ReportActivity", "Lỗi parse ngày: " + hd.getNgayLap());
                        }
                    }

                    // Chuẩn bị dữ liệu để vẽ biểu đồ
                    List<Entry> entries = new ArrayList<>();
                    Calendar tempCal = Calendar.getInstance();
                    tempCal.set(year, month - 1, 1);
                    int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

                    for (int i = 1; i <= daysInMonth; i++) {
                        float revenue = monthlyRevenueMap.getOrDefault(i, 0.0).floatValue();
                        entries.add(new Entry(i, revenue));
                    }

                    // Cập nhật biểu đồ
                    updateLineChart(entries, "Doanh thu tháng " + month);

                }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải dữ liệu doanh thu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Cập nhật LineChart với dữ liệu mới.
     * @param entries Danh sách các điểm dữ liệu
     * @param label   Nhãn cho bộ dữ liệu
     */
    private void updateLineChart(List<Entry> entries, String label) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(getResources().getColor(android.R.color.holo_blue_light));
        dataSet.setValueTextColor(getResources().getColor(android.R.color.black));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));

        LineData lineData = new LineData(dataSet);
        lineChartMonthly.setData(lineData);

        // Cấu hình trục X
        XAxis xAxis = lineChartMonthly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // Đảm bảo chỉ hiển thị các ngày nguyên

        // Cấu hình chung
        lineChartMonthly.getDescription().setEnabled(false);
        lineChartMonthly.getAxisRight().setEnabled(false); // Ẩn trục Y bên phải
        lineChartMonthly.animateX(1000); // Thêm hiệu ứng animation
        lineChartMonthly.invalidate(); // Vẽ lại biểu đồ
    }
}
