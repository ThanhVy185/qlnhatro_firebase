package com.huflit.qlnhatro;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Adapter.ContractAdapter;
import com.huflit.qlnhatro.Contract;
import Data.Room;

public class ContractManagementActivity extends AppCompatActivity {
    // Mã request để chọn ảnh từ bộ nhớ
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    // Định dạng ngày ngày/tháng/năm
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private RecyclerView rv;                      // Hiển thị danh sách hợp đồng
    private ContractAdapter adapter;              // Adapter của RecyclerView
    private List<Contract> items = new ArrayList<>(); // Dữ liệu hợp đồng
    private List<Room> roomList = new ArrayList<>();  // Dữ liệu phòng
    private Map<String, String> roomNamesMap = new HashMap<>(); // Map id phòng -> tên hiển thị
    private FloatingActionButton fabAdd;           // Nút thêm hợp đồng
    private FirebaseFirestore db;                  // Firestore
    private ListenerRegistration contractsListener; // Listener realtime cho hợp đồng

    private ImageView ivPicGlobal;                 // ImageView trong dialog thêm
    private String selectedImageUriGlobal = "";    // Uri ảnh đã chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contract_management);

        // Điều chỉnh padding khi có hệ thống Bars (status, nav)
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.rvContracts),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        rv = findViewById(R.id.rvContracts);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        // Khởi tạo adapter kèm sự kiện click mở chi tiết
        adapter = new ContractAdapter(items, roomNamesMap, contract -> {
            Intent it = new Intent(this, ContractDetailActivity.class);
            it.putExtra("contractId", contract.getId());
            startActivity(it);
        });
        rv.setAdapter(adapter);

        // Nút thêm hợp đồng
        fabAdd = findViewById(R.id.fabAddContract);
        fabAdd.setEnabled(false);

        // Tải phòng và khởi tạo tiếp
        loadRoomsAndInit();
    }

    /**
     * Load danh sách phòng từ Firestore, sắp xếp theo số phòng,
     * đổ vào map và bật nút thêm, đồng thời bắt đầu lắng nghe hợp đồng.
     */
    private void loadRoomsAndInit() {
        db.collection("rooms").get()
                .addOnSuccessListener(q -> {
                    roomList.clear();
                    for (var doc : q) {
                        Room r = doc.toObject(Room.class);
                        r.setId(doc.getId());
                        roomList.add(r);
                    }
                    // Sắp xếp phòng theo số (numeric)
                    Collections.sort(roomList, (r1, r2) -> {
                        String n1 = r1.getNumber(), n2 = r2.getNumber();
                        if (n1 == null && n2 == null) return 0;
                        if (n1 == null) return 1;
                        if (n2 == null) return -1;
                        try {
                            return Integer.compare(
                                    Integer.parseInt(n1),
                                    Integer.parseInt(n2)
                            );
                        } catch (NumberFormatException e) {
                            return n1.compareTo(n2);
                        }
                    });
                    // Tạo map id->tên hiển thị
                    roomNamesMap.clear();
                    for (Room r : roomList) {
                        roomNamesMap.put(r.getId(), "Phòng " + r.getNumber());
                    }
                    // Bật và gán sự kiện cho fab
                    fabAdd.setEnabled(true);
                    fabAdd.setOnClickListener(v -> showAddContractDialog());
                    // Bắt đầu listen hợp đồng
                    listenForContracts();
                });
    }

    /**
     * Lắng nghe realtime collection "contracts", load vào items và sắp xếp
     */
    private void listenForContracts() {
        if (contractsListener != null) contractsListener.remove();
        contractsListener = db.collection("contracts")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    items.clear();
                    for (var doc : snap) {
                        Contract c = doc.toObject(Contract.class);
                        c.setId(doc.getId());
                        items.add(c);
                    }
                    // Sắp xếp hợp đồng theo số phòng
                    Collections.sort(items, (c1, c2) -> {
                        String r1 = c1.getRoomNumber(), r2 = c2.getRoomNumber();
                        if (r1 == null && r2 == null) return 0;
                        if (r1 == null) return 1;
                        if (r2 == null) return -1;
                        try {
                            return Integer.compare(
                                    Integer.parseInt(r1),
                                    Integer.parseInt(r2)
                            );
                        } catch (NumberFormatException ex) {
                            return r1.compareTo(r2);
                        }
                    });
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Hiển thị dialog thêm hợp đồng:
     * - Lọc phòng chưa có hợp đồng
     * - Sắp xếp phòng
     * - Chọn ảnh
     * - Chọn ngày kết thúc (DatePicker)
     * - Validate ngày kết thúc phải cách ngày tạo ít nhất 6 tháng
     */
    private void showAddContractDialog() {
        // Lọc các phòng trống
        List<Room> availableRooms = new ArrayList<>();
        for (Room r : roomList) {
            boolean has = false;
            for (Contract c : items) {
                if (r.getId().equals(c.getRoomId())) {
                    has = true;
                    break;
                }
            }
            if (!has) availableRooms.add(r);
        }
        if (availableRooms.isEmpty()) {
            Toast.makeText(this, "Không còn phòng trống để tạo hợp đồng", Toast.LENGTH_SHORT).show();
            return;
        }
        // Sắp xếp availableRooms
        Collections.sort(availableRooms, (r1, r2) -> {
            String n1 = r1.getNumber(), n2 = r2.getNumber();
            if (n1 == null && n2 == null) return 0;
            if (n1 == null) return 1;
            if (n2 == null) return -1;
            try {
                return Integer.compare(
                        Integer.parseInt(n1),
                        Integer.parseInt(n2)
                );
            } catch (NumberFormatException e) {
                return n1.compareTo(n2);
            }
        });

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_contract);

        EditText etCreateDate     = dialog.findViewById(R.id.etCreateDate);
        EditText etEndDate        = dialog.findViewById(R.id.etEndDate);
        EditText etContractCode   = dialog.findViewById(R.id.etContractCode);
        Spinner  spnRoom          = dialog.findViewById(R.id.spnRoom);
        EditText etTenantName     = dialog.findViewById(R.id.etTenantName);
        EditText etIdentityNumber = dialog.findViewById(R.id.etIdentityNumber);
        ivPicGlobal               = dialog.findViewById(R.id.ivContractPic);
        Button btnPickImage       = dialog.findViewById(R.id.btnPickImage);
        Button btnSave            = dialog.findViewById(R.id.btnDialogSave);
        Button btnCancel          = dialog.findViewById(R.id.btnDialogCancel);

        // Đặt ngày tạo là hôm nay
        etCreateDate.setText(DATE_FORMAT.format(new Date()));

        // Mở DatePicker cho ngày kết thúc
        etEndDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar c = Calendar.getInstance();
                        c.set(year, month, dayOfMonth);
                        etEndDate.setText(DATE_FORMAT.format(c.getTime()));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Cấu hình Spinner phòng
        List<String> roomDisplay = new ArrayList<>();
        for (Room r : availableRooms) {
            roomDisplay.add("Phòng " + r.getNumber());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roomDisplay);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRoom.setAdapter(spinnerAdapter);

        // Chọn ảnh
        btnPickImage.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("image/*");
            pick.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(pick, REQUEST_CODE_PICK_IMAGE);
        });

        // Hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Lưu hợp đồng
        btnSave.setOnClickListener(v -> {
            String code   = etContractCode.getText().toString().trim();
            String tenant = etTenantName.getText().toString().trim();
            String idNum  = etIdentityNumber.getText().toString().trim();
            String endStr = etEndDate.getText().toString().trim();
            if (code.isEmpty() || tenant.isEmpty() || idNum.isEmpty() || endStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            // Validate: ngày kết thúc phải cách ngày tạo ít nhất 6 tháng
            try {
                Date createDate = DATE_FORMAT.parse(etCreateDate.getText().toString());
                Date endDate    = DATE_FORMAT.parse(endStr);
                Calendar tmp = Calendar.getInstance();
                tmp.setTime(createDate);
                tmp.add(Calendar.MONTH, 6);
                if (endDate.before(tmp.getTime())) {
                    Toast.makeText(this,
                            "Ngày kết thúc phải cách ngày tạo ít nhất 6 tháng",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Ngày không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo và lưu Contract
            Room sel = availableRooms.get(spnRoom.getSelectedItemPosition());
            Contract c = new Contract();
            c.setContractCode(code);
            c.setTenantName(tenant);
            c.setIdentityNumber(idNum);
            c.setRoomId(sel.getId());
            c.setRoomNumber(sel.getNumber());
            c.setImageUrl(selectedImageUriGlobal);
            try {
                c.setCreateDate(DATE_FORMAT.parse(etCreateDate.getText().toString()).getTime());
                c.setEndDate   (DATE_FORMAT.parse(endStr).getTime());
            } catch (ParseException ignore) {}

            db.collection("contracts").add(c)
                    .addOnSuccessListener(d -> {
                        Toast.makeText(this,
                                "Thêm hợp đồng thành công",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                getContentResolver().takePersistableUriPermission(
                        imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                );
                selectedImageUriGlobal = imageUri.toString();
                ivPicGlobal.setImageURI(imageUri);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (contractsListener != null) contractsListener.remove();
    }
}
