package com.dafukeji.healthcare.fragment;


import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.dafukeji.daogenerator.Cure;
import com.dafukeji.daogenerator.CureDao;
import com.dafukeji.daogenerator.DaoMaster;
import com.dafukeji.daogenerator.DaoSession;
import com.dafukeji.daogenerator.PointDao;
import com.dafukeji.healthcare.R;
import com.dafukeji.healthcare.RecordRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.orhanobut.logger.Logger;

import java.util.List;

/**
 * Created by DevCheng on 2017/5/15.
 */

public class RecordFragment extends Fragment{

	private RecordRecyclerAdapter mRecordRecyclerAdapter;
	private RecyclerView mRecyclerView;
	private View mView;
	private FloatingActionButton mFabStatistic;

	private List<Cure> mCure;
	private DaoMaster mMaster;
	private DaoSession mSession;
	private DaoMaster.DevOpenHelper mHelper;
	private PointDao mPointDao;
	private CureDao mCureDao;
	private SQLiteDatabase mDb;

	private LinearLayout llNoRecord;
	private int mCureCount;
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
		mView=inflater.inflate(R.layout.fragment_record,container,false);
		//初始化数据
		getDb();
		getCures();

		initWidgets();
		return mView;
	}

	private void initWidgets() {
		mRecyclerView= (RecyclerView) mView.findViewById(R.id.rv_record);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecordRecyclerAdapter=new RecordRecyclerAdapter(getActivity(),mCure);
		mRecordRecyclerAdapter.setOnItemClickListener(new RecordRecyclerAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				//TODO 传递数据显示表
			}
		});
		mRecyclerView.setAdapter(mRecordRecyclerAdapter);
		mRecyclerView.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (dy>10){//当移动距离大于数值时则隐藏
					mFabStatistic.hide(true);
				}else if (dy<-10){
					mFabStatistic.show(true);
				}


			}
		});
		llNoRecord= (LinearLayout) mView.findViewById(R.id.ll_no_record);
		if (mCureCount==0){
			llNoRecord.setVisibility(View.VISIBLE);
			mRecyclerView.setVisibility(View.GONE);
		}else{
			llNoRecord.setVisibility(View.GONE);
			mRecyclerView.setVisibility(View.VISIBLE);
		}

		mFabStatistic= (FloatingActionButton) mView.findViewById(R.id.fab_statistic);
	}


	/**
	 * 获取greenDao创建的数据库
	 */
	private void getDb() {
		mHelper=new DaoMaster.DevOpenHelper(getActivity(),"treat.db",null);
		mDb=mHelper.getWritableDatabase();
		mMaster=new DaoMaster(mDb);
		mSession=mMaster.newSession();

		mPointDao=mSession.getPointDao();
		mCureDao=mSession.getCureDao();
	}

	private void getCures(){
		mCure= mCureDao.queryBuilder().list();
		mCureCount=mCure.size();
		Logger.i("getCures: mCure个数"+mCure.size());
	}


	@Override
	public void onResume() {
		super.onResume();
		mFabStatistic.hide(false);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mFabStatistic.show(true);//show()和hide()中的Boolean表示是否执行动画
				mFabStatistic.setShowAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fab_scale_up));
			}
		}, 100);
	}
}
