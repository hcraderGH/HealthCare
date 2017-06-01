package com.dafukeji.healthcare.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dafukeji.healthcare.R;
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
	}
}
