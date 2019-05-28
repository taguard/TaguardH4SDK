package com.moko.beaconxplus.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.beaconxplus.AppConstants;
import com.moko.beaconxplus.R;
import com.moko.beaconxplus.activity.DeviceInfoActivity;
import com.moko.beaconxplus.utils.BeaconXParser;
import com.moko.support.MokoSupport;
import com.moko.support.entity.SlotData;
import com.moko.support.entity.SlotEnum;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.utils.MokoUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SlotFragment extends Fragment {

    private static final String TAG = "SlotFragment";
    @Bind(R.id.iv_slot1)
    ImageView ivSlot1;
    @Bind(R.id.tv_slot1)
    TextView tvSlot1;
    @Bind(R.id.rl_slot1)
    RelativeLayout rlSlot1;
    @Bind(R.id.iv_slot2)
    ImageView ivSlot2;
    @Bind(R.id.tv_slot2)
    TextView tvSlot2;
    @Bind(R.id.rl_slot2)
    RelativeLayout rlSlot2;
    @Bind(R.id.iv_slot3)
    ImageView ivSlot3;
    @Bind(R.id.tv_slot3)
    TextView tvSlot3;
    @Bind(R.id.rl_slot3)
    RelativeLayout rlSlot3;
    @Bind(R.id.iv_slot4)
    ImageView ivSlot4;
    @Bind(R.id.tv_slot4)
    TextView tvSlot4;
    @Bind(R.id.rl_slot4)
    RelativeLayout rlSlot4;
    @Bind(R.id.iv_slot5)
    ImageView ivSlot5;
    @Bind(R.id.tv_slot5)
    TextView tvSlot5;
    @Bind(R.id.rl_slot5)
    RelativeLayout rlSlot5;
    @Bind(R.id.iv_slot6)
    ImageView ivSlot6;
    @Bind(R.id.tv_slot6)
    TextView tvSlot6;
    @Bind(R.id.rl_slot6)
    RelativeLayout rlSlot6;

    private DeviceInfoActivity activity;
    private SlotData slotData;
    private int deviceType;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        SlotFragment fragment = new SlotFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_slot, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @OnClick({R.id.rl_slot1, R.id.rl_slot2, R.id.rl_slot3, R.id.rl_slot4, R.id.rl_slot5})
    public void onViewClicked(View view) {
        slotData = new SlotData();
        SlotFrameTypeEnum frameType = (SlotFrameTypeEnum) view.getTag();
        slotData.frameTypeEnum = frameType;
        // NO DATA直接跳转
        switch (view.getId()) {
            case R.id.rl_slot1:
                createData(frameType, SlotEnum.SLOT_1);
                break;
            case R.id.rl_slot2:
                createData(frameType, SlotEnum.SLOT_2);
                break;
            case R.id.rl_slot3:
                createData(frameType, SlotEnum.SLOT_3);
                break;
            case R.id.rl_slot4:
                createData(frameType, SlotEnum.SLOT_4);
                break;
            case R.id.rl_slot5:
                createData(frameType, SlotEnum.SLOT_5);
                break;
            case R.id.rl_slot6:
                createData(frameType, SlotEnum.SLOT_6);
                break;
        }
    }

    private void createData(SlotFrameTypeEnum frameType, SlotEnum slot) {
        slotData.slotEnum = slot;
        switch (frameType) {
            case NO_DATA:
//                Intent intent = new Intent(getActivity(), SlotDataActivity.class);
//                intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
//                startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
                break;
            case IBEACON:
                getiBeaconData(slot);
                break;
            case TLM:
            case URL:
            case UID:
                getEddystoneData(slot);
                break;
        }
    }

    private void getEddystoneData(SlotEnum slotEnum) {
        activity.showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(
                activity.mMokoService.setSlot(slotEnum),
                activity.mMokoService.getSlotData(),
                activity.mMokoService.getRadioTxPower(),
                activity.mMokoService.getAdvInterval()
        );
    }

    private void getiBeaconData(SlotEnum slotEnum) {
        activity.showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(
                activity.mMokoService.setSlot(slotEnum),
                activity.mMokoService.getiBeaconUUID(),
                activity.mMokoService.getiBeaconInfo(),
                activity.mMokoService.getRadioTxPower(),
                activity.mMokoService.getAdvInterval()
        );
    }

    // 10 20 50 40 FF FF
    public void updateSlotType(byte[] value) {
        changeView((int) value[0] & 0xff, tvSlot1, ivSlot1, rlSlot1);
        changeView((int) value[1] & 0xff, tvSlot2, ivSlot2, rlSlot2);
        changeView((int) value[2] & 0xff, tvSlot3, ivSlot3, rlSlot3);
        changeView((int) value[3] & 0xff, tvSlot4, ivSlot4, rlSlot4);
        if (deviceType == 0) {
            changeView((int) value[4] & 0xff, tvSlot5, ivSlot5, rlSlot5);
            changeView((int) value[5] & 0xff, tvSlot6, ivSlot6, rlSlot6);
        } else if (deviceType == 1) {
            changeView((int) value[4] & 0xff, tvSlot5, ivSlot5, rlSlot5);
            ivSlot6.setImageResource(R.drawable.axis_icon);
            tvSlot6.setText(SlotFrameTypeEnum.AXIS.getShowName());
            rlSlot6.setTag(SlotFrameTypeEnum.AXIS);
        } else if (deviceType == 2) {
            changeView((int) value[4] & 0xff, tvSlot5, ivSlot5, rlSlot5);
            ivSlot6.setImageResource(R.drawable.th_icon);
            tvSlot6.setText(SlotFrameTypeEnum.TH.getShowName());
            rlSlot6.setTag(SlotFrameTypeEnum.TH);
        } else if (deviceType == 3) {
            ivSlot5.setImageResource(R.drawable.axis_icon);
            tvSlot5.setText(SlotFrameTypeEnum.AXIS.getShowName());
            rlSlot5.setTag(SlotFrameTypeEnum.AXIS);
            ivSlot6.setImageResource(R.drawable.th_icon);
            tvSlot6.setText(SlotFrameTypeEnum.TH.getShowName());
            rlSlot6.setTag(SlotFrameTypeEnum.TH);
        }
    }

    private void changeView(int frameType, TextView tvSlot, ImageView ivSlot, RelativeLayout rlSlot) {
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum == null) {
            return;
        }
        switch (slotFrameTypeEnum) {
            case UID:
                ivSlot.setImageResource(R.drawable.eddystone_icon);
                break;
            case URL:
                ivSlot.setImageResource(R.drawable.eddystone_icon);
                break;
            case TLM:
                ivSlot.setImageResource(R.drawable.eddystone_icon);
                break;
            case IBEACON:
                ivSlot.setImageResource(R.drawable.ibeacon_icon);
                break;
            case DEVICE:
                ivSlot.setImageResource(R.drawable.device_icon);
                break;
            case NO_DATA:
                ivSlot.setImageResource(R.drawable.no_data_icon);
                break;

        }
        tvSlot.setText(slotFrameTypeEnum.getShowName());
        rlSlot.setTag(slotFrameTypeEnum);
    }

    private String iBeaconUUID;
    private String major;
    private String minor;
    private int rssi_1m;
    private int txPower;
    private int advInterval;

    // eb640010e2c56db5dffb48d2b060d0f5a71096e0
    public void setiBeaconUUID(byte[] value) {
        String valueHex = MokoUtils.bytesToHexString(value);
        iBeaconUUID = valueHex.substring(8);
        slotData.iBeaconUUID = iBeaconUUID;
    }

    // eb6600050000000000
    public void setiBeaconInfo(byte[] value) {
        String valueHex = MokoUtils.bytesToHexString(value);
        major = valueHex.substring(8, 12);
        minor = valueHex.substring(12, 16);
        rssi_1m = Integer.parseInt(valueHex.substring(16), 16);
        slotData.major = major;
        slotData.minor = minor;
        slotData.rssi_1m = 0 - rssi_1m;
    }

    // 00
    public void setTxPower(byte[] value) {
        txPower = value[0];
        slotData.txPower = txPower;
    }

    // 0064
    public void setAdvInterval(byte[] value) {
        advInterval = Integer.parseInt(MokoUtils.bytesToHexString(value), 16);
        slotData.advInterval = advInterval;
//        Intent intent = new Intent(getActivity(), SlotDataActivity.class);
//        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
//        startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == AppConstants.REQUEST_CODE_SLOT_DATA) {
                Log.i(TAG, "onActivityResult: ");
                activity.getSlotType();
            }
        }
    }

    // 不同类型的数据长度不同
    public void setSlotData(byte[] value) {
        int frameType = value[0];
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum != null) {
            switch (slotFrameTypeEnum) {
                case URL:
                    // URL：10cf014c6f766500
                    BeaconXParser.parseUrlData(slotData, value);
                    break;
                case TLM:
                    break;
                case UID:
                    BeaconXParser.parseUidData(slotData, value);
                    break;
            }
        }
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }
}