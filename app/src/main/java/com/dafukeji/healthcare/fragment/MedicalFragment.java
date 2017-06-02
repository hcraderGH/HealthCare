package com.dafukeji.healthcare.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.LogUtil;
import com.dafukeji.healthcare.util.CureSPUtil;
import com.dafukeji.healthcare.viewpagercards.CardItem;
import com.dafukeji.healthcare.viewpagercards.CardPagerMedicalAdapter;
import com.dafukeji.healthcare.viewpagercards.ShadowTransformer;

import java.util.Arrays;

/**
 * Created by DevCheng on 2017/6/1.
 */
public class MedicalFragment extends Fragment {

	private View mView;
	private ViewPager mViewPager;
	private CardPagerMedicalAdapter mCardAdapter;
	private ShadowTransformer mCardShadowTransformer;

	private static String TAG = "测试MedicalFragment";

	private Button btnStart;

	private int mStimulate;//强刺激的类型，3表示关机
	private int mCauterizeGrade = 40;//初始的一档对应的温度
	private int mCauterizeTime = 10;//初始的加热时间
	private int mNeedleType = 1;//默认为按功能
	private int mNeedleGrade = 1;//默认为1档
	private int mNeedleFrequency = 1;//默认为1档
	private int mMedicineTime = 20;//默认为3档20分钟

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_home_medical, container, false);
		initViews();

		return mView;
	}


	private void initViews() {
		//初始化ViewPagerCard
		mViewPager = (ViewPager) mView.findViewById(R.id.vp_cure);

		mCardAdapter = new CardPagerMedicalAdapter(getActivity(), getActivity().getSupportFragmentManager());
		mCardAdapter.addCardItem(new CardItem(R.string.cauterize));
		mCardAdapter.addCardItem(new CardItem(R.string.needle));
		mCardAdapter.addCardItem(new CardItem(R.string.medical));

		mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
		mCardShadowTransformer.enableScaling(true);
		mViewPager.setAdapter(mCardAdapter);
		mViewPager.setPageTransformer(false, mCardShadowTransformer);
		mViewPager.setOffscreenPageLimit(3);

		btnStart = (Button) mView.findViewById(R.id.btn_cure_start);
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (CureSPUtil.isSaved(Constants.SP_MEDICAL_STIMULATE,getActivity())){
					mStimulate = CureSPUtil.getSP(Constants.SP_MEDICAL_STIMULATE,getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_GRADE,getActivity())) {
					mCauterizeGrade = CureSPUtil.getTempByPosition(CureSPUtil.getSP(Constants.SP_CAUTERIZE_GRADE,getActivity()));
				}
				if (CureSPUtil.isSaved(Constants.SP_CAUTERIZE_TIME_GRADE,getActivity())) {
					mCauterizeTime = CureSPUtil.getCauterizeTimeByPosition(CureSPUtil.getSP(Constants.SP_CAUTERIZE_TIME_GRADE,getActivity()));
				}
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_TYPE,getActivity())) {
					mNeedleType = CureSPUtil.getSP(Constants.SP_NEEDLE_TYPE,getActivity());
				}
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_GRADE,getActivity())) {
					mNeedleGrade = CureSPUtil.getSP(Constants.SP_NEEDLE_GRADE,getActivity());
				}
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_FREQUENCY,getActivity())) {
					mNeedleFrequency = CureSPUtil.getSP(Constants.SP_NEEDLE_FREQUENCY,getActivity());
				}
				if (CureSPUtil.isSaved(Constants.SP_MEDICINE_TIME_GRADE,getActivity())) {
					mMedicineTime = CureSPUtil.getMedicineTimeByPosition(CureSPUtil.getSP(Constants.SP_MEDICINE_TIME_GRADE,getActivity()));
				}

				LogUtil.i(TAG, "发送的数据Settings:" + Arrays.toString(CureSPUtil.setSettingData(mStimulate, mCauterizeGrade, mCauterizeTime
						, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime)));

				if (HomeFragment.getBluetoothLeService()==null){
					return;
				}

				HomeFragment.getBluetoothLeService().WriteValue(CureSPUtil.setSettingData(mStimulate, mCauterizeGrade, mCauterizeTime
						, mNeedleType, mNeedleGrade, mNeedleFrequency, mMedicineTime));

//				Intent intent = new Intent(getActivity(), RunningActivity.class);
//				intent.putExtra(Constants.CURE_TYPE, getString(R.string.cure_medical));
//				intent.putExtra(Constants.ORIGINAL_TIME, mCauterizeTime + mMedicineTime);
//				getActivity().startActivity(intent);
			}
		});
	}

}
