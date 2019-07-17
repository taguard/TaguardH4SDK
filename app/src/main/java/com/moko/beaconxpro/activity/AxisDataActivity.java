package com.moko.beaconxpro.activity;


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
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.dialog.AxisDataRateDialog;
import com.moko.beaconxpro.dialog.AxisScaleDialog;
import com.moko.beaconxpro.service.MokoService;
import com.moko.beaconxpro.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AxisDataActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {


    public MokoService mMokoService;
    @Bind(R.id.iv_sync)
    ImageView ivSync;
    @Bind(R.id.tv_sync)
    TextView tvSync;
    @Bind(R.id.tv_x_data)
    TextView tvXData;
    @Bind(R.id.tv_y_data)
    TextView tvYData;
    @Bind(R.id.tv_z_data)
    TextView tvZData;
    @Bind(R.id.tv_axis_scale)
    TextView tvAxisScale;
    @Bind(R.id.tv_axis_data_rate)
    TextView tvAxisDataRate;
    @Bind(R.id.sb_trigger_sensitivity)
    SeekBar sbTriggerSensitivity;
    @Bind(R.id.tv_trigger_sensitivity)
    TextView tvTriggerSensitivity;
    private boolean mReceiverTag = false;
    private String[] axisDataRate;
    private String[] axisScales;
    private boolean isSync;
    private int mSelectedRate;
    private int mSelectedScale;
    private int mSelectedSensivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_axis);
        ButterKnife.bind(this);

        Intent intent = new Intent(this, MokoService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        axisDataRate = getResources().getStringArray(R.array.axis_data_rate);
        axisScales = getResources().getStringArray(R.array.axis_scales);
        sbTriggerSensitivity.setOnSeekBarChangeListener(this);

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
            filter.setPriority(300);
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
                MokoSupport.getInstance().sendOrder(mMokoService.getAxisParams());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                    // 设备断开，通知页面更新
                    finish();
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
                if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
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
                                    case GET_AXIX_PARAMS:
                                        if (value.length > 6) {
                                            mSelectedRate = value[4] & 0xff;
                                            tvAxisDataRate.setText(axisDataRate[mSelectedRate]);
                                            mSelectedScale = value[5] & 0xff;
                                            tvAxisScale.setText(axisScales[mSelectedScale]);
                                            mSelectedSensivity = value[6] & 0xff;
                                            tvTriggerSensitivity.setText(mSelectedSensivity + "");
                                            sbTriggerSensitivity.setProgress(mSelectedSensivity - 7);
                                        }
                                        break;
                                    case SET_AXIX_PARAMS:
                                        if (value.length > 3 && value[3] == 0) {
                                            ToastUtils.showToast(AxisDataActivity.this, "Success");
                                        } else {
                                            ToastUtils.showToast(AxisDataActivity.this, "Failed");
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
                                ToastUtils.showToast(AxisDataActivity.this, "Device Locked!");
                                back();
                            }
                            break;
                        case axisData:
                            if (value.length > 5) {
                                String axisHexStr = MokoUtils.bytesToHexString(value);
                                int length = axisHexStr.length();
                                tvZData.setText(String.format("Z-Data:0x%s", axisHexStr.substring(length - 4).toUpperCase()));
                                tvYData.setText(String.format("Y-Data:0x%s", axisHexStr.substring(length - 8, length - 4).toUpperCase()));
                                tvXData.setText(String.format("X-Data:0x%s", axisHexStr.substring(length - 12, length - 8).toUpperCase()));
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            AlertDialog.Builder builder = new AlertDialog.Builder(AxisDataActivity.this);
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

    @OnClick({R.id.tv_back, R.id.ll_sync, R.id.iv_save, R.id.tv_axis_scale, R.id.tv_axis_data_rate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.ll_sync:
                if (!isSync) {
                    isSync = true;
                    showSyncingProgressDialog();
                    MokoSupport.getInstance().sendOrder(mMokoService.setAxisNotifyOpen());
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
                    ivSync.startAnimation(animation);
                    tvSync.setText("Stop");
                } else {
                    showSyncingProgressDialog();
                    MokoSupport.getInstance().sendOrder(mMokoService.setAxisNotifyClose());
                    isSync = false;
                    ivSync.clearAnimation();
                    tvSync.setText("Sync");
                }
                break;
            case R.id.tv_axis_data_rate:
                AxisDataRateDialog dataRateDialog = new AxisDataRateDialog();
                dataRateDialog.setAxisDataRate(axisDataRate);
                dataRateDialog.setSelected(mSelectedRate);
                dataRateDialog.setListener(new AxisDataRateDialog.OnRateSettingListener() {
                    @Override
                    public void onRateSelected(int rate) {
                        mSelectedRate = rate;
                        tvAxisDataRate.setText(axisDataRate[rate]);
                    }
                });
                dataRateDialog.show(getSupportFragmentManager());
                break;
            case R.id.tv_axis_scale:
                AxisScaleDialog scaleDialog = new AxisScaleDialog();
                scaleDialog.setAxisScale(axisScales);
                scaleDialog.setSelected(mSelectedScale);
                scaleDialog.setListener(new AxisScaleDialog.OnScaleSettingListener() {
                    @Override
                    public void onScaleSelected(int scale) {
                        mSelectedScale = scale;
                        tvAxisScale.setText(axisScales[scale]);
                    }
                });
                scaleDialog.show(getSupportFragmentManager());
                break;
            case R.id.iv_save:
                // 保存
                showSyncingProgressDialog();
                MokoSupport.getInstance().sendOrder(mMokoService.setAxisParams(mSelectedRate, mSelectedScale, mSelectedSensivity));
                break;
        }
    }

    private void back() {
        // 关闭通知
        MokoSupport.getInstance().sendOrder(mMokoService.setAxisNotifyClose());
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mSelectedSensivity = progress + 7;
        tvTriggerSensitivity.setText(mSelectedSensivity + "");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
