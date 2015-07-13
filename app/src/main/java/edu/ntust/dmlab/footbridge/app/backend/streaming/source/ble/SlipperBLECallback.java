package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.util.Log;
import edu.ntust.dmlab.footbridge.app.backend.streaming.EndpointStatusListener;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.StreamSource;

import java.util.*;

/**
 * Created by maeglin89273 on 7/13/15.
 */
public class SlipperBLECallback extends BLECallback {
    public static final UUID SERVICE = UUID.fromString("0000ffa0-0000-1000-8000-00805f9b34fb");
    public static final UUID[] CHARACTERISTIC_ACC = {UUID.fromString("0000ffa2-0000-1000-8000-00805f9b34fb"),
                                                UUID.fromString("0000ffa3-0000-1000-8000-00805f9b34fb"),
                                                UUID.fromString("0000ffa4-0000-1000-8000-00805f9b34fb")};

    public static final UUID NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final Map<UUID, Integer> ACC_UUID_INDEX_MAP = new HashMap<>();

    private Set<UUID> checkingBag;
    private float[] accValueArray;
    private float[] gyroValueArray;
    private int notificationRegCounter;
    static {
        for (int i = 0; i < CHARACTERISTIC_ACC.length; i++) {
            UUID uuid = CHARACTERISTIC_ACC[i];
            ACC_UUID_INDEX_MAP.put(uuid, i);
        }
    }

    public SlipperBLECallback(StreamSource.AsyncConsumer consumer, EndpointStatusListener statusListener) {
        super(consumer, statusListener);
        this.checkingBag = new HashSet<>();
        this.accValueArray = new float[3];
        this.gyroValueArray = new float[3];
        this.notificationRegCounter = 0;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();
        if (!this.checkingBag.contains(uuid)) {
            byte[] rawData = characteristic.getValue();

            float value = toValue(rawData[0], rawData[1]);

            this.checkingBag.add(uuid);
            this.accValueArray[ACC_UUID_INDEX_MAP.get(uuid)] = value;

            if (this.checkingBag.size() == 3) {
                this.consumer.consumeData(this.buildBundle());
                this.prepareNextRound();
            }
        }

    }

    private static float toValue(byte hsb, byte lsb) {
        return ((((int)(hsb & 0xFF) << 8) + (int)(lsb & 0xFF)) - 32768) / 16384.0f;
    }

    private Bundle buildBundle() {
        Bundle data = new Bundle();
        data.putFloatArray("acc", this.accValueArray);
        data.putFloatArray("gyro", this.gyroValueArray);
        return data;
    }

    private void prepareNextRound() {
        this.accValueArray = new float[3];
        this.checkingBag.clear();
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d("ble slipper", "discover service");


        this.setNotification(gatt, this.nextCharacteristic(gatt));
    }

    private void setNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor notificationDescriptor = characteristic.getDescriptor(NOTIFICATION_DESCRIPTOR);
        notificationDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        gatt.writeDescriptor(notificationDescriptor);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (this.notificationRegCounter < 3) {
            this.setNotification(gatt, this.nextCharacteristic(gatt));
        }
    }

    private BluetoothGattCharacteristic nextCharacteristic(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(SERVICE);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_ACC[notificationRegCounter]);
        notificationRegCounter++;
        return characteristic;

    }
}
