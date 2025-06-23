package com.huflit.qlnhatro;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContractListActivity extends AppCompatActivity {
    private RecyclerView rvContracts;
    private FloatingActionButton fabAdd;
    private FirebaseFirestore db;
    private List<Contract> contracts = new ArrayList<>();
    private ContractAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_list);

        db = FirebaseFirestore.getInstance();
        rvContracts = findViewById(R.id.rvContracts);
        fabAdd      = findViewById(R.id.fabAddContract);

        adapter = new ContractAdapter(contracts, new ContractAdapter.OnActionListener() {
            @Override
            public void onEdit(Contract c) {
                showEditContractDialog(c);
            }
            @Override
            public void onDelete(Contract c) {
                confirmDeleteContract(c);
            }
        });
        rvContracts.setLayoutManager(new LinearLayoutManager(this));
        rvContracts.setAdapter(adapter);

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, CreateContractActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContracts();
    }

    private void loadContracts() {
        db.collection("contracts")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(qs -> {
                    contracts.clear();
                    for (QueryDocumentSnapshot doc : qs) {
                        Contract c = doc.toObject(Contract.class);
                        c.setId(doc.getId());
                        contracts.add(c);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không load được hợp đồng", Toast.LENGTH_SHORT).show()
                );
    }

    private void showEditContractDialog(Contract c) {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_contract_full, null);
        Spinner spRooms   = v.findViewById(R.id.spEditRooms);
        Spinner spTenants = v.findViewById(R.id.spEditTenants);
        EditText etRent   = v.findViewById(R.id.etEditRent);
        EditText etDep    = v.findViewById(R.id.etEditDeposit);
        EditText etElec   = v.findViewById(R.id.etEditElectricity);
        EditText etWater  = v.findViewById(R.id.etEditWater);
        EditText etStart  = v.findViewById(R.id.etEditStart);
        EditText etEnd    = v.findViewById(R.id.etEditEnd);

        // DatePickers
        etStart.setFocusable(false);
        etEnd.setFocusable(false);
        etStart.setOnClickListener(dl -> pickDate(etStart));
        etEnd.setOnClickListener(dl -> pickDate(etEnd));

        // Prepare room & tenant lists
        List<Room> rooms = new ArrayList<>();
        List<Tenant> tenants = new ArrayList<>();

        // Room spinner adapter
        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRooms.setAdapter(roomAdapter);
        db.collection("rooms").orderBy("createdAt").get()
                .addOnSuccessListener(rs -> {
                    rooms.clear(); roomAdapter.clear();
                    for (QueryDocumentSnapshot d : rs) {
                        Room r = new Room(
                                d.getId(), d.getString("number"), d.getString("name"),
                                d.contains("maxOccupancy") ? d.getLong("maxOccupancy").intValue() : 0
                        );
                        rooms.add(r);
                        roomAdapter.add(r.getNumber() + " - " + r.getName());
                    }
                    // pre-select current room
                    for (int i = 0; i < rooms.size(); i++) {
                        if (rooms.get(i).getId().equals(c.getRoomId())) {
                            spRooms.setSelection(i);
                            break;
                        }
                    }
                    roomAdapter.notifyDataSetChanged();
                });

        // Tenant spinner adapter
        ArrayAdapter<String> tenantAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<>());
        tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTenants.setAdapter(tenantAdapter);

        // Load tenants when room selected
        spRooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selRoomId = rooms.get(pos).getId();
                tenants.clear(); tenantAdapter.clear();
                db.collection("tenants").whereEqualTo("roomId", selRoomId).get()
                        .addOnSuccessListener(ts -> {
                            for (QueryDocumentSnapshot td : ts) {
                                Tenant t = new Tenant(
                                        td.getId(), td.getString("name"), td.getString("phone"),
                                        td.getString("roomId"), td.getString("roomNumber")
                                );
                                tenants.add(t);
                                tenantAdapter.add(t.getName() + " (" + t.getPhone() + ")");
                            }
                            // pre-select current tenant
                            for (int j = 0; j < tenants.size(); j++) {
                                if (tenants.get(j).getId().equals(c.getTenantId())) {
                                    spTenants.setSelection(j);
                                    break;
                                }
                            }
                            tenantAdapter.notifyDataSetChanged();
                        });
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Pre-fill values
        etRent .setText(String.valueOf(c.getRent()));
        etDep  .setText(String.valueOf(c.getDeposit()));
        etElec .setText(String.valueOf(c.getElectricityFee()));
        etWater.setText(String.valueOf(c.getWaterFee()));
        etStart.setText(c.getStartDate());
        etEnd  .setText(c.getEndDate());

        // Show dialog
        new AlertDialog.Builder(this)
                .setTitle("Sửa hợp đồng")
                .setView(v)
                .setPositiveButton("Lưu", (dlg, which) -> {
                    int rp = spRooms.getSelectedItemPosition();
                    int tp = spTenants.getSelectedItemPosition();
                    if (rp < 0 || tp < 0) {
                        Toast.makeText(this, "Chọn phòng và người thuê", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Room selR = rooms.get(rp);
                    Tenant selT = tenants.get(tp);

                    long rentVal  = Long.parseLong(etRent.getText().toString().trim());
                    long depVal   = Long.parseLong(etDep.getText().toString().trim());
                    long elecVal  = Long.parseLong(etElec.getText().toString().trim());
                    long waterVal = Long.parseLong(etWater.getText().toString().trim());
                    String sd     = etStart.getText().toString().trim();
                    String ed     = etEnd.getText().toString().trim();

                    Map<String,Object> upd = new HashMap<>();
                    upd.put("roomId", selR.getId());
                    upd.put("roomNumber", selR.getNumber());
                    upd.put("tenantId", selT.getId());
                    upd.put("tenantName", selT.getName());
                    upd.put("rent", rentVal);
                    upd.put("deposit", depVal);
                    upd.put("electricityFee", elecVal);
                    upd.put("waterFee", waterVal);
                    upd.put("startDate", sd);
                    upd.put("endDate", ed);

                    db.collection("contracts").document(c.getId())
                            .update(upd)
                            .addOnSuccessListener(a -> loadContracts())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteContract(Contract c) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa hợp đồng")
                .setMessage("Xóa hợp đồng phòng " + c.getRoomNumber() + "?")
                .setPositiveButton("Xóa", (dlg, which) -> {
                    db.collection("contracts").document(c.getId())
                            .delete()
                            .addOnSuccessListener(a -> loadContracts())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi xóa", Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void pickDate(EditText et) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (DatePicker view, int y, int m, int d) -> {
            Calendar tmp = Calendar.getInstance();
            tmp.set(y, m, d);
            et.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(tmp.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}