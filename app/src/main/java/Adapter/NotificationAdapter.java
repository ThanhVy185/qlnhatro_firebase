package Adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.huflit.qlnhatro.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Data.Notification;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnAction {
        void onView(Notification n);
        void onEdit(Notification n);
        void onDelete(Notification n);
    }

    private final List<Notification> items;
    private final Map<String,String> roomMap;
    private final OnAction listener;

    public NotificationAdapter(List<Notification> items,
                               Map<String,String> roomMap,
                               OnAction listener) {
        this.items    = items;
        this.roomMap  = roomMap;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int pos) {
        Notification n = items.get(pos);

        h.tvTitle.setText(n.title);
        h.tvTime .setText(
                DateFormat.getDateTimeInstance().format(n.timestamp)
        );

        // Chuyển list ID thành list label "Phòng X"
        List<String> labels = new ArrayList<>();
        if (n.rooms != null) {
            for (String id: n.rooms) {
                labels.add(roomMap.getOrDefault(id, id));
            }
        }
        h.tvRoom.setText(
                labels.isEmpty() ? "(Chưa chọn)" : TextUtils.join(", ", labels)
        );

        h.itemView .setOnClickListener(v -> listener.onView(n));
        h.btnEdit   .setOnClickListener(v -> listener.onEdit(n));
        h.btnDelete .setOnClickListener(v -> listener.onDelete(n));
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRoom, tvTime;
        ImageButton btnEdit, btnDelete;

        VH(View v) {
            super(v);
            tvTitle   = v.findViewById(R.id.tvNotifTitle);
            tvRoom    = v.findViewById(R.id.tvNotifRoom);
            tvTime    = v.findViewById(R.id.tvNotifTime);
            btnEdit   = v.findViewById(R.id.btnEditNotif);
            btnDelete = v.findViewById(R.id.btnDeleteNotif);
        }
    }
}
