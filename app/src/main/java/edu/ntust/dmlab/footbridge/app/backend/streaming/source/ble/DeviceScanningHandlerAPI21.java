package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.*;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maeglin89273 on 6/30/15.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class DeviceScanningHandlerAPI21 extends DeviceScanningHandler {
    private final BluetoothLeScanner bleScanner;

    private EyewearScanCallback deviceScanCallback;

    DeviceScanningHandlerAPI21(BluetoothAdapter btAdptr, String deviceName, ScanResultCallback callback) {
        super(deviceName, callback);

        this.bleScanner = btAdptr.getBluetoothLeScanner();
    }

    @Override
    public void startScan() {
        if (this.isScanning()) {
            return;
        }

        List<ScanFilter> filterList = new ArrayList<ScanFilter>();
        filterList.add(getFilter());
        this.deviceScanCallback = new EyewearScanCallback();
//        this.bleScanner.startScan(filterList, getSettings(), this.deviceScanCallback);
        this.bleScanner.startScan(this.deviceScanCallback);
        super.startScan();

    }

    @Override
    public void stopScan() {
        if (!this.isScanning()) {
            return;
        }
        this.bleScanner.stopScan(this.deviceScanCallback);
        super.stopScan();
    }

    private ScanFilter getFilter() {
        ScanFilter.Builder builder = new ScanFilter.Builder();

        builder.setDeviceName(this.deviceName);

        //builder.setServiceUuid(new ParcelUuid(DeviceConfig.UUIDs.SERVICE));

        return builder.build();
    }

    private ScanSettings getSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setReportDelay(0);
        return builder.build();
    }

    private class EyewearScanCallback extends ScanCallback {
        @Override
        public void onScanFailed(int errorCode) {
            stopScan();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getName().equals(deviceName)) {
                stopScan();
                callback.deviceFound(result.getDevice());
            }
        }
    }
}
