package edu.ntust.dmlab.footbridge.app.backend.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.UUID;

/**
 * Created by maeglin89273 on 6/30/15.
 */
class DeviceScanningHandlerAPI18 extends DeviceScanningHandler {
    private final BluetoothAdapter btAdptr;
    private EyewearScanCallback eyewearScanCallback;

    DeviceScanningHandlerAPI18(BluetoothAdapter btAdptr, ScanResultCallback callback) {
        super(callback);
        this.btAdptr = btAdptr;
    }

    @Override
    public void startScan() {
        this.eyewearScanCallback = new EyewearScanCallback();
        this.btAdptr.startLeScan(this.eyewearScanCallback);

        super.startScan();
    }

    @Override
    public void stopScan() {
        if (this.isScanning()) {
            this.btAdptr.stopLeScan(this.eyewearScanCallback);
        }
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


