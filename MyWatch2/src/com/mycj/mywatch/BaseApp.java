package com.mycj.mywatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.business.ProtocolForWrite;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.AbstractSimpleBlueService.OnDisconnectListener;
import com.mycj.mywatch.service.MusicService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.MessageUtil;
import com.mycj.mywatch.util.SharedPreferenceUtil;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BaseApp extends Application  {

	private final static int DIFF = 6 * 1000; // 4秒运行一次
	private MusicService mMusicService;
	private TelephonyManager telephony;
	private AbstractSimpleBlueService mSimpleBlueService;
	public static final String TAG = "ImiChatSMSReceiver";
	public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
	/**
	 * 音乐服务通信
	 */
	private ServiceConnection musicConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMusicService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mMusicService = ((MusicService.MyBinder) service).getMusicService();
			
		}
	};
	/**
	 * 蓝牙服务通信
	 */
	private ServiceConnection blueConnection = new ServiceConnection() {

		

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mSimpleBlueService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mSimpleBlueService = ((AbstractSimpleBlueService.MyBinder) service).getService();
//			bluetoothStateBroadcastReceiver = new BluetoothStateBroadcastReceiver(getApplicationContext(), mSimpleBlueService);
//			registerBoradcastReceiverForCheckBlueToothState(bluetoothStateBroadcastReceiver);
//			
//			if (!mSimpleBlueService.isEnable()) {
//					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//					enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					startActivity(enableBtIntent);
//				// showIosDialog();
//			}else{
//				mSimpleBlueService.scanDevice(true);
//			}
		}
	};

	// /**
	// * 短信数量监听
	// */
	// private ContentObserver newMmsContentObserver = new ContentObserver(new
	// Handler()) {
	// @Override
	// public void onChange(boolean selfChange, Uri uri) {
	// super.onChange(selfChange, uri);
	// Log.e("baseApp",
	// "-----------------------------------------------------");
	// int mNewSmsCount = MessageUtil.getNewSmsCount(getApplicationContext()) +
	// MessageUtil.getNewMmsCount(getApplicationContext());
	// int phone = MessageUtil.readMissCall(getApplicationContext());
	// Log.e("baseApp", "mNewSmsCount___________未读短信____________________" +
	// mNewSmsCount);
	// Log.e("baseApp", "mNewSmsCount___________未姐电话____________________" +
	// phone);
	// doWriteUnReadPhoneAndSmsToWatch(phone,mNewSmsCount);
	// mHandle.removeCallbacks(task);
	// mHandle.post(task);
	// }
	// };
	//
	// /**
	// * 电话数量监听
	// */
	// private ContentObserver newCallContentObserver = new ContentObserver(new
	// Handler()) {
	// @Override
	// public void onChange(boolean selfChange, Uri uri) {
	// int mNewSmsCount = MessageUtil.getNewSmsCount(getApplicationContext()) +
	// MessageUtil.getNewMmsCount(getApplicationContext());
	// int phone = MessageUtil.readMissCall(getApplicationContext());
	// Log.e("baseApp", "mNewSmsCount___________未读短信____________________" +
	// mNewSmsCount);
	// Log.e("baseApp", "mNewSmsCount___________未姐电话____________________" +
	// phone);
	// doWriteUnReadPhoneAndSmsToWatch(phone,mNewSmsCount);
	// super.onChange(selfChange, uri);
	// mHandle.removeCallbacks(task);
	// mHandle.post(task);
	//
	//
	// }
	//
	// };
	/**
	 * 来电电话 监听
	 */
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			Log.v("BaseApp", "CallListener call state changed ---- [incomingNumber : " + incomingNumber + ",state : " + state + "]");
			// String m = null;
			// // 如果当前状态为空闲,上次状态为响铃中的话,则认为是未接来电
			// if (lastetState == TelephonyManager.CALL_STATE_RINGING && state
			// == TelephonyManager.CALL_STATE_IDLE) {
			// sendSmgWhenMissedCall(incomingNumber);
			// }
			// // 最后改变当前值
			// lastetState = state;
			// telephony.listen(mPhoneStateListener, events);
			if (state == TelephonyManager.CALL_STATE_RINGING) {
				Log.v("BaseApp", "电话来了");
				doWriteIncomingPhoneToWatch(incomingNumber);
				// mHandle.removeCallbacks(task);
				// mHandle.post(task);
			} else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
				boolean isCallRemind;
				isCallRemind = (boolean) SharedPreferenceUtil.get(BaseApp.this, Constant.SHARE_CHECK_REMIND_CALL, false);
				if (null != mSimpleBlueService && isCallRemind && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
					mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForRemind(0x00, "00"));
				}
				// mHandle.removeCallbacks(task);
				// mHandle.post(task);
			}
		}
	};
	/**
	 * 新来短信 监听
	 */
	private BroadcastReceiver mPhoneReceiver = new BroadcastReceiver() {
		// public void onReceive(Context context, Intent intent) {
		// if
		// (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
		// {
		// Bundle bundle = intent.getExtras();
		// SmsMessage msg = null;
		// if (null != bundle) {
		// Object[] smsObj = (Object[]) bundle.get("pdus");
		// for (Object obj : smsObj) {
		// msg = SmsMessage.createFromPdu((byte[]) obj);
		// Date date = new Date(msg.getTimestampMillis());// 时间
		// SimpleDateFormat format = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// String receiveTime = format.format(date);
		// String address = msg.getOriginatingAddress();
		// System.out.println("number:" + address + "   body:" +
		// msg.getDisplayMessageBody() + "  time:" + msg.getTimestampMillis());
		// doWriteIncomingSmsToWatch(address);
		// }
		// }
		// }
		// };

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SMS_RECEIVED_ACTION))

			{
				SmsMessage[] messages = getMessagesFromIntent(intent);
				for (SmsMessage message : messages) {
					Log.i(TAG, message.getOriginatingAddress() + " : " + message.getDisplayOriginatingAddress() + " : " + message.getDisplayMessageBody() + " : " + message.getTimestampMillis());
					doWriteIncomingSmsToWatch(message.getOriginatingAddress());
				}

			}
		}

	};

	public final SmsMessage[] getMessagesFromIntent(Intent intent)

	{

		Object[] messages = (Object[]) intent.getSerializableExtra("pdus");

		byte[][] pduObjs = new byte[messages.length][];

		for (int i = 0; i < messages.length; i++)

		{

			pduObjs[i] = (byte[]) messages[i];

		}

		byte[][] pdus = new byte[pduObjs.length][];

		int pduCount = pdus.length;

		SmsMessage[] msgs = new SmsMessage[pduCount];

		for (int i = 0; i < pduCount; i++)

		{

			pdus[i] = pduObjs[i];

			msgs[i] = SmsMessage.createFromPdu(pdus[i]);

		}

		return msgs;

	}

//	private int phoneNo=-1;
//	private int smsMNo=-1;
//	/**
//	 * 查询 未接短信和电话数量 任务
//	 */
//	private Runnable taskIncoming = new Runnable() {
//
//		@Override
//		public void run() {
//			int mmsCount = MessageUtil.getNewMmsCount(getApplicationContext());
//			int msmCount = MessageUtil.getNewSmsCount(getApplicationContext());
//			int phoneCount = MessageUtil.readMissCall(getApplicationContext());
//			Log.e("BaseApp", "____电话数量 ： "+phoneNo +"-->"+phoneCount);
//			Log.e("BaseApp", "____短信 ： " + smsMNo+"-->"+(msmCount+mmsCount) +(smsMNo != (mmsCount + msmCount)));
//			// 数量只要有一个变化就发送
//			if (phoneNo != phoneCount || smsMNo != (mmsCount + msmCount)) {
//				Log.e("BaseApp", "__读取短信和电话数量 ： 有变化");
////				if (mmsCount == 0 && msmCount == 0 && phoneCount == 0) {
////					doWriteUnReadPhoneAndSmsToWatch(0, 0);
////					return;
////				} else {
//					doWriteUnReadPhoneAndSmsToWatch(phoneCount, (mmsCount+msmCount));
////				}
//			
//			}else{
//				Log.e("BaseApp", "__读取短信和电话数量 ： 无变化");
//			}
//			mHandle.sendEmptyMessage(1);
//		}
//	};
//	private Handler mHandle = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case 1:
//				mHandle.removeCallbacks(taskIncoming);
//				mHandle.postDelayed(taskIncoming, DIFF);
//				break;
//			default:
//				break;
//			}
//		};
//
//	};

//	private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
//				Log.e("BaseApp", "ACTION_ACL_CONNECTED");
//			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
//				// mSimpleBlueService.closeRing();
//				Log.e("BaseApp", "ACTION_ACL_DISCONNECTED");
//			} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
//				int state = intent.getExtras().getInt(BluetoothAdapter. EXTRA_STATE);
//				int previousState = intent.getExtras().getInt(BluetoothAdapter.EXTRA_PREVIOUS_STATE);
//				Log.e("BaseApp", "__ACTION_STATE_CHANGED");
//				Log.e("BaseApp", "____state : " + state);
//				Log.e("BaseApp", "____state_previous : " + previousState);
//				
//			} else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
//				Log.e("BaseApp", "ACTION_CONNECTION_STATE_CHANGED");
//			}
//		}
//	};
	
//	private BluetoothStateBroadcastReceiver bluetoothReceiver = new BluetoothStateBroadcastReceiver(getApplicationContext());
	@Override
	public void onCreate() {
		
		// 音乐服务
		Intent musicIntent = new Intent(this, MusicService.class);
		bindService(musicIntent, musicConnection, Context.BIND_AUTO_CREATE);
		// 蓝牙服务
		Intent simpleIntent = new Intent(this, SimpleBlueService.class);
		bindService(simpleIntent, blueConnection, Context.BIND_AUTO_CREATE);
		// 手机监听服务
		telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		IntentFilter filter = new IntentFilter();
		// filter.addAction("android.intent.action.PHONE_STATE");
		filter.addAction(SMS_RECEIVED_ACTION);
		registerReceiver(mPhoneReceiver, filter);
		telephony.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

//		int mmsCount = MessageUtil.getNewMmsCount(getApplicationContext());
//		int msmCount = MessageUtil.getNewSmsCount(getApplicationContext());
//		int phoneCount = MessageUtil.readMissCall(getApplicationContext());
//		phoneNo = phoneCount;
//		smsMNo = mmsCount + msmCount;
		
		// registerObserver();
		// mHandle.postDelayed(task, 1000);
		// 短信 第一次进入
		// boolean isCallRemind;
		// int mNewSmsCount =
		// MessageUtil.getNewSmsCount(getApplicationContext()) +
		// MessageUtil.getNewMmsCount(getApplicationContext());
		// int phone = MessageUtil.readMissCall(getApplicationContext());
		// isCallRemind = (boolean) SharedPreferenceUtil.get(this,
		// Constant.SHARE_CHECK_REMIND_CALL, false);
		// if (null != mSimpleBlueService && isCallRemind &&
		// mSimpleBlueService.getConnectState() ==
		// BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
		// mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForMissedCallAndMessage(phone,
		// mNewSmsCount));
		// }
//		mHandle.postDelayed(taskIncoming, 10*000);
//		registerBoradcastReceiverForCheckBlueToothState();
		super.onCreate();
		
	
	}

	@Override
	public void onTerminate() {
		
		mSimpleBlueService.close();
//		mHandle.removeCallbacks(taskIncoming);
		unbindService(musicConnection);
//		mMusicService.stopSelf();
		unbindService(blueConnection);
//		mSimpleBlueService.stopSelf();
		super.onTerminate();
	}

	public MusicService getMusicService() {
		return mMusicService;
	}

	public AbstractSimpleBlueService getSimpleBlueService() {
		return this.mSimpleBlueService;
	}

	/**
	 * 来电提醒
	 * 
	 * @param incomingNumber
	 */
	private void doWriteIncomingPhoneToWatch(String incomingNumber) {
		boolean isCallRemind;
		isCallRemind = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_REMIND_CALL, false);

		if (null != mSimpleBlueService && isCallRemind && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
			mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForRemind(0x80, incomingNumber));
		}
	};

	/**
	 * 来短信提醒
	 */
	private void doWriteIncomingSmsToWatch(String incomingNumber) {
		boolean isCallRemind;
		isCallRemind = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_REMIND_CALL, false);
		if (null != mSimpleBlueService && isCallRemind && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
			mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForRemind(0x20, incomingNumber));
		}
	}
//
//	/**
//	 * 未接来电和未读短信提醒
//	 */
//	private void doWriteUnReadPhoneAndSmsToWatch(int phone, int sms) {
//		Log.e("", "___________doWriteUnReadPhoneAndSmsToWatch" +sms);
//		
//		boolean isCallRemind;
//		isCallRemind = (boolean) SharedPreferenceUtil.get(this, Constant.SHARE_CHECK_REMIND_CALL, false);
//		if (null != mSimpleBlueService && isCallRemind && mSimpleBlueService.getConnectState() == BluetoothProfile.STATE_CONNECTED && mSimpleBlueService.isBinded()) {
//			Log.e("", "___________更新短信来电数量");
//			mSimpleBlueService.writeCharacteristic(ProtocolForWrite.instance().getByteForMissedCallAndMessage(phone, sms));
//			//只有当 改变了 ，才改变当前phoneNo 和smsNo
//			smsMNo = sms;
//			Log.e("", "___________更新短信来电数量后的值 ：" + smsMNo);
//			phoneNo = phone;
//		
//		
//		}
//	}

	// /**
	// * 注册监听短信
	// */
	// private void registerObserver() {
	// unregisterObserver();
	// // 在服务创建的时候注册ContentObserver，之后就会一直存在
	// getContentResolver().registerContentObserver(Uri.parse("content://sms"),
	// true, newMmsContentObserver);
	// getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI,
	// true, newCallContentObserver);
	// }

	// private void unregisterObserver() {
	// try {
	// if (newMmsContentObserver != null) {
	// getContentResolver().unregisterContentObserver(newMmsContentObserver);
	// }
	// if (newCallContentObserver != null) {
	// getContentResolver().unregisterContentObserver(newCallContentObserver);
	// }
	// } catch (Exception e) {
	// Log.e("", "unregisterObserver fail");
	// }
	// }




}
