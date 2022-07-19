package com.fenqile.shakefeedback

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import com.fenqile.shakefeedback.R
import com.fenqile.shakefeedback.floatingview.EnFloatingView
import com.fenqile.shakefeedback.floatingview.FloatingMagnetView
import com.fenqile.shakefeedback.floatingview.MagnetViewListener
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


class FeedbackManager {


    private var activity: Activity? = null

    private var mCountDownTimer: CountDownTimer? = null

    private var leftTime: Long = 3000 //倒计时3s

    private var mContainer: FrameLayout? = null  //父容器

    private var floatView: EnFloatingView? = null

    private var backgroundImage: String? = null

    var isShow = false  //是否显示悬浮按钮


    var isOnFeedbackPage = false //是否已经去到了反馈页面，去到了反馈页面shake不再显示按钮

    constructor(activity: Activity) {
        this.activity = activity
        var intentFilter = IntentFilter();
        intentFilter.addAction("FeedbackPageFinish");
        var myBroadcastReceiver = MyBroadcastReceiver();
        try {
            activity?.registerReceiver(myBroadcastReceiver, intentFilter);
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun feedback() {

        var shakeUtil: ShakeUtil = ShakeUtil(activity!!);
        //摇晃手机
        shakeUtil.registerSensorManager {
            if (isShow || isOnFeedbackPage) return@registerSensorManager

            //获取屏幕截屏
            var bitmap = screenShoot(activity!!)

            //将图片保存，传到反馈页面作为背景。
            backgroundImage = saveFile(bitmap).absolutePath

            //添加浮动按钮
            mContainer = activity!!.window.decorView as FrameLayout
            var floatView = getFloatView(activity!!)
            floatView.setIconBitmap(bitmap)
            floatView.layoutParams = initLayoutParams()

            try {
                Log.e("xxx", "將上一個懸浮按鈕去掉")
                mContainer?.removeView(floatView)

                Log.e("xxx", "添加新的懸浮按鈕")
                mContainer?.addView(floatView)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            isShow = true

            startCountDown(activity!!)
        }

    }

    //创建悬浮按钮
    private fun getFloatView(activity: Activity): EnFloatingView {
        if (floatView == null) {
            floatView = EnFloatingView(activity, R.layout.en_floating_view)
        }
        floatView?.setMagnetViewListener(object : MagnetViewListener {
            override fun onRemove(magnetView: FloatingMagnetView?) {
            }

            override fun onClick(magnetView: FloatingMagnetView?) {
                dismiss(activity)

                //去到反馈页
                val intent = Intent(activity, DrawFeedbackActivity::class.java)
                intent.putExtra("backgroundImage", backgroundImage)
                activity.startActivity(intent)

                isOnFeedbackPage = true
            }

            override fun onDown(magnetView: FloatingMagnetView?) {
                //在操作的时候停止计时
                mCountDownTimer?.cancel()
            }

            override fun onUp(magnetView: FloatingMagnetView?) {
                //手指抬起来时，重新开始计时
                startCountDown(activity)
            }

        })
        return floatView!!
    }

    //获取屏幕截屏，生成Bitmap
    private fun screenShoot(activity: Activity): Bitmap {
        //截取当前页面, 获取bitmap
        val dView: View = activity.getWindow().getDecorView()
        dView.isDrawingCacheEnabled = true
        dView.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(dView.drawingCache)

        //解决多次截图不更新的问题
        dView.setDrawingCacheEnabled(false);

        //默认getDocorView的截屏包含状态栏，需要将状态栏去掉。
        // 获取状态栏的高度
        val rect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(rect)
        val statusBarHeight: Int = rect.top
        //获取屏幕的宽和高
        val width: Int = activity.windowManager.getDefaultDisplay().getWidth()
        val height: Int = activity.windowManager.getDefaultDisplay().getHeight()
        //去掉状态栏的截图

        Log.e("xxx", "獲取新的截圖")
        return Bitmap.createBitmap(bitmap, 0, statusBarHeight, width, height - statusBarHeight)
    }

    private fun saveFile(bitmap: Bitmap): File {
        var fileName = System.currentTimeMillis().toString() + ".jpg"
        var path = activity?.filesDir?.absolutePath
        var dirFile = File("$path/feedback")
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        var myCaptureFile = File(path + fileName);
        var bos = BufferedOutputStream(FileOutputStream(myCaptureFile));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        bos.flush();
        bos.close();
        return myCaptureFile;
    }


    //去掉悬浮按钮
    private fun dismiss(activity: Activity) {
        if (ViewCompat.isAttachedToWindow(getFloatView(activity)) && mContainer != null) {
            mContainer?.removeView(getFloatView(activity));
            floatView = null
        }
        isShow = false
    }


    //开启计时器，3s没有操作自动消失
    private fun startCountDown(activity: Activity) {
        if (mCountDownTimer == null) {
            mCountDownTimer = MyCountDownTimer(leftTime, 1000, activity)
        }
        mCountDownTimer?.cancel()
        mCountDownTimer?.start()
    }

    inner class MyCountDownTimer(
        millisInFuture: Long,
        countDownInterval: Long,
        var activity: Activity
    ) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            dismiss(activity)
        }
    }


    //悬浮按钮默认初始位置
    private fun initLayoutParams(): FrameLayout.LayoutParams {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.BOTTOM or Gravity.END
        params.setMargins(0, params.topMargin, params.rightMargin + 33, 500)
        return params
    }

    inner class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e("xxx", "收到通知了")
            //通知已经从feedback页面回来了，可以弹悬浮按钮了
            isOnFeedbackPage = false
        }
    }

}