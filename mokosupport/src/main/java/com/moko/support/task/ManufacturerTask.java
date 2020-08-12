package com.moko.support.task;

import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.ManufacturerTask
 */
public class ManufacturerTask extends OrderTask {

    public byte[] data;

    public ManufacturerTask() {
        super(OrderType.manufacturer, OrderEnum.MANUFACTURER, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
