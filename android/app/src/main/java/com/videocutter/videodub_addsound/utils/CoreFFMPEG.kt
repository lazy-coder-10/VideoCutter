package com.videocutter.videodub_addsound.utils

import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import org.apache.commons.io.FilenameUtils

class CoreFFMPEG {
    fun trim2End(
        startTime: String?,
        endTime: String?,
        actualPath: String,
        z: Boolean,
        outputPath: String
    ): String {
        val arrayList: java.util.ArrayList<String?> = arrayListOf()
        arrayList.add("-y")
        if (startTime != null) {
            arrayList.add(STARTTIME)
            arrayList.add(startTime)
        }
        arrayList.add("-t")
        arrayList.add(endTime)
        arrayList.add("-i")
        arrayList.add("\"" + actualPath + "\"")
        if (!z) {
            arrayList.add("-an")
        }
        arrayList.add("-vcodec")
        arrayList.add("copy")
        arrayList.add("-acodec")
        arrayList.add("copy")
        arrayList.add("-threads")
        arrayList.add("5")
        arrayList.add("-preset")
        arrayList.add("ultrafast")
        arrayList.add("-strict")
        arrayList.add("-2")
        arrayList.add("\"" + outputPath + "\"")
        val stringBuffer = StringBuffer()
        val it: Iterator<*> = arrayList.iterator()
        while (it.hasNext()) {
            stringBuffer.append(it.next() as String?)
            stringBuffer.append(' ')
        }
        FFmpeg.execute(stringBuffer.toString())
        Config.printLastCommandOutput(6)
        return outputPath
    }

    fun conCat(tempTextFilePath: String, outPutFilePath: String): String {
        val arrayList: java.util.ArrayList<String?> = arrayListOf()
        arrayList.add("-y")
        arrayList.add("-f")
        arrayList.add("concat")
        arrayList.add("-safe")
        arrayList.add("0")
        arrayList.add("-i")
        arrayList.add("\"" + tempTextFilePath + "\"")
        arrayList.add("-c")
        arrayList.add("copy")
        arrayList.add("-threads")
        arrayList.add("5")
        arrayList.add("-preset")
        arrayList.add("ultrafast")
        arrayList.add("-strict")
        arrayList.add("-2")
        arrayList.add("\"" + outPutFilePath + "\"")
        val stringBuffer = StringBuffer()
        val it: Iterator<*> = arrayList.iterator()
        while (it.hasNext()) {
            stringBuffer.append(it.next() as String?)
            stringBuffer.append(' ')
        }
        FFmpeg.execute(stringBuffer.toString())
        Config.printLastCommandOutput(6)
        return outPutFilePath
    }

    companion object {
        const val STARTTIME = "-ss"

        /* The parameters that control the quality are the -s (resolution, currently set on 160x120) and the -b (the bitrate, currently set on 150k).
        * Increase them, e.g -s 480x320
        * And -b 900k
        * To improve quality (and get less compression)
        */
        fun startCompressing(
            inputPath: String?,
            outPutPath: String?,
            width: Int,
            height: Int,
            FRAME_RATE: Int
        ): String? {
            if (inputPath == null || inputPath.isEmpty()) {
                return inputPath
            }

            //return extension without dot (ex. mp4, webm)
            val fileExtension = FilenameUtils.getExtension(inputPath)
            Log.d("FILE_EXT",fileExtension)
            if(fileExtension != "mp4"){
                return inputPath
            }

            val arrayList: java.util.ArrayList<String?> = arrayListOf()

            arrayList.apply {
                add("-y")
                add("-i")
                add("\"" + inputPath + "\"")
                add("-s")
                add("${width}x${height}")
                add("-r")
                add("${if (FRAME_RATE >= 10) FRAME_RATE - 5 else FRAME_RATE}")
                add("-vcodec")
                add("mpeg4")
                add("-b:v")
                add("150k")
                add("-b:a")
                add("48000")
                add("-ac")
                add("2")
                /*add("-ar")
                add(sampleRate)*/
                add("-preset")
                add("ultrafast")
                add("\"" + outPutPath + "\"")

            }
            val stringBuffer = StringBuffer()
            val it: Iterator<*> = arrayList.iterator()
            while (it.hasNext()) {
                stringBuffer.append(it.next() as String?)
                stringBuffer.append(' ')
            }
            Log.d("TOTAL_PATH", stringBuffer.toString())
            FFmpeg.execute(stringBuffer.toString())
            Config.printLastCommandOutput(6)
            return outPutPath
        }
    }
}