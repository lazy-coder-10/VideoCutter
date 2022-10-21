package com.videocutter.videodub_addsound.model

import com.videocutter.videodub_addsound.constant.AppConstant


data class VideoCutModel(var actualStartTime: String = AppConstant.DEFAULT_TIME,
                         var actualEndTime: String = AppConstant.DEFAULT_TIME,
                         var editedStartTime: String = AppConstant.DEFAULT_TIME,
                         var editedEndTime: String = AppConstant.DEFAULT_TIME)