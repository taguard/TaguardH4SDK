package com.moko.beaconxplus.dialog;

import android.text.TextUtils;
import android.view.View;

import com.moko.beaconxplus.R;
import com.moko.beaconxplus.view.WheelView;
import com.moko.support.utils.MokoUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StorageTempDialog extends MokoBaseDialog {

    @Bind(R.id.wv_storage_temp)
    WheelView wvStorageTemp;
    private int selected;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_storage_temp;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);

        wvStorageTemp.setData(createData());
        wvStorageTemp.setDefault(selected);
    }

    private ArrayList<String> createData() {
        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i <= 200; i++) {
            data.add(MokoUtils.getDecimalFormat("#.0").format(i * 0.5));
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
                if (TextUtils.isEmpty(wvStorageTemp.getSelectedText())) {
                    return;
                }
                if (wvStorageTemp.getSelected() < 0) {
                    return;
                }
                if (listener != null) {
                    listener.onDataSelected(wvStorageTemp.getSelectedText());
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
