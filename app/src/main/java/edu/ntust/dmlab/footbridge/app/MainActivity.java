package edu.ntust.dmlab.footbridge.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import edu.ntust.dmlab.footbridge.app.backend.Backend;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;


public class MainActivity extends Activity {


    private Button connectBtn;
    private EditText ipTxt;
    private TextView bleStatusLbl;
    private Backend backend;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipTxt = (EditText) this.findViewById(R.id.ipTxt);
        bleStatusLbl = (TextView)this.findViewById(R.id.bleStatusLbl);
        connectBtn = (Button) this.findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backend.connectSocket(ipTxt.getText().toString());
            }
        });

        initReceiver();
        backend = new Backend(this);
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Backend.Actions.BLE_CONNECTION_STATE_CHANGED);
        filter.addAction(Backend.Actions.INTERNET_CONNECTION_STATE_CHANGED);

        this.receiver = new EventReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.receiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        backend.quit();
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

    private class EventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Backend.Actions.BLE_CONNECTION_STATE_CHANGED:
                    bleStatusLbl.setText(intent.getStringExtra(Backend.KEY_CONTENT));
                    break;

                case Backend.Actions.INTERNET_CONNECTION_STATE_CHANGED:
                    if (intent.getStringExtra(Backend.KEY_CONTENT).equals("Disconnected")) {
                        connectBtn.setEnabled(true);
                        connectBtn.setText("Connect");
                    } else {
                        connectBtn.setEnabled(false);
                        connectBtn.setText("Connected");
                    }


            }
        }
    }


}
