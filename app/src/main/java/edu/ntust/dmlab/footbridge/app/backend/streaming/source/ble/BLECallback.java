package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.bluetooth.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.ntust.dmlab.footbridge.app.backend.Backend;
import edu.ntust.dmlab.footbridge.app.backend.streaming.EndpointStatusListener;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.StreamSource;

/**
 * Created by maeglin89273 on 6/17/15.
 */
public abstract class BLECallback extends BluetoothGattCallback {

    protected final EndpointStatusListener statusListener;
    protected final StreamSource.AsyncConsumer consumer;

    public BLECallback(StreamSource.AsyncConsumer consumer, EndpointStatusListener statusListener) {
        this.consumer = consumer;
        this.statusListener = statusListener;
    }



    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                this.statusListener.onEndpointConnected("connected");
                gatt.discoverServices();

                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                this.statusListener.onEndpointConnected("disconnected");
                break;
        }
    }
}
