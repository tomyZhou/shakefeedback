package com.fenqile.shakefeedback.floatingview;


/**
 * Created by liyunpeng on 17/11/29.
 */
public interface MagnetViewListener {

    void onRemove(FloatingMagnetView magnetView);

    void onClick(FloatingMagnetView magnetView);

    void onDown(FloatingMagnetView magnetView); //点下去，就停止计时

    void onUp(FloatingMagnetView magnetView);   //手抬起来，就开始计时
}
