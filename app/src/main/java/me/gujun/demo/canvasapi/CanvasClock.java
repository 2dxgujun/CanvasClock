package me.gujun.demo.canvasapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import java.util.TimeZone;

/**
 * Analog clock purely use Canvas API.
 *
 * @author Jun Gu (http://2dxgujun.com)
 * @version 1.0
 * @since 2015-1-26 19:58:54
 */
public class CanvasClock extends View {
    /**
     * Welcome to my personal blog.
     */
    public static final String BLOG_2DXGUJUN = "http://2dxgujun.com";

    private Paint mDialPaint;

    private Paint mLittleScalePaint;
    private Paint mLargeScalePaint;

    private Paint mHourHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mSecondHandPaint;

    private Paint mLinkPaint;

    // ---------------------- Dimension ---------------------- //
    private int mDialRadius;
    private int mDialStrokeWidth;
    private int mDialPadding;

    private int mLittleScaleLength;
    private int mLittleScaleStrokeWidth;

    private int mLargeScaleLength;
    private int mLargeScaleStrokeWidth;

    private int mHourHandLength;
    private int mMinuteHandLength;
    private int mSecondHandLength;

    private int mHourHandTailLength;
    private int mMinuteHandTailLength;
    private int mSecondHandTailLength;

    private int mHourHandStrokeWidth;
    private int mMinuteHandStrokeWidth;
    private int mSecondHandStrokeWidth;

    private int mLinkTextSize;
    // ---------------------- Dimension ----------------------//

    private Time mCalendar;

    private int mHours;
    private int mMinutes;
    private int mSeconds;

    private boolean mAttached;

    private int mDistance;
    private Path mLinkPath;
    private RectF mLinkRectF;

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

    public CanvasClock(Context context) {
        this(context, null);
    }

    public CanvasClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();

        mCalendar = new Time();
        mLinkPath = new Path();
        mLinkRectF = new RectF();
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
        mDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDialPaint.setStyle(Paint.Style.STROKE);
        mDialPaint.setStrokeWidth(mDialStrokeWidth);

        mLinkPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mLinkPaint.setTextSize(mLinkTextSize);
        mLinkPaint.setTextAlign(TextPaint.Align.CENTER);

        mLittleScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLittleScalePaint.setStyle(Paint.Style.STROKE);
        mLittleScalePaint.setStrokeWidth(mLittleScaleStrokeWidth);

        mLargeScalePaint = new Paint(mLittleScalePaint);
        mLargeScalePaint.setStrokeWidth(mLargeScaleStrokeWidth);

        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourHandPaint.setStyle(Paint.Style.STROKE);
        mHourHandPaint.setStrokeWidth(mHourHandStrokeWidth);

        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinuteHandPaint.setStyle(Paint.Style.STROKE);
        mMinuteHandPaint.setStrokeWidth(mMinuteHandStrokeWidth);

        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondHandPaint.setStyle(Paint.Style.STROKE);
        mSecondHandPaint.setStrokeWidth(mSecondHandStrokeWidth);
        mSecondHandPaint.setColor(Color.RED);
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.drawCircle(0, 0, mDialRadius, mDialPaint);

        canvas.save();
        canvas.translate(-mDistance, -mDistance);
        canvas.drawTextOnPath(BLOG_2DXGUJUN, mLinkPath, 0, 0, mLinkPaint);
        canvas.restore();

        for (int i = 0; i < 60; i++) {
            int y = -mDialRadius + mDialPadding;
            if (i % 5 == 0) {
                canvas.drawLine(0, y, 0, y + mLargeScaleLength, mLargeScalePaint);
            } else {
                canvas.drawLine(0, y, 0, y + mLittleScaleLength, mLittleScalePaint);
            }
            canvas.rotate(360 / 60);
        }


        canvas.save();
        canvas.rotate(mHours / 12.0f * 360.0f);
        canvas.drawLine(0, mHourHandTailLength, 0, -mHourHandLength, mHourHandPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f);
        canvas.drawLine(0, mMinuteHandTailLength, 0, -mMinuteHandLength, mMinuteHandPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(mSeconds / 60.0f * 360.0f);
        canvas.drawLine(0, mSecondHandTailLength, 0, -mSecondHandLength, mSecondHandPaint);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDialRadius = Math.min(w, h) / 2 - 10;
        mDialStrokeWidth = mDialRadius / 20;
        mDialPadding = mDialStrokeWidth;

        mLittleScaleLength = mDialStrokeWidth;
        mLittleScaleStrokeWidth = mLittleScaleLength / 4 + 1;

        mLargeScaleLength = mLittleScaleLength * 2;
        mLargeScaleStrokeWidth = mLargeScaleLength / 4 + 3;

        mHourHandLength = mDialRadius / 2;
        mMinuteHandLength = mDialRadius / 2 + mDialRadius / 3;
        mSecondHandLength = mMinuteHandLength;

        mHourHandTailLength = mHourHandLength / 10;
        mMinuteHandTailLength = mHourHandTailLength;
        mSecondHandTailLength = mSecondHandLength / 6;

        mHourHandStrokeWidth = (int) dp2px(3);
        mMinuteHandStrokeWidth = (int) dp2px(3);
        mSecondHandStrokeWidth = (int) dp2px(1);

        mLinkTextSize = (int) sp2px(7);

        initPaint();

        mDistance = mDialRadius * (int) Math.sqrt(2) - mDialRadius / 2;
        mLinkRectF.set(0, 0, mDistance * 2, mDistance * 2);
        mLinkPath.reset();
        mLinkPath.addArc(mLinkRectF, -180, 180);
    }

    private void onTimeChanged() {
        mCalendar.setToNow();

        mHours = mCalendar.hour;
        mMinutes = mCalendar.minute;
        mSeconds = mCalendar.second;
    }

    /**
     * Broadcast receiver.
     */
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

    private float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    private float sp2px(float sp) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    /**
     * Android robot purely drawn by Canvas API.
     */
    private class AndroidRobot extends View {
        private Paint mPaint;
        private Paint mEyePaint;

        private float mHeadRadius = 74f;
        private float mHeadBodySpace = 5f;
        private float mEyeRadius = 10f;
        private float mEyeSpace = 74f;
        private float mAntWidth = 10f;
        private float mAntHeight = 24f;
        private float mAntRoundCorner = 10f;
        private float mBodyWidth = 150f;
        private float mBodyHeight = 140f;
        private float mArmWidth = 34f;
        private float mArmHeight = 110f;
        private float mArmBodySpace = 5f;
        private float mArmRoundCorner = 15f;
        private float mFootWidth = 40f;
        private float mFootHeight = 54f;
        private float mFootSpace = 10f;
        private float mFootRoundCorner = 5f;

        private RectF mHeadRectF;
        private PointF mLeftEyePointF;
        private PointF mRightEyePointF;
        private RectF mLeftAntRectF;
        private RectF mRightAntRectF;
        private RectF mBodyRectF;
        private RectF mLeftArmRectF;
        private RectF mRightArmRectF;
        private RectF mLeftFootRectF;
        private RectF mRightFootRectF;


        public AndroidRobot(Context context) {
            this(context, null);
        }

        public AndroidRobot(Context context, AttributeSet attrs) {
            super(context, attrs);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            mEyePaint = new Paint(mPaint);
            mEyePaint.setColor(Color.WHITE);

            mHeadRectF = new RectF();
            mLeftEyePointF = new PointF();
            mRightEyePointF = new PointF();
            mLeftAntRectF = new RectF();
            mRightAntRectF = new RectF();
            mBodyRectF = new RectF();
            mLeftArmRectF = new RectF();
            mRightArmRectF = new RectF();
            mLeftFootRectF = new RectF();
            mRightFootRectF = new RectF();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawArc(mHeadRectF, -180, 180, true, mPaint);
            canvas.drawRoundRect(mBodyRectF, 10, 10, mPaint);
            canvas.drawRoundRect(mLeftArmRectF, mArmRoundCorner, mArmRoundCorner, mPaint);
            canvas.drawRoundRect(mRightArmRectF, mArmRoundCorner, mArmRoundCorner, mPaint);
            canvas.drawRoundRect(mLeftFootRectF, mFootRoundCorner, mFootRoundCorner, mPaint);
            canvas.drawRoundRect(mRightFootRectF, mFootRoundCorner, mFootRoundCorner, mPaint);

            canvas.drawCircle(mLeftEyePointF.x, mLeftEyePointF.y, mEyeRadius, mEyePaint);
            canvas.drawCircle(mRightEyePointF.x, mRightEyePointF.y, mEyeRadius, mEyePaint);

            float px = mLeftEyePointF.x + (mRightEyePointF.x - mLeftEyePointF.x) / 2;
            float py = mLeftEyePointF.y;
            canvas.save();
            canvas.rotate(-30, px, py);
            canvas.drawRoundRect(mLeftAntRectF, mAntRoundCorner, mAntRoundCorner, mPaint);
            canvas.restore();

            canvas.save();
            canvas.rotate(30, px, py);
            canvas.drawRoundRect(mRightAntRectF, mAntRoundCorner, mAntRoundCorner, mPaint);
            canvas.restore();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            float heightSum = mAntHeight + mHeadRadius + mBodyHeight + mFootHeight + 5;
            float proportion = h / heightSum;

            mHeadRadius *= proportion;
            mHeadBodySpace *= proportion;
            mEyeRadius *= proportion;
            mEyeSpace *= proportion;
            mAntWidth *= proportion;
            mAntHeight *= proportion;
            mAntRoundCorner *= proportion;
            mBodyWidth *= proportion;
            mBodyHeight *= proportion;
            mArmWidth *= proportion;
            mArmHeight *= proportion;
            mArmBodySpace *= proportion;
            mArmRoundCorner *= proportion;
            mFootWidth *= proportion;
            mFootHeight *= proportion;
            mFootSpace *= proportion;
            mFootRoundCorner *= proportion;

            float headLeft = w / 2 - mHeadRadius;
            float headRight = headLeft + mHeadRadius * 2;
            float headTop = mAntHeight;
            float headBottom = headTop + mHeadRadius * 2;
            mHeadRectF.set(headLeft, headTop, headRight, headBottom);

            float leftEyeX = w / 2 - mEyeSpace / 2;
            float leftEyeY = mAntHeight + mHeadRadius / 2;
            mLeftEyePointF.set(leftEyeX, leftEyeY);

            float rightEyeX = leftEyeX + mEyeSpace;
            float rightEyeY = leftEyeY;
            mRightEyePointF.set(rightEyeX, rightEyeY);

            float leftAntLeft = w / 2 - mAntWidth / 2;
            float leftAntRight = leftAntLeft + mAntWidth;
            float leftAntTop = 0;
            float leftAntBottom = leftAntTop + mAntHeight;
            mLeftAntRectF.set(leftAntLeft, leftAntTop, leftAntRight, leftAntBottom);

            float rightAntLeft = leftAntLeft;
            float rightAntRight = rightAntLeft + mAntWidth;
            float rightAntTop = 0;
            float rightAntBottom = rightAntTop + mAntHeight;
            mRightAntRectF.set(rightAntLeft, rightAntTop, rightAntRight, rightAntBottom);

            float bodyLeft = (w - mBodyWidth) / 2;
            float bodyRight = bodyLeft + mBodyWidth;
            float bodyTop = mAntHeight + mHeadRadius + mHeadBodySpace;
            float bodyBottom = bodyTop + mBodyHeight;
            mBodyRectF.set(bodyLeft, bodyTop, bodyRight, bodyBottom);

            float leftArmLeft = bodyLeft - mArmBodySpace - mArmWidth;
            float leftArmRight = leftArmLeft + mArmWidth;
            float leftArmTop = bodyTop;
            float leftArmBottom = leftArmTop + mArmHeight;
            mLeftArmRectF.set(leftArmLeft, leftArmTop, leftArmRight, leftArmBottom);

            float rightArmLeft = bodyRight + mArmBodySpace;
            float rightArmRight = rightArmLeft + mArmWidth;
            float rightArmTop = bodyTop;
            float rightArmBottom = rightArmTop + mArmHeight;
            mRightArmRectF.set(rightArmLeft, rightArmTop, rightArmRight, rightArmBottom);

            float leftFootLeft = w / 2 - mFootSpace / 2 - mFootWidth;
            float leftFootRight = leftFootLeft + mFootWidth;
            float leftFootTop = bodyBottom - 5;
            float leftFootBottom = leftFootTop + mFootHeight;
            mLeftFootRectF.set(leftFootLeft, leftFootTop, leftFootRight, leftFootBottom);

            float rightFootLeft = leftFootRight + mFootSpace;
            float rightFootRight = rightFootLeft + mFootWidth;
            float rightFootTop = bodyBottom - 5;
            float rightFootBottom = rightFootTop + mFootHeight;
            mRightFootRectF.set(rightFootLeft, rightFootTop, rightFootRight, rightFootBottom);
        }
    }
}