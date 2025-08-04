package com.huflit.qlnhatro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import Adapter.CategoryAdapter;
import Data.Category;

public class QuanLyNoiQuyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_noi_quy);

        // Tiêu đề
        TextView tv = findViewById(R.id.tvTitleCat);
        tv.setText("QUẢN LÝ NỘI QUY");
        tv.setOnClickListener(v -> finish());

        // 4 danh mục
        List<Category> danhMucs = Arrays.asList(
                new Category("hygiene",  "VỆ SINH",               R.drawable.ic_broom),
                new Category("security", "AN NINH TRẬT TỰ",       R.drawable.ic_lock),
                new Category("safety",   "AN TOÀN CHUNG",         R.drawable.ic_shield),
                new Category("fire",     "PHÒNG CHÁY CHỮA CHÁY",  R.drawable.ic_fire)
        );

        RecyclerView rv = findViewById(R.id.rvCategories);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setAdapter(new CategoryAdapter(danhMucs, cat -> {
            // Chuyển sang ChiTietNoiQuyActivity theo từng danh mục
            Intent i = new Intent(this, ChiTietNoiQuyActivity.class);
            i.putExtra("catId",    cat.id);
            i.putExtra("catTitle", cat.title);
            startActivity(i);
        }));
    }
}
