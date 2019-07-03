package com.moko.support;

public class MokoConstants {
    // 发现状态
    public static final String ACTION_DISCOVER_SUCCESS = "com.moko.beaconxpro.ACTION_DISCOVER_SUCCESS";
    public static final String ACTION_DISCOVER_TIMEOUT = "com.moko.beaconxpro.ACTION_DISCOVER_TIMEOUT";
    // 断开连接
    public static final String ACTION_CONN_STATUS_DISCONNECTED = "com.moko.beaconxpro.ACTION_CONN_STATUS_DISCONNECTED";
    // 命令结果
    public static final String ACTION_ORDER_RESULT = "com.moko.beaconxpro.ACTION_ORDER_RESULT";
    public static final String ACTION_ORDER_TIMEOUT = "com.moko.beaconxpro.ACTION_ORDER_TIMEOUT";
    public static final String ACTION_ORDER_FINISH = "com.moko.beaconxpro.ACTION_ORDER_FINISH";
    public static final String ACTION_CURRENT_DATA = "com.moko.beaconxpro.ACTION_CURRENT_DATA";

    // extra_key
    public static final String EXTRA_KEY_RESPONSE_ORDER_TASK = "EXTRA_KEY_RESPONSE_ORDER_TASK";
    public static final String EXTRA_KEY_CURRENT_DATA_TYPE = "EXTRA_KEY_CURRENT_DATA_TYPE";
    public static final String EXTRA_KEY_RESPONSE_VALUE = "EXTRA_KEY_RESPONSE_VALUE";

}
