package com.huflit.qlnhatro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.huflit.qlnhatro.Contract;
import Data.Room;

public class ContractDetailActivity extends AppCompatActivity {
    private TextView tvContractHeader, tvContractCode, tvTenantName, tvIdNumber;
    private TextView tvCreateDate, tvEndDate;
    private ImageView ivContractImage;
    private MaterialButton btnDelete;

    private FirebaseFirestore db;
    private String contractId;
    private Uri imageUri;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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

        tvContractHeader = findViewById(R.id.tvContractHeader);
        tvContractCode   = findViewById(R.id.tvContractCode);
        tvTenantName     = findViewById(R.id.tvTenantName);
        tvIdNumber       = findViewById(R.id.tvIdNumber);
        tvCreateDate     = findViewById(R.id.tvCreateDate);
        tvEndDate        = findViewById(R.id.tvEndDate);
        ivContractImage  = findViewById(R.id.ivContractImage);
        btnDelete        = findViewById(R.id.btnDelete);

        db = FirebaseFirestore.getInstance();
        contractId = getIntent().getStringExtra("contractId");
        if (contractId == null) {
            Toast.makeText(this, "Không tìm thấy hợp đồng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadContract();
        btnDelete.setOnClickListener(v -> confirmDeletion());
    }

    private void loadContract() {
        db.collection("contracts").document(contractId).get()
                .addOnSuccessListener(doc -> {
                    Contract c = doc.toObject(Contract.class);
                    if (c == null) return;

                    // 1) Lấy và hiển thị ngày
                    tvCreateDate.setText(
                            DATE_FORMAT.format(new Date(c.getCreateDate())));
                    tvEndDate.setText(
                            DATE_FORMAT.format(new Date(c.getEndDate())));

                    // 2) Header phòng + giá phòng
                    db.collection("rooms").document(c.getRoomId()).get()
                            .addOnSuccessListener(rdoc -> {
                                Room r = rdoc.toObject(Room.class);
                                if (r != null) {
                                    tvContractHeader.setText("HỢP ĐỒNG PHÒNG " + r.getNumber());

                                    // Tìm TextView giá phòng trong layout
                                    TextView tvRoomPrice = findViewById(R.id.tvRoomPrice);
                                    double priceToShow = 0;

                                    // Nếu contract có giá phòng -> ưu tiên
                                    if (c.getRoomPrice() > 0) {
                                        priceToShow = c.getRoomPrice();
                                    }
                                    // Nếu contract chưa có giá -> lấy từ Room
                                    else if (r.getPrice() > 0) {
                                        priceToShow = r.getPrice();
                                    }

                                    if (priceToShow > 0) {
                                        tvRoomPrice.setText(String.format(Locale.getDefault(),
                                                "%,.0f VNĐ", priceToShow));
                                    } else {
                                        tvRoomPrice.setText("Chưa có giá");
                                    }
                                }
                            });

                    // 3) Thông tin text
                    tvContractCode.setText(c.getContractCode());
                    tvTenantName.setText(c.getTenantName());
                    tvIdNumber.setText(c.getIdentityNumber());

                    // 4) Ảnh hợp đồng
                    String uriString = c.getImageUrl();
                    if (uriString != null && !uriString.isEmpty()) {
                        imageUri = Uri.parse(uriString);
                        if ("content".equals(imageUri.getScheme())) {
                            try (InputStream is = getContentResolver().openInputStream(imageUri)) {
                                ivContractImage.setImageBitmap(BitmapFactory.decodeStream(is));
                            } catch (Exception e) {
                                ivContractImage.setImageResource(R.drawable.phongngu); // ảnh mặc định
                            }
                        } else {
                            Glide.with(this)
                                    .load(imageUri)
                                    .centerCrop()
                                    .into(ivContractImage);
                        }
                    } else {
                        ivContractImage.setImageResource(R.drawable.phongngu); // ảnh mặc định
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }


    private void showFullScreenImage(Uri uri) {
        if (uri == null) return;
        Dialog d = new Dialog(this,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if ("content".equals(uri.getScheme())) {
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                iv.setImageBitmap(BitmapFactory.decodeStream(is));
            } catch (Exception e) {
                iv.setImageResource(R.drawable.phongngu);
            }
        } else {
            Glide.with(this).load(uri).into(iv);
        }

        iv.setOnClickListener(x -> d.dismiss());
        d.setContentView(iv);
        d.show();
    }

    private void confirmDeletion() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa hợp đồng")
                .setMessage("Bạn có chắc muốn xóa hợp đồng này không?")
                .setPositiveButton("Xóa", (d,w) -> {
                    db.collection("contracts").document(contractId)
                            .delete()
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Đã xóa hợp đồng",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
