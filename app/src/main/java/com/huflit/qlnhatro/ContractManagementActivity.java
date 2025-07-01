package com.huflit.qlnhatro;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.huflit.qlnhatro.ContractAdapter;
import com.huflit.qlnhatro.Contract;
import com.huflit.qlnhatro.Room;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContractManagementActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private RecyclerView rv;
    private ContractAdapter adapter;
    private List<Contract> items = new ArrayList<>();
    private List<Room> roomList = new ArrayList<>();
    private Map<String,String> roomNamesMap = new HashMap<>();
    private FloatingActionButton fabAdd;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contract_management);

        // nếu dùng edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.rvContracts),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        db = FirebaseFirestore.getInstance();

        // 1. Setup RecyclerView
        rv = findViewById(R.id.rvContracts);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 2. Setup FAB
        fabAdd = findViewById(R.id.fabAddContract);
        fabAdd.setEnabled(false);

        // 3. Load rooms → init adapter & FAB & load contracts
        loadRoomsAndInit();
    }

    private void loadRoomsAndInit() {
        db.collection("rooms")
                .get()
                .addOnSuccessListener(q -> {
                    roomList.clear();
                    for (var doc : q) roomList.add(doc.toObject(Room.class));
                    // build map roomId → displayName
                    roomNamesMap.clear();
                    for (Room r : roomList) {
                        roomNamesMap.put(r.getId(), "Phòng " + r.getNumber());
                    }
                    // init adapter với map
                    adapter = new ContractAdapter(items, roomNamesMap, c -> {
                        Intent it = new Intent(this, ContractDetailActivity.class);
                        it.putExtra("contractId", c.getId());
                        startActivity(it);
                    });
                    rv.setAdapter(adapter);
                    // enable FAB và gán sự kiện
                    fabAdd.setEnabled(true);
                    fabAdd.setOnClickListener(v -> showAddContractDialog());
                    // load hợp đồng
                    loadContracts();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi load phòng: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void loadContracts() {
        db.collection("contracts")
                .orderBy("endDate")
                .get()
                .addOnSuccessListener(q -> {
                    items.clear();
                    for (var doc : q) items.add(doc.toObject(Contract.class));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi load hợp đồng: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void showAddContractDialog() {
        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_add_contract, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm hợp đồng")
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        // tìm view
        EditText etCode    = dialogView.findViewById(R.id.etContractCode);
        Spinner spnRoom    = dialogView.findViewById(R.id.spnRoom);
        EditText etTenant  = dialogView.findViewById(R.id.etTenantName);
        EditText etIdNum   = dialogView.findViewById(R.id.etIdentityNumber);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        ImageView ivPic    = dialogView.findViewById(R.id.ivContractPic);
        Button btnPickImg  = dialogView.findViewById(R.id.btnPickImage);

        // build list cho spinner
        List<String> roomNumbers = new ArrayList<>();
        roomNumbers.add("Chọn phòng");
        for (Room r : roomList) roomNumbers.add(r.getNumber());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roomNumbers);
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spnRoom.setAdapter(spinnerAdapter);

        // chọn ảnh nếu cần
        btnPickImg.setOnClickListener(v -> {
            Intent pick = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            );
            startActivityForResult(pick, REQUEST_CODE_PICK_IMAGE);
        });

        // override nút Lưu
        dialog.setOnShowListener(dlg -> {
            Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String code = etCode.getText().toString().trim();
                int posRoom = spnRoom.getSelectedItemPosition();
                String tenant = etTenant.getText().toString().trim();
                String idNum = etIdNum.getText().toString().trim();
                String end = etEndDate.getText().toString().trim();

                // validate
                if (code.isEmpty() || posRoom == 0 ||
                        tenant.isEmpty() || idNum.isEmpty() || end.isEmpty()) {
                    Toast.makeText(this,
                            "Nhập đầy đủ thông tin và chọn phòng",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // parse endDate
                long endTs;
                try {
                    Date d = DATE_FORMAT.parse(end);
                    endTs = (d != null ? d.getTime() : System.currentTimeMillis());
                } catch (ParseException ex) {
                    Toast.makeText(this,
                            "Định dạng Hạn hợp đồng không đúng",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // build data
                Map<String,Object> data = new HashMap<>();
                data.put("contractCode", code);
                String roomNum = roomNumbers.get(posRoom);
                String roomId = "";
                for (Room r : roomList) {
                    if (r.getNumber().equals(roomNum)) {
                        roomId = r.getId();
                        break;
                    }
                }
                data.put("roomId", roomId);
                data.put("tenantName", tenant);
                data.put("identityNumber", idNum);
                data.put("endDate", endTs);
                data.put("imageUrl", "");      // nếu có upload ảnh thì cập nhật
                data.put("invoiceId", null);
                data.put("isPaid", false);

                db.collection("contracts")
                        .add(data)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(this,
                                    "Thêm hợp đồng thành công",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadContracts();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        "Lỗi lưu hợp đồng: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
            });
        });

        dialog.show();
    }

    // TODO: override onActivityResult(...) nếu cần upload ảnh
}
