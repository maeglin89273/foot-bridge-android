package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import android.content.Context;

/**
 * Created by maeglin89273 on 7/10/15.
 */
public class DoorBLESource extends BLESource {
    public static final String DEVICE_NAME = "DoorBLE";

    public DoorBLESource(Context context) {
        super(context, DEVICE_NAME);
    }

    @Override
    protected BLECallback newCallback() {
        return new DoorBLECallback(this.consumer, this.statusListener);
    }
}
