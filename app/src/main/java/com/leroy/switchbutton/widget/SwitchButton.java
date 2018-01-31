package com.leroy.switchbutton.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Checkable;

import com.leroy.switchbutton.R;

/**
 * DayOrNight SwitchButton
 * Created by leroy on 2016/9/6.
 */
public class SwitchButton extends View implements Checkable{
    private static final int ANIMATION_DURATION = 300;

    private static final int DEFAULT_WIDTH = 120;      //width of SwitchButton
    private static final int DEFAULT_HEIGHT = DEFAULT_WIDTH / 2;
    private static final int DEFAULT_SPOT_PADDING = 6;
    private static final int DEFAULT_BORDER_WIDTH = 4;

    private static final int DEFAULT_SWITCH_ON_COLOR = 0xFF9EE3FB;
    private static final int DEFAULT_SWITCH_ON_COLOR_OUT = 0xFF86C3D7;
    private static final int DEFAULT_SWITCH_OFF_COLOR = 0xFF3C4145;
    private static final int DEFAULT_SWITCH_OFF_COLOR_OUT = 0xFF1C1C1C;
    private static final int DEFAULT_SPOT_ON_COLOR = 0xFFE1C348;
    private static final int DEFAULT_SPOT_ON_COLOR_IN = 0xFFFFDF6D;
    private static final int DEFAULT_SPOT_OFF_COLOR = 0xFFE3E7C7;
    private static final int DEFAULT_SPOT_OFF_COLOR_IN = 0xFFFFFFFF;

    private static final int SWITCH_OFF_POS = 0;
    private static final int SWITCH_ON_POS = 1;

    private int switchOnColor;
    private int switchOffColor;
    private int spotOnColor;
    private int spotOnColorIn;
    private int spotOffColor;
    private int spotOffColorIn;
    private int switchOnStrokeColor;
    private int switchOffStrokeColor;
    private int spotPadding;
    private float currentPos;
    private boolean mChecked;
    private boolean mBroadcasting;
    private boolean isMoving;
    private int duration;

    private OnCheckedChangeListener onCheckedChangeListener;

    private ValueAnimator valueAnimator;

    private enum State {
        SWITCH_ANIMATION_OFF, SWITCH_ANIMATION_ON, SWITCH_ON, SWITCH_OFF
    }

    private State state;

    public SwitchButton(Context context) {
        super(context);
        switchOnColor = DEFAULT_SWITCH_ON_COLOR;
        switchOffColor = DEFAULT_SWITCH_OFF_COLOR;
        spotOnColor = DEFAULT_SPOT_ON_COLOR;
        spotOnColorIn = DEFAULT_SPOT_ON_COLOR_IN;
        spotOffColor = DEFAULT_SPOT_OFF_COLOR;
        spotOffColorIn = DEFAULT_SPOT_OFF_COLOR_IN;
        spotPadding = dp2px(DEFAULT_SPOT_PADDING);
        switchOnStrokeColor = switchOnColor;
        switchOffStrokeColor = switchOffColor;
        duration = ANIMATION_DURATION;
        state = mChecked ? State.SWITCH_ON : State.SWITCH_OFF;

        setClickable(true);
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Switch);
        switchOnColor = a.getColor(R.styleable.Switch_switchOnColor, DEFAULT_SWITCH_ON_COLOR);
        switchOffColor = a.getColor(R.styleable.Switch_switchOffColor, DEFAULT_SWITCH_OFF_COLOR);
        spotOnColor = a.getColor(R.styleable.Switch_spotOnColor, DEFAULT_SPOT_ON_COLOR);
        spotOnColorIn = a.getColor(R.styleable.Switch_spotOnColor, DEFAULT_SPOT_ON_COLOR_IN);
        spotOffColor = a.getColor(R.styleable.Switch_spotOffColor, DEFAULT_SPOT_OFF_COLOR);
        spotOffColorIn = a.getColor(R.styleable.Switch_spotOnColor, DEFAULT_SPOT_OFF_COLOR_IN);
        spotPadding = a.getDimensionPixelSize(R.styleable.Switch_spotPadding, dp2px(DEFAULT_SPOT_PADDING));
        switchOnStrokeColor = a.getColor(R.styleable.Switch_switchOnStrokeColor, switchOnColor);
        switchOffStrokeColor = a.getColor(R.styleable.Switch_switchOffStrokeColor, switchOffColor);
        duration = a.getInteger(R.styleable.Switch_duration, ANIMATION_DURATION);
        mChecked = a.getBoolean(R.styleable.Switch_checked, false);
        a.recycle();

        state = mChecked ? State.SWITCH_ON : State.SWITCH_OFF;
        setClickable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = dp2px(DEFAULT_WIDTH) + getPaddingLeft() + getPaddingRight();
        int height = dp2px(DEFAULT_HEIGHT) + getPaddingTop() + getPaddingBottom();

        if (widthSpecMode != MeasureSpec.AT_MOST) {
            width = Math.max(width, widthSpecSize);
        }

        if (heightSpecMode != MeasureSpec.AT_MOST) {
            height = Math.max(height, heightSpecSize);
        }

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        int pl = getPaddingLeft();
        int pt = getPaddingTop();
        int pr = getPaddingRight();
        int pb = getPaddingBottom();
        int wp = w - pl - pr;
        int hp = h - pt - pb;
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);

        int dx = pl + (wp - sw) / 2;
        int dy = pt + (hp - sh) / 2;
        canvas.translate(dx, dy);

        switch (state) {
            case SWITCH_ON:
                drawSwitchOn(canvas);
                break;
            case SWITCH_OFF:
                drawSwitchOff(canvas);
                break;
            case SWITCH_ANIMATION_ON:
                drawSwitchOnAnim(canvas);
                break;
            case SWITCH_ANIMATION_OFF:
                drawSwitchOffAnim(canvas);
                break;
        }
    }

    private void drawSwitchOn(Canvas canvas) {
        float[] rectAttrs = compRoundRectAttr(SWITCH_OFF_POS);
        drawRoundRect(canvas, switchOnColor, rectAttrs);

        float[] ovalAttrs = compOvalAttr(SWITCH_ON_POS);
        drawOval(canvas, spotOnColor, ovalAttrs);
        drawOvalIn(canvas, spotOnColorIn, ovalAttrs);
        drawCloud(canvas, 1);

        drawRoundRectStroke(canvas, DEFAULT_SWITCH_ON_COLOR_OUT);
    }

    private void drawSwitchOff(Canvas canvas) {
        float[] rectAttrs = compRoundRectAttr(SWITCH_OFF_POS);
        drawRoundRect(canvas, switchOffColor, rectAttrs);

        float[] ovalAttrs = compOvalAttr(SWITCH_OFF_POS);
        drawOval(canvas, spotOffColor,  ovalAttrs);
        drawOvalIn(canvas, spotOffColorIn, ovalAttrs);
        drawCircleDot(canvas, spotOffColor, spotOffColorIn, 1, ovalAttrs);
        drawCircleDot2(canvas, spotOffColor, spotOffColorIn, 1, ovalAttrs);
        drawCircleDot3(canvas, spotOffColor, spotOffColorIn, 1, ovalAttrs);
        drawStar(canvas, DEFAULT_SPOT_OFF_COLOR_IN, 1);

        drawRoundRectStroke(canvas, DEFAULT_SWITCH_OFF_COLOR_OUT);
    }

    private void drawSwitchOnAnim(Canvas canvas) {
        float[] rectAttrs = compRoundRectAttr(SWITCH_OFF_POS);
        drawRoundRect(canvas, switchOnColor, rectAttrs);

//        rectAttrs = compRoundRectAttr(currentPos);    fix drawRoundRect issue  by lgyjg
//        drawRoundRect(canvas, switchOffColor, rectAttrs);

        float[] ovalShadeOnAttrs = compRoundRectShadeOnAttr(currentPos * 3/2);
        float[] ovalAttrs = compOvalAttr(currentPos* 3/2);
        int color = compColor(currentPos, DEFAULT_SPOT_OFF_COLOR, DEFAULT_SPOT_ON_COLOR);
        int colorIn = compColor(currentPos, DEFAULT_SPOT_OFF_COLOR_IN, DEFAULT_SPOT_ON_COLOR_IN);
        drawRoundRect(canvas, color, ovalShadeOnAttrs);
        drawOval(canvas, color, ovalAttrs);
        drawOvalIn(canvas, colorIn, ovalAttrs);
        if(currentPos > 0.6) {
            drawCloud(canvas, currentPos);
        }

        int strokeColor = compColor(currentPos, DEFAULT_SWITCH_OFF_COLOR_OUT, DEFAULT_SWITCH_ON_COLOR_OUT);
        drawRoundRectStroke(canvas, strokeColor);
    }

    private void drawSwitchOffAnim(Canvas canvas) {
        float[] rectAttrs = compRoundRectAttr(SWITCH_OFF_POS);
        if (currentPos != 1) {
            drawRoundRect(canvas, switchOffColor, rectAttrs);
        }

//        rectAttrs = compRoundRectAttr(1 - currentPos);
        drawRoundRect(canvas, switchOffColor, rectAttrs);

        float[] ovalAttrs;
        if(currentPos > 2.0/3){
            ovalAttrs = compOvalAttr(0);
        }else{
            ovalAttrs = compOvalAttr(1 - currentPos * 3/2);
        }
        float[] ovalShadeOffAttrs = compRoundRectShadeOffAttr(1 - currentPos * 3/2);
        int color = compColor(currentPos, DEFAULT_SPOT_ON_COLOR, DEFAULT_SPOT_OFF_COLOR);
        int colorIn = compColor(currentPos, DEFAULT_SPOT_ON_COLOR_IN, DEFAULT_SPOT_OFF_COLOR_IN);
        drawRoundRect(canvas, color, ovalShadeOffAttrs);
        drawOval(canvas, color, ovalAttrs);
        drawOvalIn(canvas, colorIn, ovalAttrs);
        if(currentPos > 2.0/3){
            drawCircleDot(canvas, DEFAULT_SPOT_OFF_COLOR, DEFAULT_SPOT_OFF_COLOR_IN, 1, ovalAttrs);
            drawCircleDot2(canvas, DEFAULT_SPOT_OFF_COLOR, DEFAULT_SPOT_OFF_COLOR_IN, 1, ovalAttrs);
            drawCircleDot3(canvas, DEFAULT_SPOT_OFF_COLOR, DEFAULT_SPOT_OFF_COLOR_IN, 1, ovalAttrs);
        }else{
            drawCircleDot(canvas, DEFAULT_SPOT_OFF_COLOR, DEFAULT_SPOT_OFF_COLOR_IN, currentPos * 3/2, ovalAttrs);
            drawCircleDot2(canvas, DEFAULT_SPOT_OFF_COLOR, DEFAULT_SPOT_OFF_COLOR_IN, currentPos * 3/2, ovalAttrs);
            drawCircleDot3(canvas, DEFAULT_SPOT_OFF_COLOR, DEFAULT_SPOT_OFF_COLOR_IN, currentPos * 3/2, ovalAttrs);
        }
        if(currentPos > 0.6) {
            drawStar(canvas, DEFAULT_SPOT_OFF_COLOR_IN, currentPos);
        }

        int strokeColor = compColor(currentPos, DEFAULT_SWITCH_ON_COLOR_OUT, DEFAULT_SWITCH_OFF_COLOR_OUT);
        drawRoundRectStroke(canvas, strokeColor);
    }

    private void drawRoundRect(Canvas canvas, int color, float[] attrs) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
        RectF rectF = new RectF();
        paint.setColor(color);
        rectF.set(attrs[0], attrs[1], attrs[2], attrs[3]);
        canvas.drawRoundRect(rectF, attrs[4], attrs[4], paint);
    }

    private void drawRoundRectStroke(Canvas canvas, int color) {
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);

        float left = dp2pxFloat((float) 2.4);
        float right = sw - left;
        float top = dp2pxFloat((float) 2.4);
        float bottom = sh - top;
        float radius = (bottom - top) * 0.5f;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(dp2pxFloat((float) 3.6));
        RectF rectF = new RectF();
        rectF.set(left, top, right, bottom);
        canvas.drawRoundRect(rectF, radius, radius, paint);
    }

    private void drawOvalIn(Canvas canvas, int color, float[] attrs) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        int borderWidth = dp2px(DEFAULT_BORDER_WIDTH);
        RectF rectFIn = new RectF(attrs[0] + borderWidth, attrs[1] + borderWidth, attrs[2] - borderWidth, attrs[3] - borderWidth);
        canvas.drawOval(rectFIn, paint);
    }

    private void drawOval(Canvas canvas, int color, float[] attrs) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        RectF rectF = new RectF(attrs[0], attrs[1], attrs[2], attrs[3]);
        canvas.drawOval(rectF, paint);
    }

    private void drawCircleDot(Canvas canvas, int color,int colorIn, float pos, float[] attrs) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        float rad = attrs[2] - dp2px(9) - (attrs[0] + attrs[2])/2;
        float x = attrs[2] - dp2px(9) - rad + (float)(rad * Math.cos(pos * Math.PI/3));
        float y = (attrs[1] + attrs[3])/2 - (float)(rad * Math.sin(pos * Math.PI/3));
        paint.setColor(color);
        RectF rectF = new RectF(x - dp2px(7), y - dp2px(7), x + dp2px(7), y + dp2px(7));
        canvas.drawOval(rectF, paint);
        paint.setColor(colorIn);
        RectF rectFIn = new RectF(x - dp2px(3), y - dp2px(3), x + dp2px(3), y + dp2px(3));
        canvas.drawOval(rectFIn, paint);
    }

    private void drawCircleDot2(Canvas canvas, int color,int colorIn, float pos, float[] attrs) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        float rad = attrs[2] - dp2px(7) - (attrs[0] + attrs[2])/2;
        float x = attrs[2] - dp2px(7) - rad + (float)(rad * Math.cos(Math.PI * 5 /12 + pos * Math.PI* 5 /12));
        float y = (attrs[1] + attrs[3])/2 - (float)(rad * Math.sin(Math.PI * 5 /12 + pos * Math.PI* 5 /12));
        paint.setColor(color);
        RectF rectF = new RectF(x - dp2px(5), y - dp2px(5), x + dp2px(5), y + dp2px(5));
        canvas.drawOval(rectF, paint);
        paint.setColor(colorIn);
        RectF rectFIn = new RectF(x - dp2px(1), y - dp2px(1), x + dp2px(1), y + dp2px(1));
        canvas.drawOval(rectFIn, paint);
    }

    private void drawCircleDot3(Canvas canvas, int color,int colorIn, float pos, float[] attrs) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        float rad = attrs[2] - dp2px(9) - (attrs[0] + attrs[2])/2;
        float x = attrs[2] - dp2px(9) - rad + (float)(rad * Math.cos(Math.PI * 16 /12 + pos * Math.PI* 5 /12));
        float y = (attrs[1] + attrs[3])/2 - (float)(rad * Math.sin(Math.PI * 16 /12 + pos * Math.PI* 5 /12));
        paint.setColor(color);
        RectF rectF = new RectF(x - dp2px(5), y - dp2px(5), x + dp2px(5), y + dp2px(5));
        canvas.drawOval(rectF, paint);
        paint.setColor(colorIn);
        RectF rectFIn = new RectF(x - dp2px(1), y - dp2px(1), x + dp2px(1), y + dp2px(1));
        canvas.drawOval(rectFIn, paint);
    }

    private void drawCloud(Canvas canvas, float pos) {
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        Bitmap cloudBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.cloud)).getBitmap();
        int cloudWidth = cloudBitmap.getWidth();
        int cloudHeight = cloudBitmap.getHeight();
        Rect mSrcRect = new Rect(0, 0, cloudWidth, cloudHeight);
        RectF mDestRect;
        if(pos <= 0.9){
            float t = pos*10 - 6;
            mDestRect = new RectF(sw/2 - dp2px(18) - dp2px(t), sh/2 - dp2px(4) - dp2px(t), sw/2 + dp2px(18) + dp2px(t), sh/2 + dp2px(20) + dp2px(t));
        }else{
            float t = 2*(pos*10 - 9);
            mDestRect = new RectF(sw/2 - dp2px(22) + dp2px(t), sh/2 - dp2px(8) + dp2px(t), sw/2 + dp2px(22) - dp2px(t), sh/2 + dp2px(24) - dp2px(t));
        }
        canvas.drawBitmap(cloudBitmap, mSrcRect, mDestRect, paint);
    }

    private void drawStar(Canvas canvas, int color, float pos) {
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);

        float stars[][] = new float[7][2];
        stars[0][0] = (float) (sw/2.0);
        stars[0][1] = (float) (sh/5.0);

        stars[1][0] = (float) (sw * 3/4.0);
        stars[1][1] = (float) (sh/5.0);

        stars[2][0] = (float) (sw * 5/8.0);
        stars[2][1] = (float) (sh * 2/5.0);

        stars[3][0] = (float) (sw * 27/40.0);
        stars[3][1] = (float) (sh * 3/5.0);

        stars[4][0] = (float) (sw * 5/6.0);
        stars[4][1] = (float) (sh * 9/20.0);

        stars[5][0] = (float) (sw * 4/5.0);
        stars[5][1] = (float) (sh * 7/10.0);

        stars[6][0] = (float) (sw * 11/20.0);
        stars[6][1] = (float) (sh * 3/4.0);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        float t = 10 * pos - 6;
        if(pos > 0.8){
            t = 10 - 10 * pos;
        }
        canvas.drawCircle(stars[0][0], stars[0][1], 6 + 2*t, paint);
        canvas.drawCircle(stars[1][0], stars[1][1], 5 + 2*t, paint);
        canvas.drawCircle(stars[2][0], stars[2][1], 5 + 2*t, paint);
        canvas.drawCircle(stars[3][0], stars[3][1], 4 + 2*t, paint);
        canvas.drawCircle(stars[4][0], stars[4][1], 8 - 2*t, paint);
        canvas.drawCircle(stars[5][0], stars[5][1], 7 - 2*t, paint);
        canvas.drawCircle(stars[6][0], stars[6][1], 7 - 2*t, paint);
    }

    private float[] compRoundRectAttr(float pos) {
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);

        float left = sw * pos;
        float right = sw - left;
        float top = sh * pos;
        float bottom = sh - top;
        float radius = (bottom - top) * 0.5f;

        return new float[]{left, top, right, bottom, radius};
    }

    private float[] compRoundRectShadeOnAttr(float pos) {
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);
        int oh = sh - 2 * spotPadding;
        float left, right, top, bottom;
        if(pos < 0.35){
            left = 0;
            right = spotPadding + (sw - sh) * pos + oh;
            top = spotPadding;
            bottom = oh + top;
        }else{
            left = spotPadding + (sw - sh) * pos *2/3;
            right = spotPadding + (sw - sh) * pos *2/3+ oh;
            top = spotPadding;
            bottom = oh + top;
        }
        float radius = (bottom - top) * 0.5f;
        return new float[]{left, top, right, bottom, radius};
    }

    private float[] compRoundRectShadeOffAttr(float pos) {
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);
        int oh = sh - 2 * spotPadding;
        float left, right, top, bottom;

        if(pos > 0.65){
            left = spotPadding + (sw - sh) * pos;
            right = sw - spotPadding;
            top = spotPadding;
            bottom = oh + top;
        }else{
            left = spotPadding + (sw - sh) * (2*pos + 1)/3;
            right = spotPadding + (sw - sh) * (2*pos + 1)/3 + oh;
            top = spotPadding;
            bottom = oh + top;
        }
        float radius = (bottom - top) * 0.5f;
        return new float[]{left, top, right, bottom, radius};
    }

    private float[] compOvalAttr(float pos) {
        if(pos > 1){
            pos = 1;
        }
        int sw = dp2px(DEFAULT_WIDTH);
        int sh = dp2px(DEFAULT_HEIGHT);
        int oh = sh - 2 * spotPadding;

        float left = spotPadding + (sw - sh) * pos;
        float right = left + oh;
        float top = spotPadding;
        float bottom = oh + top;

        return new float[]{left, top, right, bottom};
    }

    private int compColor(float fraction, int startColor, int endColor) {
        return (Integer) new ArgbEvaluator().evaluate(fraction, startColor, endColor);
    }

    @Override
    public boolean performClick() {
        toggle();

        final boolean handled = super.performClick();
        if (!handled) {
            // View only makes a sound effect if the onClickListener was
            // called, so we'll need to make one here instead.
            playSoundEffect(SoundEffectConstants.CLICK);
        }

        return handled;
    }

    public int dp2px(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public float dp2pxFloat(float dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void setChecked(boolean checked) {
        if (isMoving) {
            return;
        }

        if (mChecked != checked) {
            mChecked = checked;

            // Avoid infinite recursions if setChecked() is called from a listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(this, mChecked);
            }
            mBroadcasting = false;

            if (mChecked) {
                state = State.SWITCH_ANIMATION_ON;
            } else {
                state = State.SWITCH_ANIMATION_OFF;
            }

            if (isAttachedToWindow() && isLaidOut()) {
                animateToCheckedState();
            } else {
                // Immediately move the thumb to the new position.
                cancelPositionAnimator();
                currentPos = 0;
            }
        }
    }

    private void cancelPositionAnimator() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    private void animateToCheckedState() {
        valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentPos = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isMoving = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isMoving = false;
            }
        });

        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
            currentPos = 0;
        }
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    public int getSwitchOnColor() {
        return switchOnColor;
    }

    public void setSwitchOnColor(@ColorInt int switchOnColor) {
        this.switchOnColor = switchOnColor;
        invalidate();
    }

    public int getSwitchOffColor() {
        return switchOffColor;
    }

    public void setSwitchOffColor(@ColorInt int switchOffColor) {
        this.switchOffColor = switchOffColor;
        invalidate();
    }

    public int getSpotOnColor() {
        return spotOnColor;
    }

    public void setSpotOnColor(@ColorInt int spotOnColor) {
        this.spotOnColor = spotOnColor;
        invalidate();
    }

    public int getSpotOffColor() {
        return spotOffColor;
    }

    public void setSpotOffColor(@ColorInt int spotOffColor) {
        this.spotOffColor = spotOffColor;
        invalidate();
    }

    public int getSpotPadding() {
        return spotPadding;
    }

    public void setSpotPadding(int spotPadding) {
        this.spotPadding = spotPadding;
        invalidate();
    }

    public int getSwitchOffStrokeColor() {
        return switchOffStrokeColor;
    }

    public void setSwitchOffStrokeColor(int switchOffStrokeColor) {
        this.switchOffStrokeColor = switchOffStrokeColor;
        invalidate();
    }

    public int getSwitchOnStrokeColor() {
        return switchOnStrokeColor;
    }

    public void setSwitchOnStrokeColor(int switchOnStrokeColor) {
        this.switchOnStrokeColor = switchOnStrokeColor;
        invalidate();
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a switch has changed.
         *
         * @param s         The switch whose state has changed.
         * @param isChecked The new checked state of switch.
         */
        void onCheckedChanged(SwitchButton s, boolean isChecked);
    }
}
