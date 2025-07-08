package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.huflit.qlnhatro.R;
import java.util.List;
import java.util.Locale;
import Data.HoaDon;

public class HoaDonAdapter extends BaseAdapter {
    private Context context;
    private List<HoaDon> hoaDonList;

    public HoaDonAdapter(Context context, List<HoaDon> hoaDonList) {
        this.context = context;
        this.hoaDonList = hoaDonList;
    }

    @Override
    public int getCount() {
        return hoaDonList != null ? hoaDonList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return hoaDonList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView txtPhong;
        TextView txtTien;
        TextView txtNgay;
        TextView txtTrangThai;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_hoadon, parent, false);

            holder = new ViewHolder();
            holder.txtPhong = convertView.findViewById(R.id.txtPhong);
            holder.txtTien = convertView.findViewById(R.id.txtTien);
            holder.txtNgay = convertView.findViewById(R.id.txtNgay);
            holder.txtTrangThai = convertView.findViewById(R.id.txtTrangThai);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HoaDon hd = hoaDonList.get(position);

        // Gán dữ liệu
        holder.txtPhong.setText("Phòng: " + hd.getMaPhong());
        holder.txtNgay.setText("Ngày lập: " + hd.getNgayLap());

        // SỬA LỖI: Tính tổng tiền thủ công
        double tongTien = hd.getTienPhong() + hd.getTienDien() + hd.getTienNuoc();
        holder.txtTien.setText(String.format(Locale.getDefault(), "Tổng tiền: %,.0fđ", tongTien));

        String trangThai = hd.getTrangThai();
        holder.txtTrangThai.setText(trangThai);

        // Đổi màu dựa trên trạng thái
        if ("Đã thanh toán".equalsIgnoreCase(trangThai)) {
            holder.txtTrangThai.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_status_paid));
            holder.txtTrangThai.setTextColor(ContextCompat.getColor(context, R.color.paid_text_color));
        } else {
            holder.txtTrangThai.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_status_unpaid));
            holder.txtTrangThai.setTextColor(ContextCompat.getColor(context, R.color.unpaid_text_color));
        }

        return convertView;
    }
}