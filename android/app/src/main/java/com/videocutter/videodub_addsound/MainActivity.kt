package com.videocutter.videodub_addsound

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.exoplayer2.C.UTF8_NAME
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.videocutter.R
import com.videocutter.videodub_addsound.constant.AppConstant
import com.videocutter.videodub_addsound.model.VideoCutModel
import com.videocutter.videodub_addsound.utils.CoreFFMPEG
import idv.luchafang.videotrimmer.VideoTrimmerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), VideoTrimmerView.OnSelectedRangeChangedListener,
    CoroutineScope {

    private val REQ_PICK_VIDEO = 100
    private val REQ_PERMISSION = 200

    var playerView: PlayerView? = null

    lateinit var trimmer: VideoTrimmerView

    lateinit var durationView: TextView

    lateinit var btnClose: ImageView
    lateinit var btnCutVideo: TextView

    val  VIDEO_TRIM_DIRECTORY  by lazy {
        this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }

    //val VIDEO_TRIM_DIRECTORY = "${Environment.getExternalStorageDirectory()}/VideoFilterTrimVideos"

    val folder by lazy {
        File(VIDEO_TRIM_DIRECTORY, "TempFolder")
    }
    private val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this).also {
            it.repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL
            playerView?.player = it
        }
    }

    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, "VideoTrimmer")
    }

    private var videoPath: String = ""

    private val videoModel by lazy { VideoCutModel() }
    private val dialog by lazy { ProgressDialog(this@MainActivity) }


    //actual time is set or not (only once)
    private var setActualTime = false
    var videoFile: File? = null
    var FRAME_RATE = 24;
    var width: Int = 240
    var height: Int = 320 //default
    //var sampleRate: String = "22050"  // default

    /* -------------------------------------------------------------------------------------------*/
    /* Activity */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_trim_and_merge)

        playerView = findViewById(R.id.playerView)
        trimmer = findViewById(R.id.videoTrimmerView)
        durationView = findViewById(R.id.durationView)
        btnCutVideo = findViewById(R.id.idBtnCutVideo)
        btnClose = findViewById(R.id.idIvClose)

        val bundle = intent.extras
        if (bundle != null) {
            val dubbed_video_path = bundle.getString("dubbed_video_path", "")
            Log.d("AudioPath", bundle.getString("dubbed_audio_path", "--"))
            Log.d("VideoPath", dubbed_video_path)

            if (dubbed_video_path.isNotBlank()) {
                videoPath = dubbed_video_path
                videoFile = File(dubbed_video_path)
                Log.d("VideoFile", videoFile.toString())
                if (videoFile != null && !videoPath.isNullOrBlank()) {
                    launch(Dispatchers.IO) {
                        //supportedAudioRate()
                        getFrameRate(videoFile!!)
                        getWidthHeight(videoFile!!)
                    }
                    displayTrimmerView(videoFile!!, videoPath)
                    playVideo(videoPath)

                }
            }
        }


        btnClose.setOnClickListener {
            finish()
        }


        btnCutVideo.setOnClickListener {
            showProgressDialog()
            if (videoModel.editedEndTime == videoModel.editedStartTime) {
                sendBackToRN(videoPath)
            } else {
                startTrimming()
            }


            /*   val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
               alertDialogBuilder.setMessage("Are you sure you want to cut selected part ??")
               alertDialogBuilder.setCancelable(true)
               alertDialogBuilder.setPositiveButton(
                       getString(android.R.string.ok)
               ) { dialog, _ ->

                   if (videoModel.editedEndTime == videoModel.actualEndTime && videoModel.editedStartTime == videoModel.actualStartTime) {
                       Toast.makeText(this, "Width is same", Toast.LENGTH_LONG).show()
                       return@setPositiveButton
                   }



                   dialog.cancel()
               }

               val alertDialog: AlertDialog = alertDialogBuilder.create()
               alertDialog.show()*/
        }


    }

    private fun getWidthHeight(videoFile: File) {
        val retriever = MediaMetadataRetriever()
        retriever?.setDataSource(videoPath)
        val bit = retriever?.frameAtTime
        width = bit?.width ?: 240
        height = bit?.height ?: 320

    }

  /*  private fun supportedAudioRate() {
        val audioManager: AudioManager =
            this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)

    }*/

    fun getFrameRate(file: File) {
        val extractor = MediaExtractor()
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            val fd = fis.fd
            extractor.setDataSource(fd)
            val numTracks = extractor.trackCount
            for (i in 0 until numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("video/") == true) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        FRAME_RATE =
                            format.getInteger(MediaFormat.KEY_FRAME_RATE)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            extractor.release()
            try {
                fis?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startTrimming() {
        player.stop()
        player.release()
        launch {
            val outPath = executeTrim()
            withContext(Dispatchers.Main) {
                Log.d("OUTPUT", outPath)
                sendBackToRN(outPath)
            }
        }
    }

    private fun sendBackToRN(videoPath: String) {
        if (shouldCompressVideo(File(videoPath))) {
            compressVideo(videoPath, width, height) {
                doneWithRN(it)
            }
        } else {
            doneWithRN(videoPath)
        }

    }

    private fun doneWithRN(destPath: String) {
        launch(Dispatchers.IO) {
            val thumbnailUrl = getThumbnail(File(destPath))
            withContext(Dispatchers.Main){
                dialog.dismiss()
                Toast.makeText(this@MainActivity, destPath, Toast.LENGTH_LONG).show()
                //generate thumbnail in background thread
                folder.deleteRecursively()

                Intent().apply {
                    setResult(RESULT_OK, this)
                }
                finish();

            }

        }


     }

    private fun compressVideo(
        filePath: String,
        width: Int,
        height: Int,
        onSuccess: (finalPath: String) -> Unit
    ) {
       /* val folder = File(VIDEO_TRIM_DIRECTORY)
        if (!folder.exists())
            folder.mkdir()*/

        val outPutPath = createFolder().absolutePath
        Log.d("OUT_PATH", outPutPath.toString())
        Log.d("FILE_PATH", filePath.toString())

        launch {
            val compressedPath =
                CoreFFMPEG.startCompressing(filePath, outPutPath,
                    width, height, FRAME_RATE)
            if (!compressedPath.isNullOrBlank()) {
                withContext(Dispatchers.Main) {
                    onSuccess.invoke(compressedPath)
                }
            }
        }


        val fileUris = listOf<Uri>(Uri.fromFile(File(filePath)))


        /*  VideoCompressor.start(
              context = applicationContext, // => This is required
              uris = fileUris, // => Source can be provided as content uris
              saveAt = folder.path, // => the directory to save the compressed video(s)
              listener = object : CompressionListener {
                  override fun onProgress(index: Int, percent: Float) {
                      // Update UI with progress value
                      *//*runOnUiThread {
                    }*//*
                }

                override fun onStart(index: Int) {
                    // Compression start
                }

                override fun onSuccess(index: Int, size: Long, path: String?) {
                    // On Compression success
                    dialog.dismiss()
                    if(!path.isNullOrBlank()){
                        onSuccess.invoke(path)
                    }

                }

                override fun onFailure(index: Int, failureMessage: String) {
                    // On Failure
                    Log.d("FAILURE_MESSAGE",failureMessage)
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity,"Please Try again !!",Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(index: Int) {
                    // On Cancelled
                }

            },
            configureWith = Configuration(quality = VideoQuality.MEDIUM, isMinBitrateCheckEnabled = false)
        )
*/


        /*
                    val destPath : String = createDestinationPath(videoPath)

        VideoCompress.compressVideoHigh(
              outPath,
              destPath,
              object : VideoCompress.CompressListener {
                  override fun onStart() {
                      //Start Compress
                  }


                  override fun onSuccess() {
                      onSuccess.invoke()
                  }

                  override fun onFail() {
                      //Failed
                      *//*baseActivity.hideProgressDialog()
                    context?.showToast(getString(R.string.unable_to_compress_video_file))*//*
                    dialog.dismiss()
                    Toast.makeText(this@MainActivity,"Please Try again",Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(percent: Float) {
                    //Progress
                }
            })*/
    }

    private fun shouldCompressVideo(file: File): Boolean {
        val length: Long = file.length()
        val fileSize = (length / 1024) / 1024
        //file size is more than 5 mb
        Log.d("Acutal_FILE_SIZE", fileSize.toString())
        return fileSize > AppConstant.VIDEO_COMPRESS_REQUIRED
    }

    private fun calculateFileSize(filePath: String) {
        val length: Long = File(filePath).length()
        val fileSize = (length / 1024) / 1024
        Log.d("COMPRESSED_FILE_SIZE", fileSize.toString())

    }

    private suspend fun getThumbnail(videoFile: File): String {
        var retriever: MediaMetadataRetriever? = null;
        var bmp: Bitmap? = null;
        var inputStream: FileInputStream? = null;
        var outPutStream: FileOutputStream? = null
        var thumbnailFile: File? = null
        try {
            retriever = MediaMetadataRetriever();
            inputStream = FileInputStream(videoFile.absolutePath);
            retriever.setDataSource(inputStream.fd);
            bmp = retriever.frameAtTime;

            thumbnailFile = File(VIDEO_TRIM_DIRECTORY, "${UUID.randomUUID()}.jpg")
            if(!thumbnailFile.exists())
                thumbnailFile.parentFile?.mkdirs()

            // Get the file output stream
            outPutStream = FileOutputStream(thumbnailFile)
            bmp.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, outPutStream)

            outPutStream.flush()

        } catch (e: FileNotFoundException) {
            e.printStackTrace();
        } catch (e: IOException) {
            e.printStackTrace();
        } catch (e: RuntimeException) {
            e.printStackTrace();
        } finally {
            retriever?.release()
            inputStream?.close()
            outPutStream?.close()
        }
        return thumbnailFile?.absolutePath ?: ""
    }

    private fun createDestinationPath(outPath: String): String {
        val folder = File(VIDEO_TRIM_DIRECTORY,"")
        if (!folder.exists())
            folder.mkdir()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val videoFileName = "compressed_" + timeStamp + "_"
        val storageDir = File(outPath)
        val image = File.createTempFile(
            videoFileName,  /* prefix */".mp4",         /* suffix */
            folder      /* directory */
        )
        return image.absolutePath
    }

    /*fun createThumbFolder(): File {
        val folder = File("${Environment.getExternalStorageDirectory()}/VideoFilterTrimVideos")
        if (!folder.exists())
            folder.mkdir()
        val filPrefix = videoFile?.name
        return File(folder, filPrefix)

    }*/


    /**
     *There can be 3 conditions
     * 1. Start slider is not moved , end slider moved
     * 2. Start slider moved but end slider not moved
     * 3. both start and end slider moved*/

    private fun executeTrim(): String {
        try {

            val coreFFMPEG = CoreFFMPEG()
            if (AppConstant.DEFAULT_TIME == videoModel.editedStartTime) {
                Log.d(
                    "Cut Middle Start",
                    "From:${videoModel.editedStartTime}" + " To:${videoModel.editedEndTime}"
                )
                return coreFFMPEG.trim2End(
                    videoModel.actualStartTime,
                    videoModel.editedEndTime,
                    videoPath,
                    true,
                    createFolder().absolutePath
                )

            } else if (AppConstant.DEFAULT_TIME == videoModel.editedEndTime) {
                Log.d(
                    "Cut Middle End",
                    "From:${videoModel.editedStartTime}" + " To:${videoModel.actualEndTime}"
                )
                return coreFFMPEG.trim2End(
                    videoModel.editedStartTime,
                    videoModel.actualEndTime,
                    videoPath,
                    true,
                    createFolder().absolutePath
                )

            } else {
                //here comes the main logic
                //when you want delete part you have to divide video in two parts
                //video first will be -> actualStartTime ---> editedStartTime
                //video second will be -> editedEndTime ---> actualEndTime
                var tempInputFile = createTempFolder("tempInput.txt", true)

                val firstTempPath = coreFFMPEG.trim2End(
                    videoModel.actualStartTime,
                    videoModel.editedStartTime,
                    videoPath,
                    true,
                    createTempFolder("temp1").absolutePath
                )

                val secondTempPath = coreFFMPEG.trim2End(
                    videoModel.editedEndTime,
                    videoModel.actualEndTime,
                    videoPath,
                    true,
                    createTempFolder("temp2").absolutePath
                )


                //To concat two video first create a textFile and put there folder name in it.
                try {
                    val printWriter = PrintWriter(tempInputFile.absolutePath, UTF8_NAME)
                    printWriter.println("file '" + firstTempPath + "'")
                    printWriter.println("file '" + secondTempPath + "'")
                    printWriter.close()
                } catch (e: Exception) {
                    Log.e("Create list file", "" + e.printStackTrace())
                }

                //delete temp folder

                return coreFFMPEG.conCat(
                    tempInputFile.path,
                    createFolder().absolutePath
                )

            }
        } catch (e: java.lang.Exception) {
            return e.printStackTrace().toString()
        }

    }

    /**
     * this folder will be deleted after video merge*/
    private fun createTempFolder(tempStr: String, isTempFile: Boolean = false): File {
        if (!folder.exists())
            folder.mkdirs()
        var filPrefix: String = ""
        filPrefix = if (isTempFile) tempStr else tempStr + "_" + videoFile?.name
        return File(folder, filPrefix)
    }

    fun createFolder(): File {
        val folder = File(VIDEO_TRIM_DIRECTORY,"")
        if (!folder.exists())
            folder.mkdir()
        val filPrefix = "${System.currentTimeMillis()}_${videoFile?.name}"
        return File(folder, filPrefix)

    }

    private fun showProgressDialog() {
        dialog.setTitle("Please wait")
        dialog.setMessage("Please wait while processing...")
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_PICK_VIDEO -> {
                if (resultCode == Activity.RESULT_OK) {
                    videoPath = getRealPathFromMediaData(data?.data)
                    // displayTrimmerView(videoPath)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQ_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_PERMISSION && grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED) {
            finish()
        }
    }

    /* -------------------------------------------------------------------------------------------*/
    /* VideoTrimmerView.OnSelectedRangeChangedListener */
    override fun onSelectRangeStart() {
        player.playWhenReady = false
    }

    override fun onSelectRange(startMillis: Long, endMillis: Long) {
        showDuration(startMillis, endMillis)
    }

    override fun onSelectRangeEnd(startMillis: Long, endMillis: Long) {
        showDuration(startMillis, endMillis)
        playVideo(videoPath, startMillis, endMillis)

        if (!setActualTime) {
            setActualTime = true
            videoModel.apply {
                actualStartTime = convertSS(startMillis)
                actualEndTime = convertSS(endMillis)
            }
        } else {

            //trimming start time changed
            if (videoModel.actualStartTime != convertSS(startMillis)) {
                Log.d("TimeChange", "Start time change")
                videoModel.editedStartTime = convertSS(startMillis)
            }

            //trimming end time changed
            if (videoModel.actualEndTime != convertSS(endMillis)) {
                Log.d("TimeChange", "End time change")
                videoModel.editedEndTime = convertSS(endMillis)
            }

        }


    }

    /* -------------------------------------------------------------------------------------------*/
    /* VideoTrimmer */
    private fun displayTrimmerView(path: File, videoPath: String) {

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        val duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()

       trimmer
            .setVideo(path)
            .setMaxDuration(duration)
            .setMinDuration(1000)
            .setFrameCountInWindow(8)
            .setExtraDragSpace(dpToPx(2f))
            .setOnSelectedRangeChangedListener(this)
            .show()
    }

    /* -------------------------------------------------------------------------------------------*/
    /* ExoPlayer2 */
    private fun playVideo(path: String, startMillis: Long, endMillis: Long) {
        if (path.isBlank()) return

        val source = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(path))
            .let {
                ClippingMediaSource(
                    it,
                    startMillis * 1000L,
                    endMillis * 1000L
                )
            }

        player.playWhenReady = true
        player.prepare(source)
    }

    private fun playVideo(path: String) {
        if (path.isBlank()) return
        val source = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(path))
        player.playWhenReady = true
        player.prepare(source)
        playerView?.player = player
    }

    /* -------------------------------------------------------------------------------------------*/
    /* Internal helpers */
    private fun getRealPathFromMediaData(data: Uri?): String {
        data ?: return ""

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                data,
                arrayOf(MediaStore.Video.Media.DATA),
                null, null, null
            )

            if (cursor == null) return ""
            val col = cursor?.getColumnIndex(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()

            return cursor.getString(col)
        } finally {
            cursor?.close()
        }
    }

    private fun showDuration(startMillis: Long, endMillis: Long) {
        val duration = (endMillis - startMillis)
        durationView.text = "${convertSS(duration)}sec selected"
    }

    fun convertSS(j: Long): String {
        return String.format(
            Locale.US,
            "%02d:%02d:%02d",
            *arrayOf<Any>(
                java.lang.Long.valueOf(TimeUnit.MILLISECONDS.toHours(j)),
                java.lang.Long.valueOf(
                    TimeUnit.MILLISECONDS.toMinutes(j) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(j)
                    )
                ),
                java.lang.Long.valueOf(
                    TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            j
                        )
                    )
                )
            )
        )
    }

    private fun dpToPx(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density
    }

    override fun onPause() {
        if (playerView != null) {
            playerView?.player?.stop()
        }
        super.onPause()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

}
