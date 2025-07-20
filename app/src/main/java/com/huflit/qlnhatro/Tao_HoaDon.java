package com.huflit.qlnhatro;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private ArrayList<Room> roomList; // Thêm để xử lý spinner tốt hơn

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
            roomList = new ArrayList<>();
            ArrayList<String> roomDisplayList = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Room room = doc.toObject(Room.class);
                if (room != null) {
                    room.setId(doc.getId());
                    roomList.add(room);
                    roomDisplayList.add("Phòng " + room.getNumber());
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, roomDisplayList);
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

        // <<< THAY ĐỔI: Gọi hàm kiểm tra trước khi lưu >>>
        btnLuu.setOnClickListener(v -> {
            if (!validateInput()) {
                return;
            }
            capNhatTongTien();
            HoaDon hoaDon = new HoaDon(maPhong, tenNguoi, sdt, tienPhong, tienDien, tienNuoc, ngayLap, "Chưa thanh toán", soDien, soNuoc);
            checkAndSaveInvoice(hoaDon);
        });

        spinnerPhong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Room selectedRoom = roomList.get(position);
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

    /**
     * <<< HÀM MỚI: Kiểm tra hóa đơn đã tồn tại trong tháng chưa trước khi lưu >>>
     * @param hoaDon Hóa đơn chuẩn bị lưu
     */
    private void checkAndSaveInvoice(final HoaDon hoaDon) {
        btnLuu.setEnabled(false); // Vô hiệu hóa nút Lưu trong khi kiểm tra

        // Lấy tháng và năm của hóa đơn đang tạo
        Calendar calNew = Calendar.getInstance();
        try {
            Date newDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(hoaDon.getNgayLap());
            if (newDate != null) {
                calNew.setTime(newDate);
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Lỗi định dạng ngày", Toast.LENGTH_SHORT).show();
            btnLuu.setEnabled(true);
            return;
        }
        final int currentMonth = calNew.get(Calendar.MONTH);
        final int currentYear = calNew.get(Calendar.YEAR);

        // Truy vấn các hóa đơn đã có của phòng này
        db.collection("HoaDon")
                .whereEqualTo("maPhong", hoaDon.getMaPhong())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean invoiceExistsForMonth = false;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HoaDon existingHoaDon = doc.toObject(HoaDon.class);
                        try {
                            Date existingDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(existingHoaDon.getNgayLap());
                            if (existingDate != null) {
                                Calendar calExisting = Calendar.getInstance();
                                calExisting.setTime(existingDate);
                                int existingMonth = calExisting.get(Calendar.MONTH);
                                int existingYear = calExisting.get(Calendar.YEAR);

                                // Nếu tìm thấy hóa đơn có cùng tháng và năm, đánh dấu là đã tồn tại
                                if (existingMonth == currentMonth && existingYear == currentYear) {
                                    invoiceExistsForMonth = true;
                                    break;
                                }
                            }
                        } catch (ParseException e) {
                            Log.e("Tao_HoaDon", "Lỗi parse ngày hóa đơn cũ: " + existingHoaDon.getNgayLap());
                        }
                    }

                    if (invoiceExistsForMonth) {
                        // Nếu đã tồn tại, hiển thị lỗi và bật lại nút Lưu
                        Toast.makeText(this, "Phòng này đã có hóa đơn trong tháng. Vui lòng xóa hóa đơn cũ để tạo mới.", Toast.LENGTH_LONG).show();
                        btnLuu.setEnabled(true);
                    } else {
                        // Nếu chưa tồn tại, tiến hành lưu hóa đơn
                        luuHoaDonLenFirestore(hoaDon);
                    }
                })
                .addOnFailureListener(e -> {
                    // Nếu có lỗi khi kiểm tra, hiển thị lỗi và bật lại nút Lưu
                    Toast.makeText(this, "Lỗi khi kiểm tra hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnLuu.setEnabled(true);
                });
    }

    /**
     * Lưu đối tượng hóa đơn lên Firestore.
     * @param hoaDon Hóa đơn cần lưu
     */
    private void luuHoaDonLenFirestore(HoaDon hoaDon) {
        db.collection("HoaDon").add(hoaDon).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Lưu hóa đơn thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Quay về màn hình trước đó
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi lưu hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnLuu.setEnabled(true); // Bật lại nút Lưu nếu có lỗi
        });
    }
}
