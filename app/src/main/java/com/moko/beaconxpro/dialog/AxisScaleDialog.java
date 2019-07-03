package com.moko.beaconxpro.dialog;

import android.text.TextUtils;
import android.view.View;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.view.WheelView;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AxisScaleDialog extends MokoBaseDialog {
    @Bind(R.id.wv_scale)
    WheelView wvScale;
    private String[] axisScale;
    private int selected;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_axis_scale;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);

        wvScale.setData(new ArrayList<>(Arrays.asList(getAxisScale())));
        wvScale.setDefault(selected);
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
                if (TextUtils.isEmpty(wvScale.getSelectedText())) {
                    return;
                }
                if (wvScale.getSelected() < 0) {
                    return;
                }
                if (listener != null) {
                    listener.onScaleSelected(wvScale.getSelected());
                }
                break;
        }
    }

    private OnScaleSettingListener listener;

    public void setListener(OnScaleSettingListener listener) {
        this.listener = listener;
    }

    public interface OnScaleSettingListener {
        void onScaleSelected(int scale);
    }

    public String[] getAxisScale() {
        return axisScale;
    }

    public void setAxisScale(String[] axisScale) {
        this.axisScale = axisScale;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }
}
