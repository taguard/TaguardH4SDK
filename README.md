# MokoBeaconXPro Android SDK Instruction DOC（English）

----

## 1. Import project

**1.1 Import "Module mokosupport" to root directory**

**1.2 Edit "settings.gradle" file**

```
include ':app', ':mokosupport'
```

**1.3 Edit "build.gradle" file under the APP project**


	dependencies {
		...
		implementation project(path: ':mokosupport')
	}


----

## 2. How to use

**Initialize sdk at project initialization**

```
MokoSupport.getInstance().init(getApplicationContext());
```

**SDK provides three main functions:**

* Scan the device;
* Connect to the device;
* Send and receive data.

### 2.1 Scan the device

 **Start scanning**

```
MokoSupport.getInstance().startScanDevice(callback);
```

 **End scanning**

```
MokoSupport.getInstance().stopScanDevice();
```
 **Implement the scanning callback interface**

```java
/**
 * @ClassPath com.moko.support.callback.MokoScanDeviceCallback
 */
public interface MokoScanDeviceCallback {
    void onStartScan();

    void onScanDevice(DeviceInfo device);

    void onStopScan();
}
```
* **Analysis `DeviceInfo` ; inferred `BeaconXInfo`**

```
BeaconXInfo beaconXInfo = new BeaconXInfoParseableImpl().parseDeviceInfo(device);
```

Device types can be distinguished by `parseDeviceInfo(DeviceInfo deviceInfo)`.Refer `deviceInfo.scanResult.getScanRecord().getServiceData()` we can get parcelUuid,etc.

```
                if (parcelUuid.toString().startsWith("0000feaa")) {
                    isEddystone = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_UID:
                                if (bytes.length != 20)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_UID;
                                // 00ee0102030405060708090a0102030405060000
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_URL:
                                if (bytes.length < 4 || bytes.length > 20)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_URL;
                                // 100c0141424344454609
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM:
                                if (bytes.length != 14)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_TLM;
                                // 20000d18158000017eb20002e754
                                break;
                        }
                    }
                    values = bytes;
                } else if (parcelUuid.toString().startsWith("0000feab")) {
                    isBeaconXPro = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO:
                                if (bytes.length != 15)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO;
                                battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 3, 5));
                                lockState = bytes[5] & 0xff;
                                connectState = bytes[6] & 0xff;
                                // 40000a0d0d0001ff02030405063001
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON:
                                if (bytes.length != 23)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_IBEACON;
                                // 50ee0c0102030405060708090a0b0c0d0e0f1000010002
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS:
                                if (bytes.length != 12)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_AXIS;
                                // 60f60e010007f600d5002e00
                                break;
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_TH:
                                if (bytes.length != 7)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_TH;
                                // 700b1000fb02f5
                                break;
                        }
                    }
                    values = bytes;
                } else if (parcelUuid.toString().startsWith("0000feac")) {
                    isBeaconXPro = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO:
                                if (bytes.length != 15)
                                    return null;
                                type = BeaconXInfo.VALID_DATA_FRAME_TYPE_INFO;
                                battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 3, 5));
                                lockState = bytes[5] & 0xff;
                                connectState = bytes[6] & 0xff;
                                // 40000a0d0d0001ff02030405063001
                                break;
                        }
                    }
                    values = bytes;
                }

```

### 2.2 Connect to the device


```
MokoSupport.getInstance().connDevice(context, address);
```

When connecting to the device, context, MAC address and callback by EventBus.

```
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
            ...
        }
        if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            ...
        }
    }
```

It uses `EventBus` to notify activity after receiving the status

### 2.3 Send and receive data.

All the request data is encapsulated into **TASK**, and sent to the device in a **QUEUE** way.
SDK gets task status from task callback (`OrderTaskResponse `) after sending tasks successfully.

* **Task**

At present, all the tasks sent from the SDK can be divided into 4 types:

> 1.  READ：Readable
> 2.  WRITE：Writable
> 3.  NOTIFY：Can be listened( Need to enable the notification property of the relevant characteristic values)
> 4.  WRITE_NO_RESPONSE：After enabling the notification property, send data to the device and listen to the data returned by device.
> 5.  RESPONSE_TYPE_DISABLE_NOTIFY close the notification property.

Encapsulated tasks are as follows:

|Task Class|Task Type|Function
|----|----|----
|`NotifyConfigTask`|NOTIFY|Enable notification property
|`NotifyAxisTask`|NOTIFY/RESPONSE_TYPE_DISABLE_NOTIFY|Enable/Disable 3-axis notification property
|`NotifyHTTask`|NOTIFY/RESPONSE_TYPE_DISABLE_NOTIFY|Enable/Disable temp&humidity notification property
|`NotifySavedHTTask`|NOTIFY/RESPONSE_TYPE_DISABLE_NOTIFY|Enable/Disable temp&humidity saved notification property


Custom device information
--

|Task Class|Task Type|Function
|----|----|----
|`LockStateTask`|READ|Get Lock State; **0x00** stands for LOCKED and needs to be unlocked; **0x01** stands for UNLOCKED; **0x02** stands for Uulocked and automatic relock disabled.
|`LockStateTask`|WRITE|Set new password; AES encryption of 16 byte new password with 16 byte old password ( To prevent the new password from being broadcast in the clear, the client shall AES-128-ECB encrypt the new password with the existing password. The BeaconX shall perform the decryption with its existing password and set that value as the new password. ).
|`UnLockTask`|READ|Get a 128-bit challenge token. This token is for one-time use and cannot be replayed.To securely unlock the BeaconX, the host must write a one-time use unlock_token into the characteristic. To create the unlock_token, it first reads the randomly generated 16-byte challenge and generates it using AES-128-ECB encrypt.
|`UnLockTask`|WRITE|Unlock，If the result of this calculation matches the unlock_token written to the characteristic, the beacon is unlocked. Sets the LOCK STATE to 0x01 on success.
|`ManufacturerTask`|READ|Get manufacturer.
|`DeviceModelTask`|READ|Get product model.
|`ProductDateTask`|READ|Get production date.
|`HardwareVersionTask`|READ|Get hardware version.
|`FirmwareVersionTask`|READ|Get firmware version.
|`SoftwareVersionTask`|READ|Get software version.
|`BatteryTask`|READ|Get battery capacity.
|`ConnectableTask`|READ|Get connectable.
|`ConnectableTask`|WRITE|Set connectable.
|`RadioTxPowerTask`|READ|Get current SLOT Tx Power.
|`RadioTxPowerTask`|WRITE|Set current SLOT Tx Power(1bytes). Please take `TxPowerEnum` as reference
|`AdvIntervalTask`|READ|Get current SLOT broadcasting Interval.
|`AdvIntervalTask`|WRITE|Set current SLOT broadcasting Interval(2bytes). Range：100ms- 5000ms. Example：0x03E8=1000 (Unit:ms).
|`AdvTxPowerTask`|WRITE|Set currnent SLOT advTxPower(RSSI@0m, 1bytes). Range：-127dBm—0dBm. Example：0xED=-19dBm.
|`ResetDeviceTask`|WRITE|Reset
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write `ConfigKeyEnum.GET_DEVICE_MAC`，get MAC address.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write`ConfigKeyEnum.SET_CLOSE`，close the device.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write `GET_AXIX_PARAMS`，get 3-axis params.|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setAxisParams(int rate, int scale, int sensitivity)`，set 3-axis params.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write `GET_TH_PERIOD`，get temp&humidity period.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setTHPriod(int period)`，set temp&humidity period.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write `SET_TH_EMPTY`，clear temp&humidity data.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write `GET_DEVICE_TIME`，get device time.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setDeviceTime(int year, int month, int day, int hour, int minute, int second)`，sync time.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write `GET_STORAGE_CONDITION`，get storage condition.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setStorageCondition(int storageType, String storageData)`，set torage condition.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Write `GET_TRIGGER_DATA`，get trigger data.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setTriggerData(int triggerType, boolean isAbove, int params, boolean isStart)`，set trigger data.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setTriggerData()`，set trigger data.
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setTriggerData(int triggerType, int params, boolean isStart)`，set trigger data.

iBeacon information
--

|Task Class|Task Type|Function
|----|----|----
|`WriteConfigTask`|WRITE_NO_RESPONSE|Call`setiBeaconUUID(String uuidHex)`，set iBeacon UUID(16bytes).
|`AdvSlotDataTask`|READ|After switching the SLOT, get the current SLOT data
|`AdvSlotDataTask`|WRITE|After switching the SLOT, set the current SLOT data

	iBeacon data composition：SLOT type(0x50) + UUID(16bytes) + Major(2bytes) + Minor(2bytes)

Eddystone information（URL,UID,TLM）
---

|Task Class|Task Type|Function
|----|----|----
|`AdvSlotTask`|WRITE|Switch SLOT. Please take `SlotEnum` as reference
|`AdvSlotDataTask`|READ|After switching the SLOT, get the current SLOT data and parse the returned data according to the SLOT type.
|`AdvSlotDataTask`|WRITE|After switching the SLOT, set the current SLOT data

	UID data composition：SLOT type(0x00) + Namespace(10bytes) + Instance ID(6bytes)
	URL data composition：SLOT type(0x10) + URLScheme(1bytes) + URLContent(Max 17bytes)
	TLM data composition：SLOT type(0x20)
	NO_DATA data composition：0
	
Device Info
---

Task Class|Task Type|Function
|----|----|----
|`AdvSlotDataTask`|READ|After switching the SLOT, get the current SLOT data
|`AdvSlotDataTask`|WRITE|After switching the SLOT, set the current SLOT data

	Device data composition：SLOT type(0x40) + DeviceName(20bytes)
	
3-axis
---

Task Class|Task Type|Function
|----|----|----
|`AdvSlotDataTask`|READ|After switching the SLOT, get the current SLOT data
|`AdvSlotDataTask`|WRITE|After switching the SLOT, set the current SLOT data

	Axis data composition：SLOT type(0x60)

Temp & Humidity
---

Task Class|Task Type|Function
|----|----|----
|`AdvSlotDataTask`|READ|After switching the SLOT, get the current SLOT data
|`AdvSlotDataTask`|WRITE|After switching the SLOT, set the current SLOT data
	
	TH data composition：SLOT type(0x70)

* **Create tasks**

The task callback (`MokoOrderTaskCallback`) and task type need to be passed when creating a task. Some tasks also need corresponding parameters to be passed.

Examples of creating tasks are as follows:

```
    /**
     * @Description   get LOCK STATE
     */
    public OrderTask getLockState() {
        LockStateTask lockStateTask = new LockStateTask(this, OrderTask.RESPONSE_TYPE_READ);
        return lockStateTask;
    }
	...
	/**
     * @Description set temp&humidity period
     */
    public OrderTask setTHPeriod(int period) {
        WriteConfigTask writeConfigTask = new WriteConfigTask(this);
        writeConfigTask.setTHPriod(period);
        return writeConfigTask;
    }
	...
    /**
     * @Description   Switch SLOT
     */
    public OrderTask setSlot(SlotEnum slot) {
        AdvSlotTask advSlotTask = new AdvSlotTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        advSlotTask.setData(slot);
        return advSlotTask;
    }
    }
```

* **Send tasks**

```
MokoSupport.getInstance().sendOrder(OrderTask... orderTasks);
```

The task can be one or more.

* **Task callback**

```java
	@Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
        }
        if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
        }
        if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
        }
    }
   
```

`ACTION_ORDER_RESULT `

	After the task is sent to the device, the data returned by the device can be obtained by using the `onOrderResult` function, and you can determine witch class the task is according to the `response.orderType` function. The `response.responseValue` is the returned data.

`ACTION_ORDER_TIMEOUT `

	Every task has a default timeout of 3 seconds to prevent the device from failing to return data due to a fault and the fail will cause other tasks in the queue can not execute normally. After the timeout, the `onOrderTimeout` will be called back. You can determine witch class the task is according to the `response.orderType` function and then the next task continues.

`ACTION_ORDER_FINISH `

	When the task in the queue is empty, `onOrderFinish` will be called back.

* **Listening task**

When there is data returned from the device, the data will be sent in the form of broadcast, and the action of receiving broadcast is `MokoConstants.ACTION_CURRENT_DATA`.

```
String action = intent.getAction();
...
if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
    OrderTaskResponse response = event.getResponse();
    OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
    int responseType = response.responseType;
    byte[] value = response.responseValue;
    ...
}
```

Get `OrderTaskResponse` from the `OrderTaskResponseEvent`, and the corresponding **key** value is `response.responseValue`.

## 3. Special instructions

> 1. AndroidManifest.xml of SDK has declared to access SD card and get Bluetooth permissions.
> 2. The SDK comes with logging, and if you want to view the log in the SD card, please to use "LogModule". The log path is : root directory of SD card/mokoBeaconXPro/mokoBeaconXPro.txt. It only records the log of the day and the day before.















