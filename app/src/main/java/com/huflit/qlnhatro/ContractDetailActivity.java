package com.huflit.qlnhatro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import Data.Invoice;
import Data.Room;

public class ContractDetailActivity extends AppCompatActivity {
    private TextView tvContractHeader, tvContractCode, tvTenantName, tvIdNumber;
    private TextView tvElecLabel, tvElecValue, tvWaterLabel, tvWaterValue;
    private TextView tvServiceLabel, tvServiceValue, tvParkingLabel, tvParkingValue;
    private TextView tvTotalFee;
    private ImageView ivContractImage;
    private MaterialButton btnMarkPaid, btnDelete;

    private FirebaseFirestore db;
    private String contractId;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contract_detail);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.scrollDetail),
                (v,insets) -> {
                    Insets s = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(s.left,s.top,s.right,s.bottom);
                    return insets;
                }
        );

        // Bind views
        tvContractHeader = findViewById(R.id.tvContractHeader);
        tvContractCode   = findViewById(R.id.tvContractCode);
        tvTenantName     = findViewById(R.id.tvTenantName);
        tvIdNumber       = findViewById(R.id.tvIdNumber);

        LinearLayout elecLayout    = findViewById(R.id.layoutElec);
        LinearLayout waterLayout   = findViewById(R.id.layoutWater);
        LinearLayout serviceLayout = findViewById(R.id.layoutService);
        LinearLayout parkingLayout = findViewById(R.id.layoutParking);

        tvElecLabel    = elecLayout.findViewById(R.id.tvFeeLabel);
        tvElecValue    = elecLayout.findViewById(R.id.tvFeeValue);
        tvWaterLabel   = waterLayout.findViewById(R.id.tvFeeLabel);
        tvWaterValue   = waterLayout.findViewById(R.id.tvFeeValue);
        tvServiceLabel = serviceLayout.findViewById(R.id.tvFeeLabel);
        tvServiceValue = serviceLayout.findViewById(R.id.tvFeeValue);
        tvParkingLabel = parkingLayout.findViewById(R.id.tvFeeLabel);
        tvParkingValue = parkingLayout.findViewById(R.id.tvFeeValue);

        tvTotalFee      = findViewById(R.id.tvTotalFee);
        ivContractImage = findViewById(R.id.ivContractImage);
        btnMarkPaid     = findViewById(R.id.btnMarkPaid);
        btnDelete       = findViewById(R.id.btnDelete);

        db = FirebaseFirestore.getInstance();
        contractId = getIntent().getStringExtra("contractId");
        if (contractId == null) {
            Toast.makeText(this, "Không tìm thấy hợp đồng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadContract();

        btnMarkPaid.setOnClickListener(v -> markAsPaid());
        btnDelete.setOnClickListener(v -> confirmDeletion());
    }

    private void loadContract() {
        db.collection("contracts").document(contractId).get()
                .addOnSuccessListener(doc -> {
                    Contract c = doc.toObject(Contract.class);
                    if (c == null) return;

                    // Header
                    db.collection("rooms").document(c.getRoomId()).get()
                            .addOnSuccessListener(rdoc -> {
                                Room r = rdoc.toObject(Room.class);
                                if (r != null)
                                    tvContractHeader.setText("HỢP ĐỒNG PHÒNG " + r.getNumber());
                            });

                    tvContractCode.setText(c.getContractCode());
                    tvTenantName.setText(c.getTenantName());
                    tvIdNumber.setText(c.getIdentityNumber());

                    // Image
                    imageUrl = c.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(Uri.parse(imageUrl))
                                .centerCrop()
                                .into(ivContractImage);
                        ivContractImage.setOnClickListener(v -> showFullScreenImage());
                    }

                    // Invoice
                    if (c.getInvoiceId() != null) {
                        db.collection("invoices").document(c.getInvoiceId()).get()
                                .addOnSuccessListener(inv -> {
                                    Invoice i = inv.toObject(Invoice.class);
                                    if (i != null) {
                                        tvElecLabel.setText("Tiền điện");
                                        tvElecValue.setText(String.format("%,d₫", i.getElectricityFee()));
                                        tvWaterLabel.setText("Tiền nước");
                                        tvWaterValue.setText(String.format("%,d₫", i.getWaterFee()));
                                        tvServiceLabel.setText("Phí dịch vụ");
                                        tvServiceValue.setText(String.format("%,d₫", i.getServiceFee()));
                                        tvParkingLabel.setText("Phí gửi xe");
                                        tvParkingValue.setText(String.format("%,d₫", i.getParkingFee()));
                                        tvTotalFee.setText(String.format("%,d₫", i.getTotalFee()));
                                    }
                                });
                    }

                    // Disable if already paid
                    if (c.isPaid()) btnMarkPaid.setEnabled(false);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showFullScreenImage() {
        if (imageUrl == null) return;
        Dialog d = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Glide.with(this).load(Uri.parse(imageUrl)).into(iv);
        d.setContentView(iv);
        d.show();
    }

    private void markAsPaid() {
        db.collection("contracts").document(contractId)
                .update("isPaid", true)
                .addOnSuccessListener(a -> {
                    setResult(RESULT_OK);
                    Toast.makeText(this, "Đã đánh dấu hợp đồng đã toán", Toast.LENGTH_SHORT).show();
                    finish();  // <- Đóng detail để list tự reload
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi đánh dấu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void confirmDeletion() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa hợp đồng")
                .setMessage("Bạn có chắc muốn xóa hợp đồng này không?")
                .setPositiveButton("Xóa", (d,w) -> deleteContract())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteContract() {
        db.collection("contracts").document(contractId)
                .delete()
                .addOnSuccessListener(a -> {
                    setResult(RESULT_OK);
                    Toast.makeText(this, "Đã xóa hợp đồng", Toast.LENGTH_SHORT).show();
                    finish();  // <- Đóng detail để list tự reload
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
