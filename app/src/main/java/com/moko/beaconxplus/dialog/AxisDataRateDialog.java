package com.moko.beaconxplus.dialog;

import android.text.TextUtils;
import android.view.View;

import com.moko.beaconxplus.R;
import com.moko.beaconxplus.view.WheelView;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AxisDataRateDialog extends MokoBaseDialog {
    @Bind(R.id.wv_data_rate)
    WheelView wvDataRate;
    private String[] axisDataRate;
    private int selected;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_axis_data_rate;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);

        wvDataRate.setData(new ArrayList<>(Arrays.asList(getAxisDataRate())));
        wvDataRate.setDefault(selected);
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
                if (TextUtils.isEmpty(wvDataRate.getSelectedText())) {
                    return;
                }
                if (wvDataRate.getSelected() < 0) {
                    return;
                }
                if (listener != null) {
                    listener.onRateSelected(wvDataRate.getSelected());
                }
                break;
        }
    }

    private OnRateSettingListener listener;

    public void setListener(OnRateSettingListener listener) {
        this.listener = listener;
    }

    public interface OnRateSettingListener {
        void onRateSelected(int rate);
    }

    public String[] getAxisDataRate() {
        return axisDataRate;
    }

    public void setAxisDataRate(String[] axisDataRate) {
        this.axisDataRate = axisDataRate;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
}
