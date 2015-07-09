package edu.ntust.dmlab.footbridge.app.backend.streaming.path;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public class UDPPath extends StreamPath {
    private static final int PORT = 3070;

    private final String ip;

    private HandlerThread thread;
    private Handler transferHandler;
    private DatagramSocket delegate;

    public UDPPath(String ip) {
        this.ip = ip;

        this.thread = new HandlerThread("UDP Socket Thread") {
            @Override
            protected void onLooperPrepared() {
                connectSocket();
            }
        };

    }

    private void connectSocket() {
        try {
            delegate = new DatagramSocket();
            delegate.connect(new InetSocketAddress(this.ip, PORT));
            this.statusListener.onEndpointConnected("UDP Path connected");
        } catch (SocketException e) {
            e.printStackTrace();
            Log.d("udp path", "cannot connect to server");
            endStreaming();
        }

    }

    @Override
    protected void uncheckedTransfer(JSONObject jsonData) {
        if (!isStreamingStarted()) {
            return;
        }
        Message msg = Message.obtain();
        msg.obj = jsonData;
        this.transferHandler.sendMessage(msg);
    }

    @Override
    public void startStreaming() {
        this.thread.start();
        this.transferHandler = new TransferHandler(this.thread.getLooper()); // force the looper to be initialized
    }

    @Override
    public boolean isStreamingStarted() {
        return this.delegate != null && this.delegate.isConnected() && !this.delegate.isClosed();
    }

    @Override
    public void endStreaming() {
        if (this.delegate != null) {
            this.delegate.close();
        }
        this.thread.quitSafely();
        this.statusListener.onEndpointDisconnected("UDP Path disconnected");
    }

    private class TransferHandler extends Handler {

        private TransferHandler(Looper socketThreadLooper) {
            super(socketThreadLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            String jsonString = msg.obj.toString();
            byte[] bytesData = jsonString.getBytes();
            DatagramPacket packet = new DatagramPacket(bytesData, bytesData.length);
            try {
                UDPPath.this.delegate.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("udp path", "sending error");
                UDPPath.this.endStreaming();
            }
        }
    }
}
