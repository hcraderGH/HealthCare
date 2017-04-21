package com.dafukeji.healthcare;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by DevCheng on 2017/3/15.
 *
 * T泛型必须要说明具体的类型
 */

public class LeRecyclerAdapter extends RecyclerView.Adapter<LeRecyclerAdapter.RecyclerViewHolder>{

	private ArrayList<BluetoothDevice> mLeDevices;
	private LayoutInflater mInflater;

	private OnItemClickListener mOnItemClickListener;

	public LeRecyclerAdapter(Context context) {
		mLeDevices=new ArrayList<>();
		this.mInflater=LayoutInflater.from(context);
	}

	public interface OnItemClickListener{
		void onItemClick(View view,int position);
//		void onItemLongClick(View view,int position);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener){
		this.mOnItemClickListener=onItemClickListener;
	}

	public void addDevice(BluetoothDevice device){
		if (!mLeDevices.contains(device)){
			mLeDevices.add(device);
		}
	}

	public BluetoothDevice getDevice(int position){
		return mLeDevices.get(position);
	}

	public void clear(){
		mLeDevices.clear();
	}

	@Override
	public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		RecyclerViewHolder holder=new RecyclerViewHolder(mInflater.inflate(R.layout.item_device,parent,false));
		return  holder;
	}

	@Override
	public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
		//绑定数据
		BluetoothDevice device=mLeDevices.get(position);
		String deviceName=device.getName();
		if (deviceName != null && deviceName.length() > 0){
			holder.deviceName.setText(deviceName);
		}else{
			holder.deviceName.setText("未知设备");
		}

		holder.deviceAddress.setText(device.getAddress());

		//如果设置了回调，则设置点击事件
		if (mOnItemClickListener!=null){
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos=holder.getLayoutPosition();
					mOnItemClickListener.onItemClick(holder.itemView,pos);
				}
			});
		}
	}

	@Override
	public int getItemCount() {
		return mLeDevices.size();
	}

	class RecyclerViewHolder extends RecyclerView.ViewHolder{
		TextView deviceName;
		TextView deviceAddress;

		public RecyclerViewHolder(View itemView) {
			super(itemView);
			deviceName= (TextView) itemView.findViewById(R.id.device_name);
			deviceAddress= (TextView) itemView.findViewById(R.id.device_address);
		}
	}
}
