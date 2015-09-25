package com.mycj.mywatch;


import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class DialogActivity extends BaseActivity {
	private MediaPlayer mediaPlayer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 隐藏标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setFinishOnTouchOutside(false);// 设置为true点击区域外消失
		setContentView(R.layout.activity_dialog);
		
	
		TextView tvBtn = (TextView) findViewById(R.id.btn_neg);
		TextView tvMsg = (TextView) findViewById(R.id.txt_msg);
		TextView tvTitle = (TextView) findViewById(R.id.txt_title);
		
		setDisplay();
		
		
		try {
			if (mediaPlayer==null) {
				mediaPlayer = ring();
			}
		} catch (Exception e) {
			if (mediaPlayer!=null) {
				mediaPlayer.release();
				mediaPlayer=null;
			}
			e.printStackTrace();
		}
		
		Intent intent = getIntent();
		if (intent!=null) {
			String title = intent.getStringExtra("title");
			String msg = intent.getStringExtra("msg");
			String btn = intent.getStringExtra("btn");
			tvBtn.setText(btn);
			tvMsg.setText(msg);
			tvTitle.setText(title);
		}
	
		tvBtn.setOnClickListener(new OnClickListener() {


			@Override
			public void onClick(View v) {
			finish();
			if (mediaPlayer!=null) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
					mediaPlayer.release();
					mediaPlayer=null;
				}
			}
			}
		});
	}

	private void setDisplay() {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (display.getHeight() > display.getWidth()) {
			// lp.height = (int) (display.getHeight() * 0.5);
			lp.width = (int) (display.getWidth() * 1.0);
		} else {
			// lp.height = (int) (display.getHeight() * 0.75);
			lp.width = (int) (display.getWidth() * 0.5);
		}
		getWindow().setAttributes(lp);
	}

	@Override
	public void initViews() {
		
	}

	@Override
	public void setListener() {
		
	}
	
    public MediaPlayer ring() throws Exception, IOException {
    // TODO Auto-generated method stub
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		MediaPlayer player = new MediaPlayer();
		player.setDataSource(this, alert);
		final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {
		player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
		player.setLooping(true);
		player.prepare();
		player.start();

		}
		return player;
    }
}
