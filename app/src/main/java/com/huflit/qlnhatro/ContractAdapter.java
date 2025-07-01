package com.huflit.qlnhatro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huflit.qlnhatro.R;
import com.huflit.qlnhatro.Contract;

import java.util.List;
import java.util.Map;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ViewHolder> {
    public interface OnItemClickListener { void onClick(Contract c); }

    private List<Contract> list;
    private Map<String, String> roomNames;       // map roomId -> roomDisplayName
    private OnItemClickListener listener;

    public ContractAdapter(List<Contract> list,
                           Map<String, String> roomNames,
                           OnItemClickListener listener) {
        this.list = list;
        this.roomNames = roomNames;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contract, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Contract c = list.get(pos);

        // Lấy tên phòng từ map, fallback thành "Phòng <ID>" nếu không có trong map
        String roomId = c.getRoomId();
        String displayName = roomNames.get(roomId);
        if (displayName == null || displayName.isEmpty()) {
            displayName = "Phòng " + roomId;
        }
        h.tvRoom.setText(displayName);

        h.tvStatus.setText(c.isPaid() ? "Đã thanh toán" : "Chưa thanh toán");
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoom, tvStatus;
        ViewHolder(View v) {
            super(v);
            tvRoom   = v.findViewById(R.id.tvContractRoom);
            tvStatus = v.findViewById(R.id.tvContractStatus);
        }
    }
}
