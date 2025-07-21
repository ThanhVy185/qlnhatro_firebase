package com.huflit.qlnhatro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class Hoadon_chitiet extends AppCompatActivity {
    TextView txtMaPhg, txtTen, txtSDT, txtTienPhg, txtTienDien, txtNuoc, txtTong, txtNgayLap, txtTrangThaiThanhToan, txtChiTietDien, txtChiTietNuoc;
    ImageView imgClose;

    private FirebaseFirestore db;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoadon_chitiet);

        db = FirebaseFirestore.getInstance();

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        addControls();
        loadDataFromIntent();
        handleEvents();
    }

    private void addControls() {
        txtMaPhg = findViewById(R.id.txtMaPhg);
        imgClose = findViewById(R.id.imgClose);
        txtTen = findViewById(R.id.txtTen);
        txtSDT = findViewById(R.id.txtSDT);
        txtTienPhg = findViewById(R.id.txtTienPhg);
        txtTienDien = findViewById(R.id.txtTienDien);
        txtNuoc = findViewById(R.id.txtNuoc);
        txtTong = findViewById(R.id.txtTong);
        txtNgayLap = findViewById(R.id.txtNgayLap);
        txtTrangThaiThanhToan = findViewById(R.id.txtTrangThaiThanhToan);
        txtChiTietDien = findViewById(R.id.txtChiTietDien);
        txtChiTietNuoc = findViewById(R.id.txtChiTietNuoc);
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();

        documentId = intent.getStringExtra("documentId");

        String maPhong = intent.getStringExtra("maPhong");
        String tenNguoi = intent.getStringExtra("tenNguoi");
        String sdt = intent.getStringExtra("sdt");
        String ngayLap = intent.getStringExtra("ngayLap");
        String trangThai = intent.getStringExtra("trangThai");

        double tienPhong = intent.getDoubleExtra("tienPhong", 0.0);
        double tienDien = intent.getDoubleExtra("tienDien", 0.0);
        double tienNuoc = intent.getDoubleExtra("tienNuoc", 0.0);
        double soDien = intent.getDoubleExtra("soDien", 0.0);
        double soNuoc = intent.getDoubleExtra("soNuoc", 0.0);
        double tongTien = tienPhong + tienDien + tienNuoc;

        txtMaPhg.setText(maPhong);
        txtTen.setText(tenNguoi);
        txtSDT.setText(sdt);
        txtTienPhg.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", tienPhong));
        txtTienDien.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", tienDien));
        txtNuoc.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", tienNuoc));
        txtTong.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", tongTien));
        txtNgayLap.setText(ngayLap);
        txtTrangThaiThanhToan.setText(trangThai);
        txtChiTietDien.setText(String.format(Locale.getDefault(), "(%.1f kWh)", soDien));
        txtChiTietNuoc.setText(String.format(Locale.getDefault(), "(%.1f m³)", soNuoc));
    }

    private void handleEvents() {
        imgClose.setOnClickListener(v -> finish());

        // Xử lý sự kiện khi nhấn vào trạng thái thanh toán
        txtTrangThaiThanhToan.setOnClickListener(v -> {
            String currentStatus = txtTrangThaiThanhToan.getText().toString();
            String newStatus = currentStatus.equals("Chưa thanh toán") ? "Đã thanh toán" : "Chưa thanh toán";
            updatePaymentStatus(newStatus);
        });
    }

    private void updatePaymentStatus(String newStatus) {
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID hóa đơn.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference hoaDonRef = db.collection("HoaDon").document(documentId);

        // Cập nhật trường "trangThai" trên Firestore
        hoaDonRef.update("trangThai", newStatus)
                .addOnSuccessListener(aVoid -> {
                    txtTrangThaiThanhToan.setText(newStatus);
                    Toast.makeText(Hoadon_chitiet.this, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Hoadon_chitiet.this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void updateStatusUI(String status) {
        txtTrangThaiThanhToan.setText(status);
        if ("Đã thanh toán".equalsIgnoreCase(status)) {
            // Đổi sang màu xanh
            txtTrangThaiThanhToan.setTextColor(ContextCompat.getColor(this, R.color.paid_text_color));
            txtTrangThaiThanhToan.setBackgroundResource(R.drawable.bg_status_paid);
        } else {
            // Đổi sang màu đỏ
            txtTrangThaiThanhToan.setTextColor(ContextCompat.getColor(this, R.color.unpaid_text_color));
            txtTrangThaiThanhToan.setBackgroundResource(R.drawable.bg_status_unpaid);
        }
    }
}