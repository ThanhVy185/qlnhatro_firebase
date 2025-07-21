package com.huflit.qlnhatro;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Adapter.HoaDonAdapter;
import Data.HoaDon;

public class HoaDonActivity extends AppCompatActivity {

    // Khai báo các View
    private ListView lvHoaDon;
    private Button btnTaoMoi, btnDelete;
    private RadioGroup rgFilterStatus;
    private Spinner spnFilterMonth, spnFilterYear; // Spinner mới

    private HoaDonAdapter hoaDonAdapter;
    private ArrayList<HoaDon> fullHoaDonList; // Danh sách chứa TẤT CẢ hóa đơn từ Firestore
    private ArrayList<HoaDon> filteredHoaDonList; // Danh sách hiển thị sau khi lọc

    private FirebaseFirestore db;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoa_don);

        db = FirebaseFirestore.getInstance();

        // <<< THAY ĐỔI: Gọi hàm để thêm dữ liệu mẫu >>>
        addSampleData();

        // Ánh xạ các View
        bindViews();

        // Khởi tạo danh sách
        fullHoaDonList = new ArrayList<>();
        filteredHoaDonList = new ArrayList<>();
        hoaDonAdapter = new HoaDonAdapter(this, filteredHoaDonList);
        lvHoaDon.setAdapter(hoaDonAdapter);

        // Cấu hình các bộ lọc
        setupFilters();

        // Đăng ký các sự kiện
        setupEventListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Tải dữ liệu từ Firestore mỗi khi Activity được hiển thị
        loadAllInvoicesFromFirestore();
    }

    private void bindViews() {
        lvHoaDon = findViewById(R.id.listHoaDon);
        btnTaoMoi = findViewById(R.id.btnTaoMoi);
        btnDelete = findViewById(R.id.btnDelete);
        rgFilterStatus = findViewById(R.id.rgFilterStatus);
        spnFilterMonth = findViewById(R.id.spnFilterMonth);
        spnFilterYear = findViewById(R.id.spnFilterYear);
    }

    /**
     * Cấu hình giá trị ban đầu và sự kiện cho các Spinner lọc.
     */
    private void setupFilters() {
        // --- Cấu hình Spinner Tháng ---
        List<String> months = new ArrayList<>();
        months.add("Tất cả tháng"); // Lựa chọn để không lọc theo tháng
        for (int m = 1; m <= 12; m++) {
            months.add("Tháng " + m);
        }
        spnFilterMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months));

        // --- Cấu hình Spinner Năm ---
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        years.add("Tất cả năm"); // Lựa chọn để không lọc theo năm
        for (int y = currentYear - 5; y <= currentYear; y++) {
            years.add(String.valueOf(y));
        }
        spnFilterYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years));

        // --- Tạo listener chung cho tất cả các bộ lọc ---
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        rgFilterStatus.setOnCheckedChangeListener((group, checkedId) -> applyFilters());
        spnFilterMonth.setOnItemSelectedListener(spinnerListener);
        spnFilterYear.setOnItemSelectedListener(spinnerListener);
    }

    /**
     * Tải toàn bộ hóa đơn từ Firestore và lưu vào danh sách gốc (fullHoaDonList).
     */
    private void loadAllInvoicesFromFirestore() {
        db.collection("HoaDon").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullHoaDonList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HoaDon hoaDon = doc.toObject(HoaDon.class);
                        hoaDon.setId(doc.getId());
                        fullHoaDonList.add(hoaDon);
                    }
                    // Sau khi tải xong, áp dụng bộ lọc để hiển thị
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Áp dụng tất cả các bộ lọc hiện tại vào danh sách gốc và cập nhật ListView.
     */
    private void applyFilters() {
        // Lấy giá trị từ các bộ lọc
        int selectedStatusId = rgFilterStatus.getCheckedRadioButtonId();
        int selectedMonthPos = spnFilterMonth.getSelectedItemPosition();
        int selectedYearPos = spnFilterYear.getSelectedItemPosition();

        String statusFilter = null;
        if (selectedStatusId == R.id.rbPaid) {
            statusFilter = "Đã thanh toán";
        } else if (selectedStatusId == R.id.rbUnpaid) {
            statusFilter = "Chưa thanh toán";
        }

        int monthFilter = selectedMonthPos > 0 ? selectedMonthPos : -1;
        int yearFilter = -1;
        if (selectedYearPos > 0) {
            yearFilter = Integer.parseInt((String) spnFilterYear.getItemAtPosition(selectedYearPos));
        }

        // Bắt đầu lọc
        ArrayList<HoaDon> tempFilteredList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (HoaDon hd : fullHoaDonList) {
            // 1. Kiểm tra trạng thái
            boolean statusMatch = (statusFilter == null) || hd.getTrangThai().equalsIgnoreCase(statusFilter);

            // Nếu trạng thái không khớp, bỏ qua và xét hóa đơn tiếp theo
            if (!statusMatch) {
                continue;
            }

            // 2. Kiểm tra ngày tháng năm
            boolean dateMatch = (monthFilter == -1 && yearFilter == -1); // Mặc định khớp nếu không có bộ lọc ngày
            if (!dateMatch) { // Chỉ kiểm tra nếu có chọn bộ lọc tháng hoặc năm
                try {
                    Date date = sdf.parse(hd.getNgayLap());
                    if (date != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        // Điều kiện khớp khi: bộ lọc là "Tất cả" HOẶC giá trị khớp
                        boolean monthMatches = (monthFilter == -1) || (cal.get(Calendar.MONTH) + 1 == monthFilter);
                        boolean yearMatches = (yearFilter == -1) || (cal.get(Calendar.YEAR) == yearFilter);
                        if (monthMatches && yearMatches) {
                            dateMatch = true;
                        }
                    }
                } catch (ParseException e) {
                    dateMatch = false; // Nếu ngày không hợp lệ, coi như không khớp
                }
            }

            // Nếu cả hai điều kiện đều khớp, thêm vào danh sách hiển thị
            if (dateMatch) {
                tempFilteredList.add(hd);
            }
        }

        // Cập nhật danh sách hiển thị và ListView
        filteredHoaDonList.clear();
        filteredHoaDonList.addAll(tempFilteredList);
        hoaDonAdapter.notifyDataSetChanged();
        selectedPosition = -1; // Reset lựa chọn
    }

    private void setupEventListeners() {
        btnTaoMoi.setOnClickListener(v -> {
            Intent intent = new Intent(HoaDonActivity.this, Tao_HoaDon.class);
            startActivity(intent);
        });

        lvHoaDon.setOnItemClickListener((adapterView, itemView, position, id) -> {
            selectedPosition = position;
            hoaDonAdapter.setSelectedPosition(position);
            hoaDonAdapter.notifyDataSetChanged();
        });

        lvHoaDon.setOnItemLongClickListener((parent, view, position, id) -> {
            HoaDon hd = filteredHoaDonList.get(position); // Lấy từ danh sách đã lọc
            Intent intent = new Intent(HoaDonActivity.this, Hoadon_chitiet.class);
            intent.putExtra("documentId", hd.getId());
            intent.putExtra("maPhong", hd.getMaPhong());
            intent.putExtra("tenNguoi", hd.getTenNguoiThue());
            intent.putExtra("sdt", hd.getSdt());
            intent.putExtra("tienPhong", hd.getTienPhong());
            intent.putExtra("tienDien", hd.getTienDien());
            intent.putExtra("tienNuoc", hd.getTienNuoc());
            intent.putExtra("ngayLap", hd.getNgayLap());
            intent.putExtra("trangThai", hd.getTrangThai());
            intent.putExtra("soDien", hd.getSoDien());
            intent.putExtra("soNuoc", hd.getSoNuoc());
            startActivity(intent);
            return true;
        });

        btnDelete.setOnClickListener(v -> {
            if (selectedPosition == -1) {
                Toast.makeText(this, "Vui lòng chọn một hóa đơn để xóa", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa hóa đơn này?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteSelectedHoaDon())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    /**
     * <<< HÀM ĐƯỢC CẬP NHẬT: Thêm dữ liệu mẫu dựa trên hình ảnh vào tháng 6 >>>
     */
    private void addSampleData() {
        // Dùng một mã phòng đặc biệt để kiểm tra xem dữ liệu đã được thêm chưa
        db.collection("HoaDon").whereEqualTo("maPhong", "P-SAMPLE-TEST").limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Nếu chưa có, tiến hành thêm dữ liệu mẫu
                        WriteBatch batch = db.batch();
                        ArrayList<HoaDon> sampleList = new ArrayList<>();

                        // Dữ liệu từ hình ảnh, nhưng đổi ngày sang tháng 6

                        sampleList.add(new HoaDon("10", "Sang", "0902223344", 2000000, 220000, 120000, "21/05/2025", "Chưa thanh toán", 65.0, 8.0));
                        sampleList.add(new HoaDon("15", "Vy", "0903334455", 2000000, 190000, 90000, "25/05/2025", "Đã thanh toán", 55.0, 6.0));
                        sampleList.add(new HoaDon("19", "Trang", "0904445566", 2000000, 250000, 130000, "09/05/2025", "Chưa thanh toán", 70.0, 9.0));

                        for (HoaDon hoaDon : sampleList) {
                            DocumentReference docRef = db.collection("HoaDon").document();
                            batch.set(docRef, hoaDon);
                        }
                        batch.commit().addOnSuccessListener(aVoid -> {
                            Toast.makeText(HoaDonActivity.this, "Đã thêm dữ liệu mẫu.", Toast.LENGTH_SHORT).show();
                            // Tải lại dữ liệu sau khi thêm thành công
                            loadAllInvoicesFromFirestore();
                        }).addOnFailureListener(e -> {
                            Log.e("HoaDonActivity", "Lỗi khi thêm dữ liệu mẫu: ", e);
                        });
                    }
                    // Nếu đã có dữ liệu thì không làm gì cả
                });
    }


    private void deleteSelectedHoaDon() {
        if (selectedPosition < 0 || selectedPosition >= filteredHoaDonList.size()) return;
        String documentId = filteredHoaDonList.get(selectedPosition).getId();
        db.collection("HoaDon").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa hóa đơn thành công", Toast.LENGTH_SHORT).show();
                    loadAllInvoicesFromFirestore(); // Tải lại toàn bộ dữ liệu
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
