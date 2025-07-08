package com.huflit.qlnhatro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DangkiActivity extends AppCompatActivity {

    EditText edtFullNamedk, edtEmaildk, edtPassworddk, edtConfirmPassworddk;
    Button btnDangKy;
    TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangki);

        edtFullNamedk = findViewById(R.id.edtFullNamedk);
        edtEmaildk = findViewById(R.id.edtEmaildk);
        edtPassworddk = findViewById(R.id.edtPassworddk);
        edtConfirmPassworddk = findViewById(R.id.edtConfirmPassworddk);
        btnDangKy = findViewById(R.id.button_dangky);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(DangkiActivity.this, DangnhapActivity.class));
            finish();
        });

        btnDangKy.setOnClickListener(v -> {
            String name = edtFullNamedk.getText().toString().trim();
            String email = edtEmaildk.getText().toString().trim();
            String password = edtPassworddk.getText().toString().trim();
            String confirmPassword = edtConfirmPassworddk.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu thông tin vào SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("fullname", name);
            editor.putString("email", email);
            editor.putString("password", password);
            editor.apply();

            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển về màn hình đăng nhập
            startActivity(new Intent(DangkiActivity.this, DangnhapActivity.class));
            finish();
        });
    }
}
