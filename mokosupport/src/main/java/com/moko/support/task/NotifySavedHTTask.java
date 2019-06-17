package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2019/6/14
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.NotifySavedHTTask
 */
public class NotifySavedHTTask extends OrderTask {

    public byte[] data;

    public NotifySavedHTTask(MokoOrderTaskCallback callback, int responseType) {
        super(OrderType.htSavedData, OrderEnum.SAVED_HT_NOTIFY, callback, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
