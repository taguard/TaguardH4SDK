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
import com.moko.beaconxplus.dialog.StorageTempDialog;
import com.moko.support.utils.MokoUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorageTempFragment extends Fragment {

    private static final String TAG = "StorageTempFragment";
    @Bind(R.id.tv_storage_temp_only)
    TextView tvStorageTempOnly;
    @Bind(R.id.tv_temp_only_tips)
    TextView tvTempOnlyTips;

    private THDataActivity activity;


    public StorageTempFragment() {
    }

    public static StorageTempFragment newInstance() {
        StorageTempFragment fragment = new StorageTempFragment();
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
        View view = inflater.inflate(R.layout.fragment_storage_temp, container, false);
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
        StorageTempDialog dialog = new StorageTempDialog();
        dialog.setListener(new StorageTempDialog.OnDataSelectedListener() {
            @Override
            public void onDataSelected(String data) {
                float temp = Float.parseFloat(data);
                mSelected = (int) (temp * 2);
                if (mSelected == 0) {
                    tvTempOnlyTips.setText(R.string.temp_only_tips_0);
                } else {
                    tvTempOnlyTips.setText(getString(R.string.temp_only_tips_1, data));
                }
                tvStorageTempOnly.setText(data);
                activity.setSelectedTemp(mSelected);
            }
        });
        dialog.setSelected(mSelected);
        dialog.show(activity.getSupportFragmentManager());
    }

    private int mSelected;

    public void setTempData(int data) {
        mSelected = data / 5;
        String tempStr = MokoUtils.getDecimalFormat("0.0").format(mSelected * 0.5);
        tvStorageTempOnly.setText(tempStr);
        if (mSelected == 0) {
            tvTempOnlyTips.setText(R.string.temp_only_tips_0);
        } else {
            tvTempOnlyTips.setText(getString(R.string.temp_only_tips_1, tempStr));
        }
    }
}
