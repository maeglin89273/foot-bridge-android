package edu.ntust.dmlab.footbridge.app;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;


public class MainActivity extends Activity implements SensorEventListener {
    private static final int PORT = 3070;

    private static final float S2REAL_VOL = 0.02f;

    private Handler dataHandler;
    private Handler uiHandler;

    private SensorManager sensorMgr;
    private Sensor gyroSensor;

    private Button connectBtn;
    private EditText ipTxt;
    private SocketThread socketThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                dataHandler = null;
                connectBtn.setText("Connect");
                connectBtn.setEnabled(true);
            }
        };

        ipTxt = (EditText) this.findViewById(R.id.ipTxt);
        connectBtn = (Button) this.findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketThread = new SocketThread(ipTxt.getText().toString());
                socketThread.start();
                dataHandler = socketThread.getDataHandler();
                connectBtn.setText("Connected");
                connectBtn.setEnabled(false);
            }
        });

        initSensor();
    }

    private void initSensor() {
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        sensorMgr.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);
        super.onResume();
    }

    @Override
    protected void onStop() {
        sensorMgr.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (socketThread != null) {
            socketThread.quitSocket();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (dataHandler == null) {
            return;
        }

        float[] gyroValCopy = new float[3];

        for (int i = 0; i < gyroValCopy.length; i++) {
            gyroValCopy[i] = event.values[i] * S2REAL_VOL;
        }

        Message msg = Message.obtain();
        msg.obj = gyroValCopy;
        dataHandler.sendMessage(msg);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class SocketThread extends HandlerThread {
        private final String address;
        private Handler tranferHandler;
        private DatagramSocket socket;

        public SocketThread(String address) {
            super("socket_thread");
            this.address = address;
        }

        @Override
        protected void onLooperPrepared() {
            connectSocket();
        }

        public Handler getDataHandler() {
            if (this.tranferHandler == null) {
                this.tranferHandler = new TransferHandler(this.getLooper());
            }
            return this.tranferHandler;
        }

        private void connectSocket() {
            try {
                socket = new DatagramSocket();
                socket.connect(new InetSocketAddress(this.address, PORT));
            } catch (SocketException e) {
                e.printStackTrace();
                Log.d("server", "cannot connect to server");
                quitSocket();
            }

        }
        public void quitSocket() {
            this.socket.close();
            this.quitSafely();
            uiHandler.sendEmptyMessage(0);
        }

        private class TransferHandler extends Handler {
            public TransferHandler(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                if (socket.isClosed()) {
                    return;
                }
                float[] gyroValue = (float[]) msg.obj;
                sendToServer(toJsonString(gyroValue));


            }

            private void sendToServer(String jsonString) {
                byte[] data = jsonString.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("server", "sending error");
                    quitSocket();
                }
            }


            private String toJsonString(float[] gyroValue) {
                JSONObject json = new JSONObject();
                try {
                    json.put("gx", gyroValue[0]);
                    json.put("gy", gyroValue[1]);
                    json.put("gz", gyroValue[2]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return json.toString();
            }
        }

    }


}
