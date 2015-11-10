package com.mycj.mywatch.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mycj.mywatch.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class HistoryView extends View {

	private int sizeX;
	private int sizeY;
	private Paint mPaintXY;
	private Paint mPaintLine;
	private Paint mPaintText;
	private float spaceX = 40;
	private float spaceY = 40;
	private float mWidth;
	private float mHeight;
	private float max = 12;
	public void setMax(float max){
		this.max = max;
	}

	private String[] textY = new String[] { "1", "2", "3", "4", "5", "6" };
	private List<Float> data = new ArrayList<>();
	private float perX;
	private float perY;
	private Paint mPaintPath;
	private Path path;

	public HistoryView(Context context) {
		super(context);
		init(context);
	}
	
	public void  setTextY(String[] textY){
		this.textY = textY;
	}

	public HistoryView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public HistoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	
	public interface OnDataChangeListener{
		void onchang(List<Float> data);
	}
	
	private OnDataChangeListener mOnDataChangeListener;
	public void setOnDataChangeListener(OnDataChangeListener l){
		this.mOnDataChangeListener = l	;
	}
	
	public void setData(List<Float> data){
		this.data = data;
		invalidate();
		if (mOnDataChangeListener!=null) {
			mOnDataChangeListener.onchang(data);
		}
	}
	
	private void init(Context context) {
		mPaintXY = new Paint();
		mPaintXY.setAntiAlias(true);
		mPaintXY.setStrokeWidth(4);
		mPaintXY.setStyle(Paint.Style.STROKE);
		mPaintXY.setColor(getResources().getColor(R.color.black_device));
		mPaintXY.setStrokeCap(Paint.Cap.ROUND);// 设置圆角

		mPaintLine = new Paint();
		mPaintLine.setAntiAlias(true);
		mPaintLine.setStrokeWidth(1);
		mPaintLine.setStyle(Paint.Style.STROKE);
		mPaintLine.setColor(getResources().getColor(R.color.grey));
		mPaintLine.setStrokeCap(Paint.Cap.ROUND);// 设置圆角

		mPaintText = new Paint();
		mPaintText.setColor(Color.BLACK);
		mPaintText.setAntiAlias(true);
		mPaintText.setStrokeWidth(5);

		mPaintPath = new Paint();
		mPaintPath.setStyle(Paint.Style.STROKE);
		mPaintPath.setColor(getResources().getColor(R.color.color_deep));
		mPaintPath.setAntiAlias(true);
		mPaintPath.setStrokeWidth(4);
		mPaintPath.setStrokeCap(Paint.Cap.ROUND); // 设置圆角

//		this.data.add(2222f);
//		this.data.add(4555f);
		//
//		for (int i = 0; i < 31; i++) {
//			data.add((float)new Random().nextInt(6));
//		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mWidth = getWidth();
		mHeight = getHeight();

		// 区间的个数
		
		sizeX = data.size();
		if (sizeX==0) {
			sizeX=31;
		}
		sizeY = textY.length;

	

		// 每个区间的宽度
		perX = (float) (mWidth - 2 * spaceX) / sizeX;
		perY = (float) (mHeight - 2 * spaceY) / sizeY;

		drawXY(canvas);
		drawPath(canvas);
		super.onDraw(canvas);
	}

	private void drawPath(Canvas canvas) {

		// y方向
		for (int i = 0; i < sizeY; i++) {
			// 画坐标
			Rect rectText = new Rect();
			mPaintText.getTextBounds(textY[i], 0, textY[i].length(), rectText);
			canvas.drawText(textY[i], spaceX / 2, perY * (sizeY - i - 1) + spaceY + rectText.height() / 2, mPaintText);
			// 化横线
			canvas.drawLine(spaceX, perY * (sizeY - i - 1) + spaceY, mWidth - spaceX, perY * (sizeY - i - 1) + spaceY, mPaintLine);
		}
		
		
		
		// x方向
		for (int i = 0; i <sizeX; i++) {
			if (i % 2 == 0) {
				// 画坐标
				Rect rectText = new Rect();
				String textX = (i+1) + "";
				if (i+1 < 10) {
					textX = "0" + (i+1);
				}
				mPaintText.getTextBounds(textX, 0, textX.length(), rectText);
				float y = mHeight - spaceY / 2;
				float x;
				if (i==0) {
					x  = spaceX-rectText.width()/2;
				}else{
					x = spaceX + perX * i -rectText.width()/2 ;
				}
				canvas.drawText(textX, x, y, mPaintText);
				// 画竖线
				canvas.drawLine(spaceX + perX * i , spaceY, spaceX + perX * i , mHeight - spaceY, mPaintLine);
			}
		}

		if (data.size()>0) {
			// 画路径
			path = new Path();
			path.moveTo(perX * 0 + spaceX, getYFromData(data.get(0)));
			for (int i = 1; i < sizeX; i++) {
				path.lineTo(perX * i + spaceX, getYFromData(data.get(i)));
			}
			canvas.drawPath(path, mPaintPath);
		}
	}

	private float getYFromData(float data) {
		//当数值大于12时，显示的值为12。
		if (data>max) {
			data=max;
		}
		float value = (float) (data * (mHeight - 2 * spaceY)) / max;// 得到所占的高度
		float result = mHeight - 2 * spaceY - value + spaceY;
		return result; // 得到高度的Y坐标
	}

	private void drawXY(Canvas canvas) {
		canvas.drawLine(spaceX, mHeight - spaceY, mWidth - spaceX / 2, mHeight - spaceY, mPaintXY);// X轴
		canvas.drawLine(spaceX, mHeight - spaceY, spaceX, spaceY / 2, mPaintXY);//Y轴
	}
	
	public float getToal(){
		Float total = 0f;
		for (int i = 0; i < data.size(); i++) {
			total += data.get(i);
		}
		return total;
	}
}
