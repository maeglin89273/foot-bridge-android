package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

/**
 * Created by maeglin89273 on 6/30/15.
 */

public abstract class DeviceScanningHandler {
    private static final long SCAN_PERIOD = 30000;

    protected final String deviceName;
    protected final ScanResultCallback callback;
    private final Handler cancellingTimer;
    private final ScanCanceler canceler;

    private volatile boolean scanning;

    public DeviceScanningHandler(String deviceName, ScanResultCallback callback) {
        this.deviceName = deviceName;
        this.callback = callback;
        this.canceler = new ScanCanceler();
        this.cancellingTimer = new Handler();
    }

    public static DeviceScanningHandler newInstance(BluetoothAdapter btAdptr, String deviceName, ScanResultCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new DeviceScanningHandlerAPI21(btAdptr, deviceName, callback);
        }
        return new DeviceScanningHandlerAPI18(btAdptr, deviceName, callback);

    }

    public void startScan() {
        this.cancellingTimer.postDelayed(this.canceler, SCAN_PERIOD);
        this.scanning = true;
        Log.d("ble scan", "start");
    }

    public boolean isScanning() {
        return scanning;
    }

    public void stopScan() {
        this.cancellingTimer.removeCallbacks(this.canceler);
        this.scanning = false;

    }

    private class ScanCanceler implements Runnable {

        @Override
        public void run() {
            Log.d("ble scan", "stop");
            if (isScanning()) {
                stopScan();
                callback.deviceNotFound();
            }
        }
    }

    public interface ScanResultCallback {
        public abstract void deviceFound(BluetoothDevice eyewear);
        public abstract void deviceNotFound();
    }
}
