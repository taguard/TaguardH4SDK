package com.moko.beaconxplus.activity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moko.beaconxplus.AppConstants;
import com.moko.beaconxplus.R;
import com.moko.beaconxplus.dialog.AlertMessageDialog;
import com.moko.beaconxplus.service.MokoService;
import com.moko.beaconxplus.utils.ToastUtils;
import com.moko.beaconxplus.utils.Utils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.sql.Array;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExportDataActivity extends BaseActivity {


    public MokoService mMokoService;
    @Bind(R.id.iv_sync)
    ImageView ivSync;
    @Bind(R.id.tv_export)
    TextView tvExport;
    @Bind(R.id.ll_data)
    LinearLayout llData;
    @Bind(R.id.tv_sync)
    TextView tvSync;

    private boolean mReceiverTag = false;
    private StringBuffer storeString = new StringBuffer();
    private boolean mIsShown;
    private boolean isSync;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);
        ButterKnife.bind(this);

        Intent intent = new Intent(this, MokoService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMokoService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_ORDER_RESULT);
            filter.addAction(MokoConstants.ACTION_ORDER_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_ORDER_FINISH);
            filter.addAction(MokoConstants.ACTION_CURRENT_DATA);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.setPriority(400);
            registerReceiver(mReceiver, filter);
            mReceiverTag = true;
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                // 蓝牙未打开，开启蓝牙
                MokoSupport.getInstance().enableBluetooth();
            } else {
                if (mMokoService == null) {
                    finish();
                    return;
                }
                showSyncingProgressDialog();
                MokoSupport.getInstance().sendOrder(mMokoService.setSavedTHNotifyOpen());
                Animation animation = AnimationUtils.loadAnimation(ExportDataActivity.this, R.anim.rotate_refresh);
                ivSync.startAnimation(animation);
                tvSync.setText("Stop");
                isSync = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                    // 设备断开，通知页面更新
                    back();
                }
                if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                    // 设备连接成功，通知页面更新
                }
            }
        });

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();
                if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(action) && !MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                    abortBroadcast();
                }
                if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                }
                if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                    dismissSyncProgressDialog();
                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderType orderType = response.orderType;
                    int responseType = response.responseType;
                    byte[] value = response.responseValue;
                    switch (orderType) {
                        case writeConfig:
                            if (value.length >= 2) {
                                int key = value[1] & 0xff;
                                ConfigKeyEnum configKeyEnum = ConfigKeyEnum.fromConfigKey(key);
                                if (configKeyEnum == null) {
                                    return;
                                }
                                switch (configKeyEnum) {
                                    case SET_TH_EMPTY:
                                        if (value.length > 3 && value[3] == 0) {
                                            storeString = new StringBuffer();
                                            LogModule.writeTHFile("");
                                            mIsShown = false;
                                            llData.removeViews(1, llData.getChildCount() - 1);
                                            llData.setVisibility(View.GONE);
                                            Drawable top = getResources().getDrawable(R.drawable.ic_download);
                                            tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                                            ToastUtils.showToast(ExportDataActivity.this, "Empty success!");
                                        } else {
                                            ToastUtils.showToast(ExportDataActivity.this, "Failed");
                                        }
                                        break;
                                }
                            }
                            break;
                    }
                }

                if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_CURRENT_DATA_TYPE);
                    byte[] value = intent.getByteArrayExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE);
                    switch (orderType) {
                        case notifyConfig:
                            String valueHexStr = MokoUtils.bytesToHexString(value);
                            if ("eb63000100".equals(valueHexStr.toLowerCase())) {
                                ToastUtils.showToast(ExportDataActivity.this, "Device Locked!");
                                back();
                            }
                            break;
                        case htSavedData:
                            if (!mIsShown) {
                                mIsShown = true;
                                llData.setVisibility(View.VISIBLE);
                                Drawable top = getResources().getDrawable(R.drawable.ic_download_checked);
                                tvExport.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
                            }

                            if (value.length > 19) {
                                byte[] value1 = Arrays.copyOfRange(value, 0, 10);
                                byte[] value2 = Arrays.copyOfRange(value, 10, 20);
                                int year1 = value1[0] & 0xff;
                                int month1 = value1[1] & 0xff;
                                int day1 = value1[2] & 0xff;
                                int hour1 = value1[3] & 0xff;
                                int minute1 = value1[4] & 0xff;
                                int second1 = value1[5] & 0xff;
                                byte[] tempBytes1 = Arrays.copyOfRange(value1, 6, 8);
                                float temp1 = MokoUtils.toInt(tempBytes1) * 0.1f;
                                byte[] humidityBytes1 = Arrays.copyOfRange(value1, 8, 10);
                                float humidity1 = MokoUtils.toInt(humidityBytes1) * 0.1f;
                                Calendar calendar1 = Calendar.getInstance();
                                calendar1.set(Calendar.YEAR, 2000 + year1);
                                calendar1.set(Calendar.MONTH, month1 - 1);
                                calendar1.set(Calendar.DAY_OF_MONTH, day1);
                                calendar1.set(Calendar.HOUR_OF_DAY, hour1);
                                calendar1.set(Calendar.MINUTE, minute1);
                                calendar1.set(Calendar.SECOND, second1);
                                View v1 = getLayoutInflater().inflate(R.layout.item_export_data, llData, false);
                                TextView tvTime1 = ButterKnife.findById(v1, R.id.tv_time);
                                TextView tvTemp1 = ButterKnife.findById(v1, R.id.tv_temp);
                                TextView tvHumidity1 = ButterKnife.findById(v1, R.id.tv_humidity);
                                String time1 = Utils.calendar2strDate(calendar1, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS);
                                String tempStr1 = MokoUtils.getDecimalFormat("0.0").format(temp1);
                                String humidityStr1 = MokoUtils.getDecimalFormat("0.0").format(humidity1);
                                tvTime1.setText(time1);
                                tvTemp1.setText(tempStr1);
                                tvHumidity1.setText(humidityStr1);
                                llData.addView(v1);
                                storeString.append(String.format("%s T%s H%s", time1, tempStr1, humidityStr1));
                                storeString.append("\n");

                                int year2 = value2[0] & 0xff;
                                int month2 = value2[1] & 0xff;
                                int day2 = value2[2] & 0xff;
                                int hour2 = value2[3] & 0xff;
                                int minute2 = value2[4] & 0xff;
                                int second2 = value2[5] & 0xff;
                                byte[] tempBytes2 = Arrays.copyOfRange(value2, 6, 8);
                                float temp2 = MokoUtils.toInt(tempBytes2) * 0.1f;
                                byte[] humidityBytes2 = Arrays.copyOfRange(value2, 8, 10);
                                float humidity2 = MokoUtils.toInt(humidityBytes2) * 0.1f;
                                Calendar calendar2 = Calendar.getInstance();
                                calendar2.set(Calendar.YEAR, 2000 + year2);
                                calendar2.set(Calendar.MONTH, month2 - 1);
                                calendar2.set(Calendar.DAY_OF_MONTH, day2);
                                calendar2.set(Calendar.HOUR_OF_DAY, hour2);
                                calendar2.set(Calendar.MINUTE, minute2);
                                calendar2.set(Calendar.SECOND, second2);
                                View v2 = getLayoutInflater().inflate(R.layout.item_export_data, llData, false);
                                TextView tvTime2 = ButterKnife.findById(v2, R.id.tv_time);
                                TextView tvTemp2 = ButterKnife.findById(v2, R.id.tv_temp);
                                TextView tvHumidity2 = ButterKnife.findById(v2, R.id.tv_humidity);
                                String time2 = Utils.calendar2strDate(calendar2, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS);
                                String tempStr2 = MokoUtils.getDecimalFormat("0.0").format(temp2);
                                String humidityStr2 = MokoUtils.getDecimalFormat("0.0").format(humidity2);
                                tvTime2.setText(time2);
                                tvTemp2.setText(tempStr2);
                                tvHumidity2.setText(humidityStr2);
                                llData.addView(v2);
                                storeString.append(String.format("%s T%s H%s", time2, tempStr2, humidityStr2));
                                storeString.append("\n");
                            } else if (value.length > 9) {
                                int year = value[0] & 0xff;
                                int month = value[1] & 0xff;
                                int day = value[2] & 0xff;
                                int hour = value[3] & 0xff;
                                int minute = value[4] & 0xff;
                                int second = value[5] & 0xff;
                                byte[] tempBytes = Arrays.copyOfRange(value, 6, 8);
                                float temp = MokoUtils.toInt(tempBytes) * 0.1f;
                                byte[] humidityBytes = Arrays.copyOfRange(value, 8, 10);
                                float humidity = MokoUtils.toInt(humidityBytes) * 0.1f;
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.YEAR, 2000 + year);
                                calendar.set(Calendar.MONTH, month - 1);
                                calendar.set(Calendar.DAY_OF_MONTH, day);
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, second);
                                View v = getLayoutInflater().inflate(R.layout.item_export_data, llData, false);
                                TextView tvTime = ButterKnife.findById(v, R.id.tv_time);
                                TextView tvTemp = ButterKnife.findById(v, R.id.tv_temp);
                                TextView tvHumidity = ButterKnife.findById(v, R.id.tv_humidity);
                                String time = Utils.calendar2strDate(calendar, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS);
                                String tempStr = MokoUtils.getDecimalFormat("0.0").format(temp);
                                String humidityStr = MokoUtils.getDecimalFormat("0.0").format(humidity);
                                tvTime.setText(time);
                                tvTemp.setText(tempStr);
                                tvHumidity.setText(humidityStr);
                                llData.addView(v);
                                storeString.append(String.format("%s T%s H%s", time, tempStr, humidityStr));
                                storeString.append("\n");
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            AlertDialog.Builder builder = new AlertDialog.Builder(ExportDataActivity.this);
                            builder.setTitle("Dismiss");
                            builder.setCancelable(false);
                            builder.setMessage("The current system of bluetooth is not available!");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    back();
                                }
                            });
                            builder.show();
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        unbindService(mServiceConnection);
        EventBus.getDefault().unregister(this);
    }

    private ProgressDialog syncingDialog;

    public void showSyncingProgressDialog() {
        syncingDialog = new ProgressDialog(this);
        syncingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        syncingDialog.setCanceledOnTouchOutside(false);
        syncingDialog.setCancelable(false);
        syncingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        syncingDialog.setMessage("Syncing...");
        if (!isFinishing() && syncingDialog != null && !syncingDialog.isShowing()) {
            syncingDialog.show();
        }
    }

    public void dismissSyncProgressDialog() {
        if (!isFinishing() && syncingDialog != null && syncingDialog.isShowing()) {
            syncingDialog.dismiss();
        }
    }

    @OnClick({R.id.tv_back, R.id.tv_empty, R.id.ll_sync, R.id.tv_export})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.tv_empty:
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setTitle("Warning!");
                dialog.setMessage("Are you sure to empty the saved T&H datas?");
                dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                    @Override
                    public void onClick() {
                        showSyncingProgressDialog();
                        MokoSupport.getInstance().sendOrder(mMokoService.setTHEmpty());
                    }
                });
                dialog.show(getSupportFragmentManager());
                break;
            case R.id.ll_sync:
                if (!isSync) {
                    isSync = true;
                    showSyncingProgressDialog();
                    MokoSupport.getInstance().sendOrder(mMokoService.setSavedTHNotifyOpen());
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
                    ivSync.startAnimation(animation);
                    tvSync.setText("Stop");
                } else {
                    showSyncingProgressDialog();
                    MokoSupport.getInstance().sendOrder(mMokoService.setSavedTHNotifyClose());
                    isSync = false;
                    ivSync.clearAnimation();
                    tvSync.setText("Sync");
                }
                break;
            case R.id.tv_export:
                if (mIsShown) {
                    showSyncingProgressDialog();
                    LogModule.writeTHFile("");
                    tvExport.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissSyncProgressDialog();
                            String log = storeString.toString();
                            if (!TextUtils.isEmpty(log)) {
                                LogModule.writeTHFile(log);
                                File file = LogModule.getTHFile();
                                // 发送邮件
                                String address = "Development@mokotechnology.com";
                                String title = "T&H Log";
                                String content = title;
                                Utils.sendEmail(ExportDataActivity.this, address, content, title, "Choose Email Client", file);
                            }
                        }
                    }, 500);
                }
                break;
        }
    }

    private void back() {
        // 关闭通知
        MokoSupport.getInstance().sendOrder(mMokoService.setSavedTHNotifyClose());
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
