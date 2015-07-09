package edu.ntust.dmlab.footbridge.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import edu.ntust.dmlab.footbridge.app.backend.Backend;
import edu.ntust.dmlab.footbridge.app.backend.streaming.path.StreamPath;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.StreamSource;


public class MainActivity extends Activity {

    private Spinner sourceSpinner;
    private Spinner pathSpinner;
    private TextView sourceStateLbl;
    private TextView pathStateLbl;
    private EditText ipTxt;
    private Button connectBtn;

    private Backend backend;
    private BroadcastReceiver receiver;
    private boolean ipInputEnabledTmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.sourceSpinner = (Spinner) this.findViewById(R.id.sourceSpinner);
        this.sourceStateLbl = (TextView) this.findViewById(R.id.sourceStateLbl);
        this.pathSpinner = (Spinner) this.findViewById(R.id.pathSpinner);
        this.pathStateLbl = (TextView) this.findViewById(R.id.pathStateLbl);

        this.ipTxt = (EditText) this.findViewById(R.id.ipTxt);

        this.connectBtn = (Button) this.findViewById(R.id.connectBtn);

        this.setupUIs();
        this.initReceiver();
        backend = new Backend(this);
    }
    private void setupUIs() {
        ArrayAdapter<CharSequence> sourceAdapter = ArrayAdapter.createFromResource(this, R.array.source_choices, R.layout.support_simple_spinner_dropdown_item);
        this.sourceSpinner.setAdapter(sourceAdapter);
        this.sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> pathAdapter = ArrayAdapter.createFromResource(this, R.array.path_choices, R.layout.support_simple_spinner_dropdown_item);
        this.pathSpinner.setAdapter(pathAdapter);
        this.pathSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        this.connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectBtn.getText().toString().equals("Connect")) {
                    disableInputs();
                    StreamSource source;
                    StreamPath path;
                    backend.setPath(path);
                    backend.setSource(source);
                    connectBtn.setText("Disconnect");
                } else {
                    enableInputs();
                    backend.breakBridge();
                    connectBtn.setText("Connect");
                }
            }
        });
    }

    private void enableInputs() {
        this.sourceSpinner.setEnabled(true);
        this.pathSpinner.setEnabled(true);
        this.ipTxt.setEnabled(this.ipInputEnabledTmp);
    }

    private void disableInputs() {
        this.sourceSpinner.setEnabled(false);
        this.pathSpinner.setEnabled(false);
        this.ipInputEnabledTmp = this.ipTxt.isEnabled();
        this.ipTxt.setEnabled(false);
    }


    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Backend.Actions.SOURCE_CONNECTION_STATE_CHANGED);
        filter.addAction(Backend.Actions.PATH_CONNECTION_STATE_CHANGED);

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
        backend.breakBridge();
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
                case Backend.Actions.SOURCE_CONNECTION_STATE_CHANGED:
                    sourceStateLbl.setText(intent.getStringExtra(Backend.KEY_CONTENT));
                    break;

                case Backend.Actions.PATH_CONNECTION_STATE_CHANGED:
                    pathStateLbl.setText(intent.getStringExtra(Backend.KEY_CONTENT));

            }
        }
    }


}
