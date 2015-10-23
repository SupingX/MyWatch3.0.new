package com.mycj.mywatch.util;

import android.util.Log;

/**
 * 通用公式 : 能量消耗 (kcal) = 0.43 * 身高 (cm) + 0.57 * 体重 (kg) + 0.26 * 步频(步/min) + 0.92 * 时间(min) - 108.44

啊文东儿 2015/9/23 17:22:52
步幅 = 0.43 * 身高

啊文东儿 2015/9/23 17:22:59
距离 = 步幅 * 频数

啊文东儿 2015/9/23 17:23:01
步数
 * @author Administrator
 *
 */
public class WatchDataUtil {
	/**
	 * 根据 步数、运动时间、身高、体重计算卡路里
	 * @param step
	 * @param height cm
	 * @param weight kg
	 * @param time	 min
	 * @return
	 */
	public static float getKcal(float step,int height,int weight,float time){
		float kcal = 0f;
		if (time == 0) {
			kcal =0f;
			return 0f;
		}
		kcal = 	  0.43f * height 
				+ 0.57f * weight
				+ 0.26f * (step/time)
				+ 0.92f * time
				- 108.44f;
		if (kcal<0f) {
			kcal=0f;
		}		
		return kcal;
	}
	
	/**
	 * 根据身高求步幅
	 * @param height 单位 m
	 * @return
	 */
	public static float getStepPerWidth(int height){
		return 0.45f* 0.01f * height;
	}
	
	/**
	 * 根据步数、身高求距离 
	 * @param step
	 * @param height
	 * @return
	 */
	public static float getDistance(float step,int height){
		 float distance = step * getStepPerWidth(height);
	
		return distance;
	}
	
}
