package edu.ntust.dmlab.footbridge.app.backend;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import edu.ntust.dmlab.footbridge.app.backend.streaming.TransferBridge;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble.BLECallback;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble.DeviceConfig;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble.DeviceScanningHandler;

public class Backend extends TransferBridge {

    public static final class Actions {
        public static final String BLE_CONNECTION_STATE_CHANGED =
                "pvoDevice.backend.action.BLE_CONNECTION_STATE_CHANGED";
        public static final String INTERNET_CONNECTION_STATE_CHANGED =
                "pvoDevice.backend.action.INTERNET_CONNECTION_STATE_CHANGED";
        public static final String DATA_UPDATE =
                "pvoDevice.backend.action.DATA_UPDATE";

    }

    public static final String KEY_CONTENT =
            "pvoDevice.backend.datakey.CONTENT";


    // it will be move back to AsyncProcessor, because these are raw data, it's not friendly to UIs
    public final static class IntentContentFormat {
        public static final String BLE_CONNECTED = "ble_connected";
        public static final String BLE_DISCONNECTED = "ble_disconnected";
    }

    
    private static final String LOG_TAG = "PVO Backend";


    private BackendThread thread;
    private AsyncProcessor processor;

    private DeviceScanningHandler DeviceScanner;
    private BluetoothGatt bleGatt;

    private boolean deviceFound = false;

    private Context context;

    public Backend(Context activity) {
        this.context = activity;

        this.thread = new BackendThread();
        this.thread.start();

        this.processor = this.thread.getProcessor();
        initBLE();

    }

    public void connectSocket(String address) {
        Message msg = Message.obtain();
        msg.what = AsyncProcessor.MessageWhat.CONNECT_TO_SOCKET;
        msg.obj = address;
        this.processor.sendMessage(msg);
    }

    public void connectPubNub() {
        Message msg = Message.obtain();
        msg.what = AsyncProcessor.MessageWhat.CONNECT_TO_PUBNUB;
        this.processor.sendMessage(msg);
    }

    public boolean isDeviceFound() {return this.deviceFound;}
    public boolean isInitSuccessful() {
        return this.isDeviceFound(); // and more init related flags...
    }

    private void initBLE() {
        BluetoothManager btMgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdptr = btMgr.getAdapter();

        scanDevice(btAdptr);
    }

    private void scanDevice(BluetoothAdapter btAdptr) {
        DeviceScanningHandler.ScanResultCallback callback = new DeviceScanningHandler.ScanResultCallback() {
            @Override
            public void deviceFound(BluetoothDevice device) {
                connectDevice(device);
            }

            @Override
            public void deviceNotFound() {
                deviceFound = false;

                Message msg = Message.obtain();
                msg.what = AsyncProcessor.MessageWhat.BLE_CONNECTION;
                msg.obj = IntentContentFormat.BLE_DISCONNECTED;
                Backend.this.processor.sendMessage(msg);
            }
        };

        this.DeviceScanner = DeviceScanningHandler.newInstance(btAdptr, callback);
        this.DeviceScanner.startScan();
    }

    private void connectDevice(BluetoothDevice Device) {
        deviceFound = true;
        bleGatt = Device.connectGatt(this.context.getApplicationContext(), true, new BLECallback(Backend.this.processor));
    }

    private BluetoothDevice initDevice(BluetoothAdapter btAdptr) {

        if (btAdptr == null) {
            Log.d(LOG_TAG, "bluetooth is not enabled");
            return null;
        }
        BluetoothDevice Device = searchPairedDevice(btAdptr);
        if (Device == null) {
            Log.d(LOG_TAG, "no paired pvo Device was found");
            return null;
        }

        return Device;
    }


    private static BluetoothDevice searchPairedDevice(BluetoothAdapter btAdptr) {

        for (BluetoothDevice device : btAdptr.getBondedDevices()) {
            if (device.getName().equals(DeviceConfig.DEVICE_NAME)) {
                return device;
            }
        }

        return null;
    }

    public void quit() {
        if (this.isInitSuccessful()) {
            this.bleGatt.disconnect();
            this.bleGatt.close();
            this.processor.quitSocket();
            thread.quit();
        }
    }

    private class BackendThread extends HandlerThread {
        private AsyncProcessor processor;
        private boolean isHandlerInit = false;

        BackendThread() {
            super("BackendThread", Process.THREAD_PRIORITY_FOREGROUND);
        }

        AsyncProcessor getProcessor() {
            if (!isHandlerInit) {
                this.processor = new AsyncProcessor(Backend.this.context, this.getLooper());
            }
            return this.processor;
        }
    }


}
