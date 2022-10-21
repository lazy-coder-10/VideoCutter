package com.videocutter.videodub_addsound.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.videocutter.R;

import java.util.ArrayList;
import java.util.List;


public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> {
    int clickPosition;

    private Context context;
    private LayoutInflater inflater;
    private List<Integer> colorPickerColors;
    private OnColorPickerClickListener onColorPickerClickListener;

    ColorPickerAdapter(@NonNull Context context, @NonNull List<Integer> colorPickerColors) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.colorPickerColors = colorPickerColors;
    }

    public ColorPickerAdapter(@NonNull Context context) {
        this(context, getDefaultColors(context));
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.color_picker_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        holder.colorPickerView.setColorFilter(colorPickerColors.get(position));
        buildColorPickerView(holder.colorPickerView,colorPickerColors.get(position),Color.WHITE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onColorPickerClickListener != null)
                    clickPosition = position;
                    onColorPickerClickListener.onColorPickerClickListener(colorPickerColors.get(position));
                    holder.ll.setVisibility(View.VISIBLE);
                    notifyDataSetChanged();
            }
        });

        if(clickPosition!=position){
            holder.ll.setVisibility(View.GONE);

        }else {
            holder.ll.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public int getItemCount() {
        return colorPickerColors.size();
    }

    private void buildColorPickerView(View view, int colorCode, int select) {
        view.setVisibility(View.VISIBLE);

        ShapeDrawable biggerCircle = new ShapeDrawable(new OvalShape());
        biggerCircle.setIntrinsicHeight(20);
        biggerCircle.setIntrinsicWidth(20);
        biggerCircle.setBounds(new Rect(0, 0, 20, 20));
        biggerCircle.getPaint().setColor(colorCode);

        ShapeDrawable smallerCircle = new ShapeDrawable(new OvalShape());
        smallerCircle.setIntrinsicHeight(2);
        smallerCircle.setIntrinsicWidth(2);
        smallerCircle.setBounds(new Rect(0, 0, 5, 5));
        smallerCircle.getPaint().setColor(select);
        smallerCircle.setPadding(10, 10, 10, 10);
        Drawable[] drawables = {smallerCircle, biggerCircle};

        LayerDrawable layerDrawable = new LayerDrawable(drawables);

        view.setBackgroundDrawable(layerDrawable);
    }

    public void setOnColorPickerClickListener(OnColorPickerClickListener onColorPickerClickListener) {
        this.onColorPickerClickListener = onColorPickerClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View colorPickerView;
        LinearLayout ll;


        public ViewHolder(View itemView) {
            super(itemView);
            colorPickerView = itemView.findViewById(R.id.color_picker_view);
            ll = itemView.findViewById(R.id.layout_color_select);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//
//                }
//            });
        }
    }

    public interface OnColorPickerClickListener {
        void onColorPickerClickListener(int colorCode);
    }

    public static List<Integer> getDefaultColors(Context context) {
        ArrayList<Integer> colorPickerColors = new ArrayList<>();
        colorPickerColors.add(ContextCompat.getColor(context, R.color.white));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.blue_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.brown_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.green_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.orange_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.red_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.black));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.red_orange_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.sky_blue_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.violet_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_green_color_picker));
        return colorPickerColors;
    }
}
