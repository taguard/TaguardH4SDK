package com.moko.beaconxplus;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.beaconxplus.entity.BeaconXAxis;
import com.moko.beaconxplus.entity.BeaconXInfo;
import com.moko.beaconxplus.entity.BeaconXTH;
import com.moko.beaconxplus.entity.BeaconXTLM;
import com.moko.beaconxplus.entity.BeaconXUID;
import com.moko.beaconxplus.entity.BeaconXURL;
import com.moko.beaconxplus.entity.BeaconXiBeacon;
import com.moko.beaconxplus.utils.BeaconXParser;
import com.moko.support.log.LogModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.ButterKnife;

public class BeaconXListAdapter extends BaseQuickAdapter<BeaconXInfo, BaseViewHolder> {
    public BeaconXListAdapter() {
        super(R.layout.list_item_device);
    }

    @Override
    protected void convert(BaseViewHolder helper, BeaconXInfo item) {
        helper.setText(R.id.tv_name, TextUtils.isEmpty(item.name) ? "N/A" : item.name);
        helper.setText(R.id.tv_mac, "MAC:" + item.mac);
        helper.setText(R.id.tv_rssi, item.rssi + "");
        helper.setText(R.id.tv_conn_state, item.connectState < 0 ? "N/A" : item.connectState == 0 ? "Unconnectable" : "Connectable");
        helper.setText(R.id.tv_lock_state, item.lockState < 0 ? "Lock State:N/A" : String.format("Lock State:%#x", item.lockState));
        helper.setText(R.id.tv_interval_time, item.intervalTime == 0 ? "<->N/A" : String.format("<->%dms", item.intervalTime));
        helper.setText(R.id.tv_battery, item.battery < 0 ? "N/A" : String.format("%d%%", item.battery));
        helper.addOnClickListener(R.id.tv_connect);
        LinearLayout parent = helper.getView(R.id.ll_data);
        parent.removeAllViews();
        ArrayList<BeaconXInfo.ValidData> validDatas = new ArrayList<>(item.validDataHashMap.values());
        Collections.sort(validDatas, new Comparator<BeaconXInfo.ValidData>() {
            @Override
            public int compare(BeaconXInfo.ValidData lhs, BeaconXInfo.ValidData rhs) {
                if (lhs.type > rhs.type) {
                    return 1;
                } else if (lhs.type < rhs.type) {
                    return -1;
                }
                return 0;
            }
        });

        for (BeaconXInfo.ValidData validData : validDatas) {
            LogModule.i(validData.toString());
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_UID) {
                parent.addView(createUIDView(BeaconXParser.getUID(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_URL) {
                parent.addView(createURLView(BeaconXParser.getURL(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM) {
                parent.addView(createTLMView(BeaconXParser.getTLM(validData.data)));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON) {
                BeaconXiBeacon beaconXiBeacon = BeaconXParser.getiBeacon(item.rssi, validData.data);
                beaconXiBeacon.txPower = validData.txPower + "";
                parent.addView(createiBeaconView(beaconXiBeacon));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_TH) {
                BeaconXTH beaconXTH = BeaconXParser.getTH(validData.data);
                beaconXTH.txPower = validData.txPower + "";
                parent.addView(createTHView(beaconXTH));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS) {
                BeaconXAxis beaconXAxis = BeaconXParser.getAxis(validData.data);
                beaconXAxis.txPower = validData.txPower + "";
                parent.addView(createAxisView(beaconXAxis));
            }
            if (validData.type == BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO) {
                helper.setText(R.id.tv_tx_power, String.format("Tx Power:%ddBm", validData.txPower));
                int battery = item.battery;
                if (battery >= 0 && battery <= 20) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_5);
                }
                if (battery > 20 && battery <= 40) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_4);
                }
                if (battery > 40 && battery <= 60) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_3);
                }
                if (battery > 60 && battery <= 80) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_2);
                }
                if (battery > 80 && battery <= 100) {
                    helper.setImageResource(R.id.iv_battery, R.drawable.battery_1);
                }
            }
        }
    }

    private View createUIDView(BeaconXUID uid) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_uid, null);
        TextView tvTxPower = ButterKnife.findById(view, R.id.tv_tx_power);
        TextView tvNameSpace = ButterKnife.findById(view, R.id.tv_namespace);
        TextView tvInstanceId = ButterKnife.findById(view, R.id.tv_instance_id);
        tvTxPower.setText(String.format("RSSI@0m:%sdBm", uid.rangingData));
        tvNameSpace.setText("0x" + uid.namespace.toUpperCase());
        tvInstanceId.setText("0x" + uid.instanceId.toUpperCase());
        return view;
    }

    private View createURLView(final BeaconXURL url) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_url, null);
        TextView tvTxPower = ButterKnife.findById(view, R.id.tv_tx_power);
        TextView tvUrl = ButterKnife.findById(view, R.id.tv_url);
        tvTxPower.setText(String.format("RSSI@0m:%sdBm", url.rangingData));
        tvUrl.setText(url.url);
        tvUrl.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvUrl.getPaint().setAntiAlias(true);//抗锯齿
        tvUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(url.url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mContext.startActivity(intent);
            }
        });
        return view;
    }

    private View createTLMView(BeaconXTLM tlm) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_tlm, null);
        TextView tv_vbatt = ButterKnife.findById(view, R.id.tv_vbatt);
        TextView tv_temp = ButterKnife.findById(view, R.id.tv_temp);
        TextView tv_adv_cnt = ButterKnife.findById(view, R.id.tv_adv_cnt);
        TextView tv_sec_cnt = ButterKnife.findById(view, R.id.tv_sec_cnt);
        tv_vbatt.setText(String.format("%smV", tlm.vbatt));
        tv_temp.setText(tlm.temp);
        tv_adv_cnt.setText(tlm.adv_cnt);
        tv_sec_cnt.setText(tlm.sec_cnt);
        return view;
    }

    private View createiBeaconView(BeaconXiBeacon iBeacon) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_ibeacon, null);
        TextView tv_tx_power = ButterKnife.findById(view, R.id.tv_tx_power);
        TextView tv_rssi_1m = ButterKnife.findById(view, R.id.tv_rssi_1m);
        TextView tv_uuid = ButterKnife.findById(view, R.id.tv_uuid);
        TextView tv_major = ButterKnife.findById(view, R.id.tv_major);
        TextView tv_minor = ButterKnife.findById(view, R.id.tv_minor);
        TextView tv_distance = ButterKnife.findById(view, R.id.tv_distance);

        tv_rssi_1m.setText(String.format("RSSI@1m:%sdBm", iBeacon.rangingData));
        tv_tx_power.setText(String.format("%sdBm", iBeacon.txPower));
        tv_distance.setText(iBeacon.distanceDesc);
        tv_uuid.setText(iBeacon.uuid.toLowerCase());
        tv_major.setText(iBeacon.major);
        tv_minor.setText(iBeacon.minor);
        return view;
    }

    private View createTHView(BeaconXTH beaconXTH) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_th, null);
        TextView tv_tx_power = ButterKnife.findById(view, R.id.tv_tx_power);
        TextView tv_rssi_0m = ButterKnife.findById(view, R.id.tv_rssi_0m);
        TextView tv_temperature = ButterKnife.findById(view, R.id.tv_temperature);
        TextView tv_humidity = ButterKnife.findById(view, R.id.tv_humidity);

        tv_rssi_0m.setText(String.format("RSSI@0m:%sdBm", beaconXTH.rangingData));
        tv_tx_power.setText(String.format("%sdBm", beaconXTH.txPower));
        tv_temperature.setText(String.format("%s°C", beaconXTH.temperature));
        tv_humidity.setText(String.format("%s%%", beaconXTH.humidity));
        return view;
    }

    private View createAxisView(BeaconXAxis beaconXAxis) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.beaconx_axis, null);
        TextView tv_tx_power = ButterKnife.findById(view, R.id.tv_tx_power);
        TextView tv_rssi_0m = ButterKnife.findById(view, R.id.tv_rssi_0m);
        TextView tv_data_rate = ButterKnife.findById(view, R.id.tv_data_rate);
        TextView tv_scale = ButterKnife.findById(view, R.id.tv_scale);
        TextView tv_sampled_data = ButterKnife.findById(view, R.id.tv_sampled_data);

        tv_rssi_0m.setText(String.format("RSSI@0m:%sdBm", beaconXAxis.rangingData));
        tv_tx_power.setText(String.format("%sdBm", beaconXAxis.txPower));
        tv_data_rate.setText(beaconXAxis.dataRate);
        tv_scale.setText(beaconXAxis.scale);
        tv_sampled_data.setText(String.format("X:0x%s Y:0x%s Z:0x%s", beaconXAxis.x_data.toUpperCase(), beaconXAxis.y_data.toUpperCase(), beaconXAxis.z_data.toUpperCase()));
        return view;
    }
}
