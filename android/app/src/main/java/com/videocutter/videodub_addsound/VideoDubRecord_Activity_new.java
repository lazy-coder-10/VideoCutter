package com.videocutter.videodub_addsound;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.bumptech.glide.Glide;
import com.carlosmuvi.segmentedprogressbar.SegmentedProgressBar;
import com.coremedia.iso.boxes.Container;
import com.daasuu.gpuv.camerarecorder.CameraRecordListener;
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorderBuilder;
import com.daasuu.gpuv.camerarecorder.LensFacing;
import com.videocutter.R;
import com.videocutter.utils.FilePath;
import com.videocutter.videodub_addsound.adapter.Filter_recyler_Adapter;
import com.videocutter.videodub_addsound.base.ActivityManager;
import com.videocutter.videodub_addsound.base.BaseActivity;
import com.videocutter.videodub_addsound.cameraView.SampleCameraGLView;
import com.videocutter.videodub_addsound.common.FilterType;
import com.videocutter.videodub_addsound.common.Utils;
import com.videocutter.videodub_addsound.constant.AppConstant;
import com.videocutter.videodub_addsound.dubvideopreview.DubbedVideoPreview_Activity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.wonderkiln.camerakit.CameraView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.videocutter.videodub_addsound.base.ActivityManager.file_videopath;
import static com.videocutter.videodub_addsound.base.ActivityManager.outputfile;
import static com.videocutter.videodub_addsound.base.ActivityManager.outputfile2;

public class VideoDubRecord_Activity_new extends BaseActivity {


    private static final int CODE_GALLERY_VIDEO = 1002;
    private int screenOrientation;


    private int activeFilterType = 0;


    private SampleCameraGLView sampleGLView;
    protected com.daasuu.gpuv.camerarecorder.GPUCameraRecorder GPUCameraRecorder;
    private String filepath;
    private TextView recordBtn;
    protected LensFacing lensFacing = LensFacing.BACK;
    protected int cameraWidth = 1280;
    protected int cameraHeight = 720;
    protected int videoWidth = 720;
    protected int videoHeight = 1280;


    String videoTempoto = "1";
    String videoTempoFrom = "1";
//    FFmpeg ffmpeg;

//    protected int videoWidth = 1280;
//    protected int videoHeight = 720;

    private boolean toggleClick = false;

    boolean record_process = false;


    private int seekBarValue = 0;


    @BindView(R.id.lay_filter)
    LinearLayout lay_filter;
    @BindView(R.id.filter_list)
    RecyclerView lv;


    private int smoothVal, whiteVal, liftingVal, bigEyesVal, redValue;


    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    MediaRecorder recorder;
    SurfaceHolder holder;

    //} Rasheed


    Context context;
    Camera camera;
    private static String TAG = VideoDubRecord_Activity_new.class.getSimpleName();
    boolean is_recording = false;
    boolean is_flash_on = false;

    private BottomSheetBehavior sheetBehavior;
    ProgressDialog progressDialog;
    private final int PERMISSION_CONSTANT = 1000;

    @BindView(R.id.llBottomSheet_VideoDubLay)
    LinearLayout llBottomSheet_VideoDubLay;
    @BindView(R.id.camera)
    CameraView cameraView;
    @BindView(R.id.ibVideoRecord_VideoDubLay)
    ImageButton ibVideoRecord_VideoDubLay;
    @BindView(R.id.ivBack_VideoDubLay)
    ImageButton ivBack_VideoDubLay;
    @BindView(R.id.ibRotateCamera_VideoDubLay)
    ImageButton ibRotateCamera_VideoDubLay;
  /*  @BindView(R.id.ibFlashCamera_VideoDubLay)
    ImageButton ibFlashCamera_VideoDubLay;*/
    @BindView(R.id.ibFilterCamera_VideoDoubly)
    ImageButton ibFilterCamera_VideoDoubly;
    @BindView(R.id.ibDone_VideoDubLay)
    ImageButton ibDone_VideoDubLay;
    @BindView(R.id.ivClose_AddSoundLay)
    ImageButton ivClose_AddSoundLay;
    @BindView(R.id.tvAddSound_VideoDubLay)
    TextView tvAddSound_VideoDubLay;
    @BindView(R.id.tbTypes_AddSoundLay)
    TabLayout tbTypes_AddSoundLay;
    @BindView(R.id.vpType_AddSoundLay)
    ViewPager vpType_AddSoundLay;
    @BindView(R.id.rl_filter)
    RelativeLayout rl_filter;
    @BindView(R.id.rl_capture)
    RelativeLayout rl_capture;
    @BindView(R.id.rl_camera)
    RelativeLayout rl_camera;
    @BindView(R.id.segmented_progressbar)
    SegmentedProgressBar segmented_progressbar;


    @BindView(R.id.effect_layout)
    LinearLayout effect_layout;

    @BindView(R.id.time_effects)
    LinearLayout time_effects;

    @BindView(R.id.upload_layout)
    LinearLayout upload_layout;

   /* @BindView(R.id.delete_layout)
    LinearLayout delete_layout;*/

    @BindView(R.id.point_fiveX)
    TextView point_fiveX;

    @BindView(R.id.point_threex)
    TextView point_threex;

    @BindView(R.id.oneX)
    TextView oneX;

    @BindView(R.id.twoX)
    TextView twoX;

    @BindView(R.id.threeX)
    TextView threeX;

    @BindView(R.id.text_Speed)
    TextView text_Speed;


    //new

    @BindView(R.id.ai_button)
    Button ai_button;

    @BindView(R.id.ai_effects)
    TextView ai_effects;
    @BindView(R.id.ai_mask)
    TextView ai_mask;
    @BindView(R.id.ai_filter)
    TextView ai_filters;
    @BindView(R.id.normal_cam)
    Button normal_came;


    @BindView(R.id.camera_options)
    LinearLayout normalCam;

    @BindView(R.id.camera_options_ai)
    LinearLayout cameraOptionAi;


    @BindView(R.id.lay_filter_close)
    ImageView lay_filter_close;


    int REQUEST_TAKE_GALLERY_VIDEO = 106;
   // private ViewPagerAdapter pagerAdapter;

    ArrayList<String> videopath_arraylist = new ArrayList<>();

    MediaPlayer mediaPlayer;
    String selected_audioUrl = "", Title;

    String path;
    int count = 0;


    String filter_effect;

    String fPath;
    private String orientation = "portrait";

    @BindView(R.id.idIvGallery)
    ImageButton idIvGallery;
    @BindView(R.id.add_video_progress_layout)
    LinearLayout progressLayout;
    @BindView(R.id.add_video_progress_text)
    TextView progressText;
    @BindView(R.id.loader_iv)
    ImageView loader_iv;
    

    
    AlertDialog alertDialog;

    ActivityManager activityManager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_videodubrecord;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initView() {
        context = VideoDubRecord_Activity_new.this;
        activityManager = new ActivityManager(context);
        ButterKnife.bind(this);
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraOptionAi.setVisibility(View.GONE);
        normalCam.setVisibility(View.VISIBLE);

        try {
            loader_iv = (ImageView) findViewById(R.id.loader_iv);
            Glide.with(context)
                    .asGif()
                    .load(R.drawable.loader)
                    .into(loader_iv);
            //    Glide.with(getContext()).load(R.drawable.ic_dual).into(loader_iv);


        } catch (Exception e) {
            e.printStackTrace();
        }
        final Timer[] timerArr = {new Timer()};

        idIvGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryForVideo();
            }
        });


        ibVideoRecord_VideoDubLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idIvGallery.setVisibility(View.GONE);
                record_process = true;

                if(!is_recording){
                    timerArr[0] = new Timer();
                    timerArr[0].schedule(new TimerTask() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (!is_recording) {
                                        upload_layout.setVisibility(View.GONE);
                                        count++;
                                        startOrStopRecording();
                                    }
                                }
                            });
                        }
                    }, 200);
                } else {
                    timerArr[0].cancel();
                    if (is_recording) {
                        startOrStopRecording();
                    }
                }


            }

        });
/*
        ibVideoRecord_VideoDubLay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                record_process = true;
                if (ibVideoRecord_VideoDubLay.getDrawable().getConstantState() == ContextCompat.getDrawable(VideoDubRecord_Activity_new.this,
                        R.drawable.ic_click_video).getConstantState()) {
                    timerArr[0] = new Timer();
                    timerArr[0].schedule(new TimerTask() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (!is_recording) {
                                        count++;
                                        upload_layout.setVisibility(View.GONE);
                                        startOrStopRecording();
                                    }
                                }
                            });
                        }
                    }, 200);
                } else {
                    timerArr[0].cancel();
                    if (is_recording) {

                        startOrStopRecording();
                    }
                }
                return false;
            }
        });
*/

        ibDone_VideoDubLay.setEnabled(false);
        ibDone_VideoDubLay.setBackgroundResource(R.drawable.ic_app_done);
        // mediaPlayer = MediaPlayer.create(this, R.raw.sample_tone);

      /*  pagerAdapter = new ViewPagerAdapter(getResources(), getSupportFragmentManager());
        vpType_AddSoundLay.setAdapter(pagerAdapter);
        tbTypes_AddSoundLay.setupWithViewPager(vpType_AddSoundLay);
*/

        sheetBehavior = BottomSheetBehavior.from(llBottomSheet_VideoDubLay);
        sheetBehavior.setHideable(true);//Important to add
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        sheetBehavior.setHideable(true);//Important to add
                        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

    }


    private Bitmap getBitmap(Drawable drawable){
        return ((BitmapDrawable)drawable).getBitmap();
    }


    private void openGalleryForVideo() {
        //open gallery for video
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(
                    Intent.createChooser(intent, "Select Video"),
                    CODE_GALLERY_VIDEO
            );
    }


    public void startOrStopRecording() {
        try {
            if (file_videopath.mkdir()) {
                Log.e(TAG, "Directory created"+file_videopath.getAbsolutePath());
            } else {
                Log.e(TAG, "Directory not created");
            }
            Log.e(TAG, "selected_audioUrl: " + selected_audioUrl);


//            if(!ar) {
            if (!is_recording) {
                normalCam.setVisibility(View.GONE);
               // delete_layout.setVisibility(View.GONE);

                is_recording = true;
                ibVideoRecord_VideoDubLay.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_stop));
                long tsLong = System.currentTimeMillis() / 1000;
                segmented_progressbar.setSegmentCount(count);
                segmented_progressbar.setCompletedSegments(count - 1);
                if (count > 1) {
                    segmented_progressbar.playSegment(30000 / count);
                } else {
                    segmented_progressbar.playSegment(30000);
                }
                String ts = Long.toString(tsLong);
                // 123
                if (!selected_audioUrl.equals("")) {
                    stream_audio("" + file_videopath + "/" + fileName);
                }

                StringBuilder sb = new StringBuilder();
                sb.append(file_videopath);
                sb.append("/videofilterdemo_");
                sb.append(ts);
                sb.append(".mp4");
                File file = new File(sb.toString());
                ArrayList<String> arrayList = videopath_arraylist;
                StringBuilder sb2 = new StringBuilder();
                sb2.append(file_videopath);
                sb2.append("/videofilterdemo_");
                sb2.append(ts);
                sb2.append(".mp4");
                //add only zero index
                arrayList.add(sb2.toString());
                GPUCameraRecorder.start(file.getPath());
                ibDone_VideoDubLay.setEnabled(false);
                ibDone_VideoDubLay.setBackgroundResource(R.drawable.ic_app_done);
                /*Hide Visibility */
                ibDone_VideoDubLay.setVisibility(View.GONE);
            } else {
                is_recording = false;
                normalCam.setVisibility(View.VISIBLE);
                idIvGallery.setVisibility(View.VISIBLE);

                // delete_layout.setVisibility(View.VISIBLE);


                GPUCameraRecorder.stop();


                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                }
                segmented_progressbar.pause();
                ibDone_VideoDubLay.setBackgroundResource(R.drawable.ic_app_done);
                ibDone_VideoDubLay.setEnabled(true);
                ibVideoRecord_VideoDubLay.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_click_video));
                /*Show Visibility */
                ibDone_VideoDubLay.setVisibility(View.VISIBLE);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* R.id.ibFlashCamera_VideoDubLay, R.id.delete_img_img*/
    @OnClick({R.id.tvAddSound_VideoDubLay, R.id.ivBack_VideoDubLay, R.id.ibDone_VideoDubLay, R.id.ibRotateCamera_VideoDubLay, R.id.ivClose_AddSoundLay, R.id.ibFilterCamera_VideoDoubly, R.id.ibEffects_VideoDoubly,
            R.id.upload_layout, R.id.effect_layout, R.id.point_threex, R.id.point_fiveX, R.id.oneX, R.id.twoX, R.id.threeX,
            R.id.ibSpeed_VideoDoubly,
            R.id.ai_filter, R.id.ai_mask, R.id.ai_effects, R.id.normal_cam, R.id.ai_flip, R.id.ai_button, R.id.lay_filter_close,})
    public void onViewClicked(View view) {
        int id = view.getId();

        if (view.getId() == R.id.ibFilterCamera_VideoDoubly) {
            final List<FilterType> filterTypes = FilterType.createFilterList();
            lv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            lv.setItemAnimator(new DefaultItemAnimator());
            Filter_recyler_Adapter filter_recyler_adapter = new Filter_recyler_Adapter(context, filterTypes);
            lv.setAdapter(filter_recyler_adapter);
        }

        switch (view.getId()) {

            case R.id.ibFilterCamera_VideoDoubly:
                if (lay_filter.getVisibility() == View.VISIBLE) {
                } else {
                    lay_filter.setVisibility(View.VISIBLE);
                }

                break;
        }

        if (view.getId() == R.id.tvAddSound_VideoDubLay) {
            if (is_recording) {
                Toast.makeText(context, "can't add music while video record", Toast.LENGTH_SHORT).show();
            } else if (record_process == true) {
                ShowDialog("Add Music", "Music addition not allowed in the middle of video recording. Please delete clip and try adding music");
            } else {
                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        } else if (view.getId() == R.id.ivBack_VideoDubLay) {
            onBackPressed();
        } else if (view.getId() == R.id.ibDone_VideoDubLay) {
            final_output_dub_video();
        } else if (view.getId() == R.id.ibRotateCamera_VideoDubLay) {
            flip_camera_front_back();
        } /*else if (view.getId() == R.id.ibFlashCamera_VideoDubLay) {
            if (is_flash_on) {
                is_flash_on = false;
                ibFlashCamera_VideoDubLay.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_flash));
                if (GPUCameraRecorder != null && GPUCameraRecorder.isFlashSupport()) {
                    GPUCameraRecorder.switchFlashMode();
                    GPUCameraRecorder.changeAutoFocus();
                }
                return;
            } else {
                GPUCameraRecorder.switchFlashMode();
                GPUCameraRecorder.changeAutoFocus();
            }
            is_flash_on = true;
            ibFlashCamera_VideoDubLay.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
        } */else if (view.getId() == R.id.ivClose_AddSoundLay) {
            if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setHideable(true);//Important to add
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
               /* if (mPlayer != null) {
                    mPlayer.stop();
                }*/
            }
        } else if (view.getId() == R.id.effect_layout) {

        } else if (view.getId() == R.id.upload_layout) {
           // SharedHelper.putKey(context, "channel_video_uplaod", false);
           /* ActivityManager.redirectToActivityWithoutBundle(context, DvideoList_Activity.class);
            overridePendingTransition(0, 0);*/
        } else if (view.getId() == R.id.point_threex) {
            time_effects.setVisibility(View.GONE);
            videoTempoto = "0.5";
            videoTempoFrom = "2";
            text_Speed.setText("0.5x");

        } else if (view.getId() == R.id.point_fiveX) {
            time_effects.setVisibility(View.GONE);
            videoTempoto = "0.2";
            videoTempoFrom = "1.5";
            text_Speed.setText("0.2x");


        } else if (view.getId() == R.id.oneX) {
            time_effects.setVisibility(View.GONE);
            videoTempoto = "1";
            videoTempoFrom = "1";
            text_Speed.setText("1x");


        } else if (view.getId() == R.id.twoX) {
            time_effects.setVisibility(View.GONE);
            videoTempoto = "1.5";
            videoTempoFrom = "0.75";
            text_Speed.setText("1.5x");


        } else if (view.getId() == R.id.threeX) {
            time_effects.setVisibility(View.GONE);
            videoTempoto = "2";
            videoTempoFrom = "0.5";
            text_Speed.setText("2x");


        } else if (view.getId() == R.id.ibEffects_VideoDoubly) {
        } /*else if (view.getId() == R.id.delete_img_img) {
           *//* ShowDialogD("Delete Frame", "are you sure you want delete clip?");
*//*
        }*/ else if (view.getId() == R.id.ibSpeed_VideoDoubly) {

            if (time_effects.getVisibility() == View.VISIBLE) {
                time_effects.setVisibility(View.GONE);
            } else if (time_effects.getVisibility() == View.GONE) {
                time_effects.setVisibility(View.VISIBLE);
            }

        } else if (id == R.id.ai_button) {

          /*  releaseCamera();
            ActivityManager.redirectToActivityWithoutBundle(context, VideoDubRecord_Activity_AR.class);
            finish();*/

        } else if (id == R.id.normal_cam) {

           /* releaseCamera();
            ActivityManager.redirectToActivityWithoutBundle(context, VideoDubRecord_Activity_AR.class);
            finish();*/

        } else if (id == R.id.ai_flip) {

        } else if (id == R.id.ai_mask) {


        } else if (id == R.id.ai_effects) {

        } else if (id == R.id.ai_filter) {

        } else if (id == R.id.lay_filter_close) {
            lay_filter.setVisibility(View.GONE);
        }
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

    public void ShowDialogD(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        if (count > 0) {
                            int remove = videopath_arraylist.size() - 1;
                            videopath_arraylist.remove(remove);
                            count--;
                            /* Added below code to update progress segment */
                            if (videopath_arraylist.size() > 0) {
                                segmented_progressbar.setSegmentCount(videopath_arraylist.size());
                            } else {
                                idIvGallery.setVisibility(View.VISIBLE);
                               // delete_layout.setVisibility(View.GONE);
                                ibDone_VideoDubLay.setVisibility(View.GONE);
                            }
                            segmented_progressbar.setCompletedSegments(videopath_arraylist.size());
                        }

                    }
                });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();

            }
        });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public void flip_camera_front_back() {
        releaseCamera();
        if (lensFacing == LensFacing.BACK) {
            lensFacing = LensFacing.FRONT;
        } else {
            lensFacing = LensFacing.BACK;
        }
        toggleClick = true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedImageUri = data.getData();

                // OI FILE Manager
                path = selectedImageUri.toString();
                // MEDIA GALLERY
                //                path = getPath(selectedImageUri);
                if (path != null) {


                }
            }

            if(requestCode == CODE_GALLERY_VIDEO){
                if(data.getData() != null){
                    Uri mVideoUri  = data.getData();

                    String path = FilePath.getPath(this,mVideoUri,FilePath.TYPE_VIDEO);
                    if(path == null) return;
                    MediaPlayer md = MediaPlayer.create(this,mVideoUri);
                    int duration = md.getDuration();
                    md.release();

                    long secDuration =  TimeUnit.MILLISECONDS.toSeconds(duration);

                    //video length should not be more than 3 min.
                    if(secDuration > AppConstant.MAX_VIDEO_LIMIT){
                        Toast.makeText(VideoDubRecord_Activity_new.this,
                                "Video length should not be more than 3 min.",Toast.LENGTH_SHORT).show();
                    }else{
                        Bundle bundle = new Bundle();
                        bundle.putString("dubbed_video_path", path);
                        ActivityManager.redirectToActivityWithBundle(context, DubbedVideoPreview_Activity.class, bundle);
                        finish();
                    }
                }
            }
        }
    }




    public void goToCamera(){
        if(hasPermissions(this,permissions)){
            progressLayout.setVisibility(View.VISIBLE);
            progressText.setText("Opening Camera");
            new Handler().postDelayed(() -> {
                progressLayout.setVisibility(View.GONE);

            }, 2000);
            setUpCamera();
        }else{
            ActivityCompat.requestPermissions(this, permissions, permsRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == permsRequestCode){
            boolean isAllPermissionGrant = true;
            for (int i = 0; i < permissions.length; i++) {
                if(ContextCompat.checkSelfPermission(this,permissions[i])!= PackageManager.PERMISSION_GRANTED)
                {
                    isAllPermissionGrant = false;
                    break;
                }
            }

            if(!isAllPermissionGrant){
                Toast.makeText(this, "Please enable all permission", Toast.LENGTH_SHORT).show();
                Utils.openApplicationSettings(this);
                return;
            }
            goToCamera();

            /*if(isAllPermissionGrant){
            }*/

        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        goToCamera();
    }

    @Override
    public void onStop() {

        super.onStop();
        releaseCamera();

    }

    public void onDestroy() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
        super.onDestroy();
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }
        } catch (Exception unused) {
            unused.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
      /*  if (mPlayer != null) {
            mPlayer.stop();
        }*/
        finish();
      /*  Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
       */
    }


   /* @Override
    public void onClickAddSoundDone(Sounds_Pojo sounds_pojo) {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setHideable(true);//Important to add
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        if (sounds_pojo != null) {
            selected_audioUrl = "" + sounds_pojo.getAudio();
            Title = "" + sounds_pojo.getTitle();
            tvAddSound_VideoDubLay.setText(sounds_pojo.getTitle());
            SharedHelper.putKey(activity(), "audio_id_sound", "" + sounds_pojo.getId());
            SharedHelper.putKey(activity(), "categori_id_sound", "" + sounds_pojo.getCategoryId());
        }
        if (file_videopath.exists()) {
            try {
                FileUtils.deleteDirectory(file_videopath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new DownloadFile().execute(selected_audioUrl);
    }

*/
    public void Filter_pos(int i) {
        final List<FilterType> filterTypes = FilterType.createFilterList();

        if (GPUCameraRecorder != null) {
            GPUCameraRecorder.setFilter(FilterType.createGlFilter(filterTypes.get(i), getApplicationContext()));
            lay_filter.setVisibility(View.GONE);
        }
    }


/*
    class ViewPagerAdapter extends FragmentPagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        public int getCount() {
            return 2;
        }

        public CharSequence getPageTitle(int i) {
            switch (i) {
                case 0:
                    return "Discover";
                case 1:
                    return "My Favorites";
                default:
                    return null;
            }
        }


        public ViewPagerAdapter(Resources resources, FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                   // return new AddSound_Discover_frag(VideoDubRecord_Activity_new.this);
                case 1:
                   // return new AddSound_Favorite_frag();
                default:
                    return null;
            }
        }

        public Object instantiateItem(ViewGroup viewGroup, int i) {
            Fragment fragment = (Fragment) super.instantiateItem(viewGroup, i);
            registeredFragments.put(i, fragment);
            return fragment;
        }

        public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
            registeredFragments.remove(i);
            super.destroyItem(viewGroup, i, obj);
        }

        public Fragment getRegisteredFragment(int i) {
            return (Fragment) registeredFragments.get(i);
        }
    }
*/


    private void stream_audio(String audioUrl) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean final_output_dub_video() {
        progressDialog = new ProgressDialog(this);
        new Thread(new Runnable() {
            public void run() {
                String str = null;
                runOnUiThread(new Runnable() {
                    public void run() {

                        progressDialog.setMessage("Please wait..");
                        progressDialog.show();
                    }
                });
                ArrayList arrayList = new ArrayList<String>();
                for (int i = 0; i < videopath_arraylist.size(); i++) {
                    File file = new File((String) videopath_arraylist.get(i));
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(context, Uri.fromFile(file));
                    if ("yes".equals(mediaMetadataRetriever.extractMetadata(17)) && file.length() > 3000) {
                        Log.e("resp", (String) videopath_arraylist.get(i));
                        if(arrayList.isEmpty())
                                arrayList.add(0,videopath_arraylist.get(i));
                        else arrayList.set(0,videopath_arraylist.get(i));
                    }
                }
                Log.e("arrayList", "" + arrayList.size());
                try {
                    Movie[] movieArr = new Movie[arrayList.size()];
                    for (int i2 = 0; i2 < arrayList.size(); i2++) {
                        movieArr[i2] = MovieCreator.build((String) arrayList.get(i2));
                    }
                    LinkedList linkedList = new LinkedList();
                    LinkedList linkedList2 = new LinkedList();
                    for (Movie tracks : movieArr) {
                        for (Track track : tracks.getTracks()) {
                            if (track.getHandler().equals("soun")) {
                                linkedList2.add(track);
                            }
                            if (track.getHandler().equals("vide")) {
                                linkedList.add(track);
                            }
                        }
                    }
                    Movie movie = new Movie();
                    if (linkedList2.size() > 0) {
                        movie.addTrack(new AppendTrack((Track[]) linkedList2.toArray(new Track[linkedList2.size()])));
                    }
                    if (linkedList.size() > 0) {
                        movie.addTrack(new AppendTrack((Track[]) linkedList.toArray(new Track[linkedList.size()])));
                    }
                    Container build = new DefaultMp4Builder().build(movie);
                    if (!selected_audioUrl.equals("")) {
                        str = outputfile;
                    } else {
                        str = outputfile2;
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(new File(str));
                    build.writeContainer(fileOutputStream.getChannel());
                    fileOutputStream.close();
                    String finalStr = str;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            if (!selected_audioUrl.equals("")) {
                                // video_merge_audio();
                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                retriever.setDataSource(finalStr);
                                int duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                                String du = "" + duration / 1000;
                                executeAudioTrimming(finalStr, du);

                            } else {
                                //Intent intent = new Intent(context, DubbedVideoPreview_Activity.class);
                                StringBuilder sb = new StringBuilder();
                                sb.append(file_videopath);
                                sb.append("/output2.mp4");

                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                retriever.setDataSource(finalStr);
                                long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                                long secDuration =  TimeUnit.MILLISECONDS.toSeconds(duration);

                                //video length should not be more than 3 min.
                                if(secDuration > AppConstant.MAX_VIDEO_LIMIT){
                                    Toast.makeText(VideoDubRecord_Activity_new.this,
                                            "Video length should not be more than 3 min.",Toast.LENGTH_SHORT).show();
                                }else{
                                    extract_audio_from_video(finalStr);
                                }
                            }
                        }
                    });
                } catch (Exception unused) {
                    unused.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        }).start();
        return true;
    }

    private void executeAudioTrimming(String finalStr, String du) {   // Bala Added 26082020
        try {
            File output = new File(Environment.getExternalStorageDirectory() + "/videofilterdemo/music" + ".m4a");
            String[] mergeComment = {"-i", selected_audioUrl, "-ss", "0", "-to", du, output.getPath()};
            execFFmpegBinary(mergeComment, finalStr, output.getPath(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void extract_audio_from_video(String finalStr) {
        StringBuilder sb = new StringBuilder();
        sb.append(file_videopath);
        sb.append("/outputaudio.mp3");
        String outputfile = sb.toString();
       try {
            new AudioExtractor().genVideoUsingMuxer(finalStr, outputfile, -1, -1, true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //StringBuilder sb = new StringBuilder();
        sb.append(file_videopath);
        sb.append("/output2.mp4");


        if (!videoTempoto.equals("1")) {
//            loadFFMpegBinary();
            executeFastMotionVideoCommand(finalStr, outputfile);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("dubbed_video_path", finalStr);
            bundle.putString("dubbed_audio_path", outputfile);
            ActivityManager.redirectToActivityWithBundle(context, DubbedVideoPreview_Activity.class, bundle);
        }
        finish();
    }


    private String fileName;

    private class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(VideoDubRecord_Activity_new.this);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // String timestamp = "" + new SimpleDateFormat("yyyy_MM_dd");
                long tsLong = System.currentTimeMillis() / 1000;
                String timestamp = Long.toString(tsLong);
                //Extract file name from URL
                fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length());

                //Append timestamp to file name
                fileName = timestamp + "_" + fileName;

                if (file_videopath.mkdir()) {
                    Log.e(TAG, "Directory created");
                } else {
                    Log.e(TAG, "Directory not created");
                }
                // Output stream to write file
                OutputStream output = new FileOutputStream(file_videopath + "/" + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return "Downloaded at: " + file_videopath + fileName;
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String message) {
            Log.e("PostExecute", "message " + message);
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();
            // Display File path after downloading
            //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void releaseCamera() {
        if (sampleGLView != null) {
            sampleGLView.onPause();
        }
        if (GPUCameraRecorder != null) {
            GPUCameraRecorder.stop();
            GPUCameraRecorder.release();
            GPUCameraRecorder = null;
        }
        if (sampleGLView != null) {
            ((FrameLayout) findViewById(R.id.wrap_view)).removeView(sampleGLView);
            sampleGLView = null;
        }
    }


    private void setUpCameraView() {
        runOnUiThread(() -> {
            FrameLayout frameLayout = findViewById(R.id.wrap_view);
            frameLayout.removeAllViews();
            sampleGLView = null;
            sampleGLView = new SampleCameraGLView(getApplicationContext());
            sampleGLView.setTouchListener((event, width, height) -> {
                if (GPUCameraRecorder == null) return;
                GPUCameraRecorder.changeManualFocusPoint(event.getX(), event.getY(), width, height);
            });
            frameLayout.addView(sampleGLView);
        });
    }

    private void setUpCamera() {
        setUpCameraView();
        GPUCameraRecorder = new GPUCameraRecorderBuilder(this, sampleGLView)
                //.recordNoFilter(true)
                .cameraRecordListener(new CameraRecordListener() {
                    @Override
                    public void onGetFlashSupport(boolean flashSupport) {
                        runOnUiThread(() -> {
                        });
                    }

                    @Override
                    public void onRecordComplete() {
                    }

                    @Override
                    public void onRecordStart() {
                        runOnUiThread(() -> {
                        });
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e("GPUCameraRecorder", exception.toString());
                    }

                    @Override
                    public void onCameraThreadFinish() {
                        if (toggleClick) {
                            runOnUiThread(() -> {
                                setUpCamera();
                            });
                        }
                        toggleClick = false;
                    }

                    @Override
                    public void onVideoFileReady() {
                    }
                })

                .videoSize(videoWidth, videoHeight)
                .cameraSize(cameraWidth, cameraHeight)
                .lensFacing(lensFacing)
                .build();
    }


    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();

    }

    private void executeFastMotionVideoCommand(String finalStr, String outputfileaudio) {
        try {
            File output = new File(Environment.getExternalStorageDirectory()
                    + File.separator + ""
                    + System.currentTimeMillis() + ".mp4");

            String[] complexCommand = {"-y", "-i", finalStr, "-filter_complex", "[0:v]setpts=" + videoTempoFrom + "*PTS[v];[0:a]atempo=" + videoTempoto + "[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", output.getPath()};
            execFFmpegBinary(complexCommand, output.getPath(), outputfileaudio, true); // Bala added true 26082020
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeAudioVideoMergeCommand(String finalStr, String outputfileaudio) {
        try {
            String audio = Environment.getExternalStorageDirectory() + "/videofilterdemo/music" + ".m4a";
            File output = new File(Environment.getExternalStorageDirectory() + "/videofilterdemo/video" + ".mp4");
            String[] mergeComment = {"-i", finalStr, "-i", audio, "-c:v", "copy", "-c:a", "copy", output.getPath()};
            execFFmpegBinary(mergeComment, output.getPath(), "", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private void execFFmpegBinary(final String[] command, String filePath, String audio, Boolean flag, String ne) {
//        try {
//            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
//                @Override
//                public void onFailure(String s) {
//                    Log.d(TAG, "FAILED with output : " + s);
//                }
//
//                @Override
//                public void onSuccess(String s) {
//                    try {
//                        if (flag) {
//                            Log.d(TAG, "SUCCESS with output : " + s);
//                            Bundle bundle = new Bundle();
//                            bundle.putString("dubbed_video_path", filePath);
//                            bundle.putString("dubbed_audio_path", audio);
//                            ActivityManager.redirectToActivityWithBundle(context, DubbedVideoPreview_Activity.class, bundle);
//                        } else {
//                            executeAudioVideoMergeCommand(filePath, "");
//                            DialogManager.showProgress(context);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                @Override
//                public void onProgress(String s) {
//                    Log.d(TAG, "Started command : ffmpeg " + command);
//
//
//                }
//
//                @Override
//                public void onStart() {
//                    Log.d(TAG, "Started command : ffmpeg " + command);
//                    DialogManager.showProgress(context);
//                }
//
//                @Override
//                public void onFinish() {
//                    Log.d(TAG, "Finished command : ffmpeg " + command);
//                    DialogManager.hideProgress();
//
//
//                }
//            });
//        } catch (FFmpegCommandAlreadyRunningException e) {
//            // do nothing for now
//        }
//    }


    private String getFilterPath(String filterName) {
        if (filterName.equals("none")) {
            return null;
        }
        return "file:///android_asset/" + filterName;
    }


    private void execFFmpegBinary(final String[] command, String filePath, String audio, Boolean flag) {
        DialogManager.showProgress(activity());
        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage message) {
                Log.d("apply message", message.getText());

            }
        });
        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics newStatistics) {
                Log.d("apply message", newStatistics.toString());

            }
        });

        long executionId = FFmpeg.executeAsync(command, (executionId1, returnCode) -> {

            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                DialogManager.hideProgress();
                try {
                    if (flag) {
                        Bundle bundle = new Bundle();
                        bundle.putString("dubbed_video_path", filePath);
                        bundle.putString("dubbed_audio_path", audio);
                        //ActivityManager.redirectToActivityWithBundle(context, DubbedVideoPreview_Activity.class, bundle);
                    } else {
                        executeAudioVideoMergeCommand(filePath, "");
                        DialogManager.showProgress(context);
                    }
                } catch (Exception e) {
                    DialogManager.hideProgress();
                    e.printStackTrace();
                }
            } else {
                DialogManager.hideProgress();
                Log.e(TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
            }
        });
        Log.e(TAG, "execFFmpegMergeVideo executionId-" + executionId);


    }


}
