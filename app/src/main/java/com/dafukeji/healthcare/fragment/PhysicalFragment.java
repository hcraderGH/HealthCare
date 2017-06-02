package com.dafukeji.healthcare.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.CureSPUtil;
import com.dafukeji.healthcare.viewpagercards.CardItem;
import com.dafukeji.healthcare.viewpagercards.CardPagerPhysicalAdapter;
import com.dafukeji.healthcare.viewpagercards.ShadowTransformer;

/**
 * Created by DevCheng on 2017/6/1.
 */

public class PhysicalFragment extends Fragment {

	private View mView;
	private ViewPager mViewPager;
	private CardPagerPhysicalAdapter mCardAdapter;
	private ShadowTransformer mCardShadowTransformer;

	private Button btnStart;

	private int mStimulate;//强刺激的类型，3表示关机
	private int mKneadType = 1;//默认为按功能
	private int mKneadGrade = 1;//默认为1档
	private int mKneadFrequency = 1;//默认为1档
	private int mKneadTime = 20;//默认为3档20分钟

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_home_physical,container,false);
		initViews();

		return mView;
	}

	private void initViews() {
		//初始化ViewPagerCard
		mViewPager = (ViewPager)mView.findViewById(R.id.vp_cure);

		mCardAdapter=new CardPagerPhysicalAdapter(getActivity(),getActivity().getSupportFragmentManager());
		mCardAdapter.addCardItem(new CardItem(R.string.needle));
		mCardAdapter.addCardItem(new CardItem(R.string.needle));

		mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
		mCardShadowTransformer.enableScaling(true);
		mViewPager.setAdapter(mCardAdapter);
		mViewPager.setPageTransformer(false, mCardShadowTransformer);
		mViewPager.setOffscreenPageLimit(2);

		btnStart= (Button) mView.findViewById(R.id.btn_cure_start);
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CureSPUtil.isSaved(Constants.SP_PHYSICAL_STIMULATE,getActivity())){
					mStimulate=CureSPUtil.getSP(Constants.SP_PHYSICAL_STIMULATE,getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_TYPE,getActivity())){
					mKneadType=CureSPUtil.getSP(Constants.SP_KNEAD_TYPE,getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_GRADE,getActivity())){
					mKneadGrade=CureSPUtil.getSP(Constants.SP_KNEAD_GRADE,getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_FREQUENCY,getActivity())){
					mKneadFrequency=CureSPUtil.getSP(Constants.SP_KNEAD_FREQUENCY,getActivity());
				}

				if (CureSPUtil.isSaved(Constants.SP_KNEAD_TIME_GRADE,getActivity())){
					mKneadTime=CureSPUtil.getKneadTimeByPosition(CureSPUtil.getSP(Constants.SP_KNEAD_TIME_GRADE,getActivity()));
				}
			}
		});
	}
}
