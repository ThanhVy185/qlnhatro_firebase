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
    private ArrayList<Room> roomList;

    // THÊM BIẾN ĐỂ LƯU ĐƠN GIÁ ĐỘNG
    private double donGiaDien = 0;
    private double donGiaNuoc = 0;

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
        fetchDonGia(); // THÊM BƯỚC TẢI ĐƠN GIÁ
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

    /**
     * THÊM HÀM MỚI: Tải đơn giá từ collection "CaiDat" trên Firebase.
     */
    private void fetchDonGia() {
        db.collection("CaiDat").document("donGia").get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Lấy giá trị Double, nếu không có thì mặc định là 0.0
                        Double giaDienValue = doc.getDouble("giaDien");
                        Double giaNuocValue = doc.getDouble("giaNuoc");

                        donGiaDien = (giaDienValue != null) ? giaDienValue : 0.0;
                        donGiaNuoc = (giaNuocValue != null) ? giaNuocValue : 0.0;

                        Toast.makeText(this, "Đã cập nhật đơn giá điện, nước mới nhất.", Toast.LENGTH_SHORT).show();
                        capNhatTongTien(); // Cập nhật lại tổng tiền sau khi có đơn giá
                    } else {
                        // SỬA LỖI: Thay LONG_SHOW thành LENGTH_LONG
                        Toast.makeText(this, "Lỗi: Không tìm thấy đơn giá. Vui lòng vào mục cài đặt.", Toast.LENGTH_LONG).show();
                        btnLuu.setEnabled(false); // Không cho lưu nếu không có đơn giá
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tải đơn giá.", Toast.LENGTH_SHORT).show();
                    btnLuu.setEnabled(false);
                });
    }


    private void setupInitialData() {
        ngayLap = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvNgayLap.setText(ngayLap);
        btnLuu.setEnabled(false);
    }

    private void setupSpinner() {
        // Lấy danh sách hợp đồng trước để biết phòng nào đang thuê
        db.collection("contracts").get().addOnSuccessListener(contractSnapshots -> {
            ArrayList<String> rentedRoomIds = new ArrayList<>();
            for (DocumentSnapshot contractDoc : contractSnapshots) {
                String roomId = contractDoc.getString("roomId");
                if (roomId != null) {
                    rentedRoomIds.add(roomId);
                }
            }

            // Lấy danh sách phòng
            db.collection("rooms").get().addOnSuccessListener(roomSnapshots -> {
                if (roomSnapshots.isEmpty()) {
                    Toast.makeText(this, "Không có dữ liệu phòng", Toast.LENGTH_SHORT).show();
                    return;
                }

                roomList = new ArrayList<>();
                ArrayList<String> roomDisplayList = new ArrayList<>();

                for (DocumentSnapshot doc : roomSnapshots) {
                    Room room = doc.toObject(Room.class);
                    if (room != null) {
                        room.setId(doc.getId());
                        // Chỉ thêm phòng nếu đang thuê
                        if (rentedRoomIds.contains(room.getId())) {
                            roomList.add(room);
                        }
                    }
                }

                // Sắp xếp theo số phòng tăng dần
                roomList.sort((a, b) -> {
                    try {
                        int na = Integer.parseInt(a.getNumber());
                        int nb = Integer.parseInt(b.getNumber());
                        return Integer.compare(na, nb);
                    } catch (NumberFormatException e) {
                        return a.getNumber().compareTo(b.getNumber());
                    }
                });

                // Tạo danh sách hiển thị với SỐ THỨ TỰ + GIÁ
                int index = 1;
                for (Room r : roomList) {
                    String giaPhong = (r.getPrice() > 0)
                            ? String.format("%,.0f VNĐ", r.getPrice())
                            : "Chưa có giá";
                    roomDisplayList.add(index + ". Phòng " + r.getNumber() + " - " + giaPhong);
                    index++;
                }

                // Gán vào Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        roomDisplayList
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPhong.setAdapter(adapter);

            });
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
            checkAndSaveInvoice(hoaDon);
        });

        spinnerPhong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Room selectedRoom = roomList.get(position);
                maPhong = selectedRoom.getNumber();

                // Gán giá phòng vào ô tiền phòng
                if (selectedRoom.getPrice() > 0) {
                    edtTienPhong.setText(String.valueOf((long) selectedRoom.getPrice()));
                } else {
                    edtTienPhong.setText("0");
                }

                db.collection("contracts").whereEqualTo("roomId", selectedRoom.getId()).limit(1).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot contractDoc = queryDocumentSnapshots.getDocuments().get(0);
                                tenNguoi = contractDoc.getString("tenantName");
                                tvTenNguoiThue.setText((tenNguoi != null && !tenNguoi.trim().isEmpty()) ? tenNguoi : "Phòng này chưa có người thuê");
                                btnLuu.setEnabled(tenNguoi != null && !tenNguoi.trim().isEmpty());
                            } else {
                                tvTenNguoiThue.setText("Không có hợp đồng cho phòng này");
                                btnLuu.setEnabled(false);
                            }
                        });

                capNhatTongTien();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                btnLuu.setEnabled(false);
            }
        });
    }

    private void capNhatTongTien() {
        try {
            soDien = parseDoubleOrZero(edtSoDien.getText().toString().trim());
            soNuoc = parseDoubleOrZero(edtSoNuoc.getText().toString().trim());
            tienPhong = parseDoubleOrZero(edtTienPhong.getText().toString().trim());

            tienDien = soDien * donGiaDien;
            tienNuoc = soNuoc * donGiaNuoc;

        } catch (NumberFormatException e) {
            tienDien = tienNuoc = tienPhong = 0;
        }

        tvThanhTienDien.setText(String.format(Locale.getDefault(), "Thành tiền: %,.0f VNĐ", tienDien));
        tvThanhTienNuoc.setText(String.format(Locale.getDefault(), "Thành tiền: %,.0f VNĐ", tienNuoc));
        tvTongTien.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", (tienDien + tienNuoc + tienPhong)));
    }

    private double parseDoubleOrZero(String value) {
        if (value.isEmpty()) return 0;
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private void checkAndSaveInvoice(final HoaDon hoaDon) {
        btnLuu.setEnabled(false);

        Calendar calNew = Calendar.getInstance();
        try {
            Date newDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(hoaDon.getNgayLap());
            if (newDate != null) calNew.setTime(newDate);
        } catch (ParseException e) {
            Toast.makeText(this, "Lỗi định dạng ngày", Toast.LENGTH_SHORT).show();
            btnLuu.setEnabled(true);
            return;
        }

        final int currentMonth = calNew.get(Calendar.MONTH);
        final int currentYear = calNew.get(Calendar.YEAR);

        db.collection("HoaDon").whereEqualTo("maPhong", hoaDon.getMaPhong()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean exists = false;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HoaDon existingHoaDon = doc.toObject(HoaDon.class);
                        try {
                            Date existingDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(existingHoaDon.getNgayLap());
                            if (existingDate != null) {
                                Calendar calExisting = Calendar.getInstance();
                                calExisting.setTime(existingDate);
                                if (calExisting.get(Calendar.MONTH) == currentMonth && calExisting.get(Calendar.YEAR) == currentYear) {
                                    exists = true;
                                    break;
                                }
                            }
                        } catch (ParseException ignored) {}
                    }

                    if (exists) {
                        Toast.makeText(this, "Phòng này đã có hóa đơn trong tháng này.", Toast.LENGTH_LONG).show();
                        btnLuu.setEnabled(true);
                    } else {
                        luuHoaDonLenFirestore(hoaDon);
                    }
                });
    }


    private void luuHoaDonLenFirestore(HoaDon hoaDon) {
        db.collection("HoaDon").add(hoaDon).addOnSuccessListener(documentReference -> {
            Toast.makeText(this, "Lưu hóa đơn thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi lưu hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnLuu.setEnabled(true);
        });
    }
}
