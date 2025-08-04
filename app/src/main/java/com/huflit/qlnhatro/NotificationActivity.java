package com.huflit.qlnhatro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Adapter.NotificationAdapter;
import Data.Notification;

public class NotificationActivity extends AppCompatActivity {
    private CollectionReference notifRef, roomsRef;
    private ListenerRegistration notifListener;
    private NotificationAdapter adapter;
    private List<Notification> notifications = new ArrayList<>();

    // Dữ liệu phòng: IDs, names và map ID→label
    private List<String> roomIds   = new ArrayList<>();
    private List<String> roomNames = new ArrayList<>();
    private Map<String,String> roomMap = new HashMap<>();

    // Chứa kết quả chọn trong dialog
    private List<String> selectedIds   = new ArrayList<>();
    private List<String> selectedNames = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // 1) Firestore references
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        notifRef = db.collection("notifications");
        roomsRef = db.collection("rooms");

        // 2) RecyclerView: chỉ set LayoutManager, adapter sẽ gán sau khi loadRooms
        RecyclerView rv = findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 3) Load phòng xong mới init adapter & realtime listener & nút Add
        loadRoomsThenInit(rv);

        // 4) Disable tạm thời nút Add
        ImageButton btnAdd = findViewById(R.id.btnAddNotif);
        btnAdd.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // (Thực ra listener đã khởi tạo trong loadRoomsThenInit,
        //  nhưng nếu bạn muốn tách ra thì có thể di chuyển code vào đây.)
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notifListener != null) notifListener.remove();
    }

    /**
     * 1) Load danh sách phòng lên roomMap
     * 2) Tạo adapter + gán cho RecyclerView
     * 3) Bắt đầu listen notifications
     * 4) Enable và gán click cho nút Add
     */
    private void loadRoomsThenInit(RecyclerView rv) {
        roomsRef.get()
                .addOnSuccessListener(qs -> {
                    roomMap.clear();
                    for (DocumentSnapshot d : qs.getDocuments()) {
                        String id = d.getId();
                        // Lấy số phòng dưới field "roomNumber" hoặc "number"
                        String num = d.getString("roomNumber");
                        if (num == null) num = d.getString("number");
                        if (num == null) num = "";
                        roomMap.put(id, "Phòng " + num);
                    }

                    // 2) Khởi tạo adapter với roomMap
                    adapter = new NotificationAdapter(
                            notifications,
                            roomMap,
                            new NotificationAdapter.OnAction() {
                                @Override public void onView(Notification n)   { showDetail(n); }
                                @Override public void onEdit(Notification n)   { showDialog(n); }
                                @Override public void onDelete(Notification n) { confirmDelete(n); }
                            }
                    );
                    rv.setAdapter(adapter);

                    // 3) Bắt đầu realtime listener
                    notifListener = notifRef
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .addSnapshotListener((qs2, e) -> {
                                if (e != null) return;
                                notifications.clear();
                                for (DocumentSnapshot d2 : qs2.getDocuments()) {
                                    Notification n = d2.toObject(Notification.class);
                                    n.id = d2.getId();
                                    notifications.add(n);
                                }
                                adapter.notifyDataSetChanged();
                            });

                    // 4) Enable nút Add và gán click
                    ImageButton btnAdd = findViewById(R.id.btnAddNotif);
                    btnAdd.setEnabled(true);
                    btnAdd.setOnClickListener(v -> showDialog(null));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Không tải được danh sách phòng", Toast.LENGTH_SHORT).show()
                );
    }

    /** Show dialog chi tiết (read-only) */
    private void showDetail(Notification n) {
        List<String> labels = new ArrayList<>();
        if (n.rooms != null) {
            for (String id : n.rooms) {
                labels.add(roomMap.getOrDefault(id, id));
            }
        }
        String joined = labels.isEmpty() ?
                "(Chưa chọn)" : TextUtils.join(", ", labels);

        new AlertDialog.Builder(this)
                .setTitle(n.title)
                .setMessage("Gửi đến: " + joined + "\n\n" + n.content)
                .setPositiveButton("Đóng", null)
                .show();
    }

    /** Xác nhận trước khi xóa */
    private void confirmDelete(Notification n) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (d,w) -> deleteNotification(n.id))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNotification(String id) {
        notifRef.document(id).delete()
                .addOnSuccessListener(a ->
                        Toast.makeText(this,
                                "Đã xóa thông báo", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Show dialog Thêm/Sửa.
     * Dùng final tmpIds/tmpNames để tránh lỗi lambda,
     * rồi sau OK đổ về selectedIds/selectedNames.
     */
    private void showDialog(@Nullable Notification existing) {
        View view = getLayoutInflater().inflate(R.layout.dialog_notification, null);
        Button btnSelect = view.findViewById(R.id.btnSelectRooms);
        TextView tvSel   = view.findViewById(R.id.tvSelectedRooms);
        EditText etT     = view.findViewById(R.id.etNotifTitle);
        EditText etC     = view.findViewById(R.id.etNotifContent);

        // sao chép state hiện tại vào tmp (final)
        final List<String> tmpIds   = new ArrayList<>(selectedIds);
        final List<String> tmpNames = new ArrayList<>(selectedNames);

        // Prefill nếu edit
        if (existing != null) {
            etT.setText(existing.title);
            etC.setText(existing.content);
            tvSel.setText(
                    tmpNames.isEmpty()
                            ? "(Chưa chọn)"
                            : TextUtils.join(", ", tmpNames)
            );
        }

        // Multi-choice dialog
        btnSelect.setOnClickListener(v -> {
            boolean[] checked = new boolean[roomMap.size()];
            String[] idsArr   = roomMap.keySet().toArray(new String[0]);
            String[] lblArr   = roomMap.values().toArray(new String[0]);

            for (int i = 0; i < idsArr.length; i++) {
                checked[i] = tmpIds.contains(idsArr[i]);
            }

            new AlertDialog.Builder(this)
                    .setTitle("Chọn phòng nhận")
                    .setMultiChoiceItems(
                            lblArr, checked,
                            (dlg, idx, isChecked) -> {
                                String rid = idsArr[idx];
                                String rnm = lblArr[idx];
                                if (isChecked) {
                                    if (!tmpIds.contains(rid)) {
                                        tmpIds.add(rid);
                                        tmpNames.add(rnm);
                                    }
                                } else {
                                    tmpIds.remove(rid);
                                    tmpNames.remove(rnm);
                                }
                            })
                    .setPositiveButton("OK", (d,w) -> {
                        // đổ tmp về biến lớp
                        selectedIds.clear();   selectedIds.addAll(tmpIds);
                        selectedNames.clear(); selectedNames.addAll(tmpNames);
                        tvSel.setText(
                                tmpNames.isEmpty()
                                        ? "(Chưa chọn)"
                                        : TextUtils.join(", ", tmpNames)
                        );
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        // Dialog Thêm/Sửa chính
        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Thêm thông báo" : "Sửa thông báo")
                .setView(view)
                .setPositiveButton("Lưu", (d,w) -> {
                    String title   = etT.getText().toString().trim();
                    String content = etC.getText().toString().trim();
                    long ts        = System.currentTimeMillis();

                    Map<String,Object> data = new HashMap<>();
                    data.put("rooms",     selectedIds);
                    data.put("title",     title);
                    data.put("content",   content);
                    data.put("timestamp", ts);

                    if (existing == null) {
                        notifRef.add(data);
                    } else {
                        notifRef.document(existing.id)
                                .set(data, SetOptions.merge());
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
