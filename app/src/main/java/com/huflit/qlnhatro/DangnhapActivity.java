package com.huflit.qlnhatro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DangnhapActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtFullName;
    Button buttonDangNhap, buttonDangKy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        buttonDangNhap = findViewById(R.id.button_dangnhap);
        buttonDangKy = findViewById(R.id.button_dangki);



        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (!prefs.contains("email") && !prefs.contains("password")) {
            editor.putString("fullname", "Administrator");
            editor.putString("email", "admin@gmail.com");
            editor.putString("password", "admin213");
            editor.apply();
        }

        // Xử lý nút "Đăng ký" → chuyển qua MainActivity
        buttonDangKy.setOnClickListener(v -> {
            Intent intent = new Intent(DangnhapActivity.this, DangkiActivity.class);
            startActivity(intent);
            finish();
        });

        // Xử lý nút "Đăng nhập"
        buttonDangNhap.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String inputEmail = edtEmail.getText().toString().trim();
            String inputPassword = edtPassword.getText().toString().trim();


            String savedEmail = prefs.getString("email", "");
            String savedPassword = prefs.getString("password", "");

            if (inputEmail.equals(savedEmail) && inputPassword.equals(savedPassword)) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // Chuyển sang trang chính, ví dụ: PhongActivity
                Intent intent = new Intent(DangnhapActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
            }

        });


    }

}
