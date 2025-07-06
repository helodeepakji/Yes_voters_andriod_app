package com.yesvoters.android.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class RobotoMediumButton extends AppCompatButton {

    public RobotoMediumButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoMediumButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoMediumButton(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Medium.ttf");
        setTypeface(tf ,0);
    }
}

