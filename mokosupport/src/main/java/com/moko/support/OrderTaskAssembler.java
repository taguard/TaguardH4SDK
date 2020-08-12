package com.moko.support;

import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.SlotEnum;
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
import com.moko.support.task.ProductDateTask;
import com.moko.support.task.RadioTxPowerTask;
import com.moko.support.task.ResetDeviceTask;
import com.moko.support.task.SlotTypeTask;
import com.moko.support.task.SoftwareVersionTask;
import com.moko.support.task.UnLockTask;
import com.moko.support.task.WriteConfigTask;
import com.moko.support.utils.MokoUtils;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class OrderTaskAssembler {

    /**
     * @Description 获取设备锁状态get lock state
     */
    public static OrderTask getLockState() {
        LockStateTask lockStateTask = new LockStateTask(OrderTask.RESPONSE_TYPE_READ);
        return lockStateTask;
    }

    /**
     * @Description 设置设备锁方式
     */
    public static OrderTask setLockStateDirected(boolean isDirected) {
        LockStateTask lockStateTask = new LockStateTask(OrderTask.RESPONSE_TYPE_WRITE);
        lockStateTask.setData(isDirected ? new byte[]{0x02} : new byte[]{0x01});
        return lockStateTask;
    }

    /**
     * @Description 设置设备锁状态set lock state
     */
    public static OrderTask setLockState(String newPassword) {
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
            byte[] newPasswordEncryptBytes = encrypt(newPasswordBytes, passwordBytes);
            if (newPasswordEncryptBytes != null) {
                LockStateTask lockStateTask = new LockStateTask(OrderTask.RESPONSE_TYPE_WRITE);
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
    public static OrderTask getUnLock() {
        UnLockTask unLockTask = new UnLockTask(OrderTask.RESPONSE_TYPE_READ);
        return unLockTask;
    }

    private static byte[] passwordBytes;

    /**
     * @Description 解锁set unlock
     */
    public static OrderTask setUnLock(String password, byte[] value) {
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
        byte[] unLockBytes = encrypt(value, passwordBytes);
        if (unLockBytes != null) {
            UnLockTask unLockTask = new UnLockTask(OrderTask.RESPONSE_TYPE_WRITE);
            unLockTask.setData(unLockBytes);
            return unLockTask;
        }
        return null;
    }

    /**
     * @Date 2018/1/22
     * @Author wenzheng.liu
     * @Description 加密
     */
    public static byte[] encrypt(byte[] value, byte[] password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");// 转换为AES专用密钥
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化为加密模式的密码器
            byte[] result = cipher.doFinal(value);// 加密
            byte[] data = Arrays.copyOf(result, 16);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @Description 获取通道类型
     */
    public static OrderTask getSlotType() {
        SlotTypeTask slotTypeTask = new SlotTypeTask();
        return slotTypeTask;
    }


    /**
     * @Description 获取设备类型
     */
    public static OrderTask getDeviceType() {
        DeviceTypeTask deviceTypeTask = new DeviceTypeTask();
        return deviceTypeTask;
    }

    /**
     * @Description 获取3轴参数
     */
    public static OrderTask getAxisParams() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.GET_AXIX_PARAMS);
        return writeConfigTask;
    }

    /**
     * @Description 设置3轴参数
     */
    public static OrderTask setAxisParams(int rate, int scale, int sensitivity) {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setAxisParams(rate, scale, sensitivity);
        return writeConfigTask;
    }

    /**
     * @Description 获取温湿度采样率
     */
    public static OrderTask getTHPeriod() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.GET_TH_PERIOD);
        return writeConfigTask;
    }

    /**
     * @Description 设置温湿度采样率
     */
    public static OrderTask setTHPeriod(int period) {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setTHPriod(period);
        return writeConfigTask;
    }

    /**
     * @Description 获取存储条件
     */
    public static OrderTask getStorageCondition() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.GET_STORAGE_CONDITION);
        return writeConfigTask;
    }

    /**
     * @Description 设置存储条件
     */
    public static OrderTask setStorageCondition(int storageType, String storageData) {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setStorageCondition(storageType, storageData);
        return writeConfigTask;
    }

    /**
     * @Description 获取设备时间
     */
    public static OrderTask getDeviceTime() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.GET_DEVICE_TIME);
        return writeConfigTask;
    }

    /**
     * @Description 设置设备时间
     */
    public static OrderTask setDeviceTime(int year, int month, int day, int hour, int minute, int second) {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setDeviceTime(year, month, day, hour, minute, second);
        return writeConfigTask;
    }

    public static OrderTask setTHEmpty() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.SET_TH_EMPTY);
        return writeConfigTask;
    }

    /**
     * @Description 获取设备MAC
     */
    public static OrderTask getDeviceMac() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.GET_DEVICE_MAC);
        return writeConfigTask;
    }

    /**
     * @Description 获取连接状态
     */
    public static OrderTask getConnectable() {
        ConnectableTask connectableTask = new ConnectableTask(OrderTask.RESPONSE_TYPE_READ);
        return connectableTask;
    }

    /**
     * @Description 设置连接状态
     */
    public static OrderTask setConnectable(boolean isConnectable) {
        ConnectableTask connectableTask = new ConnectableTask(OrderTask.RESPONSE_TYPE_WRITE);
        connectableTask.setData(isConnectable ? MokoUtils.toByteArray(1, 1) : MokoUtils.toByteArray(0, 1));
        return connectableTask;
    }

    /**
     * @Description 获取制造商
     */
    public static OrderTask getManufacturer() {
        ManufacturerTask manufacturerTask = new ManufacturerTask();
        return manufacturerTask;
    }

    /**
     * @Description 获取设备型号
     */
    public static OrderTask getDeviceModel() {
        DeviceModelTask deviceModelTask = new DeviceModelTask();
        return deviceModelTask;
    }

    /**
     * @Description 获取生产日期
     */
    public static OrderTask getProductDate() {
        ProductDateTask productDateTask = new ProductDateTask();
        return productDateTask;
    }

    /**
     * @Description 获取硬件版本
     */
    public static OrderTask getHardwareVersion() {
        HardwareVersionTask hardwareVersionTask = new HardwareVersionTask();
        return hardwareVersionTask;
    }

    /**
     * @Description 获取固件版本
     */
    public static OrderTask getFirmwareVersion() {
        FirmwareVersionTask firmwareVersionTask = new FirmwareVersionTask();
        return firmwareVersionTask;
    }

    /**
     * @Description 获取软件版本
     */
    public static OrderTask getSoftwareVersion() {
        SoftwareVersionTask softwareVersionTask = new SoftwareVersionTask();
        return softwareVersionTask;
    }

    /**
     * @Description 获取电池电量
     */
    public static OrderTask getBattery() {
        BatteryTask batteryTask = new BatteryTask();
        return batteryTask;
    }

    /**
     * @Description 切换通道
     */
    public static OrderTask setSlot(SlotEnum slot) {
        AdvSlotTask advSlotTask = new AdvSlotTask(OrderTask.RESPONSE_TYPE_WRITE);
        advSlotTask.setData(slot);
        return advSlotTask;
    }

    /**
     * @Description 获取通道数据
     */
    public static OrderTask getSlotData() {
        AdvSlotDataTask advSlotDataTask = new AdvSlotDataTask(OrderTask.RESPONSE_TYPE_READ);
        return advSlotDataTask;
    }

    /**
     * @Description 设置通道信息
     */
    public static OrderTask setSlotData(byte[] data) {
        AdvSlotDataTask advSlotDataTask = new AdvSlotDataTask(OrderTask.RESPONSE_TYPE_WRITE);
        advSlotDataTask.setData(data);
        return advSlotDataTask;
    }

    /**
     * @Description 获取信号强度
     */
    public static OrderTask getRadioTxPower() {
        RadioTxPowerTask radioTxPowerTask = new RadioTxPowerTask(OrderTask.RESPONSE_TYPE_READ);
        return radioTxPowerTask;
    }

    /**
     * @Description 设置信号强度
     */
    public static OrderTask setRadioTxPower(byte[] data) {
        RadioTxPowerTask radioTxPowerTask = new RadioTxPowerTask(OrderTask.RESPONSE_TYPE_WRITE);
        radioTxPowerTask.setData(data);
        return radioTxPowerTask;
    }

    /**
     * @Description 获取广播间隔
     */
    public static OrderTask getAdvInterval() {
        AdvIntervalTask advIntervalTask = new AdvIntervalTask(OrderTask.RESPONSE_TYPE_READ);
        return advIntervalTask;
    }

    /**
     * @Description 设置广播间隔
     */
    public static OrderTask setAdvInterval(byte[] data) {
        AdvIntervalTask advIntervalTask = new AdvIntervalTask(OrderTask.RESPONSE_TYPE_WRITE);
        advIntervalTask.setData(data);
        return advIntervalTask;
    }

    /**
     * @Description 设置广播强度
     */
    public static OrderTask setAdvTxPower(byte[] data) {
        AdvTxPowerTask advTxPowerTask = new AdvTxPowerTask(OrderTask.RESPONSE_TYPE_WRITE);
        advTxPowerTask.setData(data);
        return advTxPowerTask;
    }

    /**
     * @Description 设置广播强度
     */
    public static OrderTask getAdvTxPower() {
        AdvTxPowerTask advTxPowerTask = new AdvTxPowerTask(OrderTask.RESPONSE_TYPE_READ);
        return advTxPowerTask;
    }

    /**
     * @Description 获取iBeaconUUID
     */
    public static OrderTask getiBeaconUUID() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.GET_IBEACON_UUID);
        return writeConfigTask;
    }

    /**
     * @Description 设置iBeaconUUID
     */
    public static OrderTask setiBeaconUUID(String uuidHex) {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setiBeaconUUID(uuidHex);
        return writeConfigTask;
    }

    /**
     * @Description 获取iBeaconInfo
     */
    public static OrderTask getiBeaconInfo() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.GET_IBEACON_INFO);
        return writeConfigTask;
    }

    /**
     * @Description 关机
     */
    public static OrderTask setClose() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.SET_CLOSE);
        return writeConfigTask;
    }

    /**
     * @Description 恢复出厂设置
     */
    public static OrderTask resetDevice() {
        ResetDeviceTask resetDeviceTask = new ResetDeviceTask();
        return resetDeviceTask;
    }

    public static OrderTask getTrigger() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setData(ConfigKeyEnum.GET_TRIGGER_DATA);
        return writeConfigTask;
    }

    public static OrderTask setTriggerClose() {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setTriggerData();
        return writeConfigTask;
    }

    public static OrderTask setTHTrigger(int triggerType, boolean isAbove, int params, boolean isStart) {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setTriggerData(triggerType, isAbove, params, isStart);
        return writeConfigTask;
    }

    public static OrderTask setTappedMovesTrigger(int triggerType, int params, boolean isStart) {
        WriteConfigTask writeConfigTask = new WriteConfigTask();
        writeConfigTask.setTriggerData(triggerType, params, isStart);
        return writeConfigTask;
    }

    ///////////////////////////////////////////////////////////////////////////
    // NOTIFY
    ///////////////////////////////////////////////////////////////////////////
    public static OrderTask setConfigNotify() {
        NotifyConfigTask notifyConfigTask = new NotifyConfigTask(OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifyConfigTask;
    }

    public static OrderTask setAxisNotifyOpen() {
        NotifyAxisTask notifyAxisTask = new NotifyAxisTask(OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifyAxisTask;
    }

    public static OrderTask setAxisNotifyClose() {
        NotifyAxisTask notifyAxisTask = new NotifyAxisTask(OrderTask.RESPONSE_TYPE_DISABLE_NOTIFY);
        return notifyAxisTask;
    }

    public static OrderTask setTHNotifyOpen() {
        NotifyHTTask notifyHTTask = new NotifyHTTask(OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifyHTTask;
    }

    public static OrderTask setTHNotifyClose() {
        NotifyHTTask notifyHTTask = new NotifyHTTask(OrderTask.RESPONSE_TYPE_DISABLE_NOTIFY);
        return notifyHTTask;
    }

    public static OrderTask setSavedTHNotifyOpen() {
        NotifySavedHTTask notifySavedHTTask = new NotifySavedHTTask(OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifySavedHTTask;
    }

    public static OrderTask setSavedTHNotifyClose() {
        NotifySavedHTTask notifySavedHTTask = new NotifySavedHTTask(OrderTask.RESPONSE_TYPE_DISABLE_NOTIFY);
        return notifySavedHTTask;
    }
}
