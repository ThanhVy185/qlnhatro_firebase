package com.huflit.qlnhatro;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.card.MaterialCardView;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    MaterialCardView btnQLPhong, btnQLHopDong, btnQLHoaDon, btnReport,btnDienNuoc;
    TextView btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnQLPhong    = findViewById(R.id.btnQLPhong);
        btnQLHopDong  = findViewById(R.id.btnQLHopDong);
        btnQLHoaDon   = findViewById(R.id.btnQLHoaDon);
        btnReport     = findViewById(R.id.btnReport);        // <- ID mới
        btnExit       = findViewById(R.id.btnExit);
        btnDienNuoc   =findViewById(R.id.btnDienNuoc);

        btnQLPhong.setOnClickListener(v ->
                startActivity(new Intent(this, RoomManagementActivity.class)));

        btnQLHopDong.setOnClickListener(v ->
                startActivity(new Intent(this, ContractManagementActivity.class)));

        btnQLHoaDon.setOnClickListener(v ->
                startActivity(new Intent(this, HoaDonActivity.class)));

        // Khi nhấn vào “Báo Cáo Thống Kê” mở ReportActivity
        btnReport.setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class)));
        btnDienNuoc.setOnClickListener(view ->
                startActivity(new Intent(this, DienNuocActivity.class)));

        btnExit.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DangnhapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}