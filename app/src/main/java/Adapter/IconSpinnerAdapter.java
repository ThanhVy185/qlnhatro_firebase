package Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;

import com.huflit.qlnhatro.R;
import com.huflit.qlnhatro.IconManager;

/**
 * SpinnerAdapter chỉ hiển thị icon từ IconManager.ICONS
 */
public class IconSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
    private final Context context;
    private final int[] icons;

    public IconSpinnerAdapter(Context context) {
        this.context = context;
        this.icons = IconManager.ICONS;
    }

    @Override
    public int getCount() {
        return icons.length;
    }

    @Override
    public Object getItem(int position) {
        return icons[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * View hiển thị khi spinner đóng (selected view)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createImageView(position, convertView);
    }

    /**
     * View trong dropdown list
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createImageView(position, convertView);
    }

    private View createImageView(int position, View convertView) {
        ImageView iv;
        if (convertView instanceof ImageView) {
            iv = (ImageView) convertView;
        } else {
            iv = new ImageView(context);
            int size = context.getResources()
                    .getDimensionPixelSize(R.dimen.icon_size);
            iv.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iv.setPadding(8, 8, 8, 8);
        }
        iv.setImageResource(icons[position]);
        return iv;
    }
}
