package com.moko.beaconxpro.dialog;

import android.text.TextUtils;
import android.view.View;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.view.WheelView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorageHumidityDialog extends MokoBaseDialog {

    @BindView(R.id.wv_storage_humidity)
    WheelView wvStorageHumidity;
    private int selected;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_storage_humidity;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);

        wvStorageHumidity.setData(createData());
        wvStorageHumidity.setDefault(selected);
    }

    private ArrayList<String> createData() {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            data.add(i + "");
        }
        return data;
    }


    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                dismiss();
                if (TextUtils.isEmpty(wvStorageHumidity.getSelectedText())) {
                    return;
                }
                if (wvStorageHumidity.getSelected() < 0) {
                    return;
                }
                if (listener != null) {
                    listener.onDataSelected(wvStorageHumidity.getSelectedText());
                }
                break;
        }
    }

    private OnDataSelectedListener listener;

    public void setListener(OnDataSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnDataSelectedListener {
        void onDataSelected(String data);
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
}
