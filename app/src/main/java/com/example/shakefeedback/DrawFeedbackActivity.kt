package com.example.shakefeedback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class DrawFeedbackActivity : Activity() {
    private var huaBanView: HuaBanView? = null
    private var ivDraw: ImageView? = null
    private var ivUndo: ImageView? = null
    private var tvText: TextView? = null
    private var vTextCover: View? = null
    private var etEditor: EditText? = null
    private var tvLeft: TextView? = null
    private var tvLeftTwo: TextView? = null
    private var tvComplete: TextView? = null
    private var tvFeedback: TextView? = null
    private var flTextContainer: FrameLayout? = null
    private var root: ConstraintLayout? = null
    private var backgroundImage: String? = null
    private var llMenu: LinearLayout? = null
    private var mDragTextViewList: HashMap<String, DragTextView> = HashMap()
    private var mSelectedTextId: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_draw_feedback)

        huaBanView = findViewById(R.id.huaban)
        ivDraw = findViewById(R.id.iv_draw)
        ivUndo = findViewById(R.id.iv_undo)

        ivUndo?.setOnClickListener {
            huaBanView?.undo()
        }
        tvText = findViewById(R.id.tv_text)
        vTextCover = findViewById(R.id.v_text_cover)
        etEditor = findViewById(R.id.et_editor)
        tvLeft = findViewById(R.id.tv_left)
        tvLeftTwo = findViewById(R.id.tv_left2)
        tvComplete = findViewById(R.id.tv_complete)
        tvFeedback = findViewById(R.id.tv_feedback)
        llMenu = findViewById(R.id.ll_menu)
        flTextContainer = findViewById(R.id.fl_text_container)
        root = findViewById(R.id.root)

        backgroundImage = intent.getStringExtra("backgroundImage")

        //将前一个页面的截图拿过来当背景，解决新反馈页面截图不包含底层内容的问题。
        if (backgroundImage != null && backgroundImage!!.isNotEmpty()) {
            var bitmap = BitmapFactory.decodeFile(backgroundImage)
            root?.background = BitmapDrawable(bitmap)
        }

        //底部菜单，画图
        ivDraw?.setOnClickListener {
            ivDraw?.setImageResource(R.mipmap.draw_able)
            flTextContainer?.isClickable = false
            ivUndo?.visibility = View.VISIBLE
        }

        //底部菜单，文本
        tvText?.setOnClickListener {
            vTextCover?.visibility = View.VISIBLE
            etEditor?.visibility = View.VISIBLE
            ivUndo?.visibility = View.GONE
            tvFeedback?.visibility = View.GONE
            tvComplete?.visibility = View.VISIBLE
            tvComplete?.text = "完成"
            tvLeftTwo?.visibility = View.VISIBLE
            flTextContainer?.isClickable = true

            showSoftInputFromWindow(this, etEditor!!)
            ivDraw?.setImageResource(R.mipmap.draw_unable)

            var mSelectedTextView: DragTextView? = mDragTextViewList[mSelectedTextId]
            if (mSelectedTextView != null) {
                etEditor?.setText(mSelectedTextView?.text.toString())
                etEditor?.setSelection(mSelectedTextView?.text.toString().length)
            } else {
                etEditor?.setText("")
            }
        }
        tvLeftTwo?.setOnClickListener {
            vTextCover?.visibility = View.GONE
            hideSoftInputFromWindow(this, etEditor!!)
            flTextContainer?.visibility = View.VISIBLE
            ivUndo?.visibility = View.VISIBLE
        }

        tvLeft?.setOnClickListener { finish() }

        tvComplete?.setOnClickListener {
            vTextCover?.visibility = View.GONE
            hideSoftInputFromWindow(this, etEditor!!)
            flTextContainer?.visibility = View.VISIBLE
            etEditor?.visibility = View.GONE
            flTextContainer?.isClickable = true

            tvFeedback?.visibility = View.VISIBLE
            tvComplete?.visibility = View.GONE
            tvLeftTwo?.visibility = View.GONE

            if (mSelectedTextId != "") {
                var mSelectedTextView: DragTextView? = mDragTextViewList[mSelectedTextId]
                if (etEditor?.text.toString().trim() == "") {
                    flTextContainer?.removeView(mSelectedTextView)
                    mDragTextViewList.remove(mSelectedTextId)

                } else {
                    mSelectedTextView?.text = etEditor?.text.toString()
                }

            } else {
                if (etEditor?.text?.trim() != "") {
                    addView(etEditor?.text.toString())
                }
            }
        }

        tvFeedback?.setOnClickListener {

            tvLeft?.visibility = View.GONE
            tvFeedback?.visibility = View.GONE
            llMenu?.visibility = View.GONE

            var bitmap = screenShoot(this)
            upload(bitmap)

            tvLeft?.visibility = View.VISIBLE
            tvFeedback?.visibility = View.VISIBLE

            llMenu?.visibility = View.VISIBLE

        }


        flTextContainer?.setOnClickListener {
            mDragTextViewList?.forEach {
                it.value.focus(false)
            }
            mSelectedTextId = ""
        }
    }

    //获取屏幕截屏，生成Bitmap
    private fun screenShoot(activity: Activity): Bitmap {
        //截取当前页面, 获取bitmap
        val dView: View = activity.getWindow().getDecorView()
        dView.isDrawingCacheEnabled = true
        dView.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(dView.drawingCache)


        //默认getDocorView的截屏包含状态栏，需要将状态栏去掉。
        // 获取状态栏的高度
        val rect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(rect)
        val statusBarHeight: Int = rect.top
        //获取屏幕的宽和高
        val width: Int = activity.windowManager.getDefaultDisplay().getWidth()
        val height: Int = activity.windowManager.getDefaultDisplay().getHeight()
        //去掉状态栏的截图
        return Bitmap.createBitmap(bitmap, 0, statusBarHeight, width, height - statusBarHeight)
    }


    //上传反馈图片
    fun upload(bitmap: Bitmap) {

        var file = saveFile(bitmap)

        var uploadService = RetrofitManager.getInstance().getRetrofit("http://static.fenqile.com/")
            .create(UploadService::class.java)

        //在java代码里调用
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val part: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", file.getName(), requestFile)
        uploadService.upload(part)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { headBean ->
                if (headBean.retcode == 0) {
                    var uploadImageUrl = headBean.domain + headBean.src
                    Log.e("xxx", uploadImageUrl)

                    uploadInfo(uploadImageUrl)
                } else {
                    Toast.makeText(this, "上传失败", Toast.LENGTH_SHORT).show()
                }
            }
    }


    //第二步
    private fun uploadInfo(imageUrl: String) {

        var stringBuffer: StringBuffer = StringBuffer()
        mDragTextViewList.forEach {
            stringBuffer.append((it.value as DragTextView).text.toString())
            stringBuffer.append("#")
        }

        RetrofitManager.getInstance().getRetrofit("http://paya.fenqile.com/")
            .create(UploadService::class.java)
            .uploadInfo(imageUrl, stringBuffer.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { headBean ->
                if (headBean.retcode == 0) {
                    Toast.makeText(this, "上传成功", Toast.LENGTH_SHORT).show()

                    var uploadImageUrl = headBean.domain + headBean.src
                    Log.e("xxx", uploadImageUrl)

                    finish()
                } else {
                    Toast.makeText(this, "上传失败", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveFile(bitmap: Bitmap): File {
        var fileName = System.currentTimeMillis().toString() + ".jpg"
        var path = filesDir.absolutePath
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


    fun addView(content: String) {
        var textView: DragTextView = DragTextView(this)
        textView.text = etEditor?.text
        textView.setTextColor(Color.parseColor("#444444"))
        textView.textSize = 24f
        textView.background = resources.getDrawable(R.drawable.red_dash_border)
        textView.setPadding(30, 20, 30, 20)

        var id = RandomIDUtil.randomString(10)
        textView.tag = id

        textView.setDragTextViewListener(object : MyDragTextViewListener {
            override fun onRemove(textView: DragTextView?) {
            }

            override fun onClick(textView: DragTextView?) {
                Log.e("xxx", "click")
            }

            override fun onDown(textView: DragTextView?) {
                Log.e("xxx", "down")
                mDragTextViewList[mSelectedTextId]?.focus(false)
                mSelectedTextId = textView?.tag.toString()
                textView?.focus(true)
            }

            override fun onUp(textView: DragTextView?) {
                Log.e("xxx", "up")
                Log.e("xxx", textView?.x.toString())
                Log.e("xxx", textView?.y.toString())

                updatedLayoutParams(textView!!, textView.x, textView?.y)
            }

        })

        //添加到集合中
        mDragTextViewList?.put(id, textView)

        //添加到视图中
        flTextContainer?.addView(textView, getDefalutLayoutParams())
    }

    private fun getDefalutLayoutParams(): FrameLayout.LayoutParams {
        var layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER

        return layoutParams
    }

    //解决添加新的子控件，原来的旧控件位置又到默认位置了的问题。
    //https://blog.csdn.net/vv0_0vv/article/details/7517790?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_title~default-0-7517790-blog-7826227.pc_relevant_default&spm=1001.2101.3001.4242.1&utm_relevant_index=2
    private fun updatedLayoutParams(textView: DragTextView, x: Float, y: Float) {
        var layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.marginStart = x.toInt()
        layoutParams.topMargin = y.toInt()

        textView.layoutParams = layoutParams
    }

    /**
     * EditText获取焦点并显示软键盘
     */
    fun showSoftInputFromWindow(activity: Activity, editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        //显示软键盘
        val imm: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)
    }

    fun hideSoftInputFromWindow(activity: Activity, editText: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()

        //通知页面又可以出悬浮按钮了
        val intent = Intent()
        intent.action = "FeedbackPageFinish"
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        sendBroadcast(intent, null)
    }
}