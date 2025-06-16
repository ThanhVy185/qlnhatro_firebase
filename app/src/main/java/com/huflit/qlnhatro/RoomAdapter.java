package com.huflit.qlnhatro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    public interface OnItemActionListener {
        void onEdit(Room room);
        void onDelete(Room room);
    }

    private List<Room> rooms;
    private OnItemActionListener listener;

    public RoomAdapter(List<Room> rooms, OnItemActionListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder h, int pos) {
        Room r = rooms.get(pos);
        h.tvNumber.setText("Phòng " + r.getNumber());
        h.tvName.setText(r.getName());
        h.btnEdit.setOnClickListener(v -> listener.onEdit(r));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override public int getItemCount() { return rooms.size(); }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber, tvName;
        ImageButton btnEdit, btnDelete;
        RoomViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvName   = itemView.findViewById(R.id.tvRoomName);
            btnEdit  = itemView.findViewById(R.id.btnEditRoom);
            btnDelete= itemView.findViewById(R.id.btnDeleteRoom);
        }
    }
}

