package edu.ntust.dmlab.footbridge.app.backend;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import edu.ntust.dmlab.footbridge.app.backend.streaming.EndpointStatusListener;
import edu.ntust.dmlab.footbridge.app.backend.streaming.TransferBridge;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.StreamSource;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.ble.DeviceScanningHandler;
import org.json.JSONException;
import org.json.JSONObject;

public class Backend extends TransferBridge {

    public static final class Actions {
        public static final String SOURCE_CONNECTION_STATE_CHANGED =
                "pvoDevice.backend.action.SOURCE_CONNECTION_STATE_CHANGED";
        public static final String PATH_CONNECTION_STATE_CHANGED =
                "pvoDevice.backend.action.PATH_CONNECTION_STATE_CHANGED";
        public static final String DATA_UPDATE =
                "pvoDevice.backend.action.DATA_UPDATE";

    }

    public static final String KEY_CONTENT =
            "pvoDevice.backend.datakey.CONTENT";

    private static final String LOG_TAG = "Backend";

    private DeviceScanningHandler DeviceScanner;
    private BluetoothGatt bleGatt;


    private Context context;

    public Backend(Context activity) {
        initCallbacks(new TransferTrigger(),
                new BroadcastingEndpointStatusListener(Actions.SOURCE_CONNECTION_STATE_CHANGED),
                new BroadcastingEndpointStatusListener(Actions.PATH_CONNECTION_STATE_CHANGED));
        this.context = activity;



    }

    private class BroadcastingEndpointStatusListener implements EndpointStatusListener {
        private String action;
        private BroadcastingEndpointStatusListener(String broadcastAction) {
            this.action = broadcastAction;
        }

        @Override
        public void onEndpointConnected(String message) {
            broadcast(message);
        }

        @Override
        public void onEndpointDisconnected(String message) {
            broadcast(message);
        }

        private void broadcast(String msg) {
            sendBroadcast(action, msg);
        }
    }


    private class TransferTrigger implements StreamSource.AsyncConsumer {

        @Override
        public void consumeData(Bundle data) {
            JSONObject jsonData = toJson(data);
            Backend.this.path.transfer(jsonData);
//            sendBroadcast(Actions.DATA_UPDATE, data);
        }
    }

    private void sendBroadcast(String action, String content) {
        Intent intent = new Intent(action);
        intent.putExtra(Backend.KEY_CONTENT, content);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void sendBroadcast(String action, Bundle bundle) {
        Intent intent = new Intent(action);
        intent.putExtra(Backend.KEY_CONTENT, bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private JSONObject toJson(Bundle data) {
        JSONObject json = new JSONObject();
        try {
            json.put("timestamp", data.getLong("timestamp"));

            JSONObject accObj = new JSONObject();
            float[] accVals = data.getFloatArray("acc");
            accObj.put("x", accVals[0]);
            accObj.put("y", accVals[1]);
            accObj.put("z", accVals[2]);

            json.put("acc", accObj);

            JSONObject gyroObj = new JSONObject();
            float[] gyroVals = data.getFloatArray("gyro");
            gyroObj.put("x", gyroVals[0]);
            gyroObj.put("y", gyroVals[1]);
            gyroObj.put("z", gyroVals[2]);

            json.put("gyro", gyroObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
