package com.huflit.qlnhatro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DienNuocActivity extends AppCompatActivity {

    TextInputEditText edtGiaDien, edtGiaNuoc;
    Button btnLuuGia;
    FirebaseFirestore db;
    DocumentReference giaRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo bạn dùng đúng tên file layout đã tạo
        setContentView(R.layout.activity_dien_nuoc);

        db = FirebaseFirestore.getInstance();
        // Tham chiếu đến một document cố định để lưu giá
        // Điều này đảm bảo giá luôn được đọc và ghi vào cùng một chỗ
        giaRef = db.collection("CaiDat").document("donGia");

        // Ánh xạ các thành phần giao diện
        edtGiaDien = findViewById(R.id.edtGiaDien);
        edtGiaNuoc = findViewById(R.id.edtGiaNuoc);
        btnLuuGia = findViewById(R.id.btnLuuGia);

        // Tải đơn giá hiện tại lên giao diện
        loadCurrentPrices();

        // Gán sự kiện cho nút lưu
        btnLuuGia.setOnClickListener(v -> savePrices());
    }

    /**
     * Tải đơn giá hiện tại từ Firebase và hiển thị lên các ô EditText.
     */
    private void loadCurrentPrices() {
        giaRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Lấy giá trị từ document
                Double giaDien = documentSnapshot.getDouble("giaDien");
                Double giaNuoc = documentSnapshot.getDouble("giaNuoc");

                // Hiển thị giá trị lên màn hình
                if (giaDien != null) {
                    edtGiaDien.setText(String.valueOf(giaDien.longValue()));
                }
                if (giaNuoc != null) {
                    edtGiaNuoc.setText(String.valueOf(giaNuoc.longValue()));
                }
            } else {
                // Nếu chưa có giá trị nào được lưu, thông báo cho người dùng
                Toast.makeText(this, "Chưa có đơn giá. Vui lòng thiết lập.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi tải đơn giá.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Lưu đơn giá mới lên Firebase.
     */
    private void savePrices() {
        String giaDienStr = edtGiaDien.getText().toString().trim();
        String giaNuocStr = edtGiaNuoc.getText().toString().trim();

        if (giaDienStr.isEmpty() || giaNuocStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ đơn giá", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double giaDien = Double.parseDouble(giaDienStr);
            double giaNuoc = Double.parseDouble(giaNuocStr);

            // Tạo một đối tượng Map để lưu dữ liệu
            Map<String, Object> prices = new HashMap<>();
            prices.put("giaDien", giaDien);
            prices.put("giaNuoc", giaNuoc);

            // Ghi đè (hoặc tạo mới) document "donGia" với dữ liệu mới
            giaRef.set(prices)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(DienNuocActivity.this, "Lưu đơn giá thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng màn hình sau khi lưu thành công
                    })
                    .addOnFailureListener(e -> Toast.makeText(DienNuocActivity.this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Đơn giá không hợp lệ. Vui lòng chỉ nhập số.", Toast.LENGTH_SHORT).show();
        }
    }
}
