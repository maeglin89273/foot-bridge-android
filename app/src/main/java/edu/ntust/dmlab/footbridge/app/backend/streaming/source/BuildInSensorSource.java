package edu.ntust.dmlab.footbridge.app.backend.streaming.source;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public class BuildInSensorSource extends StreamSource {
    private final SensorManager senMgr;
    private final Sensor accSensor;
    private final Sensor gyroSensor;
    private final SensorEventListener sensorListener;

    private boolean streamingStarted = false;

    public BuildInSensorSource(Context context) {
        this.senMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accSensor = senMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.gyroSensor = senMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        this.sensorListener = new SensorListener();
    }

    @Override
    public void startStreaming() {
        this.senMgr.registerListener(this.sensorListener, accSensor, SensorManager.SENSOR_DELAY_GAME);
        this.senMgr.registerListener(this.sensorListener, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
        this.streamingStarted = true;
        this.statusListener.onEndpointConnected("connected");

    }

    @Override
    public boolean isStreamingStarted() {
        return this.streamingStarted;
    }

    @Override
    public void endStreaming() {
        this.streamingStarted = false;
        this.senMgr.unregisterListener(this.sensorListener);
        this.statusListener.onEndpointConnected("disconnected");
    }

    private class SensorListener implements SensorEventListener {
        private Bundle data;

        private SensorListener() {
            this.data = new Bundle();
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.equals(accSensor) && !data.containsKey("acc")) {
                data.putFloatArray("acc", event.values);
            } else if (event.sensor.equals(gyroSensor) && !data.containsKey("gyro")) {
                data.putFloatArray("gyro", event.values);
            }

            if (data.containsKey("acc") && data.containsKey("gyro")) {
                data.putLong("timestamp", System.currentTimeMillis());
                BuildInSensorSource.this.consumer.consumeData(data);
                data = new Bundle();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //pass
        }
    }
}
