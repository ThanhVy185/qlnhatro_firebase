package com.huflit.qlnhatro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    MaterialCardView btnQLPhong, btnQLHopDong, btnQLHoaDon, btnQLThongBao;
    TextView btnExit;

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
                startActivity(new Intent(this, ContractManagementActivity.class)));

        btnQLHoaDon.setOnClickListener(v ->
                startActivity(new Intent(this, HoaDonActivity.class)));

        // Nếu chưa dùng thì comment lại
        /*
        btnQLThongBao.setOnClickListener(v ->
                startActivity(new Intent(this, QuanLyThongBaoActivity.class)));
        */

        btnExit.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DangnhapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
