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
import com.moko.beaconxpro.dialog.StorageHumidityDialog;
import com.moko.beaconxpro.dialog.StorageTempDialog;
import com.moko.support.utils.MokoUtils;

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
                StorageTempDialog tempDialog = new StorageTempDialog();
                tempDialog.setListener(new StorageTempDialog.OnDataSelectedListener() {
                    @Override
                    public void onDataSelected(String data) {
                        float temp = Float.parseFloat(data);
                        mTempSelected = (int) (temp * 2);
                        if (temp > 0 && mHumiditySelected > 0) {
                            tvTHTips.setText(getString(R.string.t_h_tips_0, data, mHumiditySelected));
                        } else if (temp == 0 && mHumiditySelected > 0) {
                            tvTHTips.setText(getString(R.string.t_h_tips_1, mHumiditySelected));
                        } else if (temp > 0 && mHumiditySelected == 0) {
                            tvTHTips.setText(getString(R.string.t_h_tips_2, data));
                        } else if (temp == 0 && mHumiditySelected == 0) {
                            tvTHTips.setText(R.string.t_h_tips_3);
                        }
                        tvStorageTemp.setText(data);
                        activity.setSelectedTemp(mTempSelected);
                    }
                });
                tempDialog.setSelected(mTempSelected);
                tempDialog.show(activity.getSupportFragmentManager());
                break;
            case R.id.tv_storage_humidity:
                StorageHumidityDialog humidityDialog = new StorageHumidityDialog();
                humidityDialog.setListener(new StorageHumidityDialog.OnDataSelectedListener() {
                    @Override
                    public void onDataSelected(String data) {
                        mHumiditySelected = Integer.parseInt(data);
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
                        tvStorageHumidity.setText(data);
                        activity.setSelectedHumidity(mHumiditySelected);
                    }
                });
                humidityDialog.setSelected(mHumiditySelected);
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
