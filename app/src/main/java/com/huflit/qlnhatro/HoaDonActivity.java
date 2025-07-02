package com.huflit.qlnhatro;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;

import Adapter.HoaDonAdapter;
import Data.HoaDon;

public class HoaDonActivity extends AppCompatActivity {

    ListView lvHoaDon;
    Button btnTaoMoi;
    Button btnDelete;
    HoaDonAdapter hoaDonAdapter;
    ArrayList<HoaDon> listHD;

    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;
    private static boolean isSeeding = false;

    // Biến để lưu vị trí của hóa đơn được chọn
    private int selectedPosition = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoa_don);

        db = FirebaseFirestore.getInstance();

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        btnTaoMoi = findViewById(R.id.btnTaoMoi);
        lvHoaDon = findViewById(R.id.listHoaDon);
        btnDelete = findViewById(R.id.btnDelete);

        listHD = new ArrayList<>();
        hoaDonAdapter = new HoaDonAdapter(this, listHD);
        lvHoaDon.setAdapter(hoaDonAdapter);

        setupEventListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        seedDataIfEmpty();
        listenForDataChanges();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    private void seedDataIfEmpty() {
        if (isSeeding) {
            return;
        }
        db.collection("HoaDon").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    isSeeding = true;
                    Log.d("FirestoreSeed", "Collection 'HoaDon' is empty. Seeding data...");
                    addSampleDataToFirestore();
                } else {
                    Log.d("FirestoreSeed", "Collection 'HoaDon' already has data. No need to seed.");
                }
            } else {
                Log.w("FirestoreSeed", "Error checking for existing data.", task.getException());
            }
        });
    }

    private void addSampleDataToFirestore() {
        WriteBatch batch = db.batch();
        ArrayList<HoaDon> sampleList = new ArrayList<>();
        sampleList.add(new HoaDon("P101", "Nguyễn Văn An", "0911111111", 2500000, 150000, 80000, "25/07/2025", "Chưa thanh toán"));
        sampleList.add(new HoaDon("P102", "Trần Thị Bình", "0922222222", 3000000, 200000, 100000, "24/07/2025", "Đã thanh toán"));
        sampleList.add(new HoaDon("P103", "Lê Văn Cường", "0933333333", 2700000, 175000, 90000, "23/07/2025", "Chưa thanh toán"));
        for (HoaDon hoaDon : sampleList) {
            DocumentReference docRef = db.collection("HoaDon").document();
            batch.set(docRef, hoaDon);
        }
        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d("FirestoreSeed", "Successfully added sample data.");
            Toast.makeText(HoaDonActivity.this, "Đã tự động thêm dữ liệu mẫu lên Firebase!", Toast.LENGTH_LONG).show();
            isSeeding = false;
        }).addOnFailureListener(e -> {
            Log.w("FirestoreSeed", "Error adding sample data.", e);
            Toast.makeText(HoaDonActivity.this, "Lỗi khi thêm dữ liệu mẫu.", Toast.LENGTH_SHORT).show();
            isSeeding = false;
        });
    }

    private void listenForDataChanges() {
        Query query = db.collection("HoaDon");
        firestoreListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }
            listHD.clear();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    HoaDon hoaDon = doc.toObject(HoaDon.class);
                    hoaDon.setDocumentId(doc.getId());
                    listHD.add(hoaDon);
                }
            }
            hoaDonAdapter.notifyDataSetChanged();
            Log.d("Firestore", "Data updated. Items count: " + listHD.size());
        });
    }

    private void setupEventListeners() {
        btnTaoMoi.setOnClickListener(v -> {
            Intent intent = new Intent(HoaDonActivity.this, Tao_HoaDon.class);
            startActivity(intent);
        });

        // Sự kiện nhấn vào một item để CHỌN
        lvHoaDon.setOnItemClickListener((adapterView, itemView, position, id) -> {
            selectedPosition = position;
            Toast.makeText(HoaDonActivity.this, "Đã chọn: " + listHD.get(position).getMaPhong(), Toast.LENGTH_SHORT).show();
            // Để làm nổi bật item được chọn, bạn cần tùy chỉnh lại Adapter
        });

        // Sự kiện nhấn giữ một item để XEM CHI TIẾT
        lvHoaDon.setOnItemLongClickListener((parent, view, position, id) -> {
            HoaDon hd = listHD.get(position);
            Intent intent = new Intent(HoaDonActivity.this, Hoadon_chitiet.class);
            intent.putExtra("documentId", hd.getDocumentId());
            intent.putExtra("maPhong", hd.getMaPhong());
            intent.putExtra("tenNguoi", hd.getTenNguoi());
            intent.putExtra("sdt", hd.getSdt());
            intent.putExtra("tienPhong", hd.getTienPhong());
            intent.putExtra("tienDien", hd.getTienDien());
            intent.putExtra("tienNuoc", hd.getTienNuoc());
            intent.putExtra("ngayLap", hd.getNgayLapHoaDon());
            intent.putExtra("trangThai", hd.getTrangThai());
            startActivity(intent);
            return true; // Đánh dấu sự kiện đã được xử lý
        });

        // Sự kiện cho nút XÓA
        btnDelete.setOnClickListener(v -> {
            if (selectedPosition == -1) {
                Toast.makeText(this, "Vui lòng chọn một hóa đơn để xóa", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị hộp thoại xác nhận
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa hóa đơn cho phòng " + listHD.get(selectedPosition).getMaPhong() + "?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Nếu người dùng đồng ý, gọi hàm xóa
                        deleteSelectedHoaDon();
                    })
                    .setNegativeButton("Hủy", null) // Không làm gì khi nhấn Hủy
                    .show();
        });
    }

    /**
     * Xóa hóa đơn đã chọn khỏi Firestore.
     */
    private void deleteSelectedHoaDon() {
        if (selectedPosition < 0 || selectedPosition >= listHD.size()) {
            return; // Vị trí không hợp lệ
        }
        String documentId = listHD.get(selectedPosition).getDocumentId();
        db.collection("HoaDon").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa hóa đơn thành công", Toast.LENGTH_SHORT).show();
                    selectedPosition = -1; // Bỏ chọn sau khi xóa
                    // Listener sẽ tự động cập nhật lại danh sách
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
