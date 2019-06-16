package com.moko.support.entity;

import java.io.Serializable;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 命令枚举
 * @ClassPath com.fitpolo.support.entity.OrderEnum
 */
public enum OrderEnum implements Serializable {
    OPEN_NOTIFY("打开设备通知", 0),
    AXIS_NOTIFY("三轴数据通知", 0),
    HT_NOTIFY("温湿度数据通知", 0),
    LOCK_STATE("读取锁状态", 0),
    UNLOCK("解锁", 0),
    ADV_INTERVAL("广播间隔", 0),
    ADV_TX_POWER("广播强度", 0),
    ADV_SLOT("通道类型", 0),
    ADV_SLOT_DATA("通道数据", 0),
    RADIO_TX_POWER("发射功率", 0),
    BATTERY("电池电量", 0),
    DEVICE_MODE("设备型号", 0),
    FIRMWARE_VERSION("固件版本", 0),
    HARDWARE_VERSION("硬件版本", 0),
    SOFTWARE_VERSION("软件版本", 0),
    MANUFACTURER("厂商信息", 0),
    PRODUCT_DATE("出厂日期", 0),
    WRITE_CONFIG("设置参数", 0),
    DEVICE_TYPE("设备类型", 0),
    RESET_DEVICE("重置设备", 0),
    SLOT_TYPE("通道类型", 0),
    CONNECTABLE("连接状态", 0),
    ;


    private String orderName;
    private int orderHeader;

    OrderEnum(String orderName, int orderHeader) {
        this.orderName = orderName;
        this.orderHeader = orderHeader;
    }

    public int getOrderHeader() {
        return orderHeader;
    }

    public String getOrderName() {
        return orderName;
    }
}
