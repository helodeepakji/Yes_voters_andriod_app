package com.yesvoters.android.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class RobotoSemiBoldButton extends AppCompatButton {

    public RobotoSemiBoldButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoSemiBoldButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoSemiBoldButton(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-SemiBold.ttf");
        setTypeface(tf ,0);
    }
}

