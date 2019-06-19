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
import com.moko.beaconxplus.dialog.StorageTimeDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorageTimeFragment extends Fragment {

    private static final String TAG = "StorageTimeFragment";
    @Bind(R.id.tv_storage_time_only)
    TextView tvStorageTimeOnly;

    private THDataActivity activity;


    public StorageTimeFragment() {
    }

    public static StorageTimeFragment newInstance() {
        StorageTimeFragment fragment = new StorageTimeFragment();
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
        View view = inflater.inflate(R.layout.fragment_storage_time, container, false);
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

    @OnClick(R.id.tv_storage_temp_only)
    public void onViewClicked() {
        StorageTimeDialog dialog = new StorageTimeDialog();
        dialog.setListener(new StorageTimeDialog.OnDataSelectedListener() {
            @Override
            public void onDataSelected(String data) {
                mSelected = Integer.parseInt(data);
                tvStorageTimeOnly.setText(data);
                activity.setSelectedTime(mSelected);
            }
        });
        dialog.setSelected(mSelected);
        dialog.show(activity.getSupportFragmentManager());
    }

    private int mSelected;

    public void setTimeData(int data) {
        mSelected = data;
        tvStorageTimeOnly.setText(mSelected + "");
    }
}
