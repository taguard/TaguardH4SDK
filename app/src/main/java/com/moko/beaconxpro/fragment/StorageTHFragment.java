package com.moko.beaconxpro.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.activity.THDataActivity;
import com.moko.beaconxpro.dialog.BottomDialog;
import com.moko.support.utils.MokoUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorageTHFragment extends Fragment {

    private static final String TAG = "StorageTHFragment";
    @BindView(R.id.tv_storage_temp)
    TextView tvStorageTemp;
    @BindView(R.id.tv_storage_humidity)
    TextView tvStorageHumidity;
    @BindView(R.id.tv_t_h_tips)
    TextView tvTHTips;
    private ArrayList<String> mHumidityDatas;
    private ArrayList<String> mTempDatas;

    private THDataActivity activity;


    public StorageTHFragment() {
    }

    public static StorageTHFragment newInstance() {
        StorageTHFragment fragment = new StorageTHFragment();
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
        View view = inflater.inflate(R.layout.fragment_storage_t_h, container, false);
        ButterKnife.bind(this, view);
        activity = (THDataActivity) getActivity();
        mHumidityDatas = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            mHumidityDatas.add(i + "");
        }
        mTempDatas = new ArrayList<>();
        for (int i = 0; i <= 200; i++) {
            mTempDatas.add(MokoUtils.getDecimalFormat("0.0").format(i * 0.5));
        }
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
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @OnClick({R.id.tv_storage_temp, R.id.tv_storage_humidity})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_storage_temp:
                BottomDialog tempDialog = new BottomDialog();
                tempDialog.setListener(value -> {
                    mTempSelected = value;
                    if (mTempSelected > 0 && mHumiditySelected > 0) {
                        tvTHTips.setText(getString(R.string.t_h_tips_0, mTempDatas.get(value), mHumiditySelected));
                    } else if (mTempSelected == 0 && mHumiditySelected > 0) {
                        tvTHTips.setText(getString(R.string.t_h_tips_1, mHumiditySelected));
                    } else if (mTempSelected > 0 && mHumiditySelected == 0) {
                        tvTHTips.setText(getString(R.string.t_h_tips_2, mTempDatas.get(value)));
                    } else if (mTempSelected == 0 && mHumiditySelected == 0) {
                        tvTHTips.setText(R.string.t_h_tips_3);
                    }
                    tvStorageTemp.setText(mTempDatas.get(value));
                    activity.setSelectedTemp(mTempSelected);
                });

                tempDialog.show(activity.getSupportFragmentManager());
                break;
            case R.id.tv_storage_humidity:
                BottomDialog humidityDialog = new BottomDialog();
                humidityDialog.setDatas(mHumidityDatas, mHumiditySelected);
                humidityDialog.setListener(value -> {
                    mHumiditySelected = value;
                    if (mTempSelected > 0 && mHumiditySelected > 0) {
                        String tempStr = MokoUtils.getDecimalFormat("0.0").format(mTempSelected * 0.5);
                        tvTHTips.setText(getString(R.string.t_h_tips_0, tempStr, mHumiditySelected));
                    } else if (mTempSelected == 0 && mHumiditySelected > 0) {
                        tvTHTips.setText(getString(R.string.t_h_tips_1, mHumiditySelected));
                    } else if (mTempSelected > 0 && mHumiditySelected == 0) {
                        String tempStr = MokoUtils.getDecimalFormat("0.0").format(mTempSelected * 0.5);
                        tvTHTips.setText(getString(R.string.t_h_tips_2, tempStr));
                    } else if (mTempSelected == 0 && mHumiditySelected == 0) {
                        tvTHTips.setText(R.string.t_h_tips_3);
                    }
                    tvStorageHumidity.setText(String.valueOf(value));
                    activity.setSelectedHumidity(value);
                });
                humidityDialog.show(activity.getSupportFragmentManager());
                break;
        }

    }

    private int mTempSelected;

    public void setTempData(int data) {
        mTempSelected = data / 5;
        if (mTempSelected > 0 && mHumiditySelected > 0) {
            String tempStr = MokoUtils.getDecimalFormat("0.0").format(mTempSelected * 0.5);
            tvTHTips.setText(getString(R.string.t_h_tips_0, tempStr, mHumiditySelected));
        } else if (mTempSelected == 0 && mHumiditySelected > 0) {
            tvTHTips.setText(getString(R.string.t_h_tips_1, mHumiditySelected));
        } else if (mTempSelected > 0 && mHumiditySelected == 0) {
            String tempStr = MokoUtils.getDecimalFormat("0.0").format(mTempSelected * 0.5);
            tvTHTips.setText(getString(R.string.t_h_tips_2, tempStr));
        } else if (mTempSelected == 0 && mHumiditySelected == 0) {
            tvTHTips.setText(R.string.t_h_tips_3);
        }
        tvStorageTemp.setText(MokoUtils.getDecimalFormat("0.0").format(mTempSelected * 0.5));
    }

    private int mHumiditySelected;

    public void setHumidityData(int data) {
        mHumiditySelected = data / 10;
        if (mTempSelected > 0 && mHumiditySelected > 0) {
            String tempStr = MokoUtils.getDecimalFormat("0.0").format(mTempSelected * 0.5);
            tvTHTips.setText(getString(R.string.t_h_tips_0, tempStr, mHumiditySelected));
        } else if (mTempSelected == 0 && mHumiditySelected > 0) {
            tvTHTips.setText(getString(R.string.t_h_tips_1, mHumiditySelected));
        } else if (mTempSelected > 0 && mHumiditySelected == 0) {
            String tempStr = MokoUtils.getDecimalFormat("0.0").format(mTempSelected * 0.5);
            tvTHTips.setText(getString(R.string.t_h_tips_2, tempStr));
        } else if (mTempSelected == 0 && mHumiditySelected == 0) {
            tvTHTips.setText(R.string.t_h_tips_3);
        }
        tvStorageHumidity.setText(mHumiditySelected + "");
    }
}
