package com.yesvoters.android.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class RobotoRegularButton extends AppCompatButton {

    public RobotoRegularButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoRegularButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoRegularButton(Context context) {
        super(context);
        init();
    }

    public void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
        setTypeface(tf ,0);
    }
}

