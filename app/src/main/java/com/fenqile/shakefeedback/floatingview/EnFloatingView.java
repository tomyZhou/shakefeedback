package com.fenqile.shakefeedback.floatingview;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.fenqile.shakefeedback.R;


/**
 * @ClassName EnFloatingView
 * @Description 悬浮窗
 * @Author Yunpeng Li
 * @Creation 2018/3/15 下午5:04
 * @Mender Yunpeng Li
 * @Modification 2018/3/15 下午5:04
 */
public class EnFloatingView extends FloatingMagnetView {

    private final ImageView mIcon;

    public EnFloatingView(@NonNull Context context) {
        this(context, R.layout.en_floating_view);
    }

    public EnFloatingView(@NonNull Context context, @LayoutRes int resource) {
        super(context, null);
        inflate(context, resource, this);
        mIcon = findViewById(R.id.iv_feedback);
    }

    public void setIconImage(@DrawableRes int resId){
        mIcon.setImageResource(resId);
    }

    public void setIconBitmap(Bitmap bitmap){
        mIcon.setImageBitmap(bitmap);
    }

}
