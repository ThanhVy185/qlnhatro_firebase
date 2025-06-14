package com.huflit.qlnhatro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomManagementActivity extends AppCompatActivity {
    private EditText etRoomNumber, etRoomName;
    private Button btnSaveRoom, btnGoRooms, btnGoTenants;
    private FirebaseFirestore db;
    private RecyclerView rvRooms;
    private RoomAdapter adapter;
    private List<Room> roomList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Tìm view
        etRoomNumber = findViewById(R.id.etRoomNumber);
        etRoomName   = findViewById(R.id.etRoomName);
        btnSaveRoom  = findViewById(R.id.btnSaveRoom);
        btnGoRooms   = findViewById(R.id.btnGoRooms);
        btnGoTenants = findViewById(R.id.btnGoTenants);
        rvRooms      = findViewById(R.id.rvRooms);

        // Buttons điều hướng
        btnGoRooms.setEnabled(false);
        btnGoRooms.setAlpha(0.6f);
        btnGoTenants.setOnClickListener(v ->
                startActivity(new Intent(this, TenantManagementActivity.class))
        );

        // Thiết lập RecyclerView
        adapter = new RoomAdapter(roomList, new RoomAdapter.OnItemActionListener() {
            @Override
            public void onEdit(Room room) {
                showEditRoomDialog(room);
            }

            @Override
            public void onDelete(Room room) {
                deleteRoom(room);
            }
        });
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);

        // Load dữ liệu ban đầu
        loadRooms();

        btnSaveRoom.setOnClickListener(v -> {
            String number = etRoomNumber.getText().toString().trim();
            String name   = etRoomName.getText().toString().trim();
            if (number.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, Object> room = new HashMap<>();
            room.put("number", number);
            room.put("name", name);
            room.put("createdAt", System.currentTimeMillis());

            db.collection("rooms")
                    .add(room)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Thêm phòng thành công!", Toast.LENGTH_SHORT).show();
                        etRoomNumber.setText("");
                        etRoomName.setText("");
                        loadRooms();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi khi thêm phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void loadRooms() {
        db.collection("rooms")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    roomList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        roomList.add(new Room(
                                doc.getId(),
                                doc.getString("number"),
                                doc.getString("name")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không load được danh sách phòng", Toast.LENGTH_SHORT).show()
                );
    }

    private void showEditRoomDialog(Room room) {
        View v = getLayoutInflater().inflate(R.layout.dialog_edit_room, null);
        EditText etNum = v.findViewById(R.id.etEditRoomNumber);
        EditText etName = v.findViewById(R.id.etEditRoomName);
        etNum.setText(room.getNumber());
        etName.setText(room.getName());

        new AlertDialog.Builder(this)
                .setTitle("Sửa thông tin phòng")
                .setView(v)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newNum = etNum.getText().toString().trim();
                    String newName = etName.getText().toString().trim();
                    if (newNum.isEmpty() || newName.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, Object> upd = new HashMap<>();
                    upd.put("number", newNum);
                    upd.put("name", newName);

                    db.collection("rooms").document(room.getId())
                            .update(upd)
                            .addOnSuccessListener(aVoid -> loadRooms())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi cập nhật phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteRoom(Room room) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa phòng")
                .setMessage("Bạn có chắc muốn xóa phòng " + room.getNumber() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("rooms").document(room.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> loadRooms())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi xóa phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
