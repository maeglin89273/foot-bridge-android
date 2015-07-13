package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.content.Context;

/**
 * Created by maeglin89273 on 7/13/15.
 */
public class SlipperBLESource extends BLESource {
    public static final String DEVICE_NAME = "SimpleBLEPeripheral";

    public SlipperBLESource(Context context) {
        super(context, DEVICE_NAME);
    }

    @Override
    protected BLECallback newCallback() {
        return new SlipperBLECallback(this.consumer, this.statusListener);
    }
}
