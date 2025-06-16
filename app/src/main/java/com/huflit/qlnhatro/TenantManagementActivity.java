package com.huflit.qlnhatro;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

public class TenantManagementActivity extends AppCompatActivity {
    private EditText etTenantName, etTenantPhone;
    private Spinner spinnerRooms;
    private Button btnSaveTenant, btnGoRooms, btnGoTenants;
    private RecyclerView rvTenants;
    private FirebaseFirestore db;

    // Room data
    private List<String> roomNames = new ArrayList<>();
    private List<String> roomIds   = new ArrayList<>();
    private Map<String, Integer> roomMaxMap        = new HashMap<>();
    private Map<String, Integer> roomCurrentCount = new HashMap<>();
    private Map<String, String> roomNumberMap      = new HashMap<>();

    // Tenant data
    private List<Tenant> tenantList = new ArrayList<>();
    private TenantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_management);

        // Init Firestore
        db = FirebaseFirestore.getInstance();

        // Find views
        etTenantName  = findViewById(R.id.etTenantName);
        etTenantPhone = findViewById(R.id.etTenantPhone);
        spinnerRooms  = findViewById(R.id.spinnerRooms);
        btnSaveTenant = findViewById(R.id.btnSaveTenant);
        btnGoRooms    = findViewById(R.id.btnGoRooms);
        btnGoTenants  = findViewById(R.id.btnGoTenants);
        rvTenants     = findViewById(R.id.rvTenants);

        // Navigation
        btnGoTenants.setEnabled(false);
        btnGoRooms.setOnClickListener(v -> startActivity(new Intent(this, RoomManagementActivity.class)));

        // RecyclerView setup
        adapter = new TenantAdapter(tenantList, new TenantAdapter.OnItemActionListener() {
            @Override public void onEdit(Tenant t)  { showEditTenantDialog(t); }
            @Override public void onDelete(Tenant t){ deleteTenant(t); }
        });
        rvTenants.setLayoutManager(new LinearLayoutManager(this));
        rvTenants.setAdapter(adapter);

        // Load initial data
        loadRooms();
        loadTenants();

        btnSaveTenant.setOnClickListener(v -> addTenant());
    }

    private void addTenant() {
        String name  = etTenantName.getText().toString().trim();
        String phone = etTenantPhone.getText().toString().trim();
        int pos = spinnerRooms.getSelectedItemPosition();
        if (name.isEmpty() || phone.isEmpty() || pos < 0) {
            Toast.makeText(this, "Nhập đủ thông tin và chọn phòng", Toast.LENGTH_SHORT).show();
            return;
        }
        String roomId = roomIds.get(pos);
        int current   = roomCurrentCount.get(roomId);
        int maxOcc    = roomMaxMap.get(roomId);
        if (current >= maxOcc) {
            Toast.makeText(this, "Phòng đã full", Toast.LENGTH_SHORT).show();
            return;
        }
        String roomNum = roomNumberMap.get(roomId);

        Map<String,Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("roomId", roomId);
        data.put("roomNumber", roomNum);
        data.put("createdAt", System.currentTimeMillis());

        db.collection("tenants").add(data)
                .addOnSuccessListener(doc -> {
                    etTenantName.setText("");
                    etTenantPhone.setText("");
                    loadRooms();
                    loadTenants();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi thêm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadRooms() {
        db.collection("rooms").orderBy("createdAt").get()
                .addOnSuccessListener(roomsSnap -> {
                    roomNames.clear(); roomIds.clear();
                    roomMaxMap.clear(); roomCurrentCount.clear(); roomNumberMap.clear();
                    for (QueryDocumentSnapshot doc: roomsSnap) {
                        String id = doc.getId();
                        String num = doc.getString("number");
                        int maxOcc = doc.contains("maxOccupancy") ? doc.getLong("maxOccupancy").intValue() : 0;
                        roomIds.add(id);
                        roomNumberMap.put(id, num);
                        roomMaxMap.put(id, maxOcc);
                        roomCurrentCount.put(id, 0);
                    }
                    // count tenants
                    db.collection("tenants").get().addOnSuccessListener(ts -> {
                        for (QueryDocumentSnapshot t: ts) {
                            String rid = t.getString("roomId");
                            if (roomCurrentCount.containsKey(rid)) {
                                roomCurrentCount.put(rid, roomCurrentCount.get(rid) + 1);
                            }
                        }
                        List<String> display = new ArrayList<>(); List<Boolean> isFull = new ArrayList<>();
                        for (String id: roomIds) {
                            int cur = roomCurrentCount.get(id), max=roomMaxMap.get(id);
                            String num = roomNumberMap.get(id);
                            if (cur >= max) {
                                display.add(num + " (Full)"); isFull.add(true);
                            } else {
                                display.add(num + " (Còn " + (max - cur) + " slot)"); isFull.add(false);
                            }
                        }
                        ArrayAdapter<String> spAdapter = new ArrayAdapter<String>(
                                this, android.R.layout.simple_spinner_item, display) {
                            @Override public boolean isEnabled(int pos) { return !isFull.get(pos); }
                            @Override public View getDropDownView(int pos, View cv, ViewGroup p) {
                                TextView tv = (TextView) super.getDropDownView(pos, cv, p);
                                tv.setTextColor(isFull.get(pos)?Color.GRAY:Color.BLACK);
                                return tv;
                            }
                        };
                        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerRooms.setAdapter(spAdapter);
                    });
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Không load được phòng", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadTenants() {
        db.collection("tenants").orderBy("createdAt").get()
                .addOnSuccessListener(qSnap -> {
                    tenantList.clear();
                    for (QueryDocumentSnapshot doc: qSnap) {
                        tenantList.add(new Tenant(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("phone"),
                                doc.getString("roomId"),
                                doc.getString("roomNumber")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Không load được tenant", Toast.LENGTH_SHORT).show()
                );
    }

    private void showEditTenantDialog(Tenant t) {
        View v = getLayoutInflater().inflate(R.layout.dialog_edit_tenant, null);
        EditText etName = v.findViewById(R.id.etEditTenantName);
        EditText etPhone= v.findViewById(R.id.etEditTenantPhone);
        Spinner sp      = v.findViewById(R.id.spEditRooms);
        etName.setText(t.getName()); etPhone.setText(t.getPhone());
        loadRooms(); sp.post(() -> {
            int idx = roomIds.indexOf(t.getRoomId()); if (idx>=0) sp.setSelection(idx);
        });
        new AlertDialog.Builder(this).setTitle("Sửa người thuê").setView(v)
                .setPositiveButton("Lưu", (d,w)->{
                    String nn = etName.getText().toString().trim();
                    String np = etPhone.getText().toString().trim();
                    int pos = sp.getSelectedItemPosition();
                    if (nn.isEmpty()||np.isEmpty()||pos<0) return;
                    String newRid = roomIds.get(pos); String newNum=roomNumberMap.get(newRid);
                    Map<String,Object> u = new HashMap<>();
                    u.put("name", nn); u.put("phone", np);
                    u.put("roomId", newRid); u.put("roomNumber", newNum);
                    db.collection("tenants").document(t.getId()).update(u)
                            .addOnSuccessListener(a->{ loadRooms(); loadTenants(); })
                            .addOnFailureListener(e->Toast.makeText(this,"Lỗi update",Toast.LENGTH_SHORT).show());
                }).setNegativeButton("Hủy",null).show();
    }

    private void deleteTenant(Tenant t) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa người thuê").setMessage("Xóa " + t.getName() + "?")
                .setPositiveButton("Xóa", (d,w)->{
                    db.collection("tenants").document(t.getId()).delete()
                            .addOnSuccessListener(a->loadTenants())
                            .addOnFailureListener(e->Toast.makeText(this,"Lỗi xóa",Toast.LENGTH_SHORT).show());
                }).setNegativeButton("Hủy",null).show();
    }
}
