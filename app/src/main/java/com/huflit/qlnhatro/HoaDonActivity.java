package com.huflit.qlnhatro;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
        if (isSeeding) return;
        db.collection("HoaDon").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                isSeeding = true;
                addSampleDataToFirestore();
            }
        });
    }

    private void addSampleDataToFirestore() {
        WriteBatch batch = db.batch();
        ArrayList<HoaDon> sampleList = new ArrayList<>();
        sampleList.add(new HoaDon("P101", "Nguyễn Văn An", "0911111111", 2500000, 150000, 80000, "25/07/2025", "Chưa thanh toán", 42.8, 5.3));
        sampleList.add(new HoaDon("P102", "Trần Thị Bình", "0922222222", 3000000, 200000, 100000, "24/07/2025", "Đã thanh toán", 57.1, 6.6));
        sampleList.add(new HoaDon("P103", "Lê Văn Cường", "0933333333", 2700000, 175000, 90000, "23/07/2025", "Chưa thanh toán", 50.0, 6.0));
        for (HoaDon hoaDon : sampleList) {
            DocumentReference docRef = db.collection("HoaDon").document();
            batch.set(docRef, hoaDon);
        }
        batch.commit().addOnSuccessListener(aVoid -> {
            isSeeding = false;
        }).addOnFailureListener(e -> {
            isSeeding = false;
        });
    }

    private void listenForDataChanges() {
        firestoreListener = db.collection("HoaDon").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e);
                return;
            }
            listHD.clear();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    HoaDon hoaDon = doc.toObject(HoaDon.class);

                    // SỬA LỖI DUY NHẤT Ở ĐÂY: Dùng đúng setId()
                    hoaDon.setId(doc.getId());

                    listHD.add(hoaDon);
                }
            }
            hoaDonAdapter.notifyDataSetChanged();
        });
    }

    private void setupEventListeners() {
        btnTaoMoi.setOnClickListener(v -> {
            Intent intent = new Intent(HoaDonActivity.this, Tao_HoaDon.class);
            startActivity(intent);
        });

        lvHoaDon.setOnItemClickListener((adapterView, itemView, position, id) -> {
            selectedPosition = position;
            Toast.makeText(HoaDonActivity.this, "Đã chọn: " + listHD.get(position).getMaPhong(), Toast.LENGTH_SHORT).show();
        });

        lvHoaDon.setOnItemLongClickListener((parent, view, position, id) -> {
            HoaDon hd = listHD.get(position);
            Intent intent = new Intent(HoaDonActivity.this, Hoadon_chitiet.class);
            intent.putExtra("documentId", hd.getId());
            intent.putExtra("maPhong", hd.getMaPhong());
            intent.putExtra("tenNguoi", hd.getTenNguoiThue());
            intent.putExtra("sdt", hd.getSdt());
            intent.putExtra("tienPhong", hd.getTienPhong());
            intent.putExtra("tienDien", hd.getTienDien());
            intent.putExtra("tienNuoc", hd.getTienNuoc());
            intent.putExtra("ngayLap", hd.getNgayLap());
            intent.putExtra("trangThai", hd.getTrangThai());
            intent.putExtra("soDien", hd.getSoDien());
            intent.putExtra("soNuoc", hd.getSoNuoc());
            startActivity(intent);
            return true;
        });

        btnDelete.setOnClickListener(v -> {
            if (selectedPosition == -1) {
                Toast.makeText(this, "Vui lòng chọn một hóa đơn để xóa", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa hóa đơn cho phòng " + listHD.get(selectedPosition).getMaPhong() + "?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Xóa", (dialog, which) -> deleteSelectedHoaDon())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void deleteSelectedHoaDon() {
        if (selectedPosition < 0 || selectedPosition >= listHD.size()) return;
        String documentId = listHD.get(selectedPosition).getId();
        db.collection("HoaDon").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa hóa đơn thành công", Toast.LENGTH_SHORT).show();
                    selectedPosition = -1;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}