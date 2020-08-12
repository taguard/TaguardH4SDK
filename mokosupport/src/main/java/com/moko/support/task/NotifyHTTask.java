package com.moko.support.task;

import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2019/6/14
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.NotifyHTTask
 */
public class NotifyHTTask extends OrderTask {

    public byte[] data;

    public NotifyHTTask(int responseType) {
        super(OrderType.htData, OrderEnum.HT_NOTIFY, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
