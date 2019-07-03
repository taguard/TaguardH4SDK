package com.moko.beaconxpro.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.able.ISlotDataAction;
import com.moko.beaconxpro.activity.SlotDataActivity;
import com.moko.beaconxpro.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.entity.TxPowerEnum;
import com.moko.support.utils.MokoUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TlmFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "TlmFragment";


    @Bind(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @Bind(R.id.tv_tx_power)
    TextView tvTxPower;
    @Bind(R.id.et_adv_interval)
    EditText etAdvInterval;

    private SlotDataActivity activity;

    public TlmFragment() {
    }

    public static TlmFragment newInstance() {
        TlmFragment fragment = new TlmFragment();
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
        View view = inflater.inflate(R.layout.fragment_tlm, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbTxPower.setOnSeekBarChangeListener(this);
        setValue();
        return view;
    }

    private void setValue() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            etAdvInterval.setText("10");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            sbTxPower.setProgress(6);
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            etAdvInterval.setText(advIntervalProgress + "");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);
            txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
            tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
        }
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

    private byte[] advIntervalBytes;
    private byte[] txPowerBytes;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
            upgdateData(seekBar.getId(), progress);
            activity.onProgressChanged(seekBar.getId(), progress);
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            upgdateData(seekBar.getId(), progress);
        }
    }

    private void upgdateData(int viewId, int progress) {
        switch (viewId) {
            case R.id.sb_tx_power:
                TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
                int txPower = txPowerEnum.getTxPower();
                tvTxPower.setText(String.format("%ddBm", txPower));
                txPowerBytes = MokoUtils.toByteArray(txPower, 1);
                break;
        }
    }

    @Override
    public void upgdateProgress(int viewId, int progress) {
        switch (viewId) {
            case R.id.sb_tx_power:
                sbTxPower.setProgress(progress);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean isValid() {
        String advInterval = etAdvInterval.getText().toString();
        if (TextUtils.isEmpty(advInterval)) {
            ToastUtils.showToast(activity, "The Adv Interval can not be empty.");
            return false;
        }
        int advIntervalInt = Integer.parseInt(advInterval);
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(activity, "The Adv Interval range is 1~100");
            return false;
        }
        advIntervalBytes = MokoUtils.toByteArray(advIntervalInt, 2);
        return true;
    }

    @Override
    public void sendData() {
        byte[] tlmBytes = MokoUtils.hex2bytes(SlotFrameTypeEnum.TLM.getFrameType());
        MokoSupport.getInstance().sendOrder(
                // 切换通道，保证通道是在当前设置通道里
                activity.mMokoService.setSlot(activity.slotData.slotEnum),
                activity.mMokoService.setSlotData(tlmBytes),
                activity.mMokoService.setRadioTxPower(txPowerBytes),
                activity.mMokoService.setAdvInterval(advIntervalBytes)
        );
    }
}
