package com.mycj.mywatch.activity;

import com.mycj.mywatch.BaseActivity;
import com.mycj.mywatch.R;
import com.mycj.mywatch.R.id;
import com.mycj.mywatch.R.layout;
import com.mycj.mywatch.bean.Constant;
import com.mycj.mywatch.util.SharedPreferenceUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WeatherTempUnitSetActivity extends BaseActivity implements OnClickListener{

	private RelativeLayout rlC;
	private RelativeLayout rlF;
	private TextView tvC;
	private TextView tvF;
	private RelativeLayout rlBack;
	private TextView tvSave;
	private final String UNIT_C = "℃";
	private final String UNIT_F = "℉";
	private String unit;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_temp_unit_set);
		initViews();
		setListener();//
	}
	
	@Override
	protected void onResume() {
		
		unit = (String) SharedPreferenceUtil.get(this, Constant.SHARE_PLACE_UNIT, UNIT_C);
		if (unit.equals(UNIT_C)) {
			tvC.setVisibility(View.VISIBLE);
			tvF.setVisibility(View.INVISIBLE);
		}else{
			tvC.setVisibility(View.INVISIBLE);
			tvF.setVisibility(View.VISIBLE);
		}
		super.onResume();
	}
	
	@Override
	public void initViews() {
		rlC = (RelativeLayout) findViewById(R.id.rl_c);
		rlF = (RelativeLayout) findViewById(R.id.rl_f);
		tvC = (TextView) findViewById(R.id.tv_c);
		tvF = (TextView) findViewById(R.id.tv_f);
		rlBack = (RelativeLayout) findViewById(R.id.rl_setting);
		tvSave = (TextView) findViewById(R.id.tv_save);
	}

	@Override
	public void setListener() {
		rlC.setOnClickListener(this);
		rlF.setOnClickListener(this);
		rlBack.setOnClickListener(this);
		tvSave.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_c:
			if (!unit.equals(UNIT_C)) {
				tvC.setVisibility(View.VISIBLE);
				tvF.setVisibility(View.INVISIBLE);
				unit = UNIT_C;
			}
			break;
		case R.id.rl_f:
			if (!unit.equals(UNIT_F)) {
				tvC.setVisibility(View.INVISIBLE);
				tvF.setVisibility(View.VISIBLE);
				unit = UNIT_F; 
			}
			break;
		case R.id.rl_setting:
		finish();
			break;
		case R.id.tv_save:
			SharedPreferenceUtil.put(this, Constant.SHARE_PLACE_UNIT, unit);
			finish();
			break;

		default:
			break;
		}
	}
}
