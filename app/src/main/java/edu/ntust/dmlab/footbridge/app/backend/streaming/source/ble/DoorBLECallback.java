package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.util.Log;
import edu.ntust.dmlab.footbridge.app.backend.streaming.EndpointStatusListener;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.StreamSource;

import java.util.UUID;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public class DoorBLECallback extends BLECallback {

    public static final UUID SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public DoorBLECallback(StreamSource.AsyncConsumer consumer, EndpointStatusListener statusListener) {
        super(consumer, statusListener);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Bundle data = decodeData(characteristic);
        this.consumer.consumeData(data);
    }

    private Bundle decodeData(BluetoothGattCharacteristic characteristic) {
        this.fix(characteristic.getValue());
        Bundle data = new Bundle();

        int offset = 0;
        long timestamp;
        float[] gyroVals = new float[3];
        float[] accVals = new float[3];

        timestamp = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, offset).longValue();
        offset += 4;
        data.putLong("timestamp", timestamp);



        for (int i = 0; i < 3; i++) {
            gyroVals[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            offset += 2;
        }
        data.putFloatArray("gyro", gyroVals);

        for (int i = 0; i < 3; i++) {
            accVals[i] = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
            offset += 2;
        }
        data.putFloatArray("acc", accVals);



        return data;

        //just transfer byte array, it's more efficient
//        data.putByteArray("data", characteristic.getValue());
//        return data;
    }

    private void fix(byte[] data) {
        int signStoredByteIndex = data.length - 1;
        int bitIndex = 0;
        for (int i = 0; i < 16; i++) {
            data[i] |= (data[signStoredByteIndex] << (7 - bitIndex)) & ((byte)0x80);
            bitIndex++;
            if (bitIndex == 7) {
                signStoredByteIndex--;
                bitIndex = 0;
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d("ble door", "discover service");
        BluetoothGattService pvoService = gatt.getService(SERVICE);
        BluetoothGattCharacteristic pvoCharacteristic = pvoService.getCharacteristic(CHARACTERISTIC);
        setNotification(gatt, pvoCharacteristic);
    }

    private void setNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor notificationDescriptor = characteristic.getDescriptor(NOTIFICATION_DESCRIPTOR);
        notificationDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(notificationDescriptor);
    }
}
