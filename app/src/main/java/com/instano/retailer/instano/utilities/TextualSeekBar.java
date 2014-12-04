package com.instano.retailer.instano.utilities;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsSeekBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instano.retailer.instano.R;

/**
 * Created by vedant on 4/12/14.
 */
public class TextualSeekBar extends AbsSeekBar {

    private Drawable mThumb;
    private RelativeLayout.LayoutParams layoutParams;
    private TextualSeekBar bar;
    private TextView textView;
    private Animation animationFadeOut;

    public TextualSeekBar(Context context) {
        this(context, null);
    }

    public TextualSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.seekBarStyle);
    }

    public TextualSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TextualSeekBar(View parent, Context context) {
        super(context);
        animationFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        bar = (TextualSeekBar) parent.findViewById(R.id.seekbar);
        textView = (TextView) parent.findViewById(R.id.text);
        bar.setMax(99);
        bar.setProgress(9);
        bar.setThumb(getResources().getDrawable(
                R.drawable.seek_thumb_normal));
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout (changed, left, top, right, bottom);
        Log.d("SeekBarWithText", "onLayout");
    }

    @Override
    void onProgressRefresh(float scale, boolean fromUser) {
        super.onProgressRefresh(scale, fromUser);

        Log.d("SeekBarWithText", "progress changed");
        animationFadeOut.reset();
        textView.setAlpha((float) 1);
        textView.startAnimation(animationFadeOut);

        updateTextView(getProgress() + 1);
    }

    @Override
    void onStartTrackingTouch() {
        super.onStartTrackingTouch();
    }

    @Override
    void onStopTrackingTouch() {
        super.onStopTrackingTouch();

        textView.setAlpha((float) 0.1);
    }

    private void updateTextView(int dist) {
        layoutParams.addRule(RelativeLayout.ABOVE, bar.getId());
        Rect thumbRect = bar.getSeekBarThumb().getBounds();
        int xOfCenter = thumbRect.centerX();
        int halfWidth = textView.getWidth()/2;
        layoutParams.setMargins(xOfCenter - halfWidth, 0, 0, 0);
        textView.setLayoutParams(layoutParams);
        textView.setText(String.valueOf(dist) + " km");
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumb = thumb;
    }

    public Drawable getSeekBarThumb() {
        return mThumb;
    }
}

