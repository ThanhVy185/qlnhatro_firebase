package com.huflit.qlnhatro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnQLPhong, btnQLHopDong, btnQLHoaDon, btnQLThongBao, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnQLPhong = findViewById(R.id.btnQLPhong);
        btnQLHopDong = findViewById(R.id.btnQLHopDong);
        btnQLHoaDon = findViewById(R.id.btnQLHoaDon);
        btnQLThongBao = findViewById(R.id.btnQLThongBao);
        btnExit = findViewById(R.id.btnExit);

        btnQLPhong.setOnClickListener(v ->
                startActivity(new Intent(this, RoomManagementActivity.class)));

        btnQLHopDong.setOnClickListener(v ->
                startActivity(new Intent(this, ContractListActivity.class)));
// chưa có Hóa đơn và thông báo!!
       /* btnQLHoaDon.setOnClickListener(v ->
                startActivity(new Intent(this, QuanLyHoaDonActivity.class)));

        btnQLThongBao.setOnClickListener(v ->
                startActivity(new Intent(this, QuanLyThongBaoActivity.class)));*/

        btnExit.setOnClickListener(v -> finishAffinity()); // Thoát ứng dụng
    }
}