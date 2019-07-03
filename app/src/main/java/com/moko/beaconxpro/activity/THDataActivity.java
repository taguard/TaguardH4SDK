package com.moko.beaconxpro.activity;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.moko.beaconxpro.AppConstants;
import com.moko.beaconxpro.R;
import com.moko.beaconxpro.fragment.StorageHumidityFragment;
import com.moko.beaconxpro.fragment.StorageTHFragment;
import com.moko.beaconxpro.fragment.StorageTempFragment;
import com.moko.beaconxpro.fragment.StorageTimeFragment;
import com.moko.beaconxpro.service.MokoService;
import com.moko.beaconxpro.utils.ToastUtils;
import com.moko.beaconxpro.utils.Utils;
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

import java.util.Arrays;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class THDataActivity extends BaseActivity implements NumberPickerView.OnValueChangeListener {


    public MokoService mMokoService;
    @Bind(R.id.tv_temp)
    TextView tvTemp;
    @Bind(R.id.tv_humidity)
    TextView tvHumidity;
    @Bind(R.id.npv_storage_condition)
    NumberPickerView npvStorageCondition;
    //    @Bind(R.id.frame_storage_condition)
//    FrameLayout frameStorageCondition;
    @Bind(R.id.tv_update_date)
    TextView tvUpdateDate;
    @Bind(R.id.et_period)
    EditText etPeriod;

    private FragmentManager fragmentManager;
    private StorageTempFragment tempFragment;
    private StorageHumidityFragment humidityFragment;
    private StorageTHFragment thFragment;
    private StorageTimeFragment timeFragment;

    private boolean mReceiverTag = false;

    private boolean mIsPeriodSuccess;
    private boolean mIsStorageSuccess;

    private int mStorageType;
    private int mSelectedTemp;
    private int mSelectedHumidity;
    private int mSelectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_th);
        ButterKnife.bind(this);

        Intent intent = new Intent(this, MokoService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        fragmentManager = getFragmentManager();
        createFragments();
        npvStorageCondition.setOnValueChangedListener(this);
        npvStorageCondition.setValue(0);
        npvStorageCondition.setMinValue(0);
        npvStorageCondition.setMaxValue(3);
        EventBus.getDefault().register(this);
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        tempFragment = StorageTempFragment.newInstance();
        fragmentTransaction.add(R.id.frame_storage_condition, tempFragment);
        humidityFragment = StorageHumidityFragment.newInstance();
        fragmentTransaction.add(R.id.frame_storage_condition, humidityFragment);
        thFragment = StorageTHFragment.newInstance();
        fragmentTransaction.add(R.id.frame_storage_condition, thFragment);
        timeFragment = StorageTimeFragment.newInstance();
        fragmentTransaction.add(R.id.frame_storage_condition, timeFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        LogModule.i(newVal + "");
        mStorageType = newVal;
        LogModule.i(picker.getContentByCurrValue());
        showFragment(newVal);
    }

    private void showFragment(int newVal) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (newVal) {
            case 0:
                fragmentTransaction.show(tempFragment).hide(humidityFragment).hide(thFragment).hide(timeFragment).commit();
                break;
            case 1:
                fragmentTransaction.hide(tempFragment).show(humidityFragment).hide(thFragment).hide(timeFragment).commit();
                break;
            case 2:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).show(thFragment).hide(timeFragment).commit();
                break;
            case 3:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).hide(thFragment).show(timeFragment).commit();
                break;
        }
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
                MokoSupport.getInstance().sendOrder(mMokoService.getTHPeriod(),
                        mMokoService.getStorageCondition(),
                        mMokoService.getDeviceTime(),
                        mMokoService.setTHNotifyOpen());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
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
                                    case GET_TH_PERIOD:
                                        if (value.length > 4) {
                                            byte[] period = Arrays.copyOfRange(value, 4, 6);
                                            String periodStr = MokoUtils.toInt(period) + "";
                                            etPeriod.setText(periodStr);
                                            etPeriod.setSelection(periodStr.length());
                                        }
                                        break;
                                    case GET_STORAGE_CONDITION:
                                        if (value.length > 6 && (value[4] & 0xff) == 0) {
                                            mStorageType = 0;
                                            npvStorageCondition.setValue(0);
                                            byte[] temp = Arrays.copyOfRange(value, 5, 7);
                                            tempFragment.setTempData(MokoUtils.toInt(temp));
                                        } else if (value.length > 6 && (value[4] & 0xff) == 1) {
                                            mStorageType = 1;
                                            npvStorageCondition.setValue(1);
                                            byte[] humidity = Arrays.copyOfRange(value, 5, 7);
                                            humidityFragment.setHumidityData(MokoUtils.toInt(humidity));
                                        } else if (value.length > 8 && (value[4] & 0xff) == 2) {
                                            mStorageType = 2;
                                            npvStorageCondition.setValue(2);
                                            byte[] temp = Arrays.copyOfRange(value, 5, 7);
                                            byte[] humidity = Arrays.copyOfRange(value, 7, 9);
                                            thFragment.setTempData(MokoUtils.toInt(temp));
                                            thFragment.setHumidityData(MokoUtils.toInt(humidity));
                                        } else if (value.length > 5 && (value[4] & 0xff) == 3) {
                                            mStorageType = 3;
                                            npvStorageCondition.setValue(3);
                                            timeFragment.setTimeData(value[5] & 0xff);
                                        }
                                        showFragment(mStorageType);
                                        break;
                                    case GET_DEVICE_TIME:
                                        if (value.length > 9) {
                                            int year = value[4] & 0xff;
                                            int month = value[5] & 0xff;
                                            int day = value[6] & 0xff;
                                            int hour = value[7] & 0xff;
                                            int minute = value[8] & 0xff;
                                            int second = value[9] & 0xff;
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.set(Calendar.YEAR, 2000 + year);
                                            calendar.set(Calendar.MONTH, month - 1);
                                            calendar.set(Calendar.DAY_OF_MONTH, day);
                                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                                            calendar.set(Calendar.MINUTE, minute);
                                            calendar.set(Calendar.SECOND, second);
                                            tvUpdateDate.setText(Utils.calendar2strDate(calendar, AppConstants.PATTERN_YYYY_MM_DD_HH_MM_SS));
                                        }
                                        break;
                                    case SET_TH_PERIOD:
                                        if (value.length > 3 && value[3] == 0) {
                                            mIsPeriodSuccess = true;
                                        }
                                        break;
                                    case SET_DEVICE_TIME:
                                        if (value.length > 3 && value[3] == 0) {
                                            ToastUtils.showToast(THDataActivity.this, "Success");
                                        } else {
                                            ToastUtils.showToast(THDataActivity.this, "Failed");
                                        }
                                        break;
                                    case SET_STORAGE_CONDITION:
                                        if (value.length > 3 && value[3] == 0) {
                                            mIsStorageSuccess = true;
                                        }
                                        if (mIsPeriodSuccess && mIsStorageSuccess) {
                                            ToastUtils.showToast(THDataActivity.this, "Success");
                                        } else {
                                            ToastUtils.showToast(THDataActivity.this, "Failed");
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
                                ToastUtils.showToast(THDataActivity.this, "Device Locked!");
                                back();
                            }
                            break;
                        case htData:
                            if (value.length > 3) {
                                byte[] tempBytes = Arrays.copyOfRange(value, 0, 2);
                                float temp = MokoUtils.byte2short(tempBytes) * 0.1f;
                                tvTemp.setText(MokoUtils.getDecimalFormat("0.0").format(temp));
                                byte[] humidityBytes = Arrays.copyOfRange(value, 2, 4);
                                float humidity = MokoUtils.toInt(humidityBytes) * 0.1f;
                                tvHumidity.setText(MokoUtils.getDecimalFormat("0.0").format(humidity));
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            AlertDialog.Builder builder = new AlertDialog.Builder(THDataActivity.this);
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

    @OnClick({R.id.tv_back, R.id.iv_save, R.id.tv_update, R.id.rl_export_data})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.iv_save:
                // 保存
                String periodStr = etPeriod.getText().toString();
                if (TextUtils.isEmpty(periodStr)) {
                    ToastUtils.showToast(this, "The Sampling Period can not be empty");
                    return;
                }
                int period = Integer.parseInt(periodStr);
                if (period < 1 || period > 65535) {
                    ToastUtils.showToast(this, "The Sampling Period range is 1~65535");
                    return;
                }
                showSyncingProgressDialog();
                String storageData = "";
                switch (mStorageType) {
                    case 0:
                        storageData = String.format("%04X", mSelectedTemp * 5);
                        break;
                    case 1:
                        storageData = String.format("%04X", mSelectedHumidity * 10);
                        break;
                    case 2:
                        storageData = String.format("%04X", mSelectedTemp * 5) + String.format("%04X", mSelectedHumidity * 10);
                        break;
                    case 3:
                        storageData = String.format("%02X", mSelectedTime);
                        break;
                }
                MokoSupport.getInstance().sendOrder(mMokoService.setTHPeriod(period), mMokoService.setStorageCondition(mStorageType, storageData));
                break;
            case R.id.tv_update:
                showSyncingProgressDialog();
                Calendar calendar = Calendar.getInstance();
                MokoSupport.getInstance().sendOrder(mMokoService.setDeviceTime(calendar.get(Calendar.YEAR) - 2000, calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)),
                        mMokoService.getDeviceTime());
                break;
            case R.id.rl_export_data:
                // 跳转导出数据页面
                startActivity(new Intent(this, ExportDataActivity.class));
                break;
        }
    }

    private void back() {
        // 关闭通知
        MokoSupport.getInstance().sendOrder(mMokoService.setTHNotifyClose());
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

    public void setSelectedTemp(int selectedTemp) {
        this.mSelectedTemp = selectedTemp;
    }

    public void setSelectedHumidity(int selectedHumidity) {
        this.mSelectedHumidity = selectedHumidity;
    }

    public void setSelectedTime(int selectedTime) {
        this.mSelectedTime = selectedTime;
    }
}
