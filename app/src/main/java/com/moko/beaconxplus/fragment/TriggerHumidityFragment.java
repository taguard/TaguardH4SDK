package com.moko.beaconxplus.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.beaconxplus.R;
import com.moko.beaconxplus.activity.SlotDataActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TriggerHumidityFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "HumidityFragment";

    @Bind(R.id.sb_trigger_humidity)
    SeekBar sbTriggerHumidity;
    @Bind(R.id.tv_trigger_humidiy)
    TextView tvTriggerHumidiy;
    @Bind(R.id.rb_start)
    RadioButton rbStart;
    @Bind(R.id.rb_stop)
    RadioButton rbStop;
    @Bind(R.id.rg_advertising)
    RadioGroup rgAdvertising;
    @Bind(R.id.tv_trigger_tips)
    TextView tvTriggerTips;
    @Bind(R.id.trigger_humidiy)
    TextView triggerHumidiy;


    private SlotDataActivity activity;


    public TriggerHumidityFragment() {
    }

    public static TriggerHumidityFragment newInstance() {
        TriggerHumidityFragment fragment = new TriggerHumidityFragment();
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
        View view = inflater.inflate(R.layout.fragment_trigger_humidity, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        rgAdvertising.setOnCheckedChangeListener(this);
        sbTriggerHumidity.setOnSeekBarChangeListener(this);
        if (mIsStart) {
            rbStart.setChecked(true);
        } else {
            rbStop.setChecked(true);
        }
        sbTriggerHumidity.setProgress(mProgress);
        String humidityStr = String.format("%d%%", mProgress);
        tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start" : "stop", "humidity", mIsAbove ? "above" : "below", humidityStr));
        triggerHumidiy.setText(mIsAbove ? "Humidity Above" : "Humidity Below");
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


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mProgress = progress;
        String humidityStr = String.format("%d%%", progress);
        tvTriggerHumidiy.setText(humidityStr);
        tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start" : "stop", "humidity", mIsAbove ? "above" : "below", humidityStr));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private boolean mIsStart = true;
    private int mProgress;
    private boolean mIsAbove = true;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_start:
                mIsStart = true;
                break;
            case R.id.rb_stop:
                mIsStart = false;
                break;
        }
        String humidityStr = String.format("%d%%", mProgress);
        tvTriggerTips.setText(getString(R.string.trigger_t_h_tips,
                mIsStart ? "start" : "stop", "humidity", mIsAbove ? "above" : "below", humidityStr));
    }

    public void setStart(boolean isStart) {
        mIsStart = isStart;
    }

    public boolean isStart() {
        return mIsStart;
    }

    public void setData(int data) {
        mProgress = (int) (data * 0.1f);
    }

    public int getData() {
        return (mProgress) * 10;
    }

    public void setHumidityType(boolean isAbove) {
        mIsAbove = isAbove;
    }

    public boolean getHumidityType() {
        return mIsAbove;
    }
}
