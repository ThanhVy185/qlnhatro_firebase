package com.huflit.qlnhatro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    // SỬA LỖI: Thay đổi kiểu dữ liệu từ Button sang MaterialCardView
    MaterialCardView btnQLPhong, btnQLHopDong, btnQLHoaDon, btnQLThongBao;
    TextView btnExit, tvAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Đảm bảo bạn đang dùng layout mới

        // Ánh xạ các View
        btnQLPhong = findViewById(R.id.btnQLPhong);
        btnQLHopDong = findViewById(R.id.btnQLHopDong);
        btnQLHoaDon = findViewById(R.id.btnQLHoaDon);
        btnQLThongBao = findViewById(R.id.btnQLThongBao);
        btnExit = findViewById(R.id.btnExit);
        tvAdmin = findViewById(R.id.tvAdmin);

        // Kiểm tra và hiển thị tên người dùng (nếu có)
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userFullName = prefs.getString("user_fullname", "ADMIN");
        tvAdmin.setText("Chào mừng, " + userFullName);


        // Gán sự kiện cho các thẻ
        btnQLPhong.setOnClickListener(v ->
                startActivity(new Intent(this, RoomManagementActivity.class)));

        btnQLHopDong.setOnClickListener(v ->
                startActivity(new Intent(this, ContractListActivity.class)));

        btnQLHoaDon.setOnClickListener(v ->
                startActivity(new Intent(this, HoaDonActivity.class)));

        // Bạn có thể bỏ comment khi đã có QuanLyThongBaoActivity
        /*
        btnQLThongBao.setOnClickListener(v ->
                startActivity(new Intent(this, QuanLyThongBaoActivity.class)));
        */

        // Sự kiện cho nút thoát
        btnExit.setOnClickListener(v -> {
            // Xóa thông tin đăng nhập và quay về màn hình đăng nhập
            // Nếu bạn muốn thoát hoàn toàn, dùng finishAffinity()
            Intent intent = new Intent(HomeActivity.this, DangnhapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
