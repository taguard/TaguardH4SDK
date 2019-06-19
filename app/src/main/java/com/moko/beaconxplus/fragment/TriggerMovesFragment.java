package com.moko.beaconxplus.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.moko.beaconxplus.R;
import com.moko.beaconxplus.activity.SlotDataActivity;
import com.moko.beaconxplus.utils.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TriggerMovesFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "MovesFragment";


    @Bind(R.id.tv_trigger_tips)
    TextView tvTriggerTips;
    @Bind(R.id.rb_always_start)
    RadioButton rbAlwaysStart;
    @Bind(R.id.rb_start_advertising)
    RadioButton rbStartAdvertising;
    @Bind(R.id.rb_always_stop)
    RadioButton rbAlwaysStop;
    @Bind(R.id.rb_stop_advertising)
    RadioButton rbStopAdvertising;
    @Bind(R.id.rg_moves)
    RadioGroup rgMoves;
    @Bind(R.id.et_start)
    EditText etStart;
    @Bind(R.id.et_stop)
    EditText etStop;


    private SlotDataActivity activity;


    public TriggerMovesFragment() {
    }

    public static TriggerMovesFragment newInstance() {
        TriggerMovesFragment fragment = new TriggerMovesFragment();
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
        View view = inflater.inflate(R.layout.fragment_trigger_moves, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        rgMoves.setOnCheckedChangeListener(this);
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

    private boolean mIsStart = true;
    private int mDuration;
    private boolean mIsDouble;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_always_start:
                mIsStart = true;
                tvTriggerTips.setText(getString(R.string.trigger_moved_tips_1, "advertise"));
                break;
            case R.id.rb_start_advertising:
                mIsStart = true;
                tvTriggerTips.setText(getString(R.string.trigger_moved_tips_2, "start", String.format("%ds", mDuration)));
                break;
            case R.id.rb_always_stop:
                mIsStart = false;
                tvTriggerTips.setText(getString(R.string.trigger_moved_tips_1, "stop advertising"));
                break;
            case R.id.rb_stop_advertising:
                mIsStart = false;
                tvTriggerTips.setText(getString(R.string.trigger_moved_tips_2, "stop", String.format("%ds", mDuration)));
                break;
        }
    }

    public void setStart(boolean isStart) {
        mIsStart = isStart;
    }

    public boolean isStart() {
        return mIsStart;
    }


    public void setData(int data) {
        mDuration = data;
        if (data == 0) {
            if (mIsStart) {
                rbAlwaysStart.setChecked(true);
                etStart.setText(mDuration + "");
            } else {
                rbAlwaysStop.setChecked(true);
                etStop.setText(mDuration + "");
            }
        } else {
            if (mIsStart) {
                rbStartAdvertising.setChecked(true);
                etStart.setText(mDuration + "");
            } else {
                rbStopAdvertising.setChecked(true);
                etStop.setText(mDuration + "");
            }
        }
    }

    public int getData() {
        String duration = "";
        if (rbStartAdvertising.isChecked()) {
            duration = etStart.getText().toString();
        } else if (rbStopAdvertising.isChecked()) {
            duration = etStop.getText().toString();
        } else {
            duration = "0";
        }
        if (TextUtils.isEmpty(duration)) {
            ToastUtils.showToast(getActivity(), "The advertising can not be empty.");
            return -1;
        }
        mDuration = Integer.parseInt(duration);
        if ((rbStartAdvertising.isChecked() || rbStopAdvertising.isChecked()) && (mDuration < 1 || mDuration > 65535)) {
            ToastUtils.showToast(activity, "The advertising range is 1~65535");
            return -1;
        }
        return mDuration;
    }
}
