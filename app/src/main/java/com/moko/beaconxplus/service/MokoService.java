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
import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.SlotEnum;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.AdvIntervalTask;
import com.moko.support.task.AdvSlotDataTask;
import com.moko.support.task.AdvSlotTask;
import com.moko.support.task.AdvTxPowerTask;
import com.moko.support.task.BatteryTask;
import com.moko.support.task.ConnectableTask;
import com.moko.support.task.DeviceModelTask;
import com.moko.support.task.DeviceTypeTask;
import com.moko.support.task.FirmwareVersionTask;
import com.moko.support.task.HardwareVersionTask;
import com.moko.support.task.LockStateTask;
import com.moko.support.task.ManufacturerTask;
import com.moko.support.task.NotifyAxisTask;
import com.moko.support.task.NotifyConfigTask;
import com.moko.support.task.NotifyHTTask;
import com.moko.support.task.NotifySavedHTTask;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.task.ProductDateTask;
import com.moko.support.task.RadioTxPowerTask;
import com.moko.support.task.ResetDeviceTask;
import com.moko.support.task.SlotTypeTask;
import com.moko.support.task.SoftwareVersionTask;
import com.moko.support.task.UnLockTask;
import com.moko.support.task.WriteConfigTask;
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
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderFinish() {
        sendOrderedBroadcast(new Intent(MokoConstants.ACTION_ORDER_FINISH), null);
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

    public OrderTask setAxisNotifyOpen() {
        NotifyAxisTask notifyAxisTask = new NotifyAxisTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifyAxisTask;
    }

    public OrderTask setAxisNotifyClose() {
        NotifyAxisTask notifyAxisTask = new NotifyAxisTask(this, OrderTask.RESPONSE_TYPE_DISABLE_NOTIFY);
        return notifyAxisTask;
    }

    public OrderTask setTHNotifyOpen() {
        NotifyHTTask notifyHTTask = new NotifyHTTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifyHTTask;
    }

    public OrderTask setTHNotifyClose() {
        NotifyHTTask notifyHTTask = new NotifyHTTask(this, OrderTask.RESPONSE_TYPE_DISABLE_NOTIFY);
        return notifyHTTask;
    }

    public OrderTask setSavedTHNotifyOpen() {
        NotifySavedHTTask notifySavedHTTask = new NotifySavedHTTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifySavedHTTask;
    }

    public OrderTask setSavedTHNotifyClose() {
        NotifySavedHTTask notifySavedHTTask = new NotifySavedHTTask(this, OrderTask.RESPONSE_TYPE_DISABLE_NOTIFY);
        return notifySavedHTTask;
    }


    /**
     * @Description 获取设备锁状态get lock state
     */
    public OrderTask getLockState() {
        LockStateTask lockStateTask = new LockStateTask(this, OrderTask.RESPONSE_TYPE_READ);
        return lockStateTask;
    }

    /**
     * @Description 设置设备锁方式
     */
    public OrderTask setLockStateDirected(boolean isDirected) {
        LockStateTask lockStateTask = new LockStateTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        lockStateTask.setData(isDirected ? new byte[]{0x02} : new byte[]{0x01});
        return lockStateTask;
    }

    /**
     * @Description 设置设备锁状态set lock state
     */
    public OrderTask setLockState(String newPassword) {
        if (passwordBytes != null) {
            LogModule.i("旧密码：" + MokoUtils.bytesToHexString(passwordBytes));
            byte[] bt1 = newPassword.getBytes();
            byte[] newPasswordBytes = new byte[16];
            for (int i = 0; i < newPasswordBytes.length; i++) {
                if (i < bt1.length) {
                    newPasswordBytes[i] = bt1[i];
                } else {
                    newPasswordBytes[i] = (byte) 0xff;
                }
            }
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

    /**
     * @Description 获取通道类型
     */
    public OrderTask getSlotType() {
        SlotTypeTask slotTypeTask = new SlotTypeTask(this);
        return slotTypeTask;
    }


    /**
     * @Description 获取设备类型
     */
    public OrderTask getDeviceType() {
        DeviceTypeTask deviceTypeTask = new DeviceTypeTask(this);
        return deviceTypeTask;
    }

    /**
     * @Description 获取3轴参数
     */
    public OrderTask getAxisParams() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.GET_AXIX_PARAMS);
        return writeConfigTask;
    }

    /**
     * @Description 设置3轴参数
     */
    public OrderTask setAxisParams(int rate, int scale, int sensitivity) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setAxisParams(rate, scale, sensitivity);
        return writeConfigTask;
    }

    /**
     * @Description 获取温湿度采样率
     */
    public OrderTask getTHPeriod() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.GET_TH_PERIOD);
        return writeConfigTask;
    }

    /**
     * @Description 设置温湿度采样率
     */
    public OrderTask setTHPeriod(int period) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setTHPriod(period);
        return writeConfigTask;
    }

    /**
     * @Description 获取存储条件
     */
    public OrderTask getStorageCondition() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.GET_STORAGE_CONDITION);
        return writeConfigTask;
    }

    /**
     * @Description 设置存储条件
     */
    public OrderTask setStorageCondition(int storageType, String storageData) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setStorageCondition(storageType, storageData);
        return writeConfigTask;
    }

    /**
     * @Description 获取设备时间
     */
    public OrderTask getDeviceTime() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.GET_DEVICE_TIME);
        return writeConfigTask;
    }

    /**
     * @Description 设置设备时间
     */
    public OrderTask setDeviceTime(int year, int month, int day, int hour, int minute, int second) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setDeviceTime(year, month, day, hour, minute, second);
        return writeConfigTask;
    }

    public OrderTask setTHEmpty() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.SET_TH_EMPTY);
        return writeConfigTask;
    }

    /**
     * @Description 获取设备MAC
     */
    public OrderTask getDeviceMac() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.GET_DEVICE_MAC);
        return writeConfigTask;
    }

    /**
     * @Description 获取连接状态
     */
    public OrderTask getConnectable() {
        ConnectableTask connectableTask = new ConnectableTask(this, OrderTask.RESPONSE_TYPE_READ);
        return connectableTask;
    }

    /**
     * @Description 设置连接状态
     */
    public OrderTask setConnectable(boolean isConnectable) {
        ConnectableTask connectableTask = new ConnectableTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        connectableTask.setData(isConnectable ? MokoUtils.toByteArray(1, 1) : MokoUtils.toByteArray(0, 1));
        return connectableTask;
    }

    /**
     * @Description 获取制造商
     */
    public OrderTask getManufacturer() {
        ManufacturerTask manufacturerTask = new ManufacturerTask(this);
        return manufacturerTask;
    }

    /**
     * @Description 获取设备型号
     */
    public OrderTask getDeviceModel() {
        DeviceModelTask deviceModelTask = new DeviceModelTask(this);
        return deviceModelTask;
    }

    /**
     * @Description 获取生产日期
     */
    public OrderTask getProductDate() {
        ProductDateTask productDateTask = new ProductDateTask(this);
        return productDateTask;
    }

    /**
     * @Description 获取硬件版本
     */
    public OrderTask getHardwareVersion() {
        HardwareVersionTask hardwareVersionTask = new HardwareVersionTask(this);
        return hardwareVersionTask;
    }

    /**
     * @Description 获取固件版本
     */
    public OrderTask getFirmwareVersion() {
        FirmwareVersionTask firmwareVersionTask = new FirmwareVersionTask(this);
        return firmwareVersionTask;
    }

    /**
     * @Description 获取软件版本
     */
    public OrderTask getSoftwareVersion() {
        SoftwareVersionTask softwareVersionTask = new SoftwareVersionTask(this);
        return softwareVersionTask;
    }

    /**
     * @Description 获取电池电量
     */
    public OrderTask getBattery() {
        BatteryTask batteryTask = new BatteryTask(this);
        return batteryTask;
    }

    /**
     * @Description 切换通道
     */
    public OrderTask setSlot(SlotEnum slot) {
        AdvSlotTask advSlotTask = new AdvSlotTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advSlotTask.setData(slot);
        return advSlotTask;
    }

    /**
     * @Description 获取通道数据
     */
    public OrderTask getSlotData() {
        AdvSlotDataTask advSlotDataTask = new AdvSlotDataTask(this, OrderTask.RESPONSE_TYPE_READ);
        return advSlotDataTask;
    }

    /**
     * @Description 设置通道信息
     */
    public OrderTask setSlotData(byte[] data) {
        AdvSlotDataTask advSlotDataTask = new AdvSlotDataTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advSlotDataTask.setData(data);
        return advSlotDataTask;
    }

    /**
     * @Description 获取信号强度
     */
    public OrderTask getRadioTxPower() {
        RadioTxPowerTask radioTxPowerTask = new RadioTxPowerTask(this, OrderTask.RESPONSE_TYPE_READ);
        return radioTxPowerTask;
    }

    /**
     * @Description 设置信号强度
     */
    public OrderTask setRadioTxPower(byte[] data) {
        RadioTxPowerTask radioTxPowerTask = new RadioTxPowerTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        radioTxPowerTask.setData(data);
        return radioTxPowerTask;
    }

    /**
     * @Description 获取广播间隔
     */
    public OrderTask getAdvInterval() {
        AdvIntervalTask advIntervalTask = new AdvIntervalTask(this, OrderTask.RESPONSE_TYPE_READ);
        return advIntervalTask;
    }

    /**
     * @Description 设置广播间隔
     */
    public OrderTask setAdvInterval(byte[] data) {
        AdvIntervalTask advIntervalTask = new AdvIntervalTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advIntervalTask.setData(data);
        return advIntervalTask;
    }

    /**
     * @Description 设置广播强度
     */
    public OrderTask setAdvTxPower(byte[] data) {
        AdvTxPowerTask advTxPowerTask = new AdvTxPowerTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advTxPowerTask.setData(data);
        return advTxPowerTask;
    }

    /**
     * @Description 设置广播强度
     */
    public OrderTask getAdvTxPower() {
        AdvTxPowerTask advTxPowerTask = new AdvTxPowerTask(this, OrderTask.RESPONSE_TYPE_READ);
        return advTxPowerTask;
    }

    /**
     * @Description 获取iBeaconUUID
     */
    public OrderTask getiBeaconUUID() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.GET_IBEACON_UUID);
        return writeConfigTask;
    }

    /**
     * @Description 设置iBeaconUUID
     */
    public OrderTask setiBeaconUUID(String uuidHex) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setiBeaconUUID(uuidHex);
        return writeConfigTask;
    }

    /**
     * @Description 获取iBeaconInfo
     */
    public OrderTask getiBeaconInfo() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.GET_IBEACON_INFO);
        return writeConfigTask;
    }

    /**
     * @Description 设置iBeaconInfo
     */
    public OrderTask setiBeaconInfo(int major, int minor, int advTxPower) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setiBeaconData(major, minor, advTxPower);
        return writeConfigTask;
    }

    /**
     * @Description 关机
     */
    public OrderTask setClose() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.SET_CLOSE);
        return writeConfigTask;
    }

    /**
     * @Description 恢复出厂设置
     */
    public OrderTask resetDevice() {
        ResetDeviceTask resetDeviceTask = new ResetDeviceTask(this);
        return resetDeviceTask;
    }

    public OrderTask getTrigger() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setData(ConfigKeyEnum.GET_TRIGGER_DATA);
        return writeConfigTask;
    }

    public OrderTask setTriggerClose() {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setTriggerData();
        return writeConfigTask;
    }

    public OrderTask setTHTrigger(int triggerType, boolean isAbove, int params, boolean isStart) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setTriggerData(triggerType, isAbove, params, isStart);
        return writeConfigTask;
    }

    public OrderTask setTappedMovesTrigger(int triggerType, int params, boolean isStart) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setTriggerData(triggerType, params, isStart);
        return writeConfigTask;
    }
}
