package me.gujun.demo.canvasapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import java.util.TimeZone;

/**
 * Custom view for learn canvas api.
 *
 * @author Jun Gu (2dxgujun@gmail.com)
 * @version 1.0
 * @since 2015-1-26 19:58:54
 */
public class CanvasView extends View {
    public static final String BLOG_2DXGUJUN = "http://2dxgujun.com";

    // private Paint mPaint;
    private Paint mDialPaint;
    private Paint mLittleScalePaint;
    private Paint mLargeScalePaint;
    // private Paint mScaleMarkPaint;
    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSecondHandPaint;
    private Paint mTextPaint;

    private int mDialRadius;

    private Time mCalendar;

    private int mHours;
    private int mMinutes;
    private int mSeconds;

    private int mHourHandLength;
    private int mMinuteHandLength;
    private int mSecondHandLength;

    private int mDistance;
    private Path mBlogLinkPath = new Path();
    private RectF mBlogLinkRectF = new RectF();

    private boolean mAttached;

    public CanvasView(Context context) {
        this(context, null);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();

        mCalendar = new Time();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            getContext().registerReceiver(mIntentReceiver, filter);
            mThread.start();
        }

        // NOTE: It's safe to do these after registering the receiver since the always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time.
        mCalendar = new Time();

        // Make sure we update to the current time.
        onTimeChanged();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    private void initPaint() {
        // mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // mPaint.setStyle(Paint.Style.STROKE);

        mDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDialPaint.setStyle(Paint.Style.STROKE);
        mDialPaint.setStrokeWidth(15);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(30);
        mTextPaint.setTextAlign(TextPaint.Align.CENTER);

        mLittleScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLittleScalePaint.setStyle(Paint.Style.STROKE);
        mLittleScalePaint.setStrokeWidth(4);

        mLargeScalePaint = new Paint(mLittleScalePaint);
        mLargeScalePaint.setStrokeWidth(8);

        // mScaleMarkPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        // mScaleMarkPaint.setTextSize(40);

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.STROKE);
        mHourHandPaint.setStrokeWidth(10);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setStyle(Paint.Style.STROKE);
        mMinuteHandPaint.setStrokeWidth(10);

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.STROKE);
        mSecondHandPaint.setStrokeWidth(3);
        mSecondHandPaint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.drawCircle(0, 0, mDialRadius, mDialPaint);

        canvas.save();
        canvas.translate(-mDistance, -mDistance);
        // canvas.drawRect(mBlogLinkRectF, mPaint);
        // canvas.drawArc(mBlogLinkRectF, -180, 180, false, mPaint);
        canvas.drawTextOnPath(BLOG_2DXGUJUN, mBlogLinkPath, 0, 0, mTextPaint);
        canvas.restore();

        for (int i = 0; i < 60; i++) {
            if (i % 5 == 0) {
                canvas.drawLine(0, -mDialRadius + 30, 0, -mDialRadius + 60, mLargeScalePaint);
                // canvas.drawText(String.valueOf(i / 5), -10, -mDialRadius + 50, mScaleMarkPaint);
            } else {
                canvas.drawLine(0, -mDialRadius + 30, 0, -mDialRadius + 45, mLittleScalePaint);
            }
            canvas.rotate(360 / 60, 0, 0);
        }


        canvas.save();
        canvas.rotate(mHours / 12.0f * 360.0f);
        canvas.drawLine(0, 30, 0, -mHourHandLength, mHourHandPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f);
        canvas.drawLine(0, 30, 0, -mMinuteHandLength, mMinuteHandPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(mSeconds / 60.0f * 360.0f);
        canvas.drawLine(0, 100, 0, -mSecondHandLength, mSecondHandPaint);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDialRadius = Math.min(w, h) / 2 - 10;
        mDistance = mDialRadius * (int) Math.sqrt(2) - mDialRadius / 2;
        mBlogLinkRectF.set(0, 0, mDistance * 2, mDistance * 2);
        mBlogLinkPath.reset();
        mBlogLinkPath.addArc(mBlogLinkRectF, -180, 180);

        mHourHandLength = mDialRadius / 2;
        mMinuteHandLength = mDialRadius - 100;
        mSecondHandLength = mMinuteHandLength;
    }

    private void onTimeChanged() {
        mCalendar.setToNow();

        mHours = mCalendar.hour;
        mMinutes = mCalendar.minute;
        mSeconds = mCalendar.second;
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }

            onTimeChanged();
        }
    };

    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (Thread.interrupted() == false) {
                onTimeChanged();
                postInvalidate();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}