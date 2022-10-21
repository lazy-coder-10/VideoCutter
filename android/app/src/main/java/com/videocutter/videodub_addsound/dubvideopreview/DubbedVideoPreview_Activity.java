package com.videocutter.videodub_addsound.dubvideopreview;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.bumptech.glide.Glide;
import com.videocutter.R;
import com.videocutter.videodub_addsound.DialogManager;
import com.videocutter.videodub_addsound.MainActivity;
import com.videocutter.videodub_addsound.base.ActivityManager;
import com.videocutter.videodub_addsound.base.BaseActivity;
import com.videocutter.videodub_addsound.common.Utils;
import com.videocutter.videodub_addsound.fragment.PropertiesBSFragment;
import com.videocutter.videodub_addsound.fragment.StickerBSFragment;
import com.videocutter.videodub_addsound.fragment.TextEditorDialogFragment;
import com.videocutter.videodub_addsound.photoeditor.DimensionData;
import com.videocutter.videodub_addsound.photoeditor.OnPhotoEditorListener;
import com.videocutter.videodub_addsound.photoeditor.PhotoEditor;
import com.videocutter.videodub_addsound.photoeditor.PhotoEditorView;
import com.videocutter.videodub_addsound.photoeditor.SaveSettings;
import com.videocutter.videodub_addsound.photoeditor.TextStyleBuilder;
import com.videocutter.videodub_addsound.photoeditor.ViewType;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION;


public class DubbedVideoPreview_Activity extends BaseActivity implements Player.EventListener, OnPhotoEditorListener, PropertiesBSFragment.Properties,
        View.OnClickListener,
        StickerBSFragment.StickerListener {

    String ACTION = "";
    Context context;
    ProgressDialog dialog = null;
    private static String TAG = DubbedVideoPreview_Activity.class.getSimpleName();

    String dubbed_video_path = "", dubbed_audio_path = "";

    @BindView(R.id.pvVideoPreview_DubVideoPreLay)
    PlayerView pvVideoPreview_DubVideoPreLay;
    @BindView(R.id.ivBack_DubVideoPreLay)
    ImageView ivBack_DubVideoPreLay;
    @BindView(R.id.btnNext_DubVideoPreLay)
    Button btnNext_DubVideoPreLay;

    @BindView(R.id.ivImage)
    PhotoEditorView photoEditorView;

    @BindView(R.id.text_add)
    ImageView text_add;

    @BindView(R.id.sticker_filter)
    ImageView sticker_filter;

    boolean textPressed = false;
    boolean stickerPressed = false;

    @BindView(R.id.imgDelete)
    ImageView imgDelete;

    private StickerBSFragment mStickerBSFragment;
    PhotoEditor mPhotoEditor;

    private static final int CAMERA_REQUEST = 52;
    private static final int PICK_REQUEST = 53;
    private String globalVideoUrl = "";
    private PropertiesBSFragment propertiesBSFragment;
    private MediaPlayer mediaPlayer;
    private String videoPath = "";
    private String imagePath = "";
    private ArrayList<String> exeCmd;
    private String[] newCommand;
    private ProgressDialog progressDialog;

    private int originalDisplayWidth;
    private int originalDisplayHeight;
    private int newCanvasWidth, newCanvasHeight;
    private int DRAW_CANVASW = 0;
    private int DRAW_CANVASH = 0;


    Boolean action = false;
    private boolean FlagActivity = false;


    @Override
    public int getLayoutId() {
        return R.layout.activity_dubbed_video_preview;
    }

    SimpleExoPlayer newSimpleInstance;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initView() {
        context = DubbedVideoPreview_Activity.this;
        ButterKnife.bind(this);


        Bundle bundle = getIntent().getExtras();
        if (!FlagActivity){
            dubbed_video_path = bundle.getString("dubbed_video_path");
            dubbed_audio_path = bundle.getString("dubbed_audio_path");
        }

        Glide.with(this).load(R.drawable.trans).into(photoEditorView.getSource());
        photoEditorView.setVisibility(View.GONE);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(dubbed_video_path);

        String metaRotation = retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION);
        int rotation = metaRotation == null ? 0 : Integer.parseInt(metaRotation);
        if (rotation == 90 || rotation == 270) {
            DRAW_CANVASH = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            DRAW_CANVASW = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        } else {
            DRAW_CANVASW = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            DRAW_CANVASH = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        }
        setCanvasAspectRatio();
        pvVideoPreview_DubVideoPreLay.getLayoutParams().width = newCanvasWidth;
        pvVideoPreview_DubVideoPreLay.getLayoutParams().height = newCanvasHeight;

        photoEditorView.getLayoutParams().width = newCanvasWidth;
        photoEditorView.getLayoutParams().height = newCanvasHeight;

     /*   if (dubbed_audio_path.equals("")) {
            Toast.makeText(context, "path empty", Toast.LENGTH_SHORT).show();
        } else {
            set_video_play();
        }*/

        set_video_play();

        mPhotoEditor = new PhotoEditor.Builder(this, photoEditorView)
                .setPinchTextScalable(true) // set flag to make text scalable when pinch
                .setDeleteView(imgDelete)
                .build(); // build photo editor sdk
        mStickerBSFragment = new StickerBSFragment();
        // progressDialog = new ProgressDialog(this);

        mStickerBSFragment.setStickerListener(this);
        propertiesBSFragment = new PropertiesBSFragment();
        propertiesBSFragment.setPropertiesChangeListener(this);

        mPhotoEditor.setOnPhotoEditorListener(this);

//        imgClose.setOnClickListener(this);
//        imgDone.setOnClickListener(this);
//        imgDraw.setOnClickListener(this);
//        binding.imgText.setOnClickListener(this);
//        binding.imgUndo.setOnClickListener(this);
//        binding.imgSticker.setOnClickListener(this);

        exeCmd = new ArrayList<>();


        try {
            text_add.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    action=true;

                    textPressed = true;
                    return false;
                }
            });

            sticker_filter.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    action=true;

                    stickerPressed = true;
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void setCanvasAspectRatio() {
        originalDisplayHeight = getDisplayHeight();
        originalDisplayWidth = getDisplayWidth();
        DimensionData displayDiamenion =
                Utils.getScaledDimension(new DimensionData((int) DRAW_CANVASW, (int) DRAW_CANVASH),
                        new DimensionData(originalDisplayWidth, originalDisplayHeight));
        newCanvasWidth = displayDiamenion.width;
        newCanvasHeight = displayDiamenion.height;

    }


    private void set_video_play() {
        newSimpleInstance = ExoPlayerFactory.newSimpleInstance(this.context, (TrackSelector) new DefaultTrackSelector());
        newSimpleInstance.prepare(new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this.context, Util.getUserAgent(this.context, "TikTok")))
                .createMediaSource(Uri.parse(dubbed_video_path)));
        newSimpleInstance.addListener(this);
        pvVideoPreview_DubVideoPreLay.setPlayer(newSimpleInstance);
        newSimpleInstance.setRepeatMode(Player.REPEAT_MODE_ONE);

        newSimpleInstance.setPlayWhenReady(true);
    }

    @OnClick({R.id.ivBack_DubVideoPreLay, R.id.btnNext_DubVideoPreLay, R.id.text_add,
            R.id.camera_filter, R.id.music_filter, R.id.sticker_filter})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.ivBack_DubVideoPreLay) {
            onBackPressed();
        } else if (view.getId() == R.id.btnNext_DubVideoPreLay) {
            if (textPressed || stickerPressed) {
                saveImage();
                stickerPressed = false;
                textPressed = false;
            } else {

                if(action==false){

                    Bundle bundle = new Bundle();
                    bundle.putString("dubbed_video_path", dubbed_video_path);
                    bundle.putString("dubbed_audio_path", dubbed_audio_path);

                    goToPostDub(bundle);

                }else{
                    saveImage();

                }


            }
        } else if (view.getId() == R.id.text_add) {

            action=true;

            photoEditorView.setVisibility(View.VISIBLE);


            TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(this, 0);
            textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {

                @Override
                public void onDone(String inputText, int colorCode, int position, int bgCode, boolean flag) {
                    final TextStyleBuilder styleBuilder = new TextStyleBuilder();
                    styleBuilder.withTextColor(colorCode);
                    Typeface typeface = ResourcesCompat.getFont(context, TextEditorDialogFragment.getDefaultFontIds(context).get(2));
                    styleBuilder.withTextFont(typeface);
                    if (flag){
                        styleBuilder.withBackgroundColor(bgCode);

                    }
                    mPhotoEditor.addText(inputText, styleBuilder, 2);
                }
            });
        } else if (view.getId() == R.id.camera_filter) {
            ShowDialog("Adding Filter", "Coming Soon!!");
        } else if (view.getId() == R.id.music_filter) {
//            ShowDialog("Adding Music", "Coming Soon!!");
        } else if (view.getId() == R.id.sticker_filter) {
            photoEditorView.setVisibility(View.VISIBLE);
            mStickerBSFragment.show(getSupportFragmentManager(), mStickerBSFragment.getTag());

        }
    }

    private void goToPostDub(Bundle bundle) {

        ActivityManager.redirectActivityForResult(this, MainActivity.class,bundle);

    }


    public void ShowDialog(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
        overridePendingTransition(0, 0);

    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    public void stopPreviousVideoPlayer() {
        if (newSimpleInstance != null) {

            newSimpleInstance.removeListener(this);
            newSimpleInstance.release();

        }
    }

    public void onResume() {
        super.onResume();
        if (newSimpleInstance != null) {
            newSimpleInstance.setPlayWhenReady(true);
        }
    }

    public void onPause() {
        super.onPause();
        if (newSimpleInstance != null) {
            newSimpleInstance.setPlayWhenReady(false);
        }
    }

    public void onStop() {
        super.onStop();
        if (newSimpleInstance != null) {
            newSimpleInstance.setPlayWhenReady(false);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (newSimpleInstance != null) {
            newSimpleInstance.release();
        }
    }

    @Override
    public void onStickerClick(Bitmap bitmap) {
        mPhotoEditor.setBrushDrawingMode(false);
        mPhotoEditor.addImage(bitmap);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onEditTextChangeListener(View rootView, String text, int colorCode, int pos) {
        TextEditorDialogFragment textEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode, pos);
        textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
            @Override
            public void onDone(String inputText, int colorCode, int position, int bgCode, boolean flag) {
                final TextStyleBuilder styleBuilder = new TextStyleBuilder();
                styleBuilder.withTextColor(colorCode);
                Typeface typeface = ResourcesCompat.getFont(context, TextEditorDialogFragment.getDefaultFontIds(context).get(2));
                styleBuilder.withTextFont(typeface);
                if (flag){
                    styleBuilder.withBackgroundColor(bgCode);


                }
                mPhotoEditor.editText(rootView, inputText, styleBuilder, position);
            }
        });

    }


    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {

    }

    @Override
    public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {

    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {

    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {

    }

    @Override
    public void onColorChanged(int colorCode) {

    }

    @Override
    public void onOpacityChanged(int opacity) {

    }

    @Override
    public void onBrushSizeChanged(int brushSize) {

    }

    private int getDisplayWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    private int getDisplayHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @SuppressLint("MissingPermission")
    private void saveImage() {
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + ""
                + "tempImage" + ".png");
        try {
            file.createNewFile();

            SaveSettings saveSettings = new SaveSettings.Builder()
                    .setClearViewsEnabled(true)
                    .setTransparencyEnabled(false)
                    .build();

            mPhotoEditor.saveAsFile(file.getAbsolutePath(), saveSettings, new PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(@NonNull String imagePath) {
                    DubbedVideoPreview_Activity.this.imagePath = imagePath;
                    Log.d("imagePath>>", imagePath);
                    Log.d("imagePath2>>", Uri.fromFile(new File(imagePath)).toString());
                    photoEditorView.getSource().setImageURI(Uri.fromFile(new File(imagePath)));
//                    Toast.makeText(context, "Saved successfully...", Toast.LENGTH_SHORT).show();
                    applayWaterMark();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
//                    Toast.makeText(context, "Saving Failed...", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    private void applayWaterMark() {
        File output = new File(Environment.getExternalStorageDirectory()
                + File.separator + ""
                + System.currentTimeMillis() + ".mp4");
        try {
            output.createNewFile();

            exeCmd.add("-y");
            exeCmd.add("-i");
            exeCmd.add(dubbed_video_path);
            exeCmd.add("-i");
            exeCmd.add(imagePath);
            exeCmd.add("-filter_complex");
            exeCmd.add("[1:v]scale=" + DRAW_CANVASW + ":" + DRAW_CANVASH + "[ovrl];[0:v][ovrl]overlay=x=0:y=0");
            exeCmd.add("-c:v");
            exeCmd.add("libx264");
            exeCmd.add("-preset");
            exeCmd.add("ultrafast");
            exeCmd.add(output.getAbsolutePath());

            newCommand = new String[exeCmd.size()];
            for (int j = 0; j < exeCmd.size(); j++) {
                newCommand[j] = exeCmd.get(j);
            }

            for (int k = 0; k < newCommand.length; k++) {
                Log.d("CMD==>>", newCommand[k] + "");
            }
            int sec = 0;
            try {
                Uri uri = Uri.parse(dubbed_video_path);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(this, uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int millSecond = Integer.parseInt(durationStr);
                double time_seconds = millSecond / 1000.0;
                sec = (int) time_seconds;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
//            executeCommand(newCommand, output.getAbsolutePath(), sec);
            execFFmpegBinary(newCommand,output.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void execFFmpegBinary(final String[] command, final String absolutePath) {
        DialogManager.showProgress(activity());
        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage message) {
            }
        });
        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics newStatistics) {
            }
        });

        long executionId = com.arthenica.mobileffmpeg.FFmpeg.executeAsync(command, (executionId1, returnCode) -> {

            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                DialogManager.hideProgress();
                dubbed_video_path = absolutePath;
                Bundle bundle = new Bundle();
                bundle.putString("dubbed_video_path", dubbed_video_path);
                bundle.putString("dubbed_audio_path", dubbed_audio_path);
                FlagActivity = true;
                goToPostDub(bundle);
            }else {
                DialogManager.hideProgress();
            }
        });
        Log.e(TAG, "execFFmpegMergeVideo executionId-" + executionId);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        initView();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if(resultCode == RESULT_OK) {
                finish();
            }
        }
    }
}



