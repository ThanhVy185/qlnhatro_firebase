package com.huflit.qlnhatro.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huflit.qlnhatro.R;

import java.util.List;
import java.util.Map;

import com.huflit.qlnhatro.Contract;

public class ContractAdapter
        extends RecyclerView.Adapter<ContractAdapter.ContractVH> {

    public interface OnContractClickListener {
        void onContractClick(Contract contract);
    }

    private final List<Contract> contracts;
    private final Map<String, String> roomNamesMap;
    private final OnContractClickListener listener;

    public ContractAdapter(List<Contract> contracts,
                           Map<String, String> roomNamesMap,
                           OnContractClickListener listener) {
        this.contracts = contracts;
        this.roomNamesMap = roomNamesMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContractVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contract_grid, parent, false);
        return new ContractVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractVH holder, int position) {
        Contract c = contracts.get(position);

        // Lấy tên phòng từ map (vd: "Phòng 101")
        String roomLabel = roomNamesMap.getOrDefault(c.getRoomId(), "Phòng ?");
        holder.tvRoomLabel.setText(roomLabel);

        // Nếu muốn đổi icon theo trạng thái có thể set ở đây
        // holder.ivContractIcon.setImageResource(R.drawable.ic_contract);

        holder.itemView.setOnClickListener(v -> listener.onContractClick(c));
    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    static class ContractVH extends RecyclerView.ViewHolder {
        ImageView ivContractIcon;
        TextView tvRoomLabel;

        public ContractVH(@NonNull View itemView) {
            super(itemView);
            ivContractIcon = itemView.findViewById(R.id.ivContractIcon);
            tvRoomLabel    = itemView.findViewById(R.id.tvRoomLabel);
        }
    }
}
