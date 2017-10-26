package com.alkisum.android.cloudnotes.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.alkisum.android.cloudnotes.R;
import com.alkisum.android.cloudnotes.ui.Color;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for the grid showing the color palette.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.1
 */
public class ColorGridAdapter extends BaseAdapter {

    /**
     * Context.
     */
    private final Context context;

    /**
     * List of available colors.
     */
    private final List<Color> colors;

    /**
     * Key of the current color.
     */
    private final String currentColorKey;

    /**
     * ColorGridAdapter constructor.
     *
     * @param context         Context
     * @param colors          List of available colors
     * @param currentColorKey Key of the current color
     */
    public ColorGridAdapter(final Context context, final List<Color> colors,
                            final String currentColorKey) {
        this.context = context;
        this.colors = colors;
        this.currentColorKey = currentColorKey;
    }

    @Override
    public final int getCount() {
        return colors.size();
    }

    @Override
    public final Color getItem(final int position) {
        return colors.get(position);
    }

    @Override
    public final long getItemId(final int position) {
        return position;
    }

    @Override
    public final View getView(final int position, final View convertView,
                              final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = convertView;
        if (view == null || view.getTag() == null) {
            view = inflater.inflate(R.layout.list_item_color_palette, parent,
                    false);
            final ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
        }
        final Color color = getItem(position);
        final ViewHolder holder = (ViewHolder) view.getTag();

        GradientDrawable gradientDrawable = (GradientDrawable)
                holder.color.getBackground();
        gradientDrawable.setColor(color.getColor(context));

        if (color.getKey().equals(currentColorKey)) {
            holder.tick.setVisibility(View.VISIBLE);
        }

        return view;
    }

    /**
     * ViewHolder for color list adapter.
     */
    static class ViewHolder {

        /**
         * ImageView showing a color.
         */
        @BindView(R.id.color_palette_grid_color)
        ImageView color;

        /**
         * ImageView showing a tick indicating the current color.
         */
        @BindView(R.id.color_palette_grid_tick)
        ImageView tick;

        /**
         * ViewHolder constructor.
         *
         * @param view View to bind with ButterKnife
         */
        ViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
