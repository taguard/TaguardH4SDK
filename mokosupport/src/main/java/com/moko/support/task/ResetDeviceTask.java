package com.moko.support.task;

import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.ResetDeviceTask
 */
public class ResetDeviceTask extends OrderTask {

    public byte[] data = {(byte) 0x0b};

    public ResetDeviceTask() {
        super(OrderType.resetDevice, OrderEnum.RESET_DEVICE, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
