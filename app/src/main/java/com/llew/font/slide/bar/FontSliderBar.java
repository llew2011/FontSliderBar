package com.llew.font.slide.bar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 仿iOS的UISliderBar控件
 * <br/><br/>
 *
 * @author llew
 * @date 2017/12/16
 */

public class FontSliderBar extends View {

    private static final String TAG = "SliderBar";

    private static final int   DEFAULT_TICK_COUNT        = 3;
    private static final float DEFAULT_TICK_HEIGHT       = 24;

    private static final float DEFAULT_BAR_WIDTH         = 3;
    private static final int   DEFAULT_BAR_COLOR         = Color.LTGRAY;

    private static final int DEFAULT_TEXT_SIZE           = 16;
    private static final int DEFAULT_TEXT_COLOR          = Color.LTGRAY;
    private static final int DEFAULT_TEXT_PADDING        = 20;

    private static final float DEFAULT_THUMB_RADIUS      = 20;
    private static final int DEFAULT_THUMB_COLOR_NORMAL  = 0xff33b5e5;
    private static final int DEFAULT_THUMB_COLOR_PRESSED = 0xff33b5e5;

    private int mTickCount = DEFAULT_TICK_COUNT;
    private float mTickHeight = DEFAULT_TICK_HEIGHT;

    private float mBarWidth = DEFAULT_BAR_WIDTH;
    private int mBarColor = DEFAULT_BAR_COLOR;

    private float mThumbRadius = DEFAULT_THUMB_RADIUS;
    private int mThumbColorNormal = DEFAULT_THUMB_COLOR_NORMAL;
    private int mThumbColorPressed = DEFAULT_THUMB_COLOR_PRESSED;

    private int mTextSize = DEFAULT_TEXT_SIZE;
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mTextPadding = DEFAULT_TEXT_PADDING;

    private int mDefaultWidth = 500;

    private int mCurrentIndex = 0;
    private boolean mAnimation = true;

    private Thumb mThumb;
    private Bar mBar;

    private ValueAnimator mAnimator;
    private FontSliderBar.OnSliderBarChangeListener mListener;

    public FontSliderBar(Context context) {
        super(context);
    }

    public FontSliderBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FontSliderBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width;
        int height;

        final int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measureHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (measureWidthMode == MeasureSpec.AT_MOST) {
            width = measureWidth;
        } else if (measureWidthMode == MeasureSpec.EXACTLY) {
            width = measureWidth;
        } else {
            width = mDefaultWidth;
        }

        if (measureHeightMode == MeasureSpec.AT_MOST) {
            height = Math.min(getMinHeight(), measureHeight);
        } else if (measureHeightMode == MeasureSpec.EXACTLY) {
            height = measureHeight;
        } else {
            height = getMinHeight();
        }
        setMeasuredDimension(width, height);
    }

    private int getMinHeight() {
        final float f = getFontHeight();
        return (int) (f + mTextPadding + mThumbRadius * 2);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createBar();
        createThumbs();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBar.draw(canvas);
        mThumb.draw(canvas);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (VISIBLE != visibility) {
            stopAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        destroyResources();
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || isAnimationRunning()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return onActionDown(event.getX(), event.getY());
            case MotionEvent.ACTION_MOVE:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                return onActionMove(event.getX());
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                return onActionUp(event.getX(), event.getY());
            default:
                return true;
        }
    }

    public FontSliderBar setOnSliderBarChangeListener(FontSliderBar.OnSliderBarChangeListener listener) {
        mListener = listener;
        return FontSliderBar.this;
    }

    public FontSliderBar setTickCount(int tickCount) {
        if (isValidTickCount(tickCount)) {
            mTickCount = tickCount;
        } else {
            Log.e(TAG, "tickCount less than 2; invalid tickCount.");
            throw new IllegalArgumentException("tickCount less than 2; invalid tickCount.");
        }
        return FontSliderBar.this;
    }

    public FontSliderBar setTickHeight(float tickHeight) {
        mTickHeight = tickHeight;
        return FontSliderBar.this;
    }

    public FontSliderBar setBarWeight(float barWeight) {
        mBarWidth = barWeight;
        return FontSliderBar.this;
    }

    public FontSliderBar setBarColor(int barColor) {
        mBarColor = barColor;
        return FontSliderBar.this;
    }

    public FontSliderBar setTextSize(int textSize) {
        mTextSize = textSize;
        return FontSliderBar.this;
    }

    public FontSliderBar setTextColor(int textColor) {
        mTextColor = textColor;
        return FontSliderBar.this;
    }

    public FontSliderBar setTextPadding(int textPadding) {
        mTextPadding = textPadding;
        return FontSliderBar.this;
    }

    public FontSliderBar setThumbRadius(float thumbRadius) {
        mThumbRadius = thumbRadius;
        return FontSliderBar.this;
    }

    public FontSliderBar setThumbColorNormal(int thumbColorNormal) {
        mThumbColorNormal = thumbColorNormal;
        return FontSliderBar.this;
    }

    public FontSliderBar setThumbColorPressed(int thumbColorPressed) {
        mThumbColorPressed = thumbColorPressed;
        return FontSliderBar.this;
    }

    public FontSliderBar setThumbIndex(int currentIndex) {
        if (indexOutOfRange(currentIndex)) {
            throw new IllegalArgumentException(
                    "A thumb index is out of bounds. Check that it is between 0 and mTickCount - 1");
        } else {
            if (mCurrentIndex != currentIndex) {
                mCurrentIndex = currentIndex;
            }
        }
        return FontSliderBar.this;
    }

    public FontSliderBar withAnimation(boolean animation) {
        mAnimation = animation;
        return FontSliderBar.this;
    }

    public void applay() {
        createThumbs();
        createBar();
        requestLayout();
        invalidate();
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    private void createBar() {
        mBar = new Bar(getXCoordinate(), getYCoordinate(), getBarLength(), mTickCount, mTickHeight, mBarWidth,
                mBarColor, mTextColor, mTextSize, mTextPadding);
    }

    private void createThumbs() {
        float xCoordinate = getBarLength() / (mTickCount - 1) * mCurrentIndex + getXCoordinate();
        mThumb = new Thumb(xCoordinate, getYCoordinate(), mThumbColorNormal, mThumbColorPressed, mThumbRadius);
    }

    private float getXCoordinate() {
        return mThumbRadius;
    }

    private float getYCoordinate() {
        return getHeight() - mThumbRadius;
    }

    private float getFontHeight() {
        Paint paint = new Paint();
        paint.setTextSize(mTextSize);
        paint.measureText("大");
        FontMetrics fontMetrics = paint.getFontMetrics();
        float f = fontMetrics.descent - fontMetrics.ascent;
        return f;
    }

    private float getBarLength() {
        return getWidth() - 2 * getXCoordinate();
    }

    private boolean indexOutOfRange(int thumbIndex) {
        return (thumbIndex < 0 || thumbIndex >= mTickCount);
    }

    private boolean isValidTickCount(int tickCount) {
        return tickCount > 1;
    }

    private boolean onActionDown(float x, float y) {
        if (!mThumb.isPressed() && mThumb.isInTargetZone(x, y)) {
            pressThumb(mThumb);
        }
        return true;
    }

    private boolean onActionMove(float x) {
        if (mThumb.isPressed()) {
            moveThumb(mThumb, x);
        }
        return true;
    }

    private boolean onActionUp(float x, float y) {
        if (mThumb.isPressed()) {
            releaseThumb(mThumb);
        }
        return true;
    }

    private void pressThumb(Thumb thumb) {
        thumb.press();
        invalidate();
    }

    private void releaseThumb(final Thumb thumb) {
        final int tempIndex = mBar.getNearestTickIndex(thumb);
        if (tempIndex != mCurrentIndex) {
            mCurrentIndex = tempIndex;
            if (null != mListener) {
                mListener.onIndexChanged(this, mCurrentIndex);
            }
        }

        float start = thumb.getX();
        float end = mBar.getNearestTickCoordinate(thumb);
        if (mAnimation) {
            startAnimation(thumb, start, end);
        } else {
            thumb.setX(end);
            invalidate();
        }
        thumb.release();
    }

    private void startAnimation(final Thumb thumb, float start, float end) {
        stopAnimation();
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(80);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float x = (Float) animation.getAnimatedValue();
                thumb.setX(x);
                invalidate();
            }
        });
        mAnimator.start();
    }

    private boolean isAnimationRunning() {
        if (null != mAnimator && mAnimator.isRunning()) {
            return true;
        }
        return false;
    }

    private void destroyResources() {
        stopAnimation();
        if (null != mBar) {
            mBar.destroyResources();
            mBar = null;
        }
        if (null != mThumb) {
            mThumb.destroyResources();
            mThumb = null;
        }
    }

    private void stopAnimation() {
        if (null != mAnimator) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    private void moveThumb(Thumb thumb, float x) {
        if (x < mBar.getLeftX() || x > mBar.getRightX()) {
            // Do nothing.
        } else {
            thumb.setX(x);
            invalidate();
        }
    }

    public static interface OnSliderBarChangeListener {
        public void onIndexChanged(FontSliderBar rangeBar, int index);
    }


    private static class Thumb {

        private static final float MINIMUM_TARGET_RADIUS = 50;

        private final float mTouchZone;
        private boolean mIsPressed;

        private final float mY;
        private float mX;

        private Paint mPaintNormal;
        private Paint mPaintPressed;

        private float mRadius;
        private int mColorNormal;
        private int mColorPressed;

        public Thumb(float x, float y, int colorNormal, int colorPressed, float radius) {

            mRadius = radius;
            mColorNormal = colorNormal;
            mColorPressed = colorPressed;

            mPaintNormal = new Paint();
            mPaintNormal.setColor(mColorNormal);
            mPaintNormal.setAntiAlias(true);

            mPaintPressed = new Paint();
            mPaintPressed.setColor(mColorPressed);
            mPaintPressed.setAntiAlias(true);

            mTouchZone = (int) Math.max(MINIMUM_TARGET_RADIUS, radius);

            mX = x;
            mY = y;
        }

        public void setX(float x) {
            mX = x;
        }

        public float getX() {
            return mX;
        }

        public boolean isPressed() {
            return mIsPressed;
        }

        public void press() {
            mIsPressed = true;
        }

        public void release() {
            mIsPressed = false;
        }

        public boolean isInTargetZone(float x, float y) {
            if (Math.abs(x - mX) <= mTouchZone && Math.abs(y - mY) <= mTouchZone) {
                return true;
            }
            return false;
        }

        public void draw(Canvas canvas) {
            if (mIsPressed) {
                canvas.drawCircle(mX, mY, mRadius, mPaintPressed);
            } else {
                canvas.drawCircle(mX, mY, mRadius, mPaintNormal);
            }
        }

        public void destroyResources() {
            if (null != mPaintNormal) {
                mPaintNormal = null;
            }
            if (null != mPaintPressed) {
                mPaintPressed = null;
            }
        }
    }


    private static class Bar {

        private Paint mBarPaint;
        private Paint mTextPaint;

        private final float mLeftX;
        private final float mRightX;
        private final float mY;
        private final float mPadding;

        private int mSegments;
        private float mTickDistance;
        private final float mTickHeight;
        private final float mTickStartY;
        private final float mTickEndY;

        public Bar(float x, float y, float width, int tickCount, float tickHeight,
                   float barWidth, int barColor, int textColor, int textSize, int padding) {

            mLeftX = x;
            mRightX = x + width;
            mY = y;
            mPadding = padding;

            mSegments = tickCount - 1;
            mTickDistance = width / mSegments;
            mTickHeight = tickHeight;
            mTickStartY = mY - mTickHeight / 2f;
            mTickEndY = mY + mTickHeight / 2f;

            mBarPaint = new Paint();
            mBarPaint.setColor(barColor);
            mBarPaint.setStrokeWidth(barWidth);
            mBarPaint.setAntiAlias(true);

            mTextPaint = new Paint();
            mTextPaint.setColor(textColor);
            mTextPaint.setTextSize(textSize);
            mTextPaint.setAntiAlias(true);
        }

        public void draw(Canvas canvas) {
            drawLine(canvas);
            drawTicks(canvas);
        }

        public float getLeftX() {
            return mLeftX;
        }

        public float getRightX() {
            return mRightX;
        }

        public float getNearestTickCoordinate(Thumb thumb) {
            final int nearestTickIndex = getNearestTickIndex(thumb);
            final float nearestTickCoordinate = mLeftX + (nearestTickIndex * mTickDistance);
            return nearestTickCoordinate;
        }

        public int getNearestTickIndex(Thumb thumb) {
            return getNearestTickIndex(thumb.getX());
        }

        public int getNearestTickIndex(float x) {
            return (int) ((x - mLeftX + mTickDistance / 2f) / mTickDistance);
        }

        private void drawLine(Canvas canvas) {
            canvas.drawLine(mLeftX, mY, mRightX, mY, mBarPaint);
        }

        private void drawTicks(Canvas canvas) {
            for (int i = 0; i <= mSegments; i++) {
                final float x = i * mTickDistance + mLeftX;
                canvas.drawLine(x, mTickStartY, x, mTickEndY, mBarPaint);
                String text = 0 == i ? "小" : mSegments == i ? "大" : "";
                if (!TextUtils.isEmpty(text)) {
                    canvas.drawText(text, x - getTextWidth(text) / 2, mTickStartY - mPadding, mTextPaint);
                }
            }
        }

        float getTextWidth(String text) {
            return mTextPaint.measureText(text);
        }

        public void destroyResources() {
            if (null != mBarPaint) {
                mBarPaint = null;
            }
            if (null != mTextPaint) {
                mTextPaint = null;
            }
        }
    }
}
