package com.dafukeji.healthcare.viewpagercards;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.CureSPUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class CardPagerPhysicalAdapter extends PagerAdapter implements CardAdapter,AdapterView.OnItemSelectedListener {

//	private Button btnCauterizeGrade, btnKneadGrade, btnMedicalGrade;
//	private Button btnCauterizeTime, btnNeedleTime, btnMedicalTime;
////	private Button btnCauterizeStart, btnNeedleStart, btnMedicalStart;
//	private Button btnKneadFrequency;
//	private Button btnNeedleType;


	private Spinner spnKneadType, spnKneadGrade, spnKneadFrequency;
	private Spinner spnKneadTimeGrade;
	private SwitchCompat scStimulate;

	private FragmentManager mFragmentManager;

	private List<CardView> mViews;
	private List<CardItem> mData;
	private float mBaseElevation;
	private View mView;
	private Context mContext;

	private static String TAG="测试CardPagerAdapter";

	private String[] mKneadTypes =new String[]{"按","提","左旋按","右旋按","左旋提","右旋提"};
	private String[] mKneadGrades =new String[]{"一档", "二档", "三档", "四档", "五档","六档", "七档", "八档", "九档"};
	private String[] mKneadFrequencies =new String[]{"一档", "二档", "三档", "四档", "五档","六档", "七档", "八档", "九档"};

	private String[] mKneadTimeGrades =new String[]{"10分钟", "15分钟", "20分钟", "25分钟", "30分钟"};

	public CardPagerPhysicalAdapter(Context context, FragmentManager fragmentManager) {
		this.mFragmentManager = fragmentManager;
		this.mContext = context;
		mData = new ArrayList<>();
		mViews = new ArrayList<>();
	}

	public void addCardItem(CardItem item) {
		mViews.add(null);
		mData.add(item);
	}

	public float getBaseElevation() {
		return mBaseElevation;
	}

	@Override
	public CardView getCardViewAt(int position) {
		return mViews.get(position);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		int type=position+Constants.CURE_CAUTERIZE;//TODO 如果类型的常量改变了这边也要改变

		Logger.i("instantiateItem: type"+type);

		switch (position) {

			case 0:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_knead, container, false);

				spnKneadType = (Spinner) mView.findViewById(R.id.spn_knead_type);
				ArrayAdapter<String> adapter10=new ArrayAdapter<>(mContext,android.R.layout.simple_list_item_1, mKneadTypes);
				spnKneadType.setAdapter(adapter10);
				spnKneadType.setOnItemSelectedListener(this);
				if (CureSPUtil.isSaved(Constants.SP_KNEAD_TYPE,mContext)){
					spnKneadType.setSelection(CureSPUtil.getSP(Constants.SP_KNEAD_TYPE,mContext));
				}

				spnKneadGrade = (Spinner) mView.findViewById(R.id.spn_knead_grade);
				ArrayAdapter<String> adapter11=new ArrayAdapter<>(mContext,android.R.layout.simple_list_item_1, mKneadGrades);
				spnKneadGrade.setAdapter(adapter11);
				spnKneadGrade.setOnItemSelectedListener(this);
				if (CureSPUtil.isSaved(Constants.SP_NEEDLE_GRADE,mContext)){
					spnKneadGrade.setSelection(CureSPUtil.getSP(Constants.SP_KNEAD_GRADE,mContext));
				}

				spnKneadFrequency = (Spinner) mView.findViewById(R.id.spn_knead_frequency);
				ArrayAdapter<String> adapter12=new ArrayAdapter<>(mContext,android.R.layout.simple_list_item_1, mKneadFrequencies);
				spnKneadFrequency.setAdapter(adapter12);
				spnKneadFrequency.setOnItemSelectedListener(this);
				if (CureSPUtil.isSaved(Constants.SP_KNEAD_FREQUENCY,mContext)){
					spnKneadFrequency.setSelection(CureSPUtil.getSP(Constants.SP_KNEAD_FREQUENCY,mContext));
				}

				spnKneadTimeGrade = (Spinner) mView.findViewById(R.id.spn_knead_time_grade);
				ArrayAdapter<String> adapter13=new ArrayAdapter<>(mContext,android.R.layout.simple_list_item_1, mKneadTimeGrades);
				spnKneadTimeGrade.setAdapter(adapter13);
				spnKneadTimeGrade.setOnItemSelectedListener(this);
				if (CureSPUtil.isSaved(Constants.SP_KNEAD_TIME_GRADE,mContext)){
					spnKneadTimeGrade.setSelection(CureSPUtil.getSP(Constants.SP_KNEAD_TIME_GRADE,mContext));
				}

				scStimulate= (SwitchCompat) mView.findViewById(R.id.switch_stimulate_knead);
				scStimulate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						CureSPUtil.setSP(Constants.SP_PHYSICAL_STIMULATE, isChecked ? 1 : 0,mContext);
					}
				});
				if (CureSPUtil.isSaved(Constants.SP_PHYSICAL_STIMULATE,mContext)){
					scStimulate.setChecked(CureSPUtil.getSP(Constants.SP_PHYSICAL_STIMULATE,mContext)==1);
				}

				break;

			case 1:
				mView = LayoutInflater.from(container.getContext()).inflate(R.layout.adapter_knead, container, false);
				mView.setVisibility(View.INVISIBLE);
				break;

		}

		container.addView(mView);
		bind(mData.get(position), mView);
		CardView cardView = (CardView) mView.findViewById(R.id.cardView);

		if (mBaseElevation == 0) {
			mBaseElevation = cardView.getCardElevation();
		}

		cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
		mViews.set(position, cardView);
		return mView;
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()){
			case R.id.spn_knead_type:
				CureSPUtil.setSP(Constants.SP_KNEAD_TYPE,position,mContext);//0表示无，+1;
				break;
			case R.id.spn_knead_grade:
				CureSPUtil.setSP(Constants.SP_KNEAD_GRADE,position,mContext);//0表示无，所以在此处+1,一共9档
				break;
			case R.id.spn_knead_frequency:
				CureSPUtil.setSP(Constants.SP_KNEAD_FREQUENCY,position,mContext);//0表示无，所以在此处+1,一共9档
				break;
			case R.id.spn_knead_time_grade:
				CureSPUtil.setSP(Constants.SP_KNEAD_TIME_GRADE, position,mContext);

		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}



	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
		mViews.set(position, null);
	}

	private void bind(CardItem item, View view) {
		TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
		titleTextView.setText(item.getTitle());
	}


//	/**
//	 * 偏好设置选择的参数
//	 * @param keyName
//	 * @param keyValue
//	 */
//	private void setSP(String keyName, int keyValue){
//		SPUtils spUtils=new SPUtils(Constants.SP_CURE,mContext);
//		spUtils.put(keyName,keyValue);
//	}
//
//
//	private int getSP(String keyName){
//		SPUtils spUtils=new SPUtils(Constants.SP_CURE,mContext);
//		return spUtils.getInt(keyName);
//	}
}
