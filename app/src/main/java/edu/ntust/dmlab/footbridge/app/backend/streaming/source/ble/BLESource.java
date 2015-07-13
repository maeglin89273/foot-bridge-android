package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.StreamSource;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public abstract class BLESource extends StreamSource {

    private final BluetoothAdapter btAdptr;
    private final Context context;
    private final String deviceName;

    private BluetoothGatt bleGatt;
    private DeviceScanningHandler deviceScanner;

    private boolean streamingStarted = false;

    public BLESource(Context context, String deviceName) {
        BluetoothManager btMgr = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.btAdptr = btMgr.getAdapter();
        this.context = context;
        this.deviceName = deviceName;
    }

    protected abstract BLECallback newCallback();

    @Override
    public void startStreaming() {
        this.scanDevice();
        this.streamingStarted = true;
    }

    @Override
    public boolean isStreamingStarted() {
        return streamingStarted;
    }

    @Override
    public void endStreaming() {
        if (!this.isStreamingStarted()) {
            return;
        }

        if (this.bleGatt != null) {
            this.bleGatt.disconnect();
            this.bleGatt.close();
        } else {
            this.deviceScanner.stopScan();
        }

        this.streamingStarted = false;
    }

    protected void scanDevice() {
        DeviceScanningHandler.ScanResultCallback callback = new DeviceScanningHandler.ScanResultCallback() {
            @Override
            public void deviceFound(BluetoothDevice device) {
                connectDevice(device);
            }

            @Override
            public void deviceNotFound() {
                BLESource.this.statusListener.onEndpointDisconnected("disconnected");
            }
        };

        this.deviceScanner = DeviceScanningHandler.newInstance(btAdptr, deviceName, callback);
        this.deviceScanner.startScan();
    }

    protected void connectDevice(BluetoothDevice device) {
        this.bleGatt = device.connectGatt(this.context.getApplicationContext(), true, this.newCallback());
    }

}
