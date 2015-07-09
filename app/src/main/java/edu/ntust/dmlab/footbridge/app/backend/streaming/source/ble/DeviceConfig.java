package edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble;

import java.util.UUID;

/**
 * Created by ${USER} on 6/17/15.
 */
public final class DeviceConfig {
    public static final String DEVICE_NAME = "DoorBLE";

    public final static class UUIDs {
        public static final UUID SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
        public static final UUID CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
        public static final UUID NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }
}
