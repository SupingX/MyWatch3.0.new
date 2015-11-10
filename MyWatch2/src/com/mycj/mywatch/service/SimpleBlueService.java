package com.mycj.mywatch.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.litepal.crud.DataSupport;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;

import com.mycj.mywatch.BaseApp;
import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.bean.HeartRateData;
import com.mycj.mywatch.bean.SleepData;
import com.mycj.mywatch.bean.PedoData;
import com.mycj.mywatch.business.HeartRateJson;
import com.mycj.mywatch.business.ProtocolForNotify;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.util.DateUtil;
import com.mycj.mywatch.util.MessageUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;
import com.mycj.mywatch.view.AlertDialog;

public class SimpleBlueService extends AbstractSimpleBlueService {

	public final static String ACTION_DATA_STEP = "lite_data_step";
	public final static String ACTION_DATA_SYNC_TIME = "lite_data_sync_time";
	public final static String ACTION_DATA_CAMERA = "lite_data_camera";
	public final static String ACTION_DATA_MUSIC = "lite_data_music";
	public final static String ACTION_DATA_REMIND = "lite_data_remind";
	public final static String ACTION_DATA_HEART_RATE = "lite_data_heart_rate";
	public final static String ACTION_DATA_HISTORY_HEART_RATE = "lite_data_history_heart_rate";
	public final static String ACTION_DATA_HISTORY_STEP = "lite_data_history_step";
	public final static String ACTION_DATA_HISTORY_SLEEP = "lite_data_history_sleep";
	public final static String ACTION_DATA_HISTORY_DISTANCE = "lite_data_history_distance";
	public final static String ACTION_DATA_HISTORY_CAL = "lite_data_history_cal";
	public final static String ACTION_DATA_HISTORY_SPORT_TIME = "lite_data_history_sport_time";
	public final static String ACTION_DATA_HISTORY_SLEEP_FOR_TODAY = "lite_data_history_sleep_for_today";

	public final static String EXTRA_STEP = "extra_step";
	public final static String EXTRA_SLEEP = "extra_sleep";
	public final static String EXTRA_HEART_RATE = "EXTRA_HEART_RATE";
	public final static String EXTRA_CAMERA = "EXTRA_CAMERA";
	private AlertDialog disconnectDiloag;
	private boolean oncePlayMusic = true;

	private MediaPlayer findPhonePlay;
	// private boolean isOnce = true;
	private MusicService musicService;
	private Ringtone ringtone;
	private ProgressDialog dialogSyncSetting;

	public static IntentFilter getIntentFilter() {
		IntentFilter intentFilter = AbstractSimpleBlueService.getIntentFilter();
		intentFilter.addAction(ACTION_DATA_STEP);
		intentFilter.addAction(ACTION_DATA_SYNC_TIME);
		intentFilter.addAction(ACTION_DATA_CAMERA);
		intentFilter.addAction(ACTION_DATA_MUSIC);
		intentFilter.addAction(ACTION_DATA_REMIND);
		intentFilter.addAction(ACTION_DATA_HEART_RATE);
		intentFilter.addAction(ACTION_DATA_HISTORY_HEART_RATE);
		intentFilter.addAction(ACTION_DATA_HISTORY_STEP);
		intentFilter.addAction(ACTION_DATA_HISTORY_SLEEP);
		intentFilter.addAction(ACTION_DATA_HISTORY_DISTANCE);
		intentFilter.addAction(ACTION_DATA_HISTORY_CAL);
		intentFilter.addAction(ACTION_DATA_HISTORY_SPORT_TIME);
		intentFilter.addAction(ACTION_DATA_HISTORY_SLEEP_FOR_TODAY);
		return intentFilter;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mHander.postDelayed(new Runnable() {

			@Override
			public void run() {
				BaseApp app = (BaseApp) getApplication();
				if (musicService == null) {
					musicService = app.getMusicService();
				}
			}
		}, 2000);

		taskIncoming = new Runnable() {
			@Override
			public void run() {
				int mmsCount = MessageUtil.getNewMmsCount(getApplicationContext());
				int msmCount = MessageUtil.getNewSmsCount(getApplicationContext());
				int phoneCount = MessageUtil.readMissCall(getApplicationContext());
				Log.e("BaseApp", "____电话数量 ： " + phoneNo + "-->" + phoneCount);
				Log.e("BaseApp", "____短信 ： " + smsMNo + "-->" + (msmCount + mmsCount) + (smsMNo != (mmsCount + msmCount)));
				// 数量只要有一个变化就发送
				boolean isCallRemind;
				isCallRemind = (boolean) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_CHECK_REMIND_CALL, false);
				if (isCallRemind) {
				if (phoneNo != phoneCount || smsMNo != (mmsCount + msmCount)) {
					Log.e("BaseApp", "_______________________________________________________________________________________读取短信和电话数量 ： 有变化");
					// if (mmsCount == 0 && msmCount == 0 && phoneCount == 0) {
					// doWriteUnReadPhoneAndSmsToWatch(0, 0);
					// return;
					// } else {
					doWriteUnReadPhoneAndSmsToWatch(phoneCount, (mmsCount + msmCount));
					// }
					
					//修改与10.28
					phoneNo = phoneCount;
					smsMNo = (mmsCount + msmCount);
					
					
					
					
				} else {
					Log.e("BaseApp", "__读取短信和电话数量 ： 无变化");
				}
				}
				mHander.sendEmptyMessage(0xa1);
			}
		};
		
		mHander.postDelayed(taskIncoming, 8 * 1000);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		oncePlayMusic = false;
		hrList.clear();
		hrList=null;
		if (dialogSyncSetting != null && dialogSyncSetting.isShowing()) {
			dialogSyncSetting.dismiss();
		}
	}

	/**
	 * 保存心率数据json最后一次
	 * 
	 * @param hrd
	 */
	private void saveHeartRateJson(HeartRateData hrd) {
		String json = HeartRateJson.objToJson(hrd);
		HeartRateJson.writeFileData("json", json, getApplicationContext());
	}

	/**
	 * 保存心率
	 */
	private void saveHeartRate(List<Integer> hrList) {
		
		String datas = dataToString(hrList);
		int maxs=0;
		int mins= 0;
		int total = 0;
		int size = hrList.size();
		if (size > 0) {
			maxs = hrList.get(0);
			mins = hrList.get(0);
			for (int i = 0; i < size; i++) {
				int hr = hrList.get(i);
				maxs = Math.max(maxs, hr);
				mins = Math.min(mins, hr);
				total+=hr;
			}
		}
		int avgs = (int) (total*1.0 /size);
		
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.clear();
		c.setTime(date);
		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH) + 1);
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String min = String.valueOf(c.get(Calendar.MINUTE));

		List<HeartRateData> heartRateDatas = DataSupport.where("year=? and month=? and day=? and hour =? and min=?", year, month, day, hour, min).find(HeartRateData.class);
		if (heartRateDatas != null && heartRateDatas.size() > 0) {
		} else {
			// 说明数据库不存在
			Log.v("", "存在没有数据");
			if (datas != null && !datas.equals("")) {
				HeartRateData hrd = new HeartRateData(year, month, day, hour, min, datas);
				hrd.setMaxHr(maxs);
				hrd.setMinHr(mins);
				hrd.setAvghr(avgs);
				hrd.save();
				Log.v("", "保存心率成功 ：" + hrd.toString());
				saveHeartRateJson(hrd);
			}
		}

	}

	@Override
	public void parseData(byte[] data) {
		Log.e("SimpleBlueService", " __开始解析数据");
		ProtocolForNotify notify = ProtocolForNotify.instance();
		int type = notify.getTypeFromData(data);

		switch (type) {
		case ProtocolForNotify.NOTIFY_REMIND:
			try {
				// 防丢
				int notifyForRemind = notify.notifyForRemind(data);
				if (notifyForRemind == 0x01) {
					// this.player = ring();
					// mHandler.post( new Runnable() {
					// public void run() {
					mHander.postDelayed(new Runnable() {

						@Override
						public void run() {
							// if (findPhonePlay!=null) {
							// findPhonePlay.stop();
							// findPhonePlay.release();
							// findPhonePlay = null;
							// }
							if (musicService != null) {
								musicService.stop();
								musicService.play(R.raw.crystal, true);
							}
							// findPhonePlay = ring(R.raw.crystal);
							// showdialog(getResources().getString(R.string.find_phone));
						}
					}, 100);
					// showSystemDialog("查找手机", "查找手机中", "关闭");
					//
					// }
					// });
					// startDialogActivity("查找手机", "查找手机中", "关闭");
				} else if (notifyForRemind == 0xA1) {
					// if (findPhonePlay != null) {
					// findPhonePlay.stop();
					// findPhonePlay.release();
					// findPhonePlay = null;
					// }
					if (musicService != null) {
						musicService.stop();
					}
					// if (disconnectDiloag != null) {
					// disconnectDiloag.dismiss();
					// }
					// if (player!=null) {
					// player.stop();;
					// player.release();
					// player=null;
				}
				// player = ring();
				// if (dialogReminder!=null&&dialogReminder.isShowing()) {
				// dialogReminder.dismiss();
				// }
				// }
			} catch (Exception e) {
				// if (player!=null) {
				// player.release();
				// }
				if (player != null) {
					player.release();
					player = null;
					// isOnce = true;
				}
				if (disconnectDiloag != null) {
					disconnectDiloag.dismiss();
				}

			}
			break;
		case ProtocolForNotify.NOTIFY_SYNC_TIME:
			// 请求时间同步
			boolean notifyForSyncTime = notify.notifyForSyncTime(data);
			if (notifyForSyncTime) {
				writeCharacteristic(ProtocolForWrite.instance().getByteForSyncTime(new Date()));
			}

			break;
		case ProtocolForNotify.NOTIFY_CAMERA:
			// kai 手动打开照相界面
			// Intent it = new Intent(this, CameraActivity.class);
			// it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// startActivity(it);
			// 拍照
			int notifyForCamera = notify.notifyForCamera(data);
			Log.v("", "________________" + notifyForCamera);
			bleTakeCamera(notifyForCamera);

			// if (notifyForCamera==0x00) {
			//
			// }else if (notifyForCamera ==0x01) {
			// //guan
			// bleStopCamera();
			//
			// }
			break;
		case ProtocolForNotify.NOTIFY_MUSIC:

			// 音乐播放控制
			int notifyForMusic = notify.notifyForMusic(data);

			switch (notifyForMusic) {
			case 0x0108:// 播放
				Log.e("LiteBlueService", " 播放");
				if (musicService != null && musicService.getPlayingPosition() == -1) {
					musicService.setPlayingPosition(0);
				}
				if (musicService != null && musicService.getCurrentPostion() == 0) {
					Log.e("LiteBlueService", " 播放 : playByCurrentPlayingPosition()");
					musicService.playByCurrentPlayingPosition();
				} else {
					Log.e("LiteBlueService", " 播放 : seekto()");
					musicService.seekto();
				}
				// musicService.playByCurrentPlayingPosition(musicService.getPlayingPosition());
				break;
			case 0x0109:// 停止
				Log.e("LiteBlueService", " 停止");
				if (musicService != null) {
					musicService.pause();
				}
				break;
			case 0x0200:// 上一曲
			case 0x02:
				Log.e("LiteBlueService", " 下一曲");
				musicService.playUp();
				break;
			case 0x0300:// 下一曲
			case 0x03:
				Log.e("LiteBlueService", " 上一曲");
				musicService.playDown();

				break;
			case 0x0500:// 音量+

				break;
			case 0x0600:// 音量-

				break;
			case 0x01:// 播放
				if (musicService.isPlaying()) {
					musicService.pause();
					Log.e("LiteBlueService", " 播放--老版本");
				} else {
					Log.e("LiteBlueService", " 暂停——老版本");
					if (musicService.getPlayingPosition() == -1) {
						musicService.setPlayingPosition(0);
					}

					if (musicService.getCurrentPostion() == 0) {
						musicService.playByCurrentPlayingPosition();
					} else {

						musicService.seekto();
					}
				}
				break;
			default:
				break;
			}

			break;
		case ProtocolForNotify.NOTIFY_STEP:
			// 计步器
			PedoData stepData = notify.notifyForStepData(data);
			if (stepData != null) {
				int[] datas = new int[] { stepData.getStep(), stepData.getCal(), stepData.getDistance(), stepData.getHour(), stepData.getMinute(), stepData.getSecond() };
				bleDataForStep(datas);
			}
			break;
		case ProtocolForNotify.NOTIFY_HEART_RATE:
			HeartRateData hrData = notify.notifyForHeartRateData(data);
			if (hrData != null) {
				int hr = hrData.getHr();
				int maxHr = hrData.getMaxHr();
				int minHr = hrData.getMinHr();
				int avgHr = hrData.getAvghr();
				
				
				bleDataForHeartRate(hr);

				//第一次进去,判断心率是否大于0
				if (hr > 0) {
					isStart= true;
				}
				if (hr == 0) {
					isStart = false;
				}
				
				//判断是否开启
				if (isStart) {
					hrList.add(hr);
				}else{
		
					saveHeartRate(hrList);
					hrList.clear();
				}
					
					
				// int max = (int)
				// SharedPreferenceUtil.get(getApplicationContext(),
				// Constant.SHARE_HEART_RATE_MAX, 0);
				// int min = (int)
				// SharedPreferenceUtil.get(getApplicationContext(),
				// Constant.SHARE_HEART_RATE_MIN, 0);
				// if (hr > max || hr < min) {
				// mHander.postDelayed(new Runnable() {
				//
				// @Override
				// public void run() {
				// if (player != null && player.isPlaying()) {
				// player.stop();
				// player.release();
				// player = null;
				// }
				// ring(R.raw.luna);
				// }
				// }, 100);
				// } else {
				// if (player != null) {
				// player.stop();
				// player.release();
				// player = null;
				// }
				// }
			}

			// 心率
			break;
		case ProtocolForNotify.NOTIFY_HISTORY_HEART_RATE:
			// 历史数据 -- 心率
//			final HeartRateData heartRateData = notify.notifyForHistoryDataToHearRateData(data);
//			if (heartRateData != null) {
//				Log.v("LiteBlueService", "保存心率数据");
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						String year = heartRateData.getYear();
//						String month = heartRateData.getMonth();
//						String day = heartRateData.getDay();
//						List<HeartRateData> heartRateDatas = DataSupport.where("year=? and month=? and day=?", year, month, day).find(HeartRateData.class);
//						// 说明数据库存在
//						if (heartRateDatas != null && heartRateDatas.size() > 0) {
//							Log.v("LiteBlueService", "存在历史数据");
//							HeartRateData heartRateDataQuery = heartRateDatas.get(0);
//							heartRateDataQuery.setHr(heartRateData.getHr());
//							heartRateDataQuery.setAvghr(heartRateData.getAvghr());
//							heartRateDataQuery.setMaxHr(heartRateData.getMaxHr());
//							heartRateDataQuery.setMinHr(heartRateData.getMinHr());
//							heartRateDataQuery.save();
//						} else {
//							heartRateData.save();
//						}
//					}
//				}).start();
//			}

			break;
		case ProtocolForNotify.NOTIFY_HISTORY_STEP:
			// 历史数据 -- 计步
			final PedoData notifyForHistoryDataToStepData = notify.notifyForHistoryDataToStepData(data);
			if (notifyForHistoryDataToStepData != null) {
				Log.v("LiteBlueService", "保存计步数据");
				new Thread(new Runnable() {
					@Override
					public void run() {
						String year = notifyForHistoryDataToStepData.getYear();
						String month = notifyForHistoryDataToStepData.getMonth();
						String day = notifyForHistoryDataToStepData.getDay();
						List<PedoData> pedoDatas = DataSupport.where("year=? and month=? and day=?", year, month, day).find(PedoData.class);
						if (pedoDatas != null && pedoDatas.size() > 0) {
							Log.v("LiteBlueService", "____存在历史数据");
							pedoDatas.get(0).setStep(notifyForHistoryDataToStepData.getStep());
							pedoDatas.get(0).save();
						} else {
							notifyForHistoryDataToStepData.save();
						}
					}
				}).start();
			}
			break;
		case ProtocolForNotify.NOTIFY_HISTORY_SLEEP:
			// 历史数据 -- 睡眠
			final SleepData notifyForHistoryDataToSleepData = notify.notifyForHistoryDataToSleepData(data);
			if (notifyForHistoryDataToSleepData != null) {
				Log.v("LiteBlueService", "保存睡眠数据");
				new Thread(new Runnable() {
					@Override
					public void run() {
						Log.e("day", "notifyForHistoryDataToSleepData : " + notifyForHistoryDataToSleepData.toString());
						String year = notifyForHistoryDataToSleepData.getYear();
						String month = notifyForHistoryDataToSleepData.getMonth();
						String day = notifyForHistoryDataToSleepData.getDay();
						Log.e("day", "day : " + day);
						List<SleepData> sleepDatas = DataSupport.where("year=? and month=? and day=?", year, month, day).find(SleepData.class);
						if (sleepDatas != null && sleepDatas.size() > 0) {
							sleepDatas.get(0).setSdatas(notifyForHistoryDataToSleepData.getSdatas());
							sleepDatas.get(0).save();
						} else {
							notifyForHistoryDataToSleepData.save();
						}
					}
				}).start();
			}
			break;
		case ProtocolForNotify.NOTIFY_HISTORY_DISTACE:
			// 历史数据 -- 距离
			final PedoData notifyForHistoryDataToDistanceData = notify.notifyForHistoryDataToDistanceData(data);
			if (notifyForHistoryDataToDistanceData != null) {
				Log.v("LiteBlueService", "保存计步-距离 数据");
				new Thread(new Runnable() {
					@Override
					public void run() {
						String year = notifyForHistoryDataToDistanceData.getYear();
						String month = notifyForHistoryDataToDistanceData.getMonth();
						String day = notifyForHistoryDataToDistanceData.getDay();
						List<PedoData> pedoDatas = DataSupport.where("year=? and month=? and day=?", year, month, day).find(PedoData.class);
						if (pedoDatas != null && pedoDatas.size() > 0) {
							Log.v("LiteBlueService", "____存在历史数据");
							pedoDatas.get(0).setDistance(notifyForHistoryDataToDistanceData.getDistance());
							pedoDatas.get(0).save();
						} else {
							notifyForHistoryDataToDistanceData.save();
						}
					}
				}).start();
			}

			break;
		case ProtocolForNotify.NOTIFY_HISTORY_CAL:
			// 历史数据 -- 卡洛里
			final PedoData notifyForHistoryDataToCalData = notify.notifyForHistoryDataToCalData(data);
			if (notifyForHistoryDataToCalData != null) {
				Log.v("LiteBlueService", "保存计步-卡洛里数据");
				new Thread(new Runnable() {

					@Override
					public void run() {
						String year = notifyForHistoryDataToCalData.getYear();
						String month = notifyForHistoryDataToCalData.getMonth();
						String day = notifyForHistoryDataToCalData.getDay();
						List<PedoData> pedoDatas = DataSupport.where("year=? and month=? and day=?", year, month, day).find(PedoData.class);
						if (pedoDatas != null && pedoDatas.size() > 0) {
							Log.v("LiteBlueService", "____存在历史数据");
							pedoDatas.get(0).setCal(notifyForHistoryDataToCalData.getCal());
							pedoDatas.get(0).save();
						} else {
							notifyForHistoryDataToCalData.save();
						}
					}
				}).start();
			}
			break;
		case ProtocolForNotify.NOTIFY_HISTORY_SPORT_TIME:
			// 历史数据 -- 运动时间
			final PedoData notifyForHistoryDataToSportTimeData = notify.notifyForHistoryDataToSportTimeData(data);
			if (notifyForHistoryDataToSportTimeData != null) {
				Log.v("LiteBlueService", "保存计步-运动时间数据");
				new Thread(new Runnable() {

					@Override
					public void run() {
						String year = notifyForHistoryDataToSportTimeData.getYear();
						String month = notifyForHistoryDataToSportTimeData.getMonth();
						String day = notifyForHistoryDataToSportTimeData.getDay();
						List<PedoData> pedoDatas = DataSupport.where("year=? and month=? and day=?", year, month, day).find(PedoData.class);
						if (pedoDatas != null && pedoDatas.size() > 0) {
							Log.v("LiteBlueService", "____存在历史数据");
							pedoDatas.get(0).setHour(notifyForHistoryDataToSportTimeData.getHour());
							pedoDatas.get(0).setMinute(notifyForHistoryDataToSportTimeData.getMinute());
							pedoDatas.get(0).setSecond(notifyForHistoryDataToSportTimeData.getSecond());
							pedoDatas.get(0).save();
						} else {
							notifyForHistoryDataToSportTimeData.save();
						}
					}
				}).start();
			}
			break;
		case ProtocolForNotify.NOTIFY_HISTORY_SLEEP_FOR_TODAY:
			// 历史数据 -- 今天睡眠数据
			SleepData todaySleepData = notify.notifyForHistoryDataToTodaySleepData(data);
			if (todaySleepData != null) {
				bleDataForSleep(todaySleepData.getSdatas());
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onReconnectedOverTimeOut() {
		// if (null != dialogSyncSetting && dialogSyncSetting.isShowing()) {
		// dialogSyncSetting.dismiss();
		// }
		// // if (isOnce) {
		//
		// mHander.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// // if (findPhonePlay != null) {
		// // findPhonePlay.stop();
		// // findPhonePlay.release();
		// // findPhonePlay = null;
		// // }
		// // if (player != null && player.isPlaying()) {
		// // player.stop();
		// // player.release();
		// // player = null;
		// // }
		// // if (player != null && player.isPlaying()) {
		// // player.stop();
		// // player.release();
		// // player = null;
		// // }
		// // ring(R.raw.crystal);
		// showdialog(getResources().getString(R.string.device_is_not_connected));
		// if (musicService != null ) {
		// Log.e("", "短线了，关闭音乐");
		// musicService.stop();
		// }
		// // isOnce = false;
		// musicService.play(R.raw.jump,true);
		// }
		// }, 100);
		// // }
		//
		// mHander.postDelayed(remindCloseRunnable, 10*1000);
	}

	// /**
	// * 关闭断线声音提示
	// */
	// private Runnable remindCloseRunnable = new Runnable() {
	// @Override
	// public void run() {
	// if (musicService!=null) {
	// musicService.stop();
	// }
	// }
	// };
	@Override
	public void onServicediscoveredSuccess() {

		// closeDisconnectRemind();
		// mHander.removeCallbacks(remindCloseRunnable);
		mHander.postDelayed((new Runnable() {
			@Override
			public void run() {
				doUpdateSetting();
			}
		}), 200);
		mHander.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.e("", "同步超时");
				if (null != dialogSyncSetting && dialogSyncSetting.isShowing()) {
					dialogSyncSetting.dismiss();
				}
			}
		}, 15 * 1000);

//		mHander.removeCallbacks(taskIncoming);

	}

	@Override
	public void onServicediscoveredFail() {
	}

	@Override
	public void onWriteOver() {
		if (dialogSyncSetting != null && dialogSyncSetting.isShowing()) {
			dialogSyncSetting.dismiss();
		}

		// 同步所有历史数据
		byte[] byteForSyncHistoryData = null;
		long timeSave = (long) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_UPDATE_TIME, 0L);
		long timeToday = System.currentTimeMillis();// 记录第一次同步的时间
		if (!DateUtil.isSameDayOfMillis(timeSave, timeToday)) {// 当不是同一天时，更新今天的时间，然后再发送同步今天所有的数据
			Log.e("MainActivity", "今天第一次进入App，同步所有的历史数据");
			SharedPreferenceUtil.put(getApplicationContext(), Constant.SHARE_UPDATE_TIME, timeToday);
			byteForSyncHistoryData = ProtocolForWrite.instance().getByteForSyncHistoryData();
			writeCharacteristic(byteForSyncHistoryData);
		}
	}

	@Override
	public void onDisconnected() {
		phoneNo = -1;
		smsMNo = -1;
		
		hrList.clear();
		isStart = false;
	}

	// /**
	// * 关闭 断连提醒
	// */
	// private void closeDisconnectRemind(){
	// if (musicService!=null&&musicService.isPlaying()) {
	// musicService.stop();
	// musicService.release();
	// }
	// if (null != disconnectDiloag && disconnectDiloag.isShowing()) {
	// disconnectDiloag.dismiss();
	// }
	// }

	private void showdialog(String msg) {
		if (disconnectDiloag != null) {
			disconnectDiloag.dismiss();
		}
		disconnectDiloag = new com.mycj.mywatch.view.AlertDialog(getApplicationContext()).builder().setMsg(msg).setCancelable(true).setPositiveButton("关闭", new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				try {
					// if (player != null) {
					// player.stop();
					// player.release();
					// player = null;
					// }
					if (musicService != null) {
						musicService.stop();
					}
					// isOnce = true;
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		});
		disconnectDiloag.setCancelable(false);
		disconnectDiloag.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		disconnectDiloag.show();
	}

	private void bleTakeCamera(int notifyForCamera) {
		Intent intent = new Intent(ACTION_DATA_CAMERA);
		intent.putExtra(EXTRA_CAMERA, notifyForCamera);
		sendBroadcast(intent);
	}
	
	public final static  String EXTRA_HEART_RATE_MAX = "EXTRA_HEART_RATE_MAX";
	public final static String  EXTRA_HEART_RATE_MIN = "EXTRA_HEART_RATE_MIN";
	public final static String  EXTRA_HEART_RATE_AVG = "EXTRA_HEART_RATE_AVG";
	
	private void bleDataForHeartRate(int hr) {
		Intent intent = new Intent(ACTION_DATA_HEART_RATE);
		intent.putExtra(EXTRA_HEART_RATE, hr);
//		intent.putExtra(EXTRA_HEART_RATE_MAX, max);
//		intent.putExtra(EXTRA_HEART_RATE_MIN, min);
//		intent.putExtra(EXTRA_HEART_RATE_AVG, avg);
		sendBroadcast(intent);
	}

	private void bleDataForSleep(String sleeps) {
		Intent intent = new Intent(ACTION_DATA_HISTORY_SLEEP_FOR_TODAY);
		intent.putExtra(EXTRA_SLEEP, sleeps);
		sendBroadcast(intent);

	}

	private void bleDataForStep(int[] datas) {
		Intent intent = new Intent(ACTION_DATA_STEP);
		intent.putExtra(EXTRA_STEP, datas);
		sendBroadcast(intent);
	}

	/**
	 * 设置铃声
	 */
	private void initRing() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// 获取手机铃声音量
		int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
		RingtoneManager ringtoneManager = new RingtoneManager(this);
		ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE);
		ringtone = RingtoneManager.getRingtone(this, getResourceUri(R.raw.a1, getPackageName()));
		ringtone.play();
	}

	private int getSleepValue(int sleepValue) {
		float value = 0f;
		switch (sleepValue) {
		// case 0:
		// rectPaint.setColor(colorAwake);
		// result = 1f;
		// break;
		// case 1:
		// rectPaint.setColor(colorAwake);
		// total += 0.25f;// 获得总的睡眠时间
		// result = 1f;
		// break;
		// case 2:
		// total += 0.75f;// 获得总的睡眠时间
		// rectPaint.setColor(colorLight);
		// result = 2 / 3f;
		// break;
		// case 3:
		// result = 2 / 3f;
		// total += 1f;// 获得总的睡眠时间
		// rectPaint.setColor(colorLight);
		// break;
		// case 4:
		// total += 1f;// 获得总的睡眠时间
		// result = 1 / 3f;
		// rectPaint.setColor(colorDeep);
		// break;
		// case 5:
		// total += 1f;// 获得总的睡眠时间
		// result = 1 / 3f;
		// rectPaint.setColor(colorDeep);
		// break;
		default:
			break;
		}
		return 0;
	}

	/**
	 * 获取系统当前铃音的URI
	 * 
	 * @return
	 */
	private Uri getSystemDefultRingtoneUri() {
		return RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
	}

	/**
	 * 判断用户是否在前台
	 * 
	 * @param context
	 * @return
	 */
	private boolean isRunningForeground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String currentPackageName = cn.getPackageName();
		if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(getPackageName())) {
			return true;
		}
		return false;
	}

	public Uri getResourceUri(int resId, String packageName) {
		return Uri.parse("android.resource://" + packageName + "/" + resId);
	}

	public int getToalSleep(int[] sleeps) {
		int total = 0;
		for (int i = 0; i < sleeps.length; i++) {

			total += getSleepValue(sleeps[i]);
		}
		return total;
	}

	/**
	 * 获取断线铃音
	 * 
	 * @return
	 * @throws IllegalStateException
	 * @throws Exception
	 * @throws IOException
	 */
	public MediaPlayer ring(int music) {
		try {
			player = MediaPlayer.create(getApplicationContext(), music);
			player.setLooping(true);
			player.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer arg0) {
					player.start();
				}
			});
			player.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return player;
	}

	private void doUpdateSetting() {
		if (dialogSyncSetting == null) {
			dialogSyncSetting = new ProgressDialog(getApplicationContext());
			dialogSyncSetting.setCancelable(true);
			dialogSyncSetting.setMessage(getApplicationContext().getString(R.string.sync_all_ing));
			dialogSyncSetting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialogSyncSetting.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}

		if (isRunningForeground(getApplicationContext())) {
			Log.i("", "-----------在前台----------");
			dialogSyncSetting.show();
		}
		if (isBinded() && getConnectState() == BluetoothProfile.STATE_CONNECTED) {
			if (bytes != null) {
				writeValueToDevice(bytes);
			}

			// 第一次进入App 同步数据
			// 1。同步时间
			byte[] byteForSyncTime = ProtocolForWrite.instance().getByteForSyncTime(new Date());
			// 2。同步睡眠开始 结束时间
			int start = (int) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_SLEEP_START_HOUR, 0);
			int end = (int) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_SLEEP_END_HOUR, 0);
			byte[] byteForSleepTime = ProtocolForWrite.instance().getByteForSleepTime(start, end);
			// 3。最大最小心率
			int maxHr = (int) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_HEART_RATE_MAX, 240);
			int minHr = (int) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_HEART_RATE_MIN, 40);
			byte[] byteForHeartRate = ProtocolForWrite.instance().getByteForHeartRate(maxHr, minHr);
			// 4。闹钟时间
			// 获取初始值
			int hour_1 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_1, 12);
			int min_1 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_1, 00);
			int hour_2 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_2, 12);
			int min_2 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_2, 00);
			int hour_3 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_3, 12);
			int min_3 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_3, 00);
			int hour_4 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_4, 12);
			int min_4 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_4, 00);
			int hour_5 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_HOUR_5, 12);
			int min_5 = (int) SharedPreferenceUtil.get(this, Constant.SHARE_CLOCK_MIN_5, 00);
			boolean isChecked_1 = (boolean) SharedPreferenceUtil.get(getApplicationContext(), Constant.SHARE_CHECK_BOX_CLOCK_1, false);
			boolean isChecked_2 = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_2, false);
			boolean isChecked_3 = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_3, false);
			boolean isChecked_4 = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_4, false);
			boolean isChecked_5 = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_BOX_CLOCK_5, false);
			byte[] byteForAlarmClock = ProtocolForWrite.instance().getByteForAlarmClock(new int[] { hour_1, min_1, hour_2, min_2, hour_3, min_3, hour_4, min_4, hour_5, min_5 },
					new boolean[] { isChecked_1, isChecked_2, isChecked_3, isChecked_4, isChecked_5 });
			// 5。请求今天的睡眠数据
			byte[] byteForSleepQualityOfToday = ProtocolForWrite.instance().getByteForSleepQualityOfToday(0);

			// byte[] byteForWeather = null;
			// //7.同步天气 ？
			// String wieid = (String)
			// SharedPreferenceUtil.get(getApplicationContext(),
			// Constant.SHARE_PLACE_WOEID, "");
			// // if (wieid != null) {
			// // String weather= (String)
			// SharedPreferenceUtil.get(getApplicationContext(),
			// Constant.SHARE_PLACE_WEATHER, "");
			// // String unit= (String)
			// SharedPreferenceUtil.get(getApplicationContext(),
			// Constant.SHARE_PLACE_UNIT, "");
			// // String temp = (String)
			// SharedPreferenceUtil.get(getApplicationContext(),
			// Constant.SHARE_PLACE_TEMP, "");
			// // byteForWeather =
			// ProtocolForWrite.instance().getByteForWeather(weather,
			// unit,temp);
			// // }

			List<byte[]> values = new ArrayList<>();
			values.add(byteForSyncTime);
			values.add(byteForSleepTime);
			values.add(byteForHeartRate);
			values.add(byteForAlarmClock);
			values.add(byteForSleepQualityOfToday);
			// if (byteForSyncHistoryData != null) {
			// values.add(byteForSyncHistoryData);
			// }
			// if (byteForWeather!=null) {
			// values.add(byteForWeather);
			// }
			writeValueToDevice(values);
		}
	}

	/**
	 * 未接来电和未读短信提醒
	 */
	private void doWriteUnReadPhoneAndSmsToWatch(int phone, int sms) {
		Log.e("", "___________doWriteUnReadPhoneAndSmsToWatch" + sms);

	
		if ( getConnectState() == BluetoothProfile.STATE_CONNECTED && isBinded()) {
			Log.e("", "___________更新短信来电数量");
			writeCharacteristic(ProtocolForWrite.instance().getByteForMissedCallAndMessage(phone, sms));
			// 只有当 改变了 ，才改变当前phoneNo 和smsNo
			smsMNo = sms;
			Log.e("", "___________更新短信来电数量后的值 ：" + smsMNo);
			phoneNo = phone;

		}
	}

	private String dataToString(List<Integer> listHr) {
		StringBuffer sb = new StringBuffer();
		
		if (listHr.size() > 0) {
			for (int i = 0; i < listHr.size(); i++) {
				sb.append(String.valueOf(listHr.get(i)));
				if (i != listHr.size() - 1) {
					sb.append(",");
				}
			}
		}
		return sb.toString();
	}

	private int phoneNo = -1;
	private int smsMNo = -1;
	private List<Integer> hrList = new ArrayList<>();;
	private boolean isStart;

}
