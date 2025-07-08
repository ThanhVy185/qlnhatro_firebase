// File: com/huflit/qlnhatro/Tao_HoaDon.java
package com.huflit.qlnhatro;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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

import Data.Constants;
import Data.HoaDon;
import Data.Room;

public class Tao_HoaDon extends AppCompatActivity {

    Spinner spinnerPhong;
    EditText edtSoDien, edtSoNuoc, edtTienPhong;
    TextView tvTongTien, tvTenNguoiThue, tvNgayLap, tvThanhTienDien, tvThanhTienNuoc;
    Button btnLuu, btnHuy;

    private String maPhong, tenNguoi, sdt, ngayLap;
    private double tienDien, tienNuoc, tienPhong;
    private double soDien, soNuoc;
    private FirebaseFirestore db;

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

    private void addControls() {
        spinnerPhong = findViewById(R.id.spinnerPhong);
        edtSoDien = findViewById(R.id.edtTienDien);
        edtSoNuoc = findViewById(R.id.edtTienNuoc);
        edtTienPhong = findViewById(R.id.edtTienPhong);
        tvTongTien = findViewById(R.id.tvTongTien);
        btnLuu = findViewById(R.id.btnLuuHoaDon);
        btnHuy = findViewById(R.id.btnThoat);
        tvTenNguoiThue = findViewById(R.id.tvTenNguoiThue);
        tvNgayLap = findViewById(R.id.tvNgayLap);
        tvThanhTienDien = findViewById(R.id.tvThanhTienDien);
        tvThanhTienNuoc = findViewById(R.id.tvDien);
    }

    private void setupInitialData() {
        ngayLap = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvNgayLap.setText(ngayLap);
        edtTienPhong.setText(String.valueOf(Constants.GIA_PHONG_CO_BAN));
        btnLuu.setEnabled(false);
    }

    private void setupSpinner() {
        db.collection("rooms").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu phòng", Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<Room> roomList = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Room room = doc.toObject(Room.class);
                if (room != null) {
                    room.setId(doc.getId());
                    roomList.add(room);
                }
            }
            ArrayAdapter<Room> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPhong.setAdapter(adapter);
        });
    }

    private boolean validateInput() {
        if (spinnerPhong.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng chọn một phòng", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(edtSoDien.getText().toString().trim())) {
            edtSoDien.setError("Vui lòng nhập số điện");
            return false;
        }
        if (TextUtils.isEmpty(edtSoNuoc.getText().toString().trim())) {
            edtSoNuoc.setError("Vui lòng nhập số nước");
            return false;
        }
        return true;
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
        edtSoDien.addTextChangedListener(textWatcher);
        edtSoNuoc.addTextChangedListener(textWatcher);
        edtTienPhong.addTextChangedListener(textWatcher);

        btnHuy.setOnClickListener(v -> finish());

        btnLuu.setOnClickListener(v -> {
            if (!validateInput()) {
                return;
            }
            capNhatTongTien();
            HoaDon hoaDon = new HoaDon(maPhong, tenNguoi, sdt, tienPhong, tienDien, tienNuoc, ngayLap, "Chưa thanh toán", soDien, soNuoc);
            LuuHoaDonLenFirestore(hoaDon);
        });

        spinnerPhong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Room selectedRoom = (Room) parent.getItemAtPosition(position);
                String selectedRoomId = selectedRoom.getId();
                maPhong = selectedRoom.getNumber();
                db.collection("contracts").whereEqualTo("roomId", selectedRoomId).limit(1).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot contractDoc = queryDocumentSnapshots.getDocuments().get(0);
                                tenNguoi = contractDoc.getString("tenantName");
                                if (tenNguoi != null && !tenNguoi.trim().isEmpty()) {
                                    tvTenNguoiThue.setText(tenNguoi);
                                    btnLuu.setEnabled(true);
                                } else {
                                    tvTenNguoiThue.setText("Phòng này chưa có người thuê");
                                    btnLuu.setEnabled(false);
                                }
                            } else {
                                tvTenNguoiThue.setText("Không có hợp đồng cho phòng này");
                                btnLuu.setEnabled(false);
                            }
                        });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                btnLuu.setEnabled(false);
            }
        });
    }

    private void capNhatTongTien() {
        String soDienStr = edtSoDien.getText().toString().trim();
        String soNuocStr = edtSoNuoc.getText().toString().trim();
        String tienPhongStr = edtTienPhong.getText().toString().trim();

        try {
            this.soDien = soDienStr.isEmpty() ? 0 : Double.parseDouble(soDienStr);
            this.soNuoc = soNuocStr.isEmpty() ? 0 : Double.parseDouble(soNuocStr);
            this.tienPhong = tienPhongStr.isEmpty() ? 0 : Double.parseDouble(tienPhongStr);
            this.tienDien = this.soDien * Constants.GIA_DIEN;
            this.tienNuoc = this.soNuoc * Constants.GIA_NUOC;
        } catch (NumberFormatException e) {
            this.tienDien = this.tienNuoc = this.tienPhong = 0;
        }

        tvThanhTienDien.setText(String.format(Locale.getDefault(), "Thành tiền: %,.0f VNĐ", tienDien));
        tvThanhTienNuoc.setText(String.format(Locale.getDefault(), "Thành tiền: %,.0f VNĐ", tienNuoc));

        double tong = tienDien + tienNuoc + tienPhong;
        tvTongTien.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", tong));
    }

    private void LuuHoaDonLenFirestore(HoaDon hoaDon) {
        btnLuu.setEnabled(false); // Vô hiệu hóa nút Lưu để tránh nhấn nhiều lần

        db.collection("HoaDon").add(hoaDon).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Lưu hóa đơn thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Quay về màn hình trước đó
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi lưu hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnLuu.setEnabled(true); // Bật lại nút Lưu nếu có lỗi
        });
    }
}