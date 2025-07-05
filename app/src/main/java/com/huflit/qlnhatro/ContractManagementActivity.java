package com.huflit.qlnhatro;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ContractManagementActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private RecyclerView rv;
    private ContractAdapter adapter;
    private List<Contract> items = new ArrayList<>();
    private List<Room> roomList = new ArrayList<>();
    private Map<String, String> roomNamesMap = new HashMap<>();
    private FloatingActionButton fabAdd;
    private FirebaseFirestore db;
    private ListenerRegistration contractsListener;

    private ImageView ivPicGlobal;
    private String selectedImageUriGlobal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contract_management);

        // Handle insets for RecyclerView
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.rvContracts),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                }
        );

        db = FirebaseFirestore.getInstance();
        rv = findViewById(R.id.rvContracts);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter early to avoid null
        adapter = new ContractAdapter(items, roomNamesMap, c -> {
            Intent it = new Intent(this, ContractDetailActivity.class);
            it.putExtra("contractId", c.getId());
            startActivity(it);
        });
        rv.setAdapter(adapter);

        fabAdd = findViewById(R.id.fabAddContract);
        fabAdd.setEnabled(false);

        loadRoomsAndInit();
    }

    private void loadRoomsAndInit() {
        // Load rooms first
        db.collection("rooms").get()
                .addOnSuccessListener(q -> {
                    roomList.clear();
                    for (var doc : q) {
                        Room r = doc.toObject(Room.class);
                        r.setId(doc.getId());
                        roomList.add(r);
                    }
                    roomNamesMap.clear();
                    for (Room r : roomList) {
                        roomNamesMap.put(r.getId(), "Phòng " + r.getNumber());
                    }

                    fabAdd.setEnabled(true);
                    fabAdd.setOnClickListener(v -> showAddContractDialog());

                    // After rooms are loaded, attach listener for contracts
                    attachContractsListener();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Lỗi tải danh sách phòng: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void attachContractsListener() {
        contractsListener = db.collection("contracts")
                .orderBy("endDate", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snap,
                                        @Nullable FirebaseFirestoreException err) {
                        if (err != null) {
                            Toast.makeText(ContractManagementActivity.this,
                                    "Lỗi tải hợp đồng: " + err.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        items.clear();
                        for (var doc : snap.getDocuments()) {
                            Contract c = doc.toObject(Contract.class);
                            c.setId(doc.getId());
                            items.add(c);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (contractsListener != null) {
            contractsListener.remove();
            contractsListener = null;
        }
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

        EditText etCode   = dialogView.findViewById(R.id.etContractCode);
        Spinner spnRoom   = dialogView.findViewById(R.id.spnRoom);
        EditText etTenant = dialogView.findViewById(R.id.etTenantName);
        EditText etIdNum  = dialogView.findViewById(R.id.etIdentityNumber);
        EditText etEnd    = dialogView.findViewById(R.id.etEndDate);
        ivPicGlobal       = dialogView.findViewById(R.id.ivContractPic);
        Button btnPick    = dialogView.findViewById(R.id.btnPickImage);

        // Determine occupied rooms
        Set<String> occupied = new HashSet<>();
        for (Contract c : items) {
            occupied.add(c.getRoomId());
        }

        // Populate spinner with only available rooms
        List<Room> spinnerRooms = new ArrayList<>();
        spinnerRooms.add(new Room("", "Chọn phòng", "", 0));
        for (Room r : roomList) {
            if (!occupied.contains(r.getId())) {
                spinnerRooms.add(r);
            }
        }

        ArrayAdapter<Room> spAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerRooms);
        spAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spnRoom.setAdapter(spAdapter);

        btnPick.setOnClickListener(v -> startActivityForResult(
                new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                REQUEST_CODE_PICK_IMAGE));

        dialog.setOnShowListener(dlg -> {
            Button btnSave = dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String code = etCode.getText().toString().trim();
                int posRoom = spnRoom.getSelectedItemPosition();
                String tenant = etTenant.getText().toString().trim();
                String idNum  = etIdNum.getText().toString().trim();
                String end    = etEnd.getText().toString().trim();
                if (code.isEmpty() || posRoom == 0 ||
                        tenant.isEmpty() || idNum.isEmpty() || end.isEmpty()) {
                    Toast.makeText(this,
                            "Nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                long endTs;
                try {
                    endTs = DATE_FORMAT.parse(end).getTime();
                } catch (ParseException ex) {
                    Toast.makeText(this,
                            "Sai định dạng dd/MM/yyyy", Toast.LENGTH_SHORT).show();
                    return;
                }
                Room sel = (Room) spnRoom.getSelectedItem();
                Map<String,Object> data = new HashMap<>();
                data.put("contractCode", code);
                data.put("roomId", sel.getId());
                data.put("tenantName", tenant);
                data.put("identityNumber", idNum);
                data.put("endDate", endTs);
                data.put("imageUrl", selectedImageUriGlobal);
                data.put("invoiceId", null);
                data.put("isPaid", false);

                db.collection("contracts").add(data)
                        .addOnSuccessListener(r -> {
                            Toast.makeText(this,
                                    "Đã thêm hợp đồng", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
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

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE &&
                resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null && ivPicGlobal != null) {
                ivPicGlobal.setImageURI(uri);
                selectedImageUriGlobal = uri.toString();
            }
        }
    }
}
