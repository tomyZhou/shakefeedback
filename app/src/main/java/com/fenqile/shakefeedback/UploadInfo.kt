package com.fenqile.shakefeedback

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import java.io.Serializable

class UploadInfo(
    var imageUrl: String? = "",
    var feedbacktxt: String? = "",
    var did: String? = "",         //设备唯一标识
    var uid: String? = "",        //用户uid
    var os: String? = "",
    var osversion: String? = "",  //android取apilevel，比如33
    var appversion: String? = "", //app版本号
    var apppkgname: String? = "",
    var appname: String? = "",
    var brand: String? = "",     //设备型号
    var mobile:String? = ""      //手机号
) : Serializable



object UploadInfoManger{

    fun getVersionName(context: Context): String? {
        try {
            val manager = context.packageManager
            val info = manager.getPackageInfo(context.packageName, 0)
            var version: String? = ""
            if (!TextUtils.isEmpty(info.versionName)) {
                version = if (info.versionName.contains("-")) {
                    info.versionName.substring(0, info.versionName.indexOf("-"))
                } else {
                    info.versionName
                }
            }
            return version
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }
}