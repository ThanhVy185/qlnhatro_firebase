package com.huflit.qlnhatro;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateContractActivity extends AppCompatActivity {
    private Spinner spRooms, spTenants;
    private EditText etRent, etDeposit, etElectricity, etWater, etStart, etEnd;
    private Button btnSave;
    private FirebaseFirestore db;

    private List<Room> roomList = new ArrayList<>();
    private List<Tenant> tenantList = new ArrayList<>();
    private ArrayAdapter<String> roomAdapter, tenantAdapter;

    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final Calendar tmpCal = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contract);

        db = FirebaseFirestore.getInstance();
        spRooms   = findViewById(R.id.spnContractRooms);
        spTenants = findViewById(R.id.spnContractTenants);
        etRent    = findViewById(R.id.etContractRent);
        etDeposit = findViewById(R.id.etContractDeposit);
        etElectricity = findViewById(R.id.etContractElectricity);
        etWater   = findViewById(R.id.etContractWater);
        etStart   = findViewById(R.id.etContractStart);
        etEnd     = findViewById(R.id.etContractEnd);
        btnSave   = findViewById(R.id.btnSaveContract);

        roomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRooms.setAdapter(roomAdapter);

        tenantAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        tenantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTenants.setAdapter(tenantAdapter);

        etStart.setFocusable(false);
        etEnd.setFocusable(false);
        etStart.setOnClickListener(v -> showDatePicker(etStart));
        etEnd.setOnClickListener(v -> showDatePicker(etEnd));

        spRooms.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String roomId = roomList.get(position).getId();
                loadTenantsForRoom(roomId);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> saveContract());

        loadRooms();
    }

    private void showDatePicker(final EditText et) {
        DatePickerDialog dpd = new DatePickerDialog(this, (DatePicker view, int y, int m, int d) -> {
            tmpCal.set(y, m, d);
            et.setText(fmt.format(tmpCal.getTime()));
        }, tmpCal.get(Calendar.YEAR), tmpCal.get(Calendar.MONTH), tmpCal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void loadRooms() {
        db.collection("rooms").orderBy("createdAt").get()
                .addOnSuccessListener(snapshot -> {
                    roomList.clear();
                    roomAdapter.clear();
                    for (QueryDocumentSnapshot d : snapshot) {
                        Room r = new Room(
                                d.getId(),
                                d.getString("number"),
                                d.getString("name"),
                                d.contains("maxOccupancy") ? d.getLong("maxOccupancy").intValue() : 0
                        );
                        roomList.add(r);
                        roomAdapter.add(r.getNumber() + " - " + r.getName());
                    }
                    roomAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không load được phòng", Toast.LENGTH_SHORT).show());
    }

    private void loadTenantsForRoom(String roomId) {
        db.collection("tenants").whereEqualTo("roomId", roomId).get()
                .addOnSuccessListener(snapshot -> {
                    tenantList.clear();
                    tenantAdapter.clear();
                    for (QueryDocumentSnapshot d : snapshot) {
                        Tenant t = new Tenant(
                                d.getId(),
                                d.getString("name"),
                                d.getString("phone"),
                                d.getString("roomId"),
                                d.getString("roomNumber")
                        );
                        tenantList.add(t);
                        tenantAdapter.add(t.getName() + " (" + t.getPhone() + ")");
                    }
                    tenantAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không load được người thuê", Toast.LENGTH_SHORT).show());
    }

    private void saveContract() {
        int roomPos = spRooms.getSelectedItemPosition();
        int tenPos  = spTenants.getSelectedItemPosition();
        String rentStr = etRent.getText().toString().trim();
        String depStr  = etDeposit.getText().toString().trim();
        String elecStr = etElectricity.getText().toString().trim();
        String waterStr= etWater.getText().toString().trim();
        String sdStr   = etStart.getText().toString().trim();
        String edStr   = etEnd.getText().toString().trim();

        if (roomPos < 0 || tenPos < 0 || rentStr.isEmpty() || depStr.isEmpty() || elecStr.isEmpty()
                || waterStr.isEmpty() || sdStr.isEmpty() || edStr.isEmpty()) {
            Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Room selR = roomList.get(roomPos);
        Tenant selT = tenantList.get(tenPos);
        long rentVal = Long.parseLong(rentStr);
        long depVal  = Long.parseLong(depStr);
        long elecVal = Long.parseLong(elecStr);
        long waterVal= Long.parseLong(waterStr);

        Map<String,Object> data = new HashMap<>();
        data.put("roomId", selR.getId());
        data.put("roomNumber", selR.getNumber());
        data.put("tenantId", selT.getId());
        data.put("tenantName", selT.getName());
        data.put("rent", rentVal);
        data.put("deposit", depVal);
        data.put("electricityFee", elecVal);
        data.put("waterFee", waterVal);
        data.put("startDate", sdStr);
        data.put("endDate", edStr);
        data.put("createdAt", System.currentTimeMillis());

        db.collection("contracts").add(data)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Đã tạo hợp đồng", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo hợp đồng", Toast.LENGTH_SHORT).show());
    }
}