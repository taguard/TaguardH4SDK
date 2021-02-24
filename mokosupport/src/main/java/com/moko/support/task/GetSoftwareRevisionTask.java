package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;


public class GetSoftwareRevisionTask extends OrderTask {

    public byte[] data;

    public GetSoftwareRevisionTask() {
        super(OrderCHAR.CHAR_SOFTWARE_REVISION, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
