package com.moko.beaconxplus.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.moko.beaconxplus.utils.Utils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoConnStateCallback;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.LockStateTask;
import com.moko.support.task.NotifyConfigTask;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.task.UnLockTask;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;


/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconxplus.service.MokoService
 */
public class MokoService extends Service implements MokoConnStateCallback, MokoOrderTaskCallback {

    @Override
    public void onConnectSuccess() {
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCOVER_SUCCESS);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public void onDisConnected() {
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public void onOrderResult(OrderTaskResponse response) {
        Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_RESULT));
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderTimeout(OrderTaskResponse response) {
        Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_TIMEOUT));
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        sendBroadcast(intent);
    }

    @Override
    public void onOrderFinish() {
        sendBroadcast(new Intent(MokoConstants.ACTION_ORDER_FINISH));
    }

    @Override
    public void onCreate() {
        LogModule.v("创建MokoService...onCreate");
        mHandler = new ServiceHandler(this);
        super.onCreate();
    }

    public void connectBluetoothDevice(String address) {
        MokoSupport.getInstance().connDevice(this, address, this);
    }

    /**
     * @Date 2017/5/23
     * @Author wenzheng.liu
     * @Description 断开手环
     */
    public void disConnectBle() {
        MokoSupport.getInstance().disConnectBle();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogModule.v("启动MokoService...onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        LogModule.v("绑定MokoService...onBind");
        return mBinder;
    }

    @Override
    public void onLowMemory() {
        LogModule.v("内存吃紧，销毁MokoService...onLowMemory");
        disConnectBle();
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogModule.v("解绑MokoService...onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LogModule.v("销毁MokoService...onDestroy");
        disConnectBle();
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public MokoService getService() {
            return MokoService.this;
        }
    }

    public ServiceHandler mHandler;

    public class ServiceHandler extends BaseMessageHandler<MokoService> {

        public ServiceHandler(MokoService service) {
            super(service);
        }

        @Override
        protected void handleMessage(MokoService service, Message msg) {
        }
    }

    /**
     * @Description 打开配置通知set config notify
     */
    public OrderTask setConfigNotify() {
        NotifyConfigTask notifyConfigTask = new NotifyConfigTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifyConfigTask;
    }

    /**
     * @Description 获取设备锁状态get lock state
     */
    public OrderTask getLockState() {
        LockStateTask lockStateTask = new LockStateTask(this, OrderTask.RESPONSE_TYPE_READ);
        return lockStateTask;
    }

    /**
     * @Description 设置设备锁状态set lock state
     */
    public OrderTask setLockState(String newPassword) {
        if (passwordBytes != null) {
            LogModule.i("旧密码：" + MokoUtils.bytesToHexString(passwordBytes));
            byte[] bt1 = newPassword.getBytes();
            byte[] bt2 = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            byte[] newPasswordBytes = new byte[bt1.length + bt2.length];
            System.arraycopy(bt1, 0, newPasswordBytes, 0, bt1.length);
            System.arraycopy(bt2, 0, newPasswordBytes, bt1.length, bt2.length);
            LogModule.i("新密码：" + MokoUtils.bytesToHexString(newPasswordBytes));
            // 用旧密码加密新密码
            byte[] newPasswordEncryptBytes = Utils.encrypt(newPasswordBytes, passwordBytes);
            if (newPasswordEncryptBytes != null) {
                LockStateTask lockStateTask = new LockStateTask(this, OrderTask.RESPONSE_TYPE_WRITE);
                byte[] unLockBytes = new byte[newPasswordEncryptBytes.length + 1];
                unLockBytes[0] = 0;
                System.arraycopy(newPasswordEncryptBytes, 0, unLockBytes, 1, newPasswordEncryptBytes.length);
                lockStateTask.setData(unLockBytes);
                return lockStateTask;
            }
        }
        return null;
    }

    /**
     * @Description 获取解锁加密内容get unlock
     */
    public OrderTask getUnLock() {
        UnLockTask unLockTask = new UnLockTask(this, OrderTask.RESPONSE_TYPE_READ);
        return unLockTask;
    }

    private byte[] passwordBytes;

    /**
     * @Description 解锁set unlock
     */
    public OrderTask setUnLock(String password, byte[] value) {
        byte[] bt1 = password.getBytes();
        passwordBytes = new byte[16];
        for (int i = 0; i < passwordBytes.length; i++) {
            if (i < bt1.length) {
                passwordBytes[i] = bt1[i];
            } else {
                passwordBytes[i] = (byte) 0xff;
            }
        }
        LogModule.i("密码：" + MokoUtils.bytesToHexString(passwordBytes));
        byte[] unLockBytes = Utils.encrypt(value, passwordBytes);
        if (unLockBytes != null) {
            UnLockTask unLockTask = new UnLockTask(this, OrderTask.RESPONSE_TYPE_WRITE);
            unLockTask.setData(unLockBytes);
            return unLockTask;
        }
        return null;
    }
}
