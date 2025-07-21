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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
                        // Click 1 lần: xem chi tiết
                        showRoomDetailDialog(room);
                    }
                    @Override
                    public void onLongClick(Room room, int position) {
                        // Long-click: hiện options dialog
                        showOptionsDialog(room);
                    }
                });
        rvRooms.setAdapter(roomAdapter);

        // 2. FAB thêm phòng
        findViewById(R.id.fabAddRoom)
                .setOnClickListener(v -> showAddRoomDialog());

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

                                // → Sắp xếp theo số phòng (nếu số không parse được thì theo chuỗi)
                                Collections.sort(roomList, new Comparator<Room>() {
                                    @Override
                                    public int compare(Room a, Room b) {
                                        try {
                                            int na = Integer.parseInt(a.getNumber());
                                            int nb = Integer.parseInt(b.getNumber());
                                            return Integer.compare(na, nb);
                                        } catch (NumberFormatException e) {
                                            return a.getNumber().compareTo(b.getNumber());
                                        }
                                    }
                                });
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

            // ⚠️ 2. Check trùng số phòng
            for (Room ex : roomList) {
                if (num.equals(ex.getNumber())) {
                    Toast.makeText(this,
                            "Số phòng này đã tồn tại!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
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

    private void showOptionsDialog(Room room) {
        new AlertDialog.Builder(this)
                .setTitle("Chọn hành động cho phòng " + room.getNumber())
                .setItems(new CharSequence[]{"Chi tiết", "Sửa phòng", "Xóa phòng"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    showRoomDetailDialog(room);
                                    break;
                                case 1:
                                    showEditRoomDialog(room);
                                    break;
                                case 2:
                                    deleteRoom(room);
                                    break;
                            }
                        })
                .show();
    }


    private void showEditRoomDialog(Room room) {
        // Inflate layout dialog_add_room.xml
        View v = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_room, null);

        // Tìm các view trong dialog
        EditText etNum    = v.findViewById(R.id.etDialogRoomNumber);
        EditText etName   = v.findViewById(R.id.etDialogRoomName);
        EditText etMax    = v.findViewById(R.id.etDialogRoomMax);
        GridLayout gl     = v.findViewById(R.id.glInteriors);
        Button btnSave    = v.findViewById(R.id.btnDialogSave);
        Button btnCancel  = v.findViewById(R.id.btnDialogCancel);

        // Prefill giá trị phòng hiện tại
        etNum.setText(room.getNumber());
        etName.setText(room.getName());
        etMax.setText(String.valueOf(room.getMaxOccupancy()));

        // Thiết lập toggle cho các ImageButton nội thất theo room.getInteriors()
        Set<String> selectedTags = new HashSet<>();
        if (room.getInteriors() != null) {
            selectedTags.addAll(room.getInteriors());
        }
        for (int i = 0; i < gl.getChildCount(); i++) {
            View c = gl.getChildAt(i);
            if (c instanceof ImageButton) {
                ImageButton ib = (ImageButton) c;
                Object tag = ib.getTag();
                boolean isSel = tag instanceof String && selectedTags.contains(tag);
                ib.setSelected(isSel);
                ib.setAlpha(isSel ? 1f : 0.4f);
                ib.setOnClickListener(x -> {
                    boolean now = !ib.isSelected();
                    ib.setSelected(now);
                    ib.setAlpha(now ? 1f : 0.4f);
                });
            }
        }

        // Tạo AlertDialog chỉ với layout, không thêm positive/negative builder buttons
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa phòng " + room.getNumber())
                .setView(v)
                .setCancelable(false)
                .create();

        // Nút Hủy: chỉ dismiss dialog
        btnCancel.setOnClickListener(x -> dialog.dismiss());

        // Nút Lưu: validate + update Firestore
        btnSave.setOnClickListener(x -> {
            String num   = etNum.getText().toString().trim();
            String name  = etName.getText().toString().trim();
            String maxS  = etMax.getText().toString().trim();
            if (TextUtils.isEmpty(num) ||
                    TextUtils.isEmpty(name) ||
                    TextUtils.isEmpty(maxS)) {
                Toast.makeText(this, "Nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            int maxOcc;
            try {
                maxOcc = Integer.parseInt(maxS);
                if (maxOcc <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tối đa không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gom các nội thất đang được chọn (tag là String)
            List<String> interiors = new ArrayList<>();
            for (int i = 0; i < gl.getChildCount(); i++) {
                View c = gl.getChildAt(i);
                if (c instanceof ImageButton && c.isSelected()) {
                    Object tag = c.getTag();
                    if (tag instanceof String) interiors.add((String) tag);
                }
            }

            // Chuẩn bị map để update
            Map<String, Object> upd = new HashMap<>();
            upd.put("number", num);
            upd.put("name", name);
            upd.put("maxOccupancy", maxOcc);
            upd.put("interiors", interiors);
            // Giữ nguyên trạng thái hasContract
            upd.put("hasContract", room.isHasContract());

            // Ghi lên Firestore
            db.collection("rooms")
                    .document(room.getId())
                    .update(upd)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRoomsWithContracts();  // reload danh sách để cập nhật UI
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        dialog.show();
    }


    private void deleteRoom(Room room) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa phòng")
                .setMessage("Bạn có chắc muốn xóa phòng " + room.getNumber() + "?")
                .setPositiveButton("Xóa", (d,i) -> {
                    db.collection("rooms")
                            .document(room.getId())
                            .delete()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this,"Đã xóa",Toast.LENGTH_SHORT).show();
                                loadRoomsWithContracts();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,"Lỗi xóa: "+e.getMessage(),Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

}

