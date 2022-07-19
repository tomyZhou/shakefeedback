package com.fenqile.shakefeedback;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatTextView;

import com.fenqile.shakefeedback.floatingview.MagnetViewListener;
import com.fenqile.shakefeedback.floatingview.utils.SystemUtils;


public class DragTextView extends AppCompatTextView {

    private float originX;
    private float originY;

    private float moveX;
    private float moveY;

    private float mScreenWidth = 720;
    private float mScreenHeight = 1280;

    private MyDragTextViewListener mDragTextViewListener;
    private static final int TOUCH_TIME_THRESHOLD = 150;
    private long mLastTouchDownTime;

    private boolean isFocus = true;

    public DragTextView(Context context) {
        this(context, null);
    }

    public DragTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        setClickable(true);
        Log.e("xxx", "init");
        mScreenWidth = (SystemUtils.getScreenWidth(getContext()) - this.getWidth());
        mScreenHeight = SystemUtils.getScreenHeight(getContext());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e("xxx-down", event.getRawX() + "");

                //记录下当前按下点的坐标相对父容器的位置
                originY = event.getY();
                originX = event.getX();
                mLastTouchDownTime = System.currentTimeMillis();

                dealDownEvent();
                break;
            case MotionEvent.ACTION_MOVE:
                //在Move事件里面，拿到现在的Y坐标减去按下时候的坐标，就能计算出当前View应该移动的距离。

                float y = event.getY();
                float x = event.getX();

                moveX = x - originX;
                moveY = y - originY;

                Log.e("xxx-move", x + ":" + y);

                //view移动前的上下左右位置
                float left = getLeft() + moveX;
                float top = getTop() + moveY;
                float right = getRight() + moveX;
                float bottom = getBottom() + moveY;


                if ((left) < 0) {
                    left = 0;
                    right = getWidth();
                }

                if (right > mScreenWidth) {
                    right = mScreenWidth;
                    left = mScreenWidth - getWidth();
                }

                if (top < 0) {
                    top = 0;
                    bottom = getHeight();
                }
                if (bottom > mScreenHeight) {
                    bottom = mScreenHeight;
                    top = bottom - getHeight();
                }

                //textview 内部的移动要用layout，不能用setX,setY
                layout((int) left, (int) top, (int) right, (int) bottom);
                break;
            case MotionEvent.ACTION_UP:

                if (isOnClickEvent()) {
                    dealClickEvent();
                } else {
                    dealUpEvent();
                }
                break;
        }
        return true;
    }

    public void setDragTextViewListener(MyDragTextViewListener dragTextViewListener) {
        this.mDragTextViewListener = dragTextViewListener;
    }

    protected void dealUpEvent() {
        if (mDragTextViewListener != null) {
            mDragTextViewListener.onUp(this);
        }
    }

    protected void dealClickEvent() {
        if (mDragTextViewListener != null) {
            mDragTextViewListener.onClick(this);
        }
    }

    protected void dealDownEvent() {
        if (mDragTextViewListener != null) {
            mDragTextViewListener.onDown(this);
        }
    }

    protected boolean isOnClickEvent() {
        return System.currentTimeMillis() - mLastTouchDownTime < TOUCH_TIME_THRESHOLD;
    }

    public void focus(boolean isFocus) {
        this.isFocus = isFocus;
        if (!isFocus) {
            setBackgroundColor(Color.parseColor("#00ffffff"));
        } else {
            setBackground(getResources().getDrawable(R.drawable.red_dash_border));
        }
    }

}
