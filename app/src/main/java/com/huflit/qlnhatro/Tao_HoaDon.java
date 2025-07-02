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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import Data.HoaDon;

public class Tao_HoaDon extends AppCompatActivity {

    Spinner spinnerPhong;
    EditText edtTienDien, edtTienNuoc, edtTienPhong;
    // Đã loại bỏ tvSoNgayThue khỏi danh sách biến
    TextView tvTongTien, tvTenNguoiThue, tvNgayLap;
    Button btnLuu, btnHuy;

    private String maPhong, tenNguoi, sdt, ngayLap;
    private int soNgayThue;
    private double tienDien, tienNuoc, tienPhong;
    private FirebaseFirestore db;
    private static boolean isSeedingContracts = false;

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
        seedContractsIfEmpty();
        handleEvents();
    }

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
        // Đã loại bỏ dòng ánh xạ tvSoNgayThue
    }

    private void setupInitialData() {
        ngayLap = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvNgayLap.setText(ngayLap);
        btnLuu.setEnabled(false);
    }

    private void seedContractsIfEmpty() {
        if (isSeedingContracts) return;

        db.collection("HopDong").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                isSeedingContracts = true;
              //  addSampleContractsToFirestore();
            } else {
                setupSpinnerFromContracts();
            }
        });
    }
/*
    private void addSampleContractsToFirestore() {
        WriteBatch batch = db.batch();

        Map<String, Object> contract1 = new HashMap<>();
        contract1.put("maPhong", "P101");
        contract1.put("tenNguoiThue", "Lê Thị Hoa");
        contract1.put("sdt", "0987654321");
        contract1.put("soNgayThue", 180);
        DocumentReference docRef1 = db.collection("HopDong").document();
        batch.set(docRef1, contract1);

        Map<String, Object> contract2 = new HashMap<>();
        contract2.put("maPhong", "P102");
        contract2.put("tenNguoiThue", "Trần Văn Nam");
        contract2.put("sdt", "0912345678");
        contract2.put("soNgayThue", 365);
        DocumentReference docRef2 = db.collection("HopDong").document();
        batch.set(docRef2, contract2);

        Map<String, Object> contract3 = new HashMap<>();
        contract3.put("maPhong", "P201");
        contract3.put("tenNguoiThue", "");
        contract3.put("sdt", "");
        contract3.put("soNgayThue", 0);
        DocumentReference docRef3 = db.collection("HopDong").document();
        batch.set(docRef3, contract3);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã tạo dữ liệu hợp đồng mẫu.", Toast.LENGTH_SHORT).show();
            isSeedingContracts = false;
            setupSpinnerFromContracts();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi tạo dữ liệu hợp đồng.", Toast.LENGTH_SHORT).show();
            isSeedingContracts = false;
        });
    }  */

    private void setupSpinnerFromContracts() {
        db.collection("HopDong").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy hợp đồng nào.", Toast.LENGTH_SHORT).show();
                return;
            }
            HashSet<String> phongSet = new HashSet<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String maPhongFromContract = doc.getString("maPhong");
                if (maPhongFromContract != null && !maPhongFromContract.isEmpty()) {
                    phongSet.add(maPhongFromContract);
                }
            }
            ArrayList<String> phongList = new ArrayList<>(phongSet);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, phongList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPhong.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi tải danh sách hợp đồng.", Toast.LENGTH_SHORT).show();
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
            maPhong = spinnerPhong.getSelectedItem().toString();
            capNhatTongTien();
            HoaDon hoaDon = new HoaDon(maPhong, tenNguoi, sdt, tienPhong, tienDien, tienNuoc, ngayLap, "Chưa thanh toán");
            LuuHoaDonLenFirestore(hoaDon);
        });

        spinnerPhong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPhong = parent.getItemAtPosition(position).toString();
                db.collection("HopDong").whereEqualTo("maPhong", selectedPhong).limit(1).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot contractDoc = queryDocumentSnapshots.getDocuments().get(0);
                                tenNguoi = contractDoc.getString("tenNguoiThue");
                                sdt = contractDoc.getString("sdt");
                                Long soNgayThueLong = contractDoc.getLong("soNgayThue");
                                soNgayThue = (soNgayThueLong != null) ? soNgayThueLong.intValue() : 0;

                                if (tenNguoi != null && !tenNguoi.trim().isEmpty()) {
                                    tvTenNguoiThue.setText("Người thuê: " + tenNguoi);
                                    // Đã loại bỏ dòng setText cho tvSoNgayThue
                                    btnLuu.setEnabled(true);
                                } else {
                                    tvTenNguoiThue.setText("Phòng này không có người thuê trong hợp đồng");
                                    // Đã loại bỏ dòng setText cho tvSoNgayThue
                                    Toast.makeText(Tao_HoaDon.this, "Phòng này không có người thuê, không thể tạo hóa đơn.", Toast.LENGTH_LONG).show();
                                    btnLuu.setEnabled(false);
                                }
                            } else {
                                tvTenNguoiThue.setText("Không có hợp đồng cho phòng này");
                                // Đã loại bỏ dòng setText cho tvSoNgayThue
                                Toast.makeText(Tao_HoaDon.this, "Phòng này không có hợp đồng.", Toast.LENGTH_LONG).show();
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
