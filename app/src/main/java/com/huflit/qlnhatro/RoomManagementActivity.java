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
    private EditText etRoomNumber, etRoomName, etMaxOccupancy;
    private Button btnSaveRoom, btnGoRooms, btnGoTenants;
    private RecyclerView rvRooms;

    private FirebaseFirestore db;
    private RoomAdapter adapter;
    private List<Room> roomList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // Views
        etRoomNumber   = findViewById(R.id.etRoomNumber);
        etRoomName     = findViewById(R.id.etRoomName);
        etMaxOccupancy = findViewById(R.id.etMaxOccupancy);
        btnSaveRoom    = findViewById(R.id.btnSaveRoom);
        btnGoRooms     = findViewById(R.id.btnGoRooms);
        btnGoTenants   = findViewById(R.id.btnGoTenants);
        rvRooms        = findViewById(R.id.rvRooms);

        // Navigation
        btnGoRooms.setEnabled(false);
        btnGoRooms.setAlpha(0.6f);
        btnGoTenants.setOnClickListener(v ->
                startActivity(new Intent(this, TenantManagementActivity.class))
        );

        // RecyclerView & Adapter
        adapter = new RoomAdapter(roomList, new RoomAdapter.OnItemActionListener() {
            @Override public void onEdit(Room room)  { showEditRoomDialog(room); }
            @Override public void onDelete(Room room){ deleteRoom(room); }
        });
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(adapter);

        // Load initial data
        loadRooms();

        // Save new room
        btnSaveRoom.setOnClickListener(v -> {
            String numStr = etRoomNumber.getText().toString().trim();
            String name   = etRoomName.getText().toString().trim();
            String maxStr = etMaxOccupancy.getText().toString().trim();
            if (numStr.isEmpty() || name.isEmpty() || maxStr.isEmpty()) {
                Toast.makeText(this, "Nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            int max;
            try {
                max = Integer.parseInt(maxStr);
                if (max <= 0) throw new NumberFormatException();
            } catch (Exception e) {
                Toast.makeText(this, "Số lượng tối đa không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String,Object> data = new HashMap<>();
            data.put("number", numStr);
            data.put("name", name);
            data.put("maxOccupancy", max);
            data.put("createdAt", System.currentTimeMillis());
            db.collection("rooms").add(data)
                    .addOnSuccessListener(doc -> {
                        etRoomNumber.setText("");
                        etRoomName.setText("");
                        etMaxOccupancy.setText("");
                        loadRooms();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi thêm phòng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loadRooms() {
        db.collection("rooms").orderBy("createdAt").get()
                .addOnSuccessListener(query -> {
                    roomList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String id     = doc.getId();
                        String number = doc.getString("number");
                        String name   = doc.getString("name");
                        int max       = doc.contains("maxOccupancy")
                                ? doc.getLong("maxOccupancy").intValue()
                                : 0;
                        roomList.add(new Room(id, number, name, max));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không load được phòng", Toast.LENGTH_SHORT).show()
                );
    }

    private void showEditRoomDialog(Room room) {
        View v = getLayoutInflater().inflate(R.layout.dialog_edit_room, null);
        EditText etNum = v.findViewById(R.id.etEditRoomNumber);
        EditText etName= v.findViewById(R.id.etEditRoomName);
        EditText etMax = v.findViewById(R.id.etEditRoomMax);
        etNum.setText(room.getNumber());
        etName.setText(room.getName());
        etMax.setText(String.valueOf(room.getMaxOccupancy()));

        new AlertDialog.Builder(this)
                .setTitle("Sửa phòng")
                .setView(v)
                .setPositiveButton("Lưu", (d, w) -> {
                    String newNum = etNum.getText().toString().trim();
                    String newName= etName.getText().toString().trim();
                    String newMaxStr = etMax.getText().toString().trim();
                    if (newNum.isEmpty() || newName.isEmpty() || newMaxStr.isEmpty()) return;
                    int newMax;
                    try { newMax = Integer.parseInt(newMaxStr); if (newMax<=0) throw new NumberFormatException(); }
                    catch(Exception ex){ Toast.makeText(this,"Max không hợp lệ",Toast.LENGTH_SHORT).show(); return; }
                    Map<String,Object> upd=new HashMap<>();
                    upd.put("number", newNum);
                    upd.put("name", newName);
                    upd.put("maxOccupancy", newMax);
                    db.collection("rooms").document(room.getId())
                            .update(upd)
                            .addOnSuccessListener(a-> loadRooms())
                            .addOnFailureListener(e-> Toast.makeText(this,"Lỗi sửa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteRoom(Room room) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa phòng")
                .setMessage("Xóa phòng " + room.getNumber() + "?" )
                .setPositiveButton("Xóa", (d,w) -> {
                    db.collection("rooms").document(room.getId())
                            .delete()
                            .addOnSuccessListener(a-> loadRooms())
                            .addOnFailureListener(e-> Toast.makeText(this,"Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
