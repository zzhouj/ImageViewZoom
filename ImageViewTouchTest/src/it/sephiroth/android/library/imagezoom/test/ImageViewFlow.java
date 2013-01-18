package it.sephiroth.android.library.imagezoom.test;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ImageViewFlow extends ImageView {

	private static final String TAG = ImageViewFlow.class.getName();

	private static final long FLOW_SCROLL_TIME = 30 * 1000;
	private static final long FLOW_ANI_INTERVAL = 1000 / 24;
	private static final long FLOW_ANI_SWITCH_DELAY = 2000;
	private static final float FLOW_ANI_MIN_STEP = 0.1F;

	private float mScale;
	private float mTranslateX;
	private float mTranslateY;
	private float mTranslateXStart;
	private float mTranslateXEnd;
	private float mTranslateXStep;
	private long mFlowScrollTime = FLOW_SCROLL_TIME;
	private boolean mFlowToLeft = true;
	private Runnable mFlowAniRunnable = new FlowAniRunnable();
	private Handler mHandler = new Handler();
	private boolean mHasWindowFocus;
	private boolean mAttachedToWindow;

	public ImageViewFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ImageViewFlow(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ImageViewFlow(Context context) {
		super(context);
		init();
	}

	public void setFlowScrollTime(long flowScrollTime) {
		mFlowScrollTime = flowScrollTime;
		if (mFlowScrollTime <= 0) {
			mFlowScrollTime = FLOW_SCROLL_TIME;
		}
		calcScaleAndTranslate();
	}

	protected void init() {
		setScaleType(ImageView.ScaleType.MATRIX);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		calcScaleAndTranslate();
	}

	private void calcScaleAndTranslate() {
		Drawable drawable = getDrawable();
		if (drawable != null) {
			float viewWidth = getWidth();
			float viewHeight = getHeight();
			float w = drawable.getIntrinsicWidth();
			float h = drawable.getIntrinsicHeight();
			float widthScale = viewWidth / w;
			float heightScale = viewHeight / h;
			mScale = Math.max(widthScale, heightScale);
			mTranslateY = (viewHeight - h * mScale) / 2;
			mTranslateXStart = mTranslateX = 0;
			mTranslateXEnd = viewWidth - w * mScale;
			if (mTranslateXEnd > 0) {
				mTranslateXEnd = 0;
			}
			mTranslateXStep = (mTranslateXStart - mTranslateXEnd) * FLOW_ANI_INTERVAL / mFlowScrollTime;
			if (mTranslateXStep < FLOW_ANI_MIN_STEP) {
				mTranslateXStep = FLOW_ANI_MIN_STEP;
			}
			mFlowToLeft = true;
			setImageMatrix();
			startOrStopFlowAni();
		}
	}

	private void setImageMatrix() {
		Matrix matrix = new Matrix();
		matrix.postScale(mScale, mScale);
		matrix.postTranslate(mTranslateX, mTranslateY);
		setImageMatrix(matrix);
	}

	private void startOrStopFlowAni() {
		if (mTranslateXEnd < mTranslateXStart && mHasWindowFocus && mAttachedToWindow) {
			mHandler.removeCallbacks(mFlowAniRunnable);
			mHandler.postDelayed(mFlowAniRunnable, FLOW_ANI_INTERVAL);
		} else {
			mHandler.removeCallbacks(mFlowAniRunnable);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mAttachedToWindow = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mAttachedToWindow = false;
		startOrStopFlowAni();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		mHasWindowFocus = hasWindowFocus;
		startOrStopFlowAni();
	}

	private class FlowAniRunnable implements Runnable {
		@Override
		public void run() {
			Log.d(TAG, "FlowAniRunnable.run: mTranslateX = " + mTranslateX);
			long delayMillis = FLOW_ANI_INTERVAL;
			if (mFlowToLeft) { // to left
				mTranslateX -= mTranslateXStep;
				if (mTranslateX < mTranslateXEnd) {
					mTranslateX = mTranslateXEnd;
					mFlowToLeft = false;
					delayMillis = FLOW_ANI_SWITCH_DELAY;
				}
			} else { // to right
				mTranslateX += mTranslateXStep;
				if (mTranslateX > mTranslateXStart) {
					mTranslateX = mTranslateXStart;
					mFlowToLeft = true;
					delayMillis = FLOW_ANI_SWITCH_DELAY;
				}
			}
			setImageMatrix();
			mHandler.postDelayed(this, delayMillis);
		}
	};

}
