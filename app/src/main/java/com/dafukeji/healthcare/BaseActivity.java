package com.dafukeji.healthcare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dafukeji.healthcare.bean.Battery;
import com.dafukeji.healthcare.constants.Constants;
import com.dafukeji.healthcare.util.StatusBar;
import com.umeng.message.PushAgent;

public class BaseActivity extends AppCompatActivity {

    private BlueToothBroadCast mBlueToothBroadCast;
    private int bat=100;

    private MaterialDialog mMaterialDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        MyApplication.getInstance().addActivity(this);
        StatusBar.setImmersiveStatusBar(this,R.color.app_bar_color);//沉浸式状态栏

        //TODO 是否可以采用当处于MainActivity时使用对话框，处于其他Activity可以使用Notification
        //注册接受蓝牙电量的广播
        mBlueToothBroadCast=new BlueToothBroadCast();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.BATTERY_ELECTRIC_QUANTITY);
        registerReceiver(mBlueToothBroadCast,filter);

        //Umeng统计应用启动数据
        PushAgent.getInstance(this).onAppStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBlueToothBroadCast);
        super.onDestroy();
    }


    public class BlueToothBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //得到蓝牙的信息
            bat= intent.getIntExtra(Constants.EXTRAS_BATTERY_ELECTRIC_QUANTITY,0);
            JudgeEle(bat);
        }
    }

    /**
     * 根据电量弹出提醒对话框
     */
    private void JudgeEle(int ele) {
            if (ele<Constants.EXTRAS_BATTERY_DANGER) {
                if (Battery.isFirstBatteryRemind){
                    if (mMaterialDialog==null){//此处需要判断是否为空，求变量要为全局，否则，在对话框后，一直会new出新的对话框
                        mMaterialDialog=new MaterialDialog.Builder(this)
                                .title("提示")
                                .positiveText("确定")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    Battery.isFirstBatteryRemind =false;
                                                }
                                            }
                                ).content("设备电量过低，请及时充电")
                                .iconRes(R.mipmap.ic_warn_red)
                                .maxIconSize(64).build();
                        mMaterialDialog.show();
                    }
                }
            }
    }
}
