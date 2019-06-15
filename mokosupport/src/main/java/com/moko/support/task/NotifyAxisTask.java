package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2019/6/14
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.NotifyAxisTask
 */
public class NotifyAxisTask extends OrderTask {

    public byte[] data;

    public NotifyAxisTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.axisData, OrderEnum.AXIS_NOTIFY, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
