package com.fenqile.shakefeedback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Iterator;
import java.util.Vector;


/**
 * 实现画板功能的View
 */
public class HuaBanView extends View {

    /**
     * 缓冲位图
     */
    private Bitmap cacheBitmap;
    /**
     * 缓冲位图的画板
     */
    private Canvas cacheCanvas;
    /**
     * 缓冲画笔
     */
    private Paint paint;
    /**
     * 实际画笔
     */
    private Paint bitmapPaint;
    /**
     * 保存绘制曲线路径
     */
    private Path path;
    /**
     * 画布高
     */
    private int height;
    /**
     * 画布宽
     */
    private int width;

    /**
     * 保存上一次绘制的终点横坐标
     */
    private float pX;
    /**
     * 保存上一次绘制的终点纵坐标
     */
    private float pY;

    /**
     * 画笔初始颜色
     */
    private int paintColor = Color.RED;
    /**
     * 线状状态
     */
    private static Paint.Style paintStyle = Paint.Style.STROKE;
    /**
     * 画笔粗细
     */
    private static int paintWidth = 5;

    private Canvas canvas;

    private Stack<Path> pathList = new Stack<Path>(); //存放画笔路径

    private Stack<Path> removedPathList = new Stack<Path>();//存放撤销的路径


    /**
     * 获取View实际宽高的方法
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        height = h;
        width = w;
        init();
    }

    private void init() {
        cacheBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        cacheCanvas = new Canvas(cacheBitmap);  //保存之前的绘图
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();
        bitmapPaint = new Paint();
        updatePaint();
    }

    private void updatePaint() {
        paint.setColor(paintColor);
        paint.setStyle(paintStyle);
        paint.setStrokeWidth(paintWidth);
    }

    public HuaBanView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HuaBanView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                path = new Path();
                path.moveTo(event.getX(), event.getY());
                pX = event.getX();
                pY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                path.quadTo(pX, pY, (event.getX() + pX) / 2, (event.getY() + pY) / 2);
                pX = event.getX();
                pY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                cacheCanvas.drawPath(path, paint);
                pathList.push(path);
                Log.e("xxx", path.toString());
                Log.e("xxx", pathList.size() + "");
                break;
        }
        invalidate();

        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;

        Log.e("xxx", "canvas = " + canvas.toString());

        Log.e("xxx", "绘制cacheBitmap");
        //将之前的路径绘图绘制进新的画板上
        canvas.drawBitmap(cacheBitmap, 0, 0, bitmapPaint);

        //绘制最新的一个路径
        canvas.drawPath(path, paint);

        Log.e("xxx", "绘制最新一个路径");

    }

    /**
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void undo() {

        if (pathList != null && pathList.size() > 0) {
            //调用初始化画布函数以清空画布
            init();

            //将路径保存列表中的最后一个元素删除 ,并将其保存在路径删除列表中
            Path drawPath = pathList.pop();
            removedPathList.push(drawPath);

            //将路径保存列表中的路径重绘在画布上
            Log.e("xxx-剩余步数", pathList.size() + "");

            for (Path path : pathList.vector) {
                cacheCanvas.drawPath(path, paint);
                this.path = path;
                Log.e("xxx", path.toString());
            }

            invalidate();
        }
    }

    /**
     * 恢复的核心思想就是将撤销的路径保存到另外一个列表里面(栈)，
     * 然后从redo的列表里面取出最顶端对象，
     * 画在画布上面即可
     */
    public void redo() {
        if (removedPathList.size() > 0) {
            //调用初始化画布函数以清空画布
            init();

            //将路径保存列表中的最后一个元素删除 ,并将其保存在路径删除列表中
            Path drawPath = removedPathList.pop();
            pathList.push(drawPath);

            //将路径保存列表中的路径重绘在画布上
            Iterator iter = pathList.vector.iterator();        //重复保存
            while (iter.hasNext()) {
                Path path = (Path) iter.next();
                cacheCanvas.drawPath(path, paint); //https://www.it610.com/article/1294866028412608512.htm
            }
            invalidate();// 刷新
        }
    }

    /**
     * 更新画笔颜色
     */
    public void setColor(int color) {
        paintColor = color;
        updatePaint();
    }

    /**
     * 设置画笔粗细
     */
    public void setPaintWidth(int width) {
        paintWidth = width;
        updatePaint();
    }

    public static final int PEN = 1;
    public static final int PAIL = 2;

    /**
     * 设置画笔样式
     */
    public void setStyle(int style) {
        switch (style) {
            case PEN:
                paintStyle = Paint.Style.STROKE;
                break;
            case PAIL:
                paintStyle = Paint.Style.FILL;
                break;
        }
        updatePaint();
    }

}
