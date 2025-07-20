package com.huflit.qlnhatro.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.huflit.qlnhatro.R;

import Data.Room;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private Context context;
    private List<Room> roomList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Room room);
        void onLongClick(Room room, int position);
    }

    public RoomAdapter(Context context, List<Room> roomList, OnItemClickListener listener) {
        this.context = context;
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_room_grid, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.tvRoomNumber.setText(room.getNumber());

        // Đổi màu viền nếu phòng đã có hợp đồng
        int strokeColor = room.isHasContract()
                ? ContextCompat.getColor(context, R.color.red)
                : ContextCompat.getColor(context, R.color.green);
        holder.cardRoom.setStrokeColor(strokeColor);

        // Click / Long-click
        holder.itemView.setOnClickListener(v -> listener.onClick(room));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(room, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return roomList != null ? roomList.size() : 0;
    }

    /** Cho phép cập nhật danh sách và refresh RecyclerView */
    public void setRoomList(List<Room> newList) {
        this.roomList = newList;
        notifyDataSetChanged();
    }

    /** ViewHolder chứa reference đến card và text */
    static class RoomViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardRoom;
        TextView tvRoomNumber;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoom = itemView.findViewById(R.id.cardRoom);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
        }
    }
}
