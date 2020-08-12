package com.moko.support.task;

import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.entity.SlotEnum;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.AdvSlotTask
 */
public class AdvSlotTask extends OrderTask {

    public byte[] data;

    public AdvSlotTask(int responseType) {
        super(OrderType.advSlot, OrderEnum.ADV_SLOT, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(SlotEnum slot) {
        data = new byte[]{(byte) slot.getSlot()};
    }
}
