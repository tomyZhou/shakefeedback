package com.fenqile.shakefeedback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.fenqile.shakefeedback.R
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
    private var uploadInfo: UploadInfo? = null


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

        //???????????????????????????
        initUploadInfo()

        backgroundImage = intent.getStringExtra("backgroundImage")

        //????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (backgroundImage != null && backgroundImage!!.isNotEmpty()) {
            var bitmap = BitmapFactory.decodeFile(backgroundImage)
            root?.background = BitmapDrawable(bitmap)
        }

        //?????????????????????
        ivDraw?.setOnClickListener {
            ivDraw?.setImageResource(R.mipmap.draw_able)
            flTextContainer?.isClickable = false
            ivUndo?.visibility = View.VISIBLE
            tvText?.setTextColor(Color.parseColor("#f2f2f2"))
        }

        //?????????????????????
        tvText?.setOnClickListener {
            vTextCover?.visibility = View.VISIBLE
            etEditor?.visibility = View.VISIBLE
            ivUndo?.visibility = View.GONE
            tvFeedback?.visibility = View.GONE
            tvComplete?.visibility = View.VISIBLE
            tvComplete?.text = "??????"
            tvLeftTwo?.visibility = View.VISIBLE
            flTextContainer?.isClickable = true
            tvText?.setTextColor(Color.parseColor("#F84C4B"))

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

    //???????????????????????????Bitmap
    private fun screenShoot(activity: Activity): Bitmap {
        //??????????????????, ??????bitmap
        val dView: View = activity.getWindow().getDecorView()
        dView.isDrawingCacheEnabled = true
        dView.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(dView.drawingCache)


        //??????getDocorView??????????????????????????????????????????????????????
        // ????????????????????????
        val rect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(rect)
        val statusBarHeight: Int = rect.top
        //????????????????????????
        val width: Int = activity.windowManager.getDefaultDisplay().getWidth()
        val height: Int = activity.windowManager.getDefaultDisplay().getHeight()
        //????????????????????????
        return Bitmap.createBitmap(bitmap, 0, statusBarHeight, width, height - statusBarHeight)
    }


    //??????????????????
    /**
     *  retrofit subscribe ??????????????????????????????????????????
     * https://blog.csdn.net/msn465780/article/details/82012692
     *
     * https://blog.csdn.net/Aran_biubiu/article/details/104979672?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-1-104979672-blog-82012692.pc_relevant_multi_platform_whitelistv1&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-1-104979672-blog-82012692.pc_relevant_multi_platform_whitelistv1&utm_relevant_index=1
     *
     */
    fun upload(bitmap: Bitmap) {

        var file = saveFile(bitmap)

        var uploadService = RetrofitManager.getInstance().getRetrofit("http://static.fenqile.com/")
            .create(UploadService::class.java)

        //???java???????????????
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val part: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", file.getName(), requestFile)
        uploadService.upload(part)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ headBean ->
                if (headBean.retcode == 0) {
                    var uploadImageUrl = headBean.domain + headBean.src
                    Log.e("xxx", uploadImageUrl)

                    uploadInfo(uploadImageUrl)
                } else {
                    Toast.makeText(this, headBean.retmsg, Toast.LENGTH_SHORT).show()
                }
            }, { error ->
                Toast.makeText(this, "??????????????????????????????", Toast.LENGTH_SHORT).show()
            })
    }


    //?????????
    private fun uploadInfo(imageUrl: String) {

        var stringBuffer: StringBuffer = StringBuffer()
        mDragTextViewList.forEach {
            stringBuffer.append((it.value as DragTextView).text.toString())
            stringBuffer.append("#")
        }

        RetrofitManager.getInstance().getRetrofit("https://paya.fenqile.com/")
            .create(UploadService::class.java)
            .uploadInfo(
                imageUrl,
                stringBuffer.toString(),
                uploadInfo?.did,
                uploadInfo?.uid,
                uploadInfo?.os,
                uploadInfo?.osversion,
                uploadInfo?.appversion,
                uploadInfo?.apppkgname,
                uploadInfo?.appname,
                uploadInfo?.brand,
                uploadInfo?.mobile
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ headBean ->
                if (headBean.retcode == 0) {
                    Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show()

                    var uploadImageUrl = headBean.domain + headBean.src
                    Log.e("xxx", uploadImageUrl)

                    finish()
                } else {
                    Toast.makeText(this, headBean.retmsg, Toast.LENGTH_SHORT).show()
                }
            }, { error ->
                Toast.makeText(this, "??????????????????????????????", Toast.LENGTH_SHORT).show()
            })
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
        textView.setTextColor(Color.parseColor("#F84C4B"))
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

        //??????????????????
        mDragTextViewList?.put(id, textView)

        //??????????????????
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

    //???????????????????????????????????????????????????????????????????????????????????????
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
     * EditText??????????????????????????????
     */
    fun showSoftInputFromWindow(activity: Activity, editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        //???????????????
        val imm: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, 0)
    }

    fun hideSoftInputFromWindow(activity: Activity, editText: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun initUploadInfo() {
        uploadInfo = UploadInfo()

        uploadInfo?.did = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        uploadInfo?.uid = "3087863" //TODO ??????????????????????????????id
        uploadInfo?.os = "android"
        uploadInfo?.osversion = Build.VERSION.RELEASE
        uploadInfo?.appversion = UploadInfoManger.getVersionName(this)
        uploadInfo?.apppkgname = packageName
        uploadInfo?.appname = "??????"
        uploadInfo?.brand = Build.BRAND + "/"+ Build.MODEL
        uploadInfo?.mobile = "12311111111"   //TODO ??????????????????????????????mobile
    }

    override fun onDestroy() {
        super.onDestroy()

        //???????????????????????????????????????
        val intent = Intent()
        intent.action = "FeedbackPageFinish"
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        sendBroadcast(intent, null)
    }
}