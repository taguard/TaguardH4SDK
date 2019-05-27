package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.ProductDateTask
 */
public class ProductDateTask extends OrderTask {

    public byte[] data;

    public ProductDateTask(MokoOrderTaskCallback callback) {
        super(OrderType.productDate, OrderEnum.PRODUCT_DATE, callback, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
