package com.videocutter.videodub_addsound.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.videocutter.R;
import com.videocutter.videodub_addsound.VideoDubRecord_Activity_new;
import com.videocutter.videodub_addsound.common.FilterType;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Filter_recyler_Adapter extends RecyclerView.Adapter<Filter_recyler_Adapter.MyViewHolder> {
    Context context;
   List<FilterType> filterList;
    int[] imgList = {R.drawable.fl_bilateral_blur,R.drawable.fl_bilateral_blur,R.drawable.fl_box_blur,R.drawable.fl_brightness,
            R.drawable.fl_bulge_distortion,R.drawable.fl_cga_colorspace,R.drawable.fl_contrast,R.drawable.fl_crosshatch,
            R.drawable.fl_exposure,R.drawable.fl_filter_group_sample,R.drawable.fl_gamma,R.drawable.fl_gaussian_filter,R.drawable.fl_gray_scale,
            R.drawable.fl_halftone,R.drawable.fl_hase,R.drawable.fl_highlight_shadow,R.drawable.fl_hue,R.drawable.fl_invert,
            R.drawable.fl_lookup_table_sample,R.drawable.fl_luminance,R.drawable.fl_luminance_threshold,R.drawable.fl_monochrome,
            R.drawable.fl_opacity,R.drawable.fl_overlay,R.drawable.fl_pixelation,R.drawable.fl_pixelation,R.drawable.fl_rgb,
            R.drawable.fl_saturation,R.drawable.fl_sepia,R.drawable.fl_sharp,R.drawable.fl_solarize,R.drawable.fl_sphere_refraction,
            R.drawable.fl_swirl,R.drawable.fl_tone,R.drawable.fl_tone,R.drawable.fl_vibrant,R.drawable.fl_vignette,R.drawable.fl_watermark,
            R.drawable.fl_weakpixel,R.drawable.fl_white_balance,R.drawable.fl_zoom_blur,R.drawable.fl_bitmap_overlay_sample};



    int clickPosition;


    public Filter_recyler_Adapter(Context context, List<FilterType> filterTypes) {
        this.context = context;
        this.filterList = filterTypes;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_items, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.name.setText("F1"+i);
        myViewHolder.image.setImageResource(imgList[i]);

        myViewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((VideoDubRecord_Activity_new)context).Filter_pos(i);

            }
        });



    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {

        @BindView(R.id.filter_item_layout)
        LinearLayout itemLayout;
        @BindView(R.id.filter_item_text)
        TextView name;
        @BindView(R.id.filter_item_image)
        ImageView image;

        MyViewHolder(View view) {

            super(view);

            ButterKnife.bind(this, view);
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
                }
            });


        }


    }
}
