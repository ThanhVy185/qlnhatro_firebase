package com.huflit.qlnhatro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class TenantManagementActivity extends AppCompatActivity {
    private EditText etTenantName, etTenantPhone;
    private Spinner spinnerRooms;
    private Button btnSaveTenant, btnGoRooms, btnGoTenants;
    private RecyclerView rvTenants;
    private FirebaseFirestore db;

    private List<String> roomNames = new ArrayList<>();
    private List<String> roomIds   = new ArrayList<>();
    private List<Tenant> tenantList= new ArrayList<>();
    private TenantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_management);

        // init
        db = FirebaseFirestore.getInstance();
        etTenantName  = findViewById(R.id.etTenantName);
        etTenantPhone = findViewById(R.id.etTenantPhone);
        spinnerRooms  = findViewById(R.id.spinnerRooms);
        btnSaveTenant = findViewById(R.id.btnSaveTenant);
        rvTenants     = findViewById(R.id.rvTenants);
        btnGoRooms    = findViewById(R.id.btnGoRooms);
        btnGoTenants  = findViewById(R.id.btnGoTenants);

        // navigation
        btnGoRooms.setOnClickListener(v ->
                startActivity(new Intent(this, RoomManagementActivity.class))
        );
        btnGoTenants.setEnabled(false);

        // RecyclerView
       // adapter = new TenantAdapter(tenantList);
       // rvTenants.setLayoutManager(new LinearLayoutManager(this));
       // rvTenants.setAdapter(adapter);
// RecyclerView với callback Edit/Delete
        adapter = new TenantAdapter(tenantList, new TenantAdapter.OnItemActionListener() {
            @Override
            public void onEdit(Tenant tenant) {
                showEditTenantDialog(tenant);
            }
            @Override
            public void onDelete(Tenant tenant) {
                deleteTenant(tenant);
            }
        });
        rvTenants.setLayoutManager(new LinearLayoutManager(this));
        rvTenants.setAdapter(adapter);

        loadRooms();     // load spinner
        loadTenants();   // load danh sách tenant

        btnSaveTenant.setOnClickListener(v -> {
            String name  = etTenantName.getText().toString().trim();
            String phone = etTenantPhone.getText().toString().trim();
            int pos = spinnerRooms.getSelectedItemPosition();
            if (name.isEmpty() || phone.isEmpty() || pos < 0) {
                Toast.makeText(this, "Nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            String roomId = roomIds.get(pos);
            Map<String,Object> t = new HashMap<>();
            t.put("name", name);
            t.put("phone", phone);
            t.put("roomId", roomId);
            t.put("createdAt", System.currentTimeMillis());
            db.collection("tenants")
                    .add(t)
                    .addOnSuccessListener(rf -> {
                        Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                        etTenantName.setText("");
                        etTenantPhone.setText("");
                        loadTenants();  // refresh list
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void loadRooms() {
        db.collection("rooms")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(q -> {
                    roomNames.clear();
                    roomIds.clear();
                    for (QueryDocumentSnapshot d: q) {
                        roomNames.add(d.getString("name") + " (" + d.getString("number") + ")");
                        roomIds.add(d.getId());
                    }
                    spinnerRooms.setAdapter(new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, roomNames));
                });
    }

    private void loadTenants() {
        db.collection("tenants")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(q -> {
                    tenantList.clear();
                    for (QueryDocumentSnapshot d: q) {
                        tenantList.add(new Tenant(
                                d.getId(),
                                d.getString("name"),
                                d.getString("phone"),
                                d.getString("roomId")
                        ));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // Hiển thị dialog sửa thông tin Tenant
    private void showEditTenantDialog(Tenant t) {
        // Inflate layout dialog_edit_tenant.xml
        View v = getLayoutInflater().inflate(R.layout.dialog_edit_tenant, null);
        EditText etName  = v.findViewById(R.id.etEditTenantName);
        EditText etPhone = v.findViewById(R.id.etEditTenantPhone);
        Spinner spRooms  = v.findViewById(R.id.spEditRooms);

        // Đặt giá trị ban đầu
        etName.setText(t.getName());
        etPhone.setText(t.getPhone());
        // Load danh sách phòng để chọn
        List<String> roomNames = new ArrayList<>();
        List<String> roomIds   = new ArrayList<>();
        db.collection("rooms")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(q -> {
                    for (QueryDocumentSnapshot d : q) {
                        roomNames.add(d.getString("name") + " (" + d.getString("number") + ")");
                        roomIds.add(d.getId());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, roomNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spRooms.setAdapter(adapter);
                    // Set spinner chọn đúng phòng hiện tại
                    int idx = roomIds.indexOf(t.getRoomId());
                    if (idx >= 0) spRooms.setSelection(idx);
                });

        new AlertDialog.Builder(this)
                .setTitle("Sửa người thuê")
                .setView(v)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName  = etName.getText().toString().trim();
                    String newPhone = etPhone.getText().toString().trim();
                    int    pos      = spRooms.getSelectedItemPosition();
                    if (newName.isEmpty() || newPhone.isEmpty() || pos < 0) {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String newRoomId = roomIds.get(pos);
                    Map<String,Object> upd = new HashMap<>();
                    upd.put("name", newName);
                    upd.put("phone", newPhone);
                    upd.put("roomId", newRoomId);

                    db.collection("tenants").document(t.getId())
                            .update(upd)
                            .addOnSuccessListener(a -> loadTenants())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Xóa một Tenant
    private void deleteTenant(Tenant t) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa người thuê")
                .setMessage("Bạn có chắc muốn xóa \"" + t.getName() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("tenants").document(t.getId())
                            .delete()
                            .addOnSuccessListener(a -> loadTenants())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

}
