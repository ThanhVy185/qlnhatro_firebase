package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.huflit.qlnhatro.R;
import Data.Rule;

import java.util.List;

public class RuleAdapter
        extends RecyclerView.Adapter<RuleAdapter.VH> {

    public interface OnAction {
        void onEdit(Rule r);
        void onDelete(Rule r);
    }

    private List<Rule> items;
    private OnAction listener;

    public RuleAdapter(List<Rule> items, OnAction listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_rule, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int pos) {
        Rule r = items.get(pos);
        h.iv.setImageResource(r.iconRes);
        h.tv.setText(r.text);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(r));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tv;
        ImageButton btnEdit, btnDelete;
        VH(View v) {
            super(v);
            iv        = v.findViewById(R.id.imgRuleIcon);
            tv        = v.findViewById(R.id.tvRuleText);
            btnEdit   = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
