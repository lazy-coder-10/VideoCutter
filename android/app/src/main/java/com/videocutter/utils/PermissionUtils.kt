package com.videocutter.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


//if permission is for fragment then activity will be null and vice-versa
class PermissionUtils(
    private var fragment: Fragment? = null,
    private var arrayOfPermission: Array<String>,
    private var requestCode: Int,
    private var onPermissionCallback: OnPermissionCallback,
    private var activity: Activity? = null
) {
    fun isHasPermission(context : Context,arrayOfPermission: Array<String>): Boolean {
        for (permission in arrayOfPermission) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }


    fun checkAndRequestPermission() {
        val mListPermissionNeeded = ArrayList<String>()
        for (singlePermission in arrayOfPermission) {

            if (fragment != null) {
                if (ContextCompat.checkSelfPermission(
                        fragment!!.requireContext(),
                        singlePermission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    mListPermissionNeeded.add(singlePermission)
                }
            }
            if (activity != null) {
                if (ContextCompat.checkSelfPermission(
                        activity!!,
                        singlePermission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    mListPermissionNeeded.add(singlePermission)
                }
            }


        }

        //Ask for non granted permission
        if (mListPermissionNeeded.isNotEmpty()) {
            if (fragment != null) {
                fragment!!.requestPermissions(
                    mListPermissionNeeded.toArray(arrayOfNulls(mListPermissionNeeded.size)),
                    requestCode
                )
            }
            if (activity != null) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    mListPermissionNeeded.toArray(arrayOfNulls(mListPermissionNeeded.size)),
                    requestCode
                )
            }
            //for activity

        } else {
            //App has all the permission
            onPermissionCallback.allPermissionGranted()
        }
    }


    //gatherPermission grant result
    fun gatherResult(permissions: Array<String>, grantResult: IntArray,isAutoRequestPermission : Boolean = true) {
        val permissionResult = HashMap<String, Int>()
        var deniedCount = 0;

        //Gather Permission Grant result
        for (index in grantResult.indices) {
            if (grantResult[index] == PackageManager.PERMISSION_DENIED) {
                permissionResult[permissions[index]] = grantResult[index]
                deniedCount++
            }
        }

        //check if all the permission are granted
        if (deniedCount == 0) {
            //here all the permission are granted
            onPermissionCallback.allPermissionGranted()
        } // Atleast one or all the permission are denied
        else {
            for (deniedPermission in permissionResult.entries) {
                val permissionName = deniedPermission.key
                var permissionResult = deniedPermission.value

                //Permission is denied (this is the first time, when "never ask is not checked"
                //so ask again explaining the usage of permission
                //@ActivityCompat.shouldShowRequestPermissionRationale will return true

                if(isAutoRequestPermission){
                    if (fragment != null) {
                        if (fragment!!.shouldShowRequestPermissionRationale(permissionName)) {
                            checkAndRequestPermission()
                            break
                        }
                    }
                    //this is for activity
                    if (activity != null) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                activity!!,
                                permissionName
                            )
                        ) {
                            checkAndRequestPermission()
                        }
                    }

                    //permission is denied and never ask again is checked
                    //@ActivityCompat.shouldShowRequestPermissionRationale will return false
                    else {
                        //TODO:go to setting
                        buildSettingMessage(fragment?.context)
                        break;
                    }
                }else{
                    onPermissionCallback.singlePermissionDenied(permissionName)
                    break
                }

            }
        }

    }

    private fun buildSettingMessage(context: Context?) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Permissions are permanently denied, Please enable it from settings.")
            .setCancelable(true)
            .setPositiveButton("Go To Setting") { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", fragment?.context?.packageName, null)
                intent.data = uri
                fragment?.startActivity(intent)
            }
            .setNegativeButton("Cancel") {_,_->

            }
        val alert = builder.create()
        alert.show()
    }

    public interface OnPermissionCallback {
        fun allPermissionGranted()
        fun singlePermissionDenied(permissionName : String){}
        fun allPermissionDenied() {}
    }


}