package com.huflit.qlnhatro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {
    public interface OnItemActionListener {
        void onEdit(Tenant t);
        void onDelete(Tenant t);
    }

    private final List<Tenant> tenants;
    private final OnItemActionListener listener;

    public TenantAdapter(List<Tenant> tenants, OnItemActionListener listener) {
        this.tenants = tenants;
        this.listener = listener;
    }

    @NonNull @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        Tenant tenant = tenants.get(position);
        holder.tvName.setText(tenant.getName());
        // Hiển thị số điện thoại và phòng
        holder.tvPhone.setText("☎ " + tenant.getPhone() + "   Phòng: " + tenant.getRoomNumber());
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(tenant));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(tenant));
    }

    @Override
    public int getItemCount() {
        return tenants.size();
    }

    static class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageButton btnEdit, btnDelete;

        public TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName   = itemView.findViewById(R.id.tvTenantName);
            tvPhone  = itemView.findViewById(R.id.tvTenantPhone);
            btnEdit  = itemView.findViewById(R.id.btnEditTenant);
            btnDelete= itemView.findViewById(R.id.btnDeleteTenant);
        }
    }
}
