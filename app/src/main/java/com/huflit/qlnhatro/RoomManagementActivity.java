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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import Adapter.RoomAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import Data.Room;

public class RoomManagementActivity extends AppCompatActivity {

    private RecyclerView rvRooms;
    private RoomAdapter roomAdapter;
    private FirebaseFirestore db;
    private List<Room> roomList = new ArrayList<>();
    private RadioGroup rgRoomFilter;
    private RadioButton rbAll, rbEmpty, rbRented;

    // 🔹 Biến lưu filter hiện tại
    private String currentFilter = "Tất cả";

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
                        showOptionsDialog(room);
                    }
                });
        rvRooms.setAdapter(roomAdapter);

        // 2. FAB thêm phòng
        findViewById(R.id.fabAddRoom).setOnClickListener(v -> showAddRoomDialog());

        // 3. Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // 4. Setup RadioGroup lọc phòng
        rgRoomFilter = findViewById(R.id.rgRoomFilter);
        rbAll = findViewById(R.id.rbAll);
        rbEmpty = findViewById(R.id.rbEmpty);
        rbRented = findViewById(R.id.rbRented);

        rgRoomFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbAll) {
                currentFilter = "Tất cả";
            } else if (checkedId == R.id.rbEmpty) {
                currentFilter = "Chưa thuê";
            } else if (checkedId == R.id.rbRented) {
                currentFilter = "Đã thuê";
            }
            loadRoomsByStatus(currentFilter);
        });

        // 5. Lần đầu vào load tất cả
//        rbAll.setChecked(true);
        currentFilter = "Tất cả";
        rbAll.setChecked(true);
        loadRoomsByStatus(currentFilter);
    }

    // ===== Load phòng theo filter =====
    private void loadRoomsByStatus(String statusFilter) {
        db.collection("contracts")
                .get()
                .addOnSuccessListener(contractSnapshots -> {
                    Set<String> rentedRoomIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : contractSnapshots) {
                        String roomId = doc.getString("roomId");
                        if (roomId != null) {
                            rentedRoomIds.add(roomId);
                        }
                    }

                    db.collection("rooms")
                            .get()
                            .addOnSuccessListener(roomSnapshots -> {
                                List<Room> filtered = new ArrayList<>();

                                for (QueryDocumentSnapshot doc : roomSnapshots) {
                                    Room room = doc.toObject(Room.class);
                                    room.setId(doc.getId());

                                    // Đánh dấu trạng thái thuê
                                    boolean hasContract = rentedRoomIds.contains(room.getId());
                                    room.setHasContract(hasContract);

                                    if ("Tất cả".equals(statusFilter)) {
                                        filtered.add(room);
                                    } else if ("Đã thuê".equals(statusFilter) && hasContract) {
                                        filtered.add(room);
                                    } else if ("Chưa thuê".equals(statusFilter) && !hasContract) {
                                        filtered.add(room);
                                    }
                                }

                                // Sắp xếp theo số phòng
                                Collections.sort(filtered, (a, b) -> {
                                    try {
                                        int na = Integer.parseInt(a.getNumber());
                                        int nb = Integer.parseInt(b.getNumber());
                                        return Integer.compare(na, nb);
                                    } catch (NumberFormatException e) {
                                        return a.getNumber().compareTo(b.getNumber());
                                    }
                                });

                                roomAdapter.setRoomList(filtered);
                            });
                });
    }

    // ===== Dialog “Thêm phòng” =====
    private void showAddRoomDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_room, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        EditText etNum = view.findViewById(R.id.etDialogRoomNumber);
        EditText etName = view.findViewById(R.id.etDialogRoomName);
        EditText etMax = view.findViewById(R.id.etDialogRoomMax);
        EditText etPrice = view.findViewById(R.id.etDialogRoomPrice); // ✅ ô giá phòng
        GridLayout gl = view.findViewById(R.id.glInteriors);
        Button btnSave = view.findViewById(R.id.btnDialogSave);
        Button btnCancel = view.findViewById(R.id.btnDialogCancel);

        final int giaNoiThat = 250_000; // ✅ 250k cho mỗi nội thất

        // Hàm tính giá phòng
        Runnable updatePrice = () -> {
            int count = 0;
            for (int i = 0; i < gl.getChildCount(); i++) {
                View c = gl.getChildAt(i);
                if (c instanceof ImageButton && c.isSelected()) {
                    count++;
                }
            }
            int totalPrice = count * giaNoiThat;
            etPrice.setText(String.valueOf(totalPrice)); // ✅ cập nhật giá phòng
        };

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
                    updatePrice.run(); // ✅ mỗi lần chọn/bỏ chọn -> tính lại giá
                });
            }
        }

        btnSave.setOnClickListener(v -> {
            String num = etNum.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String maxStr = etMax.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (TextUtils.isEmpty(num) || TextUtils.isEmpty(name) || TextUtils.isEmpty(maxStr)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra trùng số phòng
            for (Room ex : roomList) {
                if (num.equals(ex.getNumber())) {
                    Toast.makeText(this, "Số phòng đã tồn tại!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            int maxOcc;
            try {
                maxOcc = Integer.parseInt(maxStr);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Số tối đa phải là số", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = 0;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException ignored) {}

            List<String> interiors = new ArrayList<>();
            for (int i = 0; i < gl.getChildCount(); i++) {
                View c = gl.getChildAt(i);
                if (c instanceof ImageButton && c.isSelected()) {
                    Object tag = c.getTag();
                    if (tag instanceof String) interiors.add((String) tag);
                }
            }

            Room room = new Room();
            room.setNumber(num);
            room.setName(name);
            room.setMaxOccupancy(maxOcc);
            room.setInteriors(interiors);
            room.setPrice(price); // ✅ lưu giá phòng
            room.setHasContract(false);

            db.collection("rooms").add(room)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Thêm phòng thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRoomsByStatus(currentFilter);
                    });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }



    // ===== Dialog chi tiết =====
    private void showRoomDetailDialog(Room room) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_detail, null);

        ((TextView) view.findViewById(R.id.tvDetailNumber))
                .setText("Phòng: " + room.getNumber());
        ((TextView) view.findViewById(R.id.tvDetailName))
                .setText("Tên: " + room.getName());
        ((TextView) view.findViewById(R.id.tvDetailMax))
                .setText("Số tối đa: " + room.getMaxOccupancy() + " người");

        // ✅ Thêm giá phòng
        ((TextView) view.findViewById(R.id.tvDetailPrice))
                .setText(String.format(Locale.getDefault(), "Giá: %,.0f VNĐ", room.getPrice()));

        LinearLayout ll = view.findViewById(R.id.llInterior);
        ll.removeAllViews();
        if (room.getInteriors() != null) {
            for (String icon : room.getInteriors()) {
                if (icon != null) {
                    int resId = getResources().getIdentifier(icon.toLowerCase(), "drawable", getPackageName());
                    if (resId != 0) {
                        ImageView iv = new ImageView(this);
                        iv.setImageResource(resId);
                        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(100, 100);
                        p.setMargins(8, 0, 8, 0);
                        iv.setLayoutParams(p);
                        ll.addView(iv);
                    }
                }
            }
        }

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Đóng", null)
                .show();
    }


    // ===== Menu hành động =====
    private void showOptionsDialog(Room room) {
        new AlertDialog.Builder(this)
                .setTitle("Chọn hành động cho phòng " + room.getNumber())
                .setItems(new CharSequence[]{"Chi tiết", "Sửa phòng", "Xóa phòng"},
                        (dialog, which) -> {
                            if (which == 0) showRoomDetailDialog(room);
                            else if (which == 1) showEditRoomDialog(room);
                            else if (which == 2) deleteRoom(room);
                        }).show();
    }

    // ===== Sửa phòng =====
    private void showEditRoomDialog(Room room) {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_room, null);

        EditText etNum = v.findViewById(R.id.etDialogRoomNumber);
        EditText etName = v.findViewById(R.id.etDialogRoomName);
        EditText etMax = v.findViewById(R.id.etDialogRoomMax);
        EditText etPrice = v.findViewById(R.id.etDialogRoomPrice); // ✅ giá phòng
        GridLayout gl = v.findViewById(R.id.glInteriors);
        Button btnSave = v.findViewById(R.id.btnDialogSave);
        Button btnCancel = v.findViewById(R.id.btnDialogCancel);

        final int giaNoiThat = 250_000;

        // Điền giá trị cũ
        etNum.setText(room.getNumber());
        etName.setText(room.getName());
        etMax.setText(String.valueOf(room.getMaxOccupancy()));
        etPrice.setText(String.valueOf(room.getPrice())); // ✅ giá phòng cũ

        // Không cho sửa số phòng & tên phòng
        etNum.setEnabled(false);
        etNum.setFocusable(false);
        etName.setEnabled(false);
        etName.setFocusable(false);

        // Hàm tính giá phòng
        Runnable updatePrice = () -> {
            int count = 0;
            for (int i = 0; i < gl.getChildCount(); i++) {
                View c = gl.getChildAt(i);
                if (c instanceof ImageButton && c.isSelected()) {
                    count++;
                }
            }
            int totalPrice = count * giaNoiThat;
            etPrice.setText(String.valueOf(totalPrice));
        };

        // Thiết lập toggle nội thất
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
                    updatePrice.run(); // ✅ tự tính lại khi bật/tắt
                });
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa phòng " + room.getNumber())
                .setView(v)
                .setCancelable(false)
                .create();

        btnCancel.setOnClickListener(x -> dialog.dismiss());

        btnSave.setOnClickListener(x -> {
            String maxS = etMax.getText().toString().trim();
            String priceS = etPrice.getText().toString().trim();
            if (TextUtils.isEmpty(maxS)) {
                Toast.makeText(this, "Nhập số tối đa", Toast.LENGTH_SHORT).show();
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

            double price = 0;
            try {
                price = Double.parseDouble(priceS);
            } catch (NumberFormatException ignored) {}

            List<String> interiors = new ArrayList<>();
            for (int i = 0; i < gl.getChildCount(); i++) {
                View c = gl.getChildAt(i);
                if (c instanceof ImageButton && c.isSelected()) {
                    Object tag = c.getTag();
                    if (tag instanceof String) interiors.add((String) tag);
                }
            }

            Map<String, Object> upd = new HashMap<>();
            upd.put("maxOccupancy", maxOcc);
            upd.put("price", price); // ✅ cập nhật giá phòng
            upd.put("interiors", interiors);
            upd.put("hasContract", room.isHasContract());

            db.collection("rooms")
                    .document(room.getId())
                    .update(upd)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRoomsByStatus(currentFilter);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        dialog.show();
    }


    // ===== Xóa phòng =====
    private void deleteRoom(Room room) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa phòng")
                .setMessage("Bạn có chắc muốn xóa phòng " + room.getNumber() + "?")
                .setPositiveButton("Xóa", (d, i) -> {
                    db.collection("contracts").whereEqualTo("roomId", room.getId()).get()
                            .addOnSuccessListener(query -> {
                                if (!query.isEmpty()) {
                                    Toast.makeText(this, "Không thể xóa: Phòng đang có hợp đồng thuê!", Toast.LENGTH_LONG).show();
                                } else {
                                    db.collection("rooms").document(room.getId()).delete()
                                            .addOnSuccessListener(a -> {
                                                Toast.makeText(this, "Đã xóa phòng thành công", Toast.LENGTH_SHORT).show();
                                                loadRoomsByStatus(currentFilter); // Giữ nguyên filter hiện tại
                                            });
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
