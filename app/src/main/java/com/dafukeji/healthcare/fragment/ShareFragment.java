package com.dafukeji.healthcare.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dafukeji.healthcare.R;

/**
 * Created by DevCheng on 2017/7/31.
 */

public class ShareFragment extends Fragment implements View.OnLongClickListener{

	private ImageView ivShareQR;
	private View mView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_share,container,false);
		//初始化数据

		initViews();
		return mView;
	}

	private void initViews() {

		ivShareQR=(ImageView)mView.findViewById(R.id.iv_qr_code);
		ivShareQR.setOnLongClickListener(this);
	}


	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()){
			case R.id.iv_qr_code:
				//TODO 弹出选择框选择程序发送二维码

				break;
		}

		return true;
	}
}
