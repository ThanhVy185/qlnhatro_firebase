package com.huflit.qlnhatro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class ContractDetailActivity extends AppCompatActivity {
    private TextView tvContractCode, tvTenantName, tvIdNumber;
    private ImageView ivContractImage;
    private TextView tvTotalFee;
    private Button btnMarkPaid;
    private FirebaseFirestore db;
    private String contractId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Edge-to-edge nếu bạn cần
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contract_detail);

        // Áp padding cho hệ thống bar nếu cần
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollContract), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        // 1) Khởi tạo các view ở phần header
        tvContractCode  = findViewById(R.id.tvContractCode);
        tvTenantName    = findViewById(R.id.tvTenantName);
        tvIdNumber      = findViewById(R.id.tvIdNumber);
        ivContractImage = findViewById(R.id.ivContractImage);

        // 2) Khởi tạo include item_fee
        View layoutElec    = findViewById(R.id.layoutElec);
        View layoutWater   = findViewById(R.id.layoutWater);
        View layoutService = findViewById(R.id.layoutService);
        View layoutParking = findViewById(R.id.layoutParking);

        // Lấy TextView bên trong mỗi include
        TextView tvElecLabel    = layoutElec.findViewById(R.id.tvFeeLabel);
        TextView tvElecValue    = layoutElec.findViewById(R.id.tvFeeValue);

        TextView tvWaterLabel   = layoutWater.findViewById(R.id.tvFeeLabel);
        TextView tvWaterValue   = layoutWater.findViewById(R.id.tvFeeValue);

        TextView tvServiceLabel = layoutService.findViewById(R.id.tvFeeLabel);
        TextView tvServiceValue = layoutService.findViewById(R.id.tvFeeValue);

        TextView tvParkingLabel = layoutParking.findViewById(R.id.tvFeeLabel);
        TextView tvParkingValue = layoutParking.findViewById(R.id.tvFeeValue);

        // 3) Tổng cộng và nút thanh toán
        tvTotalFee  = findViewById(R.id.tvTotalFee);
        btnMarkPaid = findViewById(R.id.btnMarkPaid);

        // 4) Firestore
        db = FirebaseFirestore.getInstance();
        contractId = getIntent().getStringExtra("contractId");

        // 5) Load contract và invoice
        db.collection("contracts").document(contractId)
                .get().addOnSuccessListener(doc -> {
                    Contract c = doc.toObject(Contract.class);
                    // bind thông tin contract
                    tvContractCode.setText(c.getContractCode());
                    tvTenantName.setText(c.getTenantName());
                    tvIdNumber.setText(c.getIdentityNumber());
                    Glide.with(this)
                            .load(c.getImageUrl())
                            .placeholder(R.drawable.ic_placeholder)
                            .into(ivContractImage);

                    // nếu có invoiceId thì load
                    if (c.getInvoiceId() != null) {
                        db.collection("invoices").document(c.getInvoiceId())
                                .get().addOnSuccessListener(invDoc -> {
                                    Invoice inv = invDoc.toObject(Invoice.class);
                                    // bind invoice
                                    tvElecLabel.setText("Tiền điện");
                                    tvElecValue.setText(formatCurrency(inv.getElectricityFee()));

                                    tvWaterLabel.setText("Tiền nước");
                                    tvWaterValue.setText(formatCurrency(inv.getWaterFee()));

                                    tvServiceLabel.setText("Tiền dịch vụ");
                                    tvServiceValue.setText(formatCurrency(inv.getServiceFee()));

                                    tvParkingLabel.setText("Tiền xe");
                                    tvParkingValue.setText(formatCurrency(inv.getParkingFee()));

                                    tvTotalFee.setText(formatCurrency(inv.getTotalFee()));

                                    // thiết lập nút thanh toán
                                    btnMarkPaid.setVisibility(inv.isPaid() ? View.GONE : View.VISIBLE);
                                    btnMarkPaid.setOnClickListener(v -> {
                                        db.collection("invoices").document(inv.getId())
                                                .update("paid", true)
                                                .addOnSuccessListener(a -> btnMarkPaid.setVisibility(View.GONE));
                                    });
                                });
                    }
                });
    }

    private String formatCurrency(long value) {
        return String.format("%,d₫", value);
    }
}
