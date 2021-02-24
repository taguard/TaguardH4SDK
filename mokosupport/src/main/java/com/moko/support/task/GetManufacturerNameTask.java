package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

public class GetManufacturerNameTask extends OrderTask {

    public byte[] data;

    public GetManufacturerNameTask() {
        super(OrderCHAR.CHAR_MANUFACTURER_NAME, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
