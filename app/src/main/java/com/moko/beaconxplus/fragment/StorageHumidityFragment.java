package com.moko.beaconxplus.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.beaconxplus.R;
import com.moko.beaconxplus.activity.THDataActivity;
import com.moko.beaconxplus.dialog.StorageHumidityDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorageHumidityFragment extends Fragment {

    private static final String TAG = "StorageHumidityFragment";
    @Bind(R.id.tv_storage_humidity_only)
    TextView tvStorageHumidityOnly;
    @Bind(R.id.tv_humidity_only_tips)
    TextView tvHumidityOnlyTips;

    private THDataActivity activity;


    public StorageHumidityFragment() {
    }

    public static StorageHumidityFragment newInstance() {
        StorageHumidityFragment fragment = new StorageHumidityFragment();
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
        View view = inflater.inflate(R.layout.fragment_storage_humidity, container, false);
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

    @OnClick(R.id.tv_storage_humidity_only)
    public void onViewClicked() {
        StorageHumidityDialog dialog = new StorageHumidityDialog();
        dialog.setListener(new StorageHumidityDialog.OnDataSelectedListener() {
            @Override
            public void onDataSelected(String data) {
                mSelected = Integer.parseInt(data);
                if (mSelected == 0) {
                    tvHumidityOnlyTips.setText(R.string.humidity_only_tips_0);
                } else {
                    tvHumidityOnlyTips.setText(getString(R.string.humidity_only_tips_1, mSelected));
                }
                tvStorageHumidityOnly.setText(data);
                activity.setSelectedHumidity(mSelected);
            }
        });
        dialog.setSelected(mSelected);
        dialog.show(activity.getSupportFragmentManager());
    }

    private int mSelected;

    public void setHumidityData(int data) {
        mSelected = data / 10;
        if (mSelected == 0) {
            tvHumidityOnlyTips.setText(R.string.humidity_only_tips_0);
        } else {
            tvHumidityOnlyTips.setText(getString(R.string.humidity_only_tips_1, mSelected));
        }
        tvStorageHumidityOnly.setText(mSelected + "");
    }
}
