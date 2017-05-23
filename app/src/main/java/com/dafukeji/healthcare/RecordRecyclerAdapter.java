package com.dafukeji.healthcare;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dafukeji.daogenerator.Cure;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.dafukeji.healthcare.constants.Constants.CURE_CAUTERIZE;
import static com.dafukeji.healthcare.constants.Constants.CURE_MEDICINE;
import static com.dafukeji.healthcare.constants.Constants.CURE_NEEDLE;

/**
 * Created by DevCheng on 2017/5/23.
 */

public class RecordRecyclerAdapter extends RecyclerView.Adapter<RecordRecyclerAdapter.RecyclerViewHolder>{

	private List<Cure> mCures;
	private LayoutInflater mInflater;
	private Context mContext;
	private static String TAG="测试RecordRecyclerAdapter";

	private OnItemClickListener mOnItemClickListener;

	public RecordRecyclerAdapter(Context context,List<Cure> cures){
		this.mCures=cures;
		this.mContext=context;
		this.mInflater=LayoutInflater.from(context);
	}

	public interface OnItemClickListener{
		void onItemClick(View view,int position);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener){
		this.mOnItemClickListener=onItemClickListener;
	}

	@Override
	public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerViewHolder holder=new RecyclerViewHolder(mInflater.inflate(R.layout.item_record,parent,false));
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerViewHolder holder, int position) {
		//绑定数据
		if (mCures.size()==0){
			return;
		}
		Cure cure=mCures.get(position);
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		String date=sdf.format(cure.getStartTime());
		Log.i(TAG, "onBindViewHolder: date"+date);

		SimpleDateFormat sdf2=new SimpleDateFormat("HH′mm′ss″", Locale.CHINA);
		String startTime=sdf2.format(cure.getStartTime());
		Log.i(TAG, "onBindViewHolder: startTime"+startTime);
		Log.i(TAG, "onBindViewHolder: stopTime"+cure.getStopTime());
		String stopTime=sdf2.format(cure.getStopTime());
		String wholeTime=sdf2.format(cure.getStopTime()-cure.getStartTime());

		holder.tvDate.setText(date);
		holder.tvStartTime.setText(startTime);
		holder.tvStopTime.setText(stopTime);
		holder.tvWholeTime.setText(wholeTime);

		int cureType=cure.getCureType();
		switch (cureType){
			case CURE_CAUTERIZE:
				holder.ivType.setBackground(mContext.getResources().getDrawable(R.mipmap.ic_record_cauterize_orange));
				break;
			case CURE_NEEDLE:
				holder.ivType.setBackground(mContext.getResources().getDrawable(R.mipmap.ic_record_needle_blue));
				break;
			case CURE_MEDICINE:
				holder.ivType.setBackground(mContext.getResources().getDrawable(R.mipmap.ic_record_medicine_purple));
				break;
		}
		holder.tvOrder.setText(String.valueOf(position+1));

	}

	public Cure getCure(int position){
		return mCures.get(position);
	}

	public void addCure(Cure cure){
		if (!mCures.contains(cure)){
			mCures.add(cure);
		}
	}

	public void clear(){
		mCures.clear();
	}

	@Override
	public int getItemCount() {
		return mCures.size();
	}

	class RecyclerViewHolder extends RecyclerView.ViewHolder{

		TextView tvOrder,tvWholeTime,tvStartTime,tvStopTime,tvDate;
		ImageView ivType;

		public RecyclerViewHolder(View itemView) {
			super(itemView);
			tvOrder= (TextView) itemView.findViewById(R.id.tv_cure_order);
			tvWholeTime= (TextView) itemView.findViewById(R.id.tv_cure_whole_time);
			tvStartTime= (TextView) itemView.findViewById(R.id.tv_cure_start_time);
			tvStopTime= (TextView) itemView.findViewById(R.id.tv_cure_stop_time);
			tvDate= (TextView) itemView.findViewById(R.id.tv_cure_date);
			ivType= (ImageView) itemView.findViewById(R.id.iv_cure_type_logo);
		}
	}
}
