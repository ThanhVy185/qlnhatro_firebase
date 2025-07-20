package com.huflit.qlnhatro;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.huflit.qlnhatro.Adapter.RoomAdapter;
import Data.Room;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomManagementActivity extends AppCompatActivity {

    private RecyclerView rvRooms;
    private RoomAdapter roomAdapter;
    private FirebaseFirestore db;
    private List<Room> roomList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        // 1. Setup RecyclerView + Adapter
        rvRooms = findViewById(R.id.rvRooms);
        rvRooms.setLayoutManager(new GridLayoutManager(this, 3));
        roomAdapter = new RoomAdapter(
                this,
                roomList,
                new RoomAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(Room room) {
                        showRoomDetailDialog(room);
                    }
                    @Override
                    public void onLongClick(Room room, int position) {
                        // TODO: thêm code sửa / xóa phòng nếu cần
                    }
                });
        rvRooms.setAdapter(roomAdapter);

        // 2. FAB thêm phòng
        findViewById(R.id.fabAddRoom).setOnClickListener(v -> showAddRoomDialog());

        // 3. Khởi tạo Firestore và load dữ liệu
        db = FirebaseFirestore.getInstance();
        loadRoomsWithContracts();
    }

    // ===== Load phòng + hợp đồng =====
    private void loadRoomsWithContracts() {
        // Bước 1: Lấy tất cả hợp đồng để gom roomId
        db.collection("contracts").get()
                .addOnSuccessListener(cs -> {
                    Set<String> hasContractIds = new HashSet<>();
                    for (QueryDocumentSnapshot c : cs) {
                        String roomId = c.getString("roomId");
                        if (roomId != null) hasContractIds.add(roomId);
                    }
                    // Bước 2: Load rooms và đánh dấu hasContract
                    db.collection("rooms").get()
                            .addOnSuccessListener(rs -> {
                                roomList.clear();
                                for (QueryDocumentSnapshot r : rs) {
                                    Room room = r.toObject(Room.class);
                                    room.setId(r.getId());
                                    room.setHasContract(hasContractIds.contains(r.getId()));
                                    roomList.add(room);
                                }
                                roomAdapter.setRoomList(roomList);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Lỗi load phòng: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Lỗi load hợp đồng: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    // ===== Dialog “Thêm phòng” =====
    private void showAddRoomDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_room, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        EditText etNum    = view.findViewById(R.id.etDialogRoomNumber);
        EditText etName   = view.findViewById(R.id.etDialogRoomName);
        EditText etMax    = view.findViewById(R.id.etDialogRoomMax);
        GridLayout gl     = view.findViewById(R.id.glInteriors);
        Button btnSave    = view.findViewById(R.id.btnDialogSave);
        Button btnCancel  = view.findViewById(R.id.btnDialogCancel);

        // Toggle chọn nội thất
        for (int i = 0; i < gl.getChildCount(); i++) {
            View c = gl.getChildAt(i);
            if (c instanceof ImageButton) {
                ImageButton ib = (ImageButton) c;
                ib.setAlpha(0.4f);
                ib.setOnClickListener(v -> {
                    boolean sel = !ib.isSelected();
                    ib.setSelected(sel);
                    ib.setAlpha(sel ? 1f : 0.4f);
                });
            }
        }

        btnSave.setOnClickListener(v -> {
            String num = etNum.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String maxStr = etMax.getText().toString().trim();
            if (TextUtils.isEmpty(num) ||
                    TextUtils.isEmpty(name) ||
                    TextUtils.isEmpty(maxStr)) {
                Toast.makeText(this,
                        "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            int maxOcc;
            try {
                maxOcc = Integer.parseInt(maxStr);
            } catch (NumberFormatException ex) {
                Toast.makeText(this,
                        "Số tối đa phải là số",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Gom interior dựa trên tag
            List<String> interiors = new ArrayList<>();
            for (int i = 0; i < gl.getChildCount(); i++) {
                View c = gl.getChildAt(i);
                if (c instanceof ImageButton && c.isSelected()) {
                    Object tag = c.getTag();
                    if (tag instanceof String) {
                        interiors.add((String) tag);
                    }
                }
            }

            // Tạo và lưu Room mới
            Room room = new Room();
            room.setNumber(num);
            room.setName(name);
            room.setMaxOccupancy(maxOcc);
            room.setInteriors(interiors);
            room.setHasContract(false);

            db.collection("rooms")
                    .add(room)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this,
                                "Thêm phòng thành công",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRoomsWithContracts();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Lỗi thêm phòng: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ===== Dialog “Xem chi tiết” =====
    private void showRoomDetailDialog(Room room) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_room_detail, null);

        ((TextView) view.findViewById(R.id.tvDetailNumber))
                .setText("Phòng: " + room.getNumber());
        ((TextView) view.findViewById(R.id.tvDetailName))
                .setText("Tên: " + room.getName());
        ((TextView) view.findViewById(R.id.tvDetailMax))
                .setText("Số tối đa: " + room.getMaxOccupancy() + " người");

        LinearLayout ll = view.findViewById(R.id.llInterior);
        ll.removeAllViews();
        if (room.getInteriors() != null) {
            for (String icon : room.getInteriors()) {
                if (icon == null) continue;
                int resId = getResources()
                        .getIdentifier(icon.toLowerCase(),
                                "drawable", getPackageName());
                if (resId == 0) continue;
                ImageView iv = new ImageView(this);
                iv.setImageResource(resId);
                LinearLayout.LayoutParams p =
                        new LinearLayout.LayoutParams(100, 100);
                p.setMargins(8, 0, 8, 0);
                iv.setLayoutParams(p);
                ll.addView(iv);
            }
        }

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Đóng", null)
                .show();
    }
}
