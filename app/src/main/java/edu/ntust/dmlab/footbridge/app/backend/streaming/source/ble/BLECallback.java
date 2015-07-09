package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.bluetooth.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.ntust.dmlab.footbridge.app.backend.AsyncProcessor;
import edu.ntust.dmlab.footbridge.app.backend.Backend;

/**
 * Created by maeglin89273 on 6/17/15.
 */
public abstract class BLECallback extends BluetoothGattCallback {

    private Handler processor;
    public BLECallback(Handler processor) {
        this.processor = processor;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Bundle data = decodeData(characteristic);
        Message msg = Message.obtain();
        msg.what = AsyncProcessor.MessageWhat.DATA_UPDATE;
        msg.setData(data);
        processor.sendMessage(msg);
    }

    private Bundle decodeData(BluetoothGattCharacteristic characteristic) {
        Bundle data = new Bundle();

        int offset = 0;
        long timestamp;
        int[] gyroVals = new int[3];
        int[] accVals = new int[3];

        timestamp = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset).longValue();
        offset += 4;
        data.putLong("timestamp", timestamp);



        for (int i = 0; i < 3; i++) {
            gyroVals[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            offset += 2;
        }
        data.putIntArray("gyro", gyroVals);

        for (int i = 0; i < 3; i++) {
            accVals[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            offset += 2;
        }
        data.putIntArray("acc", accVals);



        return data;

        //just transfer byte array, it's more efficient
//        data.putByteArray("data", characteristic.getValue());
//        return data;
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d("ble", "discover service");
        BluetoothGattService pvoService = gatt.getService(DeviceConfig.UUIDs.SERVICE);
        BluetoothGattCharacteristic pvoCharacteristic = pvoService.getCharacteristic(DeviceConfig.UUIDs.CHARACTERISTIC);
        setNotification(gatt, pvoCharacteristic);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

    }

    private void setNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor notificationDescriptor = characteristic.getDescriptor(DeviceConfig.UUIDs.NOTIFICATION_DESCRIPTOR);
        notificationDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(notificationDescriptor);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Message msg = Message.obtain();
        msg.what = AsyncProcessor.MessageWhat.BLE_CONNECTION;
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                msg.obj = Backend.IntentContentFormat.BLE_CONNECTED;
                gatt.discoverServices();

                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                msg.obj = Backend.IntentContentFormat.BLE_DISCONNECTED;
                break;
        }

        processor.sendMessage(msg);

    }
}
