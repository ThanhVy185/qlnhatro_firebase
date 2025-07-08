// File: com/huflit/qlnhatro/Tao_HoaDon.java
package com.huflit.qlnhatro;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import Data.HoaDon;
import Data.Room; // Import lớp Room của bạn

public class Tao_HoaDon extends AppCompatActivity {

    // --- KHAI BÁO BIẾN ---
    Spinner spinnerPhong;
    EditText edtTienDien, edtTienNuoc, edtTienPhong;
    TextView tvTongTien, tvTenNguoiThue, tvNgayLap;
    Button btnLuu, btnHuy;

    private String maPhong, tenNguoi, sdt, ngayLap;
    private double tienDien, tienNuoc, tienPhong;
    private FirebaseFirestore db;

    // --- VÒNG ĐỜI ACTIVITY ---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_hoa_don);

        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addControls();
        setupInitialData();
        setupSpinner();
        handleEvents();
    }

    // --- CÁC HÀM THIẾT LẬP & XỬ LÝ ---

    private void addControls() {
        spinnerPhong = findViewById(R.id.spinnerPhong);
        edtTienDien = findViewById(R.id.edtTienDien);
        edtTienNuoc = findViewById(R.id.edtTienNuoc);
        edtTienPhong = findViewById(R.id.edtTienPhong);
        tvTongTien = findViewById(R.id.tvTongTien);
        btnLuu = findViewById(R.id.btnLuuHoaDon);
        btnHuy = findViewById(R.id.btnThoat);
        tvTenNguoiThue = findViewById(R.id.tvTenNguoiThue);
        tvNgayLap = findViewById(R.id.tvNgayLap);
    }

    private void setupInitialData() {
        ngayLap = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvNgayLap.setText(ngayLap);
        btnLuu.setEnabled(false);
    }

    /**
     * Tải danh sách phòng từ collection 'rooms' lên Spinner.
     */
    private void setupSpinner() {
        db.collection("rooms").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy phòng nào.", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<Room> roomList = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                // TỰ ĐỘNG CHUYỂN DỮ LIỆU TỪ FIRESTORE SANG ĐỐI TƯỢNG ROOM
                Room room = doc.toObject(Room.class);
                if (room != null) {
                    room.setId(doc.getId()); // Gán ID của tài liệu vào đối tượng room
                    roomList.add(room);
                }
            }

            ArrayAdapter<Room> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPhong.setAdapter(adapter);

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi tải danh sách phòng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void handleEvents() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                capNhatTongTien();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        edtTienDien.addTextChangedListener(textWatcher);
        edtTienNuoc.addTextChangedListener(textWatcher);
        edtTienPhong.addTextChangedListener(textWatcher);

        btnHuy.setOnClickListener(v -> finish());

        btnLuu.setOnClickListener(v -> {
            capNhatTongTien();
            HoaDon hoaDon = new HoaDon(maPhong, tenNguoi, sdt, tienPhong, tienDien, tienNuoc, ngayLap, "Chưa thanh toán");
            LuuHoaDonLenFirestore(hoaDon);
        });

        spinnerPhong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Room selectedRoom = (Room) parent.getItemAtPosition(position);
                String selectedRoomId = selectedRoom.getId();
                // LẤY SỐ PHÒNG TỪ `getNumber()` THEO ĐÚNG ĐỊNH NGHĨA CLASS ROOM
                maPhong = selectedRoom.getNumber();

                db.collection("contracts").whereEqualTo("roomId", selectedRoomId).limit(1).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot contractDoc = queryDocumentSnapshots.getDocuments().get(0);
                                tenNguoi = contractDoc.getString("tenantName");

                                if (tenNguoi != null && !tenNguoi.trim().isEmpty()) {
                                    tvTenNguoiThue.setText("Người thuê: " + tenNguoi);
                                    btnLuu.setEnabled(true);
                                } else {
                                    tvTenNguoiThue.setText("Phòng này chưa có hợp đồng");
                                    btnLuu.setEnabled(false);
                                }
                            } else {
                                tvTenNguoiThue.setText("Không có hợp đồng cho phòng này");
                                btnLuu.setEnabled(false);
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(Tao_HoaDon.this, "Lỗi khi tìm hợp đồng.", Toast.LENGTH_SHORT).show();
                            btnLuu.setEnabled(false);
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                btnLuu.setEnabled(false);
            }
        });
    }

    private void capNhatTongTien() {
        String tienDienStr = edtTienDien.getText().toString().trim();
        String tienNuocStr = edtTienNuoc.getText().toString().trim();
        String tienPhongStr = edtTienPhong.getText().toString().trim();
        try {
            tienDien = tienDienStr.isEmpty() ? 0 : Double.parseDouble(tienDienStr);
            tienNuoc = tienNuocStr.isEmpty() ? 0 : Double.parseDouble(tienNuocStr);
            tienPhong = tienPhongStr.isEmpty() ? 0 : Double.parseDouble(tienPhongStr);
        } catch (NumberFormatException e) {
            tienDien = 0;
            tienNuoc = 0;
            tienPhong = 0;
        }
        double tong = tienDien + tienNuoc + tienPhong;
        tvTongTien.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", tong));
    }

    private void LuuHoaDonLenFirestore(HoaDon hoaDon) {
        db.collection("HoaDon").add(hoaDon).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Lưu hóa đơn thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi lưu hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}