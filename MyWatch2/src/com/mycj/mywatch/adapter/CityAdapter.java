package com.mycj.mywatch.adapter;

import java.util.List;

import android.content.Context;
import android.widget.TextView;

import com.mycj.mywatch.R;
import com.mycj.mywatch.bean.Place;
import com.mycj.mywatch.util.CommonAdapter;

public class CityAdapter extends CommonAdapter<Place>{

	public CityAdapter(Context context, List<Place> mDatas, int layoutId) {
		super(context, mDatas, layoutId);
		
	}

	@Override
	public void convert(com.mycj.mywatch.util.CommonAdapter.ViewHolder holder, Place item) {
		TextView tv = holder.getView(R.id.tv_address);
		tv.setText(item.getpName());
	}

}
