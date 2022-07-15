package com.example.shakefeedback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*


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
    private var mContents: ArrayList<String> = arrayListOf()
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
        flTextContainer = findViewById(R.id.fl_text_container)

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
            Toast.makeText(this, "上传", Toast.LENGTH_SHORT).show()
        }


        flTextContainer?.setOnClickListener {
            mDragTextViewList?.forEach {
                it.value.focus(false)
            }
            mSelectedTextId = ""
        }
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
        sendBroadcast(intent,null)
    }
}