package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Created by maeglin89273 on 6/30/15.
 */
class DeviceScanningHandlerAPI18 extends DeviceScanningHandler {
    private final BluetoothAdapter btAdptr;
    private EyewearScanCallback eyewearScanCallback;

    DeviceScanningHandlerAPI18(BluetoothAdapter btAdptr, String deviceName, ScanResultCallback callback) {
        super(deviceName, callback);
        this.btAdptr = btAdptr;
    }

    @Override
    public void startScan() {
        if (this.isScanning()) {
            return;
        }
        this.eyewearScanCallback = new EyewearScanCallback();
        this.btAdptr.startLeScan(this.eyewearScanCallback);

        super.startScan();

    }

    @Override
    public void stopScan() {
        if (!this.isScanning()) {
            return;
        }
        this.btAdptr.stopLeScan(this.eyewearScanCallback);
        super.stopScan();

    }



    private class EyewearScanCallback implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getName().equals(deviceName)) {
                stopScan();
                callback.deviceFound(device);
            }
        }
    }
}


