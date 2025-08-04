package com.huflit.qlnhatro;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class ChiTietNoiQuyActivity extends AppCompatActivity {
    private CollectionReference rulesRef;
    private String catId, catTitle;
    private TextView tvContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_noi_quy);

        // 1) Lấy catId, catTitle từ Intent
        catId    = getIntent().getStringExtra("catId");
        catTitle = getIntent().getStringExtra("catTitle");

        // 2) Ánh xạ View header với ID mới tvTitleDetailRule
        TextView tvTitle = findViewById(R.id.tvTitleDetailRule);
        tvTitle.setText(catTitle);
        tvTitle.setOnClickListener(v -> finish());

        // 3) Ánh xạ ô nội dung với ID mới tvRuleContent
        tvContent = findViewById(R.id.tvContent);

        // 4) Nút Edit/Xóa
        ImageButton btnEditRules = findViewById(R.id.btnEditRules);
        btnEditRules.setOnClickListener(v -> showEditDialog());

        // 5) Firestore
        rulesRef = FirebaseFirestore.getInstance().collection("rules");

        // 6) Load nội dung hiện tại
        loadContent();
    }

    private void loadContent() {
        rulesRef.document(catId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String content = doc.getString("content");
                        tvContent.setText(
                                content != null && !content.isEmpty()
                                        ? content
                                        : "Chưa có nội quy. Nhấn nút chỉnh sửa để thêm."
                        );
                    } else {
                        tvContent.setText("Chưa có nội quy. Nhấn nút chỉnh sửa để thêm.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Không tải được nội quy.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void showEditDialog() {
        // Tạo EditText đa dòng
        EditText et = new EditText(this);
        et.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );
        et.setMinLines(10);
        et.setText(tvContent.getText());

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa nội quy")
                .setView(et)
                .setPositiveButton("Lưu", (d, w) -> {
                    String newContent = et.getText().toString().trim();
                    rulesRef.document(catId)
                            .set(new java.util.HashMap<String,Object>() {{
                                put("content",  newContent);
                                put("category", catId);
                            }}, SetOptions.merge())
                            .addOnSuccessListener(a -> {
                                tvContent.setText(newContent);
                                Toast.makeText(
                                        ChiTietNoiQuyActivity.this,
                                        "Lưu thành công",
                                        Toast.LENGTH_SHORT
                                ).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(
                                        ChiTietNoiQuyActivity.this,
                                        "Lưu thất bại",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                })
                .setNeutralButton("Xóa", (d, w) -> {
                    rulesRef.document(catId)
                            .delete()
                            .addOnSuccessListener(a -> {
                                tvContent.setText(
                                        "Chưa có nội quy. Nhấn nút chỉnh sửa để thêm."
                                );
                                Toast.makeText(
                                        ChiTietNoiQuyActivity.this,
                                        "Đã xóa nội quy",
                                        Toast.LENGTH_SHORT
                                ).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(
                                        ChiTietNoiQuyActivity.this,
                                        "Xóa thất bại",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
