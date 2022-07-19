package com.fenqile.shakefeedback;


import com.fenqile.shakefeedback.floatingview.FloatingMagnetView;


public interface MyDragTextViewListener {

    void onRemove(DragTextView textView);

    void onClick(DragTextView textView);

    void onDown(DragTextView textView); //点下去，就停止计时

    void onUp(DragTextView textView);   //手抬起来，就开始计时
}
