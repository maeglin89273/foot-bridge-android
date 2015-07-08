package edu.ntust.dmlab.footbridge.app.backend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by ${USER} on 6/17/15.
 */

/*
this is the controller class
 */
public class AsyncProcessor extends Handler {
    private static final int PORT = 3070;

    private static final class PubNubConfig {
        private static final String PUBLISH_KEY = "pub-c-7b8779c0-5d71-42e8-9041-b91b538ec2b4";
        private static final String SUBSCRIBE_KEY = "sub-c-57d15590-2530-11e5-b6a9-0619f8945a4f";
        private static final String CHANNEL = "door_channel";
    }

    private static final String LOG_TAG = "PVO AsyncProcessor";

    public static final class MessageWhat {
        public static final int BLE_CONNECTION = 1;
        public static final int DATA_UPDATE = 1 << 1;
        public static final int CONNECT_TO_SOCKET = 1 << 2;
        public static final int CONNECT_TO_PUBNUB = 1 << 3;
    }

    private enum UploadMode {
        UDP_SOCKET, PUBNUB, INACTIVE;

    }

    private Context context;
    private DatagramSocket socket;
    private Pubnub pubnub;
    private UploadMode uMode;
    AsyncProcessor(Context context, Looper looper) {
        super(looper);
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {
            case MessageWhat.CONNECT_TO_PUBNUB:
                connectPubNub();
                break;

            case MessageWhat.CONNECT_TO_SOCKET:
                connectSocket((String)msg.obj);
                sendBroadcast(Backend.Actions.INTERNET_CONNECTION_STATE_CHANGED, isSocketConnected()? "Connected": "Disconnected");
                break;

            case MessageWhat.BLE_CONNECTION:
                sendBroadcast(Backend.Actions.BLE_CONNECTION_STATE_CHANGED, (String)msg.obj);
                break;

            case MessageWhat.DATA_UPDATE:
                uploadData(msg.getData());
                sendBroadcast(Backend.Actions.DATA_UPDATE, msg.getData());
                break;
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

    private boolean isSocketConnected() {
        return this.socket != null && this.socket.isConnected() && !this.socket.isClosed();
    }

    private void connectSocket(String address) {
        try {
            socket = new DatagramSocket();
            socket.connect(new InetSocketAddress(address, PORT));
            this.uMode = UploadMode.UDP_SOCKET;
        } catch (SocketException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "cannot connect to server");
            quitSocket();
        }

    }

    private void connectPubNub() {
        this.pubnub = new Pubnub(PubNubConfig.PUBLISH_KEY, PubNubConfig.SUBSCRIBE_KEY);
        this.uMode = UploadMode.PUBNUB;
    }

    public void quitSocket() {
        this.socket.close();
        this.uMode = UploadMode.INACTIVE;
    }

    private boolean sendToSocket(Bundle data) {
        String jsonString = toJson(data).toString();
        byte[] bytesData = jsonString.getBytes();
        DatagramPacket packet = new DatagramPacket(bytesData, bytesData.length);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("server", "sending error");
            quitSocket();
            return false;
        }
        return true;
    }


    private JSONObject toJson(Bundle data) {
        JSONObject json = new JSONObject();
        try {
            json.put("timestamp", data.getLong("timestamp"));

            JSONObject accObj = new JSONObject();
            int[] accVals = data.getIntArray("acc");
            accObj.put("x", accVals[0]);
            accObj.put("y", accVals[1]);
            accObj.put("z", accVals[2]);

            json.put("acc", accObj);


            JSONObject gyroObj = new JSONObject();
            int[] gyroVals = data.getIntArray("gyro");
            gyroObj.put("x", gyroVals[0]);
            gyroObj.put("y", gyroVals[1]);
            gyroObj.put("z", gyroVals[2]);

            json.put("gyro", gyroObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    private void uploadData(Bundle data) {

        switch (this.uMode) {
            case PUBNUB:
                pubnub.publish(PubNubConfig.CHANNEL, toJson(data), new PubNubCallback());
                break;

            case UDP_SOCKET:
                if (isSocketConnected()) {
                    if (!sendToSocket(data)) {
                        sendBroadcast(Backend.Actions.INTERNET_CONNECTION_STATE_CHANGED, "Disconnected");
                    }
                }
                break;
            case INACTIVE:

        }
    }

    private class PubNubCallback extends com.pubnub.api.Callback {
        public void successCallback(String channel, Object response) {
//            System.out.println(response.toString());
        }
        public void errorCallback(String channel, PubnubError error) {
//            System.out.println(error.toString());
            Log.d("pubnub", error.toString());
        }
    }

}
