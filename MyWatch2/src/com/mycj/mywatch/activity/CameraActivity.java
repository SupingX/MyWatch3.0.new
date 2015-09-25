package com.mycj.mywatch.activity;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.MainActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.business.CameraManager;
import com.mycj.mywatch.business.CameraManager.CameraOpenReady;
import com.mycj.mywatch.service.AbstractSimpleBlueService;
import com.mycj.mywatch.service.SimpleBlueService;
import com.mycj.mywatch.util.DataUtil;
import com.mycj.mywatch.util.DisplayUtil;
import com.mycj.mywatch.view.CameraSurfaceView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CameraActivity extends BaseActivity implements OnClickListener {
	private final String TAG = "CameraActivity";
	private FrameLayout frame;
	private CameraSurfaceView cameraSurfaceView;
	private ImageView imgTakePicture;
	private float previewRate = -1f;
	private CameraManager mCameraManager;
	private AbstractSimpleBlueService mSimpleBlueService;
	private Handler mHandler = new Handler() 
	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x01:
				
				break;

			default:
				break;
			}
		};

	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(SimpleBlueService.ACTION_DATA_CAMERA)) {
				int notifyForCamera = intent.getExtras().getInt(SimpleBlueService.EXTRA_CAMERA);
				switch (notifyForCamera) {
				case 0:
					mHandler.post(takePictureThread);
					break;
				case 1:
					startActivity(new Intent(CameraActivity.this,MainActivity.class));
					finish();
					break;

				default:
					break;
				}
			}

		}
	};
	private SurfaceHolder mSurfaceHolder;
	private Runnable openThread = new Runnable() {
	
		@Override
		public void run() {
			CameraManager.instance().openCamera(new CameraOpenReady() {
			

				// 打开完成后，开启预览
				@Override
				public void openReady() {
					mSurfaceHolder = cameraSurfaceView.getSurfaceHolder();
					boolean isSurfaceCreate = mSurfaceHolder.isCreating();
					Log.d(TAG, "mSurfaceHolder : " + isSurfaceCreate);
					mCameraManager.startPreview(mSurfaceHolder, previewRate);
				}
			});
		}
	};


	private Runnable takePictureThread = new Runnable() {
	
		@Override
		public void run() {
			mCameraManager.takePicture();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 拍照过程屏幕一直处于高亮
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // //设置手机屏幕朝向，一共有7种

		setContentView(R.layout.activity_camera);
		registerReceiver(mReceiver, SimpleBlueService.getIntentFilter());
		mCameraManager = CameraManager.instance();
		initViews();
		setListener();

	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mSimpleBlueService = getSimpleBlueService();
	}
	@Override
	protected void onResume() {
		super.onResume();
		// 打开相机
		// mHandler.post(openThread);
//		new Thread(openThread).start();
		
		mHandler.postDelayed(openThread, 1000);

	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (null!=mSimpleBlueService&&mSimpleBlueService.isBinded()&&mSimpleBlueService.getConnectState()==BluetoothProfile.STATE_CONNECTED) {
			mSimpleBlueService.writeCharacteristic(DataUtil.hexStringToByte("F501"));
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeCallbacks(openThread);
		mHandler.removeCallbacks(takePictureThread);
mCameraManager.stopPreviewCamera();
	}

	@Override
	public void initViews() {
		frame = (FrameLayout) findViewById(R.id.frame_camera);
		imgTakePicture = (ImageView) findViewById(R.id.img_take_picture);
		cameraSurfaceView = (CameraSurfaceView) findViewById(R.id.surface_camera);
		initCameraSurfaceViewParams();
	}

	private void initCameraSurfaceViewParams() {
		ViewGroup.LayoutParams params = cameraSurfaceView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		params.width = p.x;
		params.height = p.y;
		// 默认全屏的比例预览
		previewRate = DisplayUtil.getScreenRate(this);
		cameraSurfaceView.setLayoutParams(params);

	}

	@Override
	public void setListener() {
		imgTakePicture.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_take_picture:
//			mHandler.removeCallbacks(takePictureThread);
//			new Thread(takePictureThread).start();
			mHandler.post(takePictureThread);
		
//			mCameraManager.startPreview(cameraSurfaceView.getSurfaceHolder(), previewRate);
//			mCameraManager.setPreviewing(true);
			
//			startAnimation(imgTakePicture);
//			mHandler.sendEmptyMessage(0x01);
			break;

		default:
			break;
		}
	}
	
	private void startAnimation(View img) {
		// ObjectAnimator oaTranslationX = ObjectAnimator.ofFloat(img,
		// "translationX", 0, mScreenWidth / 2);
		// ObjectAnimator oaTranslationY = ObjectAnimator.ofFloat(img,
		// "translationY", 0, mScreenHeight / 2);
		ObjectAnimator oaScaleX = ObjectAnimator.ofFloat(img, "scaleX", 0.5f, 1.2f, 1f);
		ObjectAnimator oaScaleY = ObjectAnimator.ofFloat(img, "scaleY", 0.5f, 1.2f, 1f);

		// oaTranslationX.setDuration(1000);
		// oaTranslationY.setDuration(1000);
		// oaScaleX.setDuration(1000);
		// oaScaleY.setDuration(1000);
		AnimatorSet set = new AnimatorSet();
		set.play(oaScaleX).with(oaScaleY);
		set.setDuration(500);
		set.start();
	}
	
}
