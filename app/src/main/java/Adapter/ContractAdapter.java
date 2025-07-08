package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huflit.qlnhatro.Contract;
import com.huflit.qlnhatro.R;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ContractAdapter
        extends RecyclerView.Adapter<ContractAdapter.VH> {

    private final List<Contract> items;
    private final Map<String, String> roomNames;
    private final Consumer<Contract> onItemClick;

    public ContractAdapter(List<Contract> items,
                           Map<String, String> roomNames,
                           Consumer<Contract> onItemClick) {
        this.items = items;
        this.roomNames = roomNames;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contract, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Contract c = items.get(pos);
        // Phòng X
        holder.tvRoom.setText(
                roomNames.getOrDefault(c.getRoomId(), "Phòng ?"));
        // Trạng thái
        holder.tvStatus.setText(
                c.isPaid() ? "Đã thanh toán" : "Chưa thanh toán");
        // Click chuyển detail
        holder.itemView.setOnClickListener(v ->
                onItemClick.accept(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvRoom, tvStatus;
        VH(@NonNull View itemView) {
            super(itemView);
            tvRoom   = itemView.findViewById(R.id.tvContractRoom);
            tvStatus = itemView.findViewById(R.id.tvContractStatus);
        }
    }
}
