package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.huflit.qlnhatro.R;
import Data.Category;

import java.util.List;

public class CategoryAdapter
        extends RecyclerView.Adapter<CategoryAdapter.VH> {

    public interface OnClick { void onClick(Category cat); }

    private List<Category> items;
    private OnClick listener;

    public CategoryAdapter(List<Category> items, OnClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_category, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int pos) {
        Category c = items.get(pos);
        h.tv.setText(c.title);
        h.iv.setImageResource(c.iconRes);
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv;
        VH(View v) {
            super(v);
            iv = v.findViewById(R.id.imgCatIcon);
            tv = v.findViewById(R.id.tvCatTitle);
        }
    }
}
