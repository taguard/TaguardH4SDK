package com.moko.beaconxplus.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.beaconxplus.AppConstants;
import com.moko.beaconxplus.R;
import com.moko.beaconxplus.able.ISlotDataAction;
import com.moko.beaconxplus.dialog.TriggerTypeDialog;
import com.moko.beaconxplus.fragment.AxisFragment;
import com.moko.beaconxplus.fragment.DeviceInfoFragment;
import com.moko.beaconxplus.fragment.IBeaconFragment;
import com.moko.beaconxplus.fragment.THFragment;
import com.moko.beaconxplus.fragment.TlmFragment;
import com.moko.beaconxplus.fragment.TriggerHumidityFragment;
import com.moko.beaconxplus.fragment.TriggerMovesFragment;
import com.moko.beaconxplus.fragment.TriggerTappedFragment;
import com.moko.beaconxplus.fragment.TriggerTempFragment;
import com.moko.beaconxplus.fragment.UidFragment;
import com.moko.beaconxplus.fragment.UrlFragment;
import com.moko.beaconxplus.service.MokoService;
import com.moko.beaconxplus.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderType;
import com.moko.support.entity.SlotData;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class SlotDataActivity extends FragmentActivity implements NumberPickerView.OnValueChangeListener {
    public MokoService mMokoService;
    @Bind(R.id.tv_slot_title)
    TextView tvSlotTitle;
    @Bind(R.id.iv_save)
    ImageView ivSave;
    @Bind(R.id.frame_slot_container)
    FrameLayout frameSlotContainer;
    @Bind(R.id.npv_slot_type)
    NumberPickerView npvSlotType;
    @Bind(R.id.iv_trigger)
    ImageView ivTrigger;
    @Bind(R.id.tv_trigger_type)
    TextView tvTriggerType;
    @Bind(R.id.frame_trigger_container)
    FrameLayout frameTriggerContainer;
    @Bind(R.id.rl_trigger)
    RelativeLayout rlTrigger;
    @Bind(R.id.rl_trigger_switch)
    RelativeLayout rlTriggerSwitch;
    private FragmentManager fragmentManager;
    private UidFragment uidFragment;
    private UrlFragment urlFragment;
    private TlmFragment tlmFragment;
    private IBeaconFragment iBeaconFragment;
    private DeviceInfoFragment deviceInfoFragment;
    private AxisFragment axisFragment;
    private THFragment thFragment;
    public SlotData slotData;
    private ISlotDataAction slotDataActionImpl;
    private HashMap<Integer, Integer> seekBarProgressHashMap;
    public int deviceType;
    private TriggerTempFragment tempFragment;
    private TriggerHumidityFragment humidityFragment;
    private TriggerTappedFragment tappedFragment;
    private TriggerMovesFragment movesFragment;
    private boolean mReceiverTag = false;
    private int triggerType;
    private byte[] triggerData;
    private String[] triggerArray;
    private int triggerTypeSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_data);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            slotData = (SlotData) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_SLOT_DATA);
            deviceType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, 0);
            triggerType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_TRIGGER_TYPE, 0);
            String triggerDataStr = getIntent().getStringExtra(AppConstants.EXTRA_KEY_TRIGGER_DATA);
            if (!TextUtils.isEmpty(triggerDataStr)) {
                triggerData = MokoUtils.hex2bytes(triggerDataStr);
            }
            LogModule.i(slotData.toString());
        }
        Intent intent = new Intent(this, MokoService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        fragmentManager = getFragmentManager();
        createFragments();
        if (deviceType == 0) {
            npvSlotType.setDisplayedValues(getResources().getStringArray(R.array.slot_type_no_sensor));
            npvSlotType.setMinValue(0);
            npvSlotType.setMaxValue(5);
            triggerArray = getResources().getStringArray(R.array.trigger_type_0);
        } else if (deviceType == 1) {
            npvSlotType.setDisplayedValues(getResources().getStringArray(R.array.slot_type_axis));
            npvSlotType.setMinValue(0);
            npvSlotType.setMaxValue(6);
            triggerArray = getResources().getStringArray(R.array.trigger_type_1);
        } else if (deviceType == 2) {
            npvSlotType.setDisplayedValues(getResources().getStringArray(R.array.slot_type_th));
            npvSlotType.setMinValue(0);
            npvSlotType.setMaxValue(6);
            triggerArray = getResources().getStringArray(R.array.trigger_type_2);
        } else if (deviceType == 3) {
            npvSlotType.setDisplayedValues(getResources().getStringArray(R.array.slot_type_all));
            npvSlotType.setMinValue(0);
            npvSlotType.setMaxValue(7);
            triggerArray = getResources().getStringArray(R.array.trigger_type_3);
        }
        npvSlotType.setOnValueChangedListener(this);

        npvSlotType.setValue(slotData.frameTypeEnum.ordinal());
        tvSlotTitle.setText(slotData.slotEnum.getTitle());
        showFragment(slotData.frameTypeEnum.ordinal());
        seekBarProgressHashMap = new HashMap<>();
        if (slotData.frameTypeEnum != SlotFrameTypeEnum.NO_DATA) {
            rlTriggerSwitch.setVisibility(View.VISIBLE);
        } else {
            rlTriggerSwitch.setVisibility(View.GONE);
        }
        if (triggerType > 0) {
            ivTrigger.setImageResource(R.drawable.connectable_checked);
            rlTrigger.setVisibility(View.VISIBLE);
        } else {
            ivTrigger.setImageResource(R.drawable.connectable_unchecked);
            rlTrigger.setVisibility(View.GONE);
        }
        createTriggerFragments();
        showTriggerFragment();
        setTriggerData();

        EventBus.getDefault().register(this);
    }

    private void setTriggerData() {
        switch (triggerType) {
            case 1:
                boolean isTempAbove = (triggerData[0] & 0xff) == 1;
                tvTriggerType.setText(isTempAbove ? triggerArray[2] : triggerArray[3]);

                triggerTypeSelected = isTempAbove ? 2 : 3;
                tempFragment.setTempType(isTempAbove);
                tempFragment.setData(MokoUtils.byte2short(Arrays.copyOfRange(triggerData, 1, 3)));
                tempFragment.setStart((triggerData[3] & 0xff) == 1);
                break;
            case 2:
                boolean isHumidityAbove = (triggerData[0] & 0xff) == 1;
                tvTriggerType.setText(isHumidityAbove ? triggerArray[4] : triggerArray[5]);

                triggerTypeSelected = isHumidityAbove ? 4 : 5;
                humidityFragment.setHumidityType(isHumidityAbove);
                byte[] humidityBytes = Arrays.copyOfRange(triggerData, 1, 3);
                humidityFragment.setData((MokoUtils.toInt(humidityBytes)));
                humidityFragment.setStart((triggerData[3] & 0xff) == 1);
                break;
            case 3:
                tvTriggerType.setText(triggerArray[0]);

                triggerTypeSelected = 0;
                tappedFragment.setIsDouble(true);
                byte[] tappedDoubleBytes = Arrays.copyOfRange(triggerData, 0, 2);
                tappedFragment.setData(MokoUtils.toInt(tappedDoubleBytes));
                tappedFragment.setStart((triggerData[2] & 0xff) == 1);
                break;
            case 4:
                tvTriggerType.setText(triggerArray[1]);

                triggerTypeSelected = 1;
                tappedFragment.setIsDouble(false);
                byte[] tappedTrapleBytes = Arrays.copyOfRange(triggerData, 0, 2);
                tappedFragment.setData(MokoUtils.toInt(tappedTrapleBytes));
                tappedFragment.setStart((triggerData[2] & 0xff) == 1);
                break;
            case 5:
                if (deviceType == 1) {
                    tvTriggerType.setText(triggerArray[2]);
                    triggerTypeSelected = 2;
                } else {
                    tvTriggerType.setText(triggerArray[6]);
                    triggerTypeSelected = 6;
                }
                byte[] movesBytes = Arrays.copyOfRange(triggerData, 0, 2);
                movesFragment.setData(MokoUtils.toInt(movesBytes));
                movesFragment.setStart((triggerData[2] & 0xff) == 1);
                break;
        }
    }

    private void showTriggerFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (triggerType) {
            case 1:
                fragmentTransaction.show(tempFragment).hide(humidityFragment).hide(tappedFragment).hide(movesFragment).commit();
                break;
            case 2:
                fragmentTransaction.hide(tempFragment).show(humidityFragment).hide(tappedFragment).hide(movesFragment).commit();
                break;
            case 3:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).show(tappedFragment).hide(movesFragment).commit();
                break;
            case 4:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).show(tappedFragment).hide(movesFragment).commit();
                break;
            case 5:
                fragmentTransaction.hide(tempFragment).hide(humidityFragment).hide(tappedFragment).show(movesFragment).commit();
                break;
        }
    }

    private void createTriggerFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        tempFragment = TriggerTempFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, tempFragment);
        humidityFragment = TriggerHumidityFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, humidityFragment);
        tappedFragment = TriggerTappedFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, tappedFragment);
        movesFragment = TriggerMovesFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, movesFragment);
        fragmentTransaction.commit();
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (deviceType == 0) {
            uidFragment = UidFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
            urlFragment = UrlFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
            tlmFragment = TlmFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
            iBeaconFragment = IBeaconFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
            deviceInfoFragment = DeviceInfoFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, deviceInfoFragment);
        } else if (deviceType == 1) {
            uidFragment = UidFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
            urlFragment = UrlFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
            tlmFragment = TlmFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
            iBeaconFragment = IBeaconFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
            deviceInfoFragment = DeviceInfoFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, deviceInfoFragment);
            axisFragment = AxisFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, axisFragment);
        } else if (deviceType == 2) {
            uidFragment = UidFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
            urlFragment = UrlFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
            tlmFragment = TlmFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
            iBeaconFragment = IBeaconFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
            deviceInfoFragment = DeviceInfoFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, deviceInfoFragment);
            thFragment = THFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, thFragment);
        } else if (deviceType == 3) {
            uidFragment = UidFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
            urlFragment = UrlFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
            tlmFragment = TlmFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
            iBeaconFragment = IBeaconFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
            deviceInfoFragment = DeviceInfoFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, deviceInfoFragment);
            axisFragment = AxisFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, axisFragment);
            thFragment = THFragment.newInstance();
            fragmentTransaction.add(R.id.frame_slot_container, thFragment);
        }
        fragmentTransaction.commit();

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
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.setPriority(300);
            registerReceiver(mReceiver, filter);
            mReceiverTag = true;
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                // 蓝牙未打开，开启蓝牙
                MokoSupport.getInstance().enableBluetooth();
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
                    SlotDataActivity.this.finish();
                }
            }
        });

    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                    abortBroadcast();
                }
                if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                    abortBroadcast();
                    ToastUtils.showToast(SlotDataActivity.this, "Successfully configure");
                    dismissSyncProgressDialog();
                    SlotDataActivity.this.setResult(SlotDataActivity.this.RESULT_OK);
                    SlotDataActivity.this.finish();
                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                    abortBroadcast();
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderType orderType = response.orderType;
                    byte[] value = response.responseValue;
                    switch (orderType) {
                        case advInterval:
                            break;
                    }

                }
                if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                    abortBroadcast();
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_CURRENT_DATA_TYPE);
                    byte[] value = intent.getByteArrayExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE);
                    switch (orderType) {
                        case notifyConfig:
                            String valueHexStr = MokoUtils.bytesToHexString(value);
                            if ("eb63000100".equals(valueHexStr.toLowerCase())) {
                                // 设备上锁
                                ToastUtils.showToast(SlotDataActivity.this, "Locked");
                                SlotDataActivity.this.finish();
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            // 蓝牙断开
                            SlotDataActivity.this.finish();
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

    @OnClick({R.id.tv_back, R.id.iv_save, R.id.tv_trigger_type, R.id.iv_trigger})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.iv_save:
                OrderTask orderTask = null;
                // 发送触发条件
                switch (triggerType) {
                    case 0:
                        orderTask = mMokoService.setTriggerClose();
                        break;
                    case 1:
                        orderTask = mMokoService.setTHTrigger(triggerType, tempFragment.getTempType(), tempFragment.getData(), tempFragment.isStart());
                        break;
                    case 2:
                        orderTask = mMokoService.setTHTrigger(triggerType, humidityFragment.getHumidityType(), tempFragment.getData(), tempFragment.isStart());
                        break;
                    case 3:
                    case 4:
                        if (tappedFragment.getData() < 0) {
                            return;
                        }
                        orderTask = mMokoService.setTappedMovesTrigger(triggerType, tappedFragment.getData(), tappedFragment.isStart());
                        break;
                    case 5:
                        if (movesFragment.getData() < 0) {
                            return;
                        }
                        orderTask = mMokoService.setTappedMovesTrigger(triggerType, movesFragment.getData(), movesFragment.isStart());
                        break;
                }
                if (slotDataActionImpl == null) {
                    byte[] noData = new byte[]{(byte) 0xFF};
                    MokoSupport.getInstance().sendOrder(
                            // 切换通道，保证通道是在当前设置通道里
                            mMokoService.setSlot(slotData.slotEnum),
                            mMokoService.setSlotData(noData),
                            orderTask
                    );
                    return;
                }
                if (!slotDataActionImpl.isValid()) {
                    return;
                }
                showSyncingProgressDialog();
                slotDataActionImpl.sendData();
                if (orderTask != null) {
                    MokoSupport.getInstance().sendOrder(orderTask);
                }
                break;
            case R.id.tv_trigger_type:
                // 选择触发条件
                TriggerTypeDialog dialog = new TriggerTypeDialog();
                dialog.setListener(new TriggerTypeDialog.OnDataSelectedListener() {
                    @Override
                    public void onDataSelected(int data) {
                        triggerTypeSelected = data;
                        switch (triggerTypeSelected) {
                            case 0:
                                triggerType = 3;
                                break;
                            case 1:
                                triggerType = 4;
                                break;
                            case 2:
                                if (deviceType == 1) {
                                    triggerType = 5;
                                } else {
                                    triggerType = 1;
                                }
                                break;
                            case 3:
                                triggerType = 1;
                                break;
                            case 4:
                                triggerType = 2;
                                break;
                            case 5:
                                triggerType = 2;
                                break;
                            case 6:
                                triggerType = 5;
                                break;
                        }
                        showTriggerFragment();
                        switch (triggerTypeSelected) {
                            case 0:
                                tappedFragment.setIsDouble(true);
                                break;
                            case 1:
                                tappedFragment.setIsDouble(false);
                                break;
                            case 2:
                                if (deviceType != 1) {
                                    tempFragment.setTempType(true);
                                }
                                break;
                            case 3:
                                tempFragment.setTempType(false);
                                break;
                            case 4:
                                humidityFragment.setHumidityType(true);
                                break;
                            case 5:
                                humidityFragment.setHumidityType(false);
                                break;
                        }
                        tvTriggerType.setText(triggerArray[triggerTypeSelected]);

                    }
                });
                dialog.setTriggerArray(triggerArray);
                dialog.setSelected(triggerTypeSelected);
                dialog.show(getSupportFragmentManager());
                break;
            case R.id.iv_trigger:
                if (triggerType > 0) {
                    triggerType = 0;
                    ivTrigger.setImageResource(R.drawable.connectable_unchecked);
                    rlTrigger.setVisibility(View.GONE);
                } else {
                    ivTrigger.setImageResource(R.drawable.connectable_checked);
                    rlTrigger.setVisibility(View.VISIBLE);
                    triggerType = 3;
                    showTriggerFragment();
                }
                break;
        }
    }

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        LogModule.i(newVal + "");
        LogModule.i(picker.getContentByCurrValue());
        showFragment(newVal);
        if (SlotFrameTypeEnum.fromEnumOrdinal(newVal) != SlotFrameTypeEnum.NO_DATA) {
            rlTriggerSwitch.setVisibility(View.VISIBLE);
        } else {
            rlTriggerSwitch.setVisibility(View.GONE);
        }
        if (!seekBarProgressHashMap.isEmpty() && slotDataActionImpl != null) {
            for (int key : seekBarProgressHashMap.keySet()) {
                slotDataActionImpl.upgdateProgress(key, seekBarProgressHashMap.get(key));
            }
        }
    }

    private void showFragment(int newVal) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (newVal) {
            case 0:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(thFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).show(tlmFragment).commit();
                    slotDataActionImpl = tlmFragment;
                }
                break;
            case 1:
                if (deviceType == 0) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(thFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).show(uidFragment).commit();
                    slotDataActionImpl = uidFragment;
                }
                break;
            case 2:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(thFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).show(urlFragment).commit();
                    slotDataActionImpl = urlFragment;
                }
                break;
            case 3:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).hide(axisFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).hide(thFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(deviceInfoFragment).show(iBeaconFragment).hide(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = iBeaconFragment;
                }
                break;
            case 4:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).hide(axisFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).hide(thFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(deviceInfoFragment).hide(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = deviceInfoFragment;
                }
                break;
            case 5:
                if (deviceType == 0) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).commit();
                    slotDataActionImpl = null;
                } else if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).commit();
                    slotDataActionImpl = null;
                } else if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(thFragment).commit();
                    slotDataActionImpl = null;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).hide(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = null;
                }
                break;
            case 6:
                if (deviceType == 1) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(axisFragment).commit();
                    slotDataActionImpl = axisFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(axisFragment).hide(thFragment).commit();
                    slotDataActionImpl = axisFragment;
                }
                break;
            case 7:
                if (deviceType == 2) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(thFragment).commit();
                    slotDataActionImpl = thFragment;
                } else if (deviceType == 3) {
                    fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(deviceInfoFragment).show(thFragment).hide(axisFragment).commit();
                    slotDataActionImpl = thFragment;
                }
                break;

        }
        slotData.frameTypeEnum = SlotFrameTypeEnum.fromEnumOrdinal(newVal);
    }


    public void onProgressChanged(int viewId, int progress) {
        seekBarProgressHashMap.put(viewId, progress);
    }
}
