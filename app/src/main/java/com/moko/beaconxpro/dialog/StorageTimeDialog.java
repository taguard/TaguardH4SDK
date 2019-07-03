package com.moko.beaconxpro.dialog;

import android.text.TextUtils;
import android.view.View;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.view.WheelView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorageTimeDialog extends MokoBaseDialog {

    @Bind(R.id.wv_storage_time)
    WheelView wvStorageTime;
    private int selected;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_storage_time;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);

        wvStorageTime.setData(createData());
        wvStorageTime.setDefault(selected - 1);
    }

    private ArrayList<String> createData() {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 1; i <= 255; i++) {
            data.add(i + "");
        }
        return data;
    }


    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.tv_cancel, R.id.tv_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                dismiss();
                if (TextUtils.isEmpty(wvStorageTime.getSelectedText())) {
                    return;
                }
                if (wvStorageTime.getSelected() < 0) {
                    return;
                }
                if (listener != null) {
                    listener.onDataSelected(wvStorageTime.getSelectedText());
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
