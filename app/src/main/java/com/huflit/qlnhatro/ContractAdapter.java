package com.huflit.qlnhatro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ContractViewHolder> {
    public interface OnActionListener {
        void onEdit(Contract contract);
        void onDelete(Contract contract);
    }

    private final List<Contract> contracts;
    private final OnActionListener listener;

    public ContractAdapter(List<Contract> contracts, OnActionListener listener) {
        this.contracts = contracts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contract, parent, false);
        return new ContractViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractViewHolder holder, int position) {
        Contract contract = contracts.get(position);
        holder.tvRoom.setText("Phòng: " + contract.getRoomNumber());
        holder.tvTenant.setText("Người thuê: " + contract.getTenantName());
        holder.tvDates.setText("Từ " + contract.getStartDate() + " đến " + contract.getEndDate());
        holder.tvRent.setText("Giá thuê: " + contract.getRent());
        holder.tvDeposit.setText("Cọc: " + contract.getDeposit());
        holder.tvElec.setText("Điện: " + contract.getElectricityFee());
        holder.tvWater.setText("Nước: " + contract.getWaterFee());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(contract));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(contract));
    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    static class ContractViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoom, tvTenant, tvDates, tvRent, tvDeposit, tvElec, tvWater;
        ImageButton btnEdit, btnDelete;

        ContractViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoom    = itemView.findViewById(R.id.tvRoomLabel);
            tvTenant  = itemView.findViewById(R.id.tvTenantLabel);
            tvDates   = itemView.findViewById(R.id.tvDates);
            tvRent    = itemView.findViewById(R.id.tvContractRent);
            tvDeposit = itemView.findViewById(R.id.tvContractDeposit);
            tvElec    = itemView.findViewById(R.id.tvContractElectricity);
            tvWater   = itemView.findViewById(R.id.tvContractWater);
            btnEdit   = itemView.findViewById(R.id.btnEditContract);
            btnDelete = itemView.findViewById(R.id.btnDeleteContract);
        }
    }
}
