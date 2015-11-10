package com.mycj.mywatch.view;

import java.util.Calendar;
import java.util.Date;

import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.bean.SleepData;
import com.mycj.mywatch.business.ProtocolForNotify;
import com.mycj.mywatch.util.DateUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SleepCountView extends View {
	private Paint rectPaint;
	private int colorDeep;
	private int colorLight;
	private int colorAwake;

	private int[] sleeps = new int[] {};
	private float spaceY = 50;
	private float perWidth;
	private int size=1;

	public int getSize() {
		return this.size;
	}

//	private float total;
//	private float deep;
//	private float awakTime;
//	private float light;

	private Paint textPaint;

	private float downX;
	private int startText;
	private int endText;

	private OnScrollListener mOnScrollListener;
	private OnSleepDataChangeListener mOnSleepDataChangeListener;

	public SleepCountView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SleepCountView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public SleepCountView(Context context) {
		super(context);
		init(context);
	}

//	public float getTotal() {
//		return total;
//	}
//
//	public float getDeep() {
//		return deep;
//	}
//
//	public float getLight() {
//		return light;
//	}
//
//	public float getAwak() {
//		return awakTime;
//	}

	public void setSleepData(int[] sleeps) {
//		this.total = 0;
//		this.awakTime = 0;
//		deep = 0;
//		light = 0;
//		if (sleeps == null) {
//			Log.v("", "sleeps为空");
//			this.sleeps = null;
//			this.sleeps = new int[size];
//
//		} else {
			this.sleeps = sleeps;
			
////			parseSleepData(this.sleeps);
//			size = sleeps.length;
//		}

		invalidate();
		if (mOnSleepDataChangeListener != null) {
			mOnSleepDataChangeListener.onchange(sleeps);
		}
	}

	public void setOnScrollListener(OnScrollListener l) {
		this.mOnScrollListener = l;
	}

	public void setOnSleepDataChangeListener(OnSleepDataChangeListener l) {
		this.mOnSleepDataChangeListener = l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = event.getX();
			break;
		case MotionEvent.ACTION_UP:
			float upX = event.getX();
			float diff = upX - downX;
			if (Math.abs(diff) > 20) {
				if (diff > 0) {
					Log.e("SleepCountView", "右移");
					if (mOnScrollListener != null) {
						mOnScrollListener.previous();
					}
				} else if (diff < 0) {
					Log.e("SleepCountView", "左移");
					if (mOnScrollListener != null) {
						mOnScrollListener.next();
					}
				}
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int mWidth = measureWidth(widthMeasureSpec);
		int mHeight = measureHeight(heightMeasureSpec);
		setMeasuredDimension(mWidth, mHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		float mWidth = getWidth();
		Log.e("", "size : " + size);
		if (size != 0) {
			perWidth = mWidth / size;
		}

		drawRects(canvas);
		super.onDraw(canvas);
	}

	private void init(Context context) {
		setClickable(true);
		rectPaint = new Paint();
		rectPaint.setAntiAlias(true); // 消除锯齿
		rectPaint.setStyle(Paint.Style.FILL); // 绘制空心圆
		rectPaint.setStrokeWidth(1); // 设置进度条宽度
		rectPaint.setStrokeJoin(Paint.Join.ROUND);
		rectPaint.setStrokeCap(Paint.Cap.ROUND); // 设置圆角

		textPaint = new Paint();
		textPaint.setTextSize(20);
		textPaint.setAntiAlias(true); // 消除锯齿
		textPaint.setStyle(Paint.Style.FILL); // 绘制空心圆
		textPaint.setStrokeWidth(1); // 设置进度条宽度
		textPaint.setStrokeJoin(Paint.Join.ROUND);
		textPaint.setStrokeCap(Paint.Cap.ROUND); // 设置圆角

		startText = (int) SharedPreferenceUtil.get(context, Constant.SHARE_SLEEP_START_HOUR, 0);
		endText = (int) SharedPreferenceUtil.get(context, Constant.SHARE_SLEEP_END_HOUR, 23);
//		size = Math.abs(end - start);
		if (endText<startText) {
			size = 24-startText+endText;
		}else{
			size = endText-startText ;
		}
		if (size == 0) {
			size = 1;
		}

		colorDeep = getResources().getColor(R.color.color_deep);
		colorLight = getResources().getColor(R.color.color_light);
		colorAwake = getResources().getColor(R.color.color_awak);
	}

	public void setSize(int size){
		this.size =size;
		invalidate();
	}
	
	private int measureHeight(int heightMeasureSpec) {
		int result = 0;
		int mode = MeasureSpec.getMode(heightMeasureSpec);
		int size = MeasureSpec.getSize(heightMeasureSpec);
		if (mode == MeasureSpec.EXACTLY) {
			result = size;
		} else if (mode == MeasureSpec.AT_MOST) {
			result = getHeight();
			result = Math.min(result, size);
		}
		return result;
	}

	private int measureWidth(int widthMeasureSpec) {
		int result = 0;
		int mode = MeasureSpec.getMode(widthMeasureSpec);
		int size = MeasureSpec.getSize(widthMeasureSpec);
		if (mode == MeasureSpec.EXACTLY) { // math_content
			result = size;
		} else if (mode == MeasureSpec.AT_MOST) { // wrap_content
			result = getWidth();
			result = Math.min(result, size);
		}
		return result;
	}

	private void drawRects(Canvas canvas) {

		if (sleeps.length > 0) {
			for (int i = 0; i < size; i++) {
				// sleepData.getSleep();//
				// 得到数据。数据说明：0x00-没有数据，0x01-非常差，0x02-差，0x03-中，0x04-好，0x05-非常好
					float value = getSleepValue(sleeps[i]);// 根据睡眠的值睡眠时间
				// 方块
				float left = perWidth * i;
				float top = getRectTop(value);
				float right = perWidth * (i + 1);
				float bottom = getHeight() - spaceY;
				RectF rectF = new RectF(left, top, right, bottom);
				canvas.drawRect(rectF, rectPaint);// 画方块

//				if (i == 0) {
					String hourStartStr = getHourString(startText);
					Rect rectTextStart = new Rect();
					textPaint.getTextBounds(hourStartStr, 0, hourStartStr.length(), rectTextStart);
					canvas.drawText(hourStartStr, 0, getHeight() - spaceY / 2, textPaint);
//				} else if (i == size - 1) {
					String hourStrEnd = getHourString(endText);
					Rect rectTextEnd = new Rect();
					textPaint.getTextBounds(hourStrEnd, 0, hourStrEnd.length(), rectTextEnd);
					canvas.drawText(hourStrEnd, (size) * perWidth - rectTextEnd.width(), getHeight() - spaceY / 2, textPaint);
//				}
				}
		} else {
			float left = 0;
			float top = spaceY;
			float right = getWidth();
			float bottom = getHeight() - spaceY;
			RectF rectF = new RectF(left, top, right, bottom);
			rectPaint.setColor(colorAwake);
			canvas.drawRect(rectF, rectPaint);// 画方块

			String hourStrStart = getHourString(startText);
			Rect rectTextStart = new Rect();
			textPaint.getTextBounds(hourStrStart, 0, hourStrStart.length(), rectTextStart);
			canvas.drawText(hourStrStart, 0, getHeight() - spaceY / 2, textPaint);
			String hourStrEnd = getHourString(endText);
			Rect rectTextEnd = new Rect();
			textPaint.getTextBounds(hourStrEnd, 0, hourStrEnd.length(), rectTextEnd);
			canvas.drawText(hourStrEnd, (size) * perWidth - rectTextEnd.width(), getHeight() - spaceY / 2, textPaint);
		}

	}

	private String getHourString(int time) {
		if (time < 10) {
			return "0" + time + " : 00";
		} else {
			return time + " : 00";
		}
	}

	private String getHourString(SleepData sleepData) {
		String year = sleepData.getYear();
		String month = sleepData.getMonth();
		String day = sleepData.getDay();
		Date datetime = DateUtil.stringToDate(year + month + day, ProtocolForNotify.DATE_FORMAT);
		Calendar c = Calendar.getInstance();
		c.setTime(datetime);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		String minStr = "";
		if (min < 10) {
			minStr = "0" + min;
		} else {
			minStr = String.valueOf(min);
		}
		String hourStr = "";
		if (hour < 10) {
			hourStr = "0" + hour;
		} else {
			hourStr = String.valueOf(hour);
		}
		return hourStr + " : " + minStr;
	}

	private float getRectTop(float value) {
		float height = (getHeight() - spaceY) * (1 - value);
		return height;
	}

//	private void parseSleepData(int[] sleeps) {
//		for (int i = 0; i < sleeps.length; i++) {
//			switch (sleeps[i]) {
//			case 0:
//				awakTime+=1f;
//				break;
//			case 1:
////				total += 0.25f;// 获得总的睡眠时间
//				awakTime += 0.75f;
//				light += 0.25;
//				break;
//			case 2:
////				awakTime += 0.25f;
//				light += 1f;
////				total += 0.75f;// 获得总的睡眠时间
//				break;
//			case 3:
//				light += 0.75f;
//				deep+=0.25f;
////				total += 1f;// 获得总的睡眠时间
//				break;
//			case 4:
//				light+=0.25f ;
//				deep +=0.75f;
//				total += 1f;// 获得总的睡眠时间
//				break;
//			case 5:
//				deep += 1f;
////				total += 1f;// 获得总的睡眠时间
//				break;
//			default:
//				break;
//			}
//			
//			total=(deep+light);
//		}
//	}

	private float getSleepValue(int sleep) {
		// 得到数据。数据说明：0x00-没有数据，0x01-非常差，0x02-差，0x03-中，0x04-好，0x05-非常好
		Log.v("", "sleep:" + sleep);
		float result = 0;
		rectPaint.setColor(colorAwake);
		switch (sleep) {
		case 0:
			rectPaint.setColor(colorAwake);
			result = 1f;
			break;
		case 1:
			rectPaint.setColor(colorAwake);
			result = 1f;
			break;
		case 2:
			rectPaint.setColor(colorLight);
			result = 2 / 3f;
			break;
		case 3:
			result = 2 / 3f;
			rectPaint.setColor(colorLight);
			break;
		case 4:
			result = 1 / 3f;
			rectPaint.setColor(colorDeep);
			break;
		case 5:
			result = 1 / 3f;
			rectPaint.setColor(colorDeep);
			break;
		default:
			break;
		}
		return result;
	}

	public interface OnScrollListener {
		public void next();

		public void previous();
	}

	public interface OnSleepDataChangeListener {
		public void onchange(int[] sleeps);
	}
}
