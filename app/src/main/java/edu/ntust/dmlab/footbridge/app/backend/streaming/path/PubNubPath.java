package edu.ntust.dmlab.footbridge.app.backend.streaming.path;

import android.util.Log;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import org.json.JSONObject;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public class PubNubPath extends StreamPath {
    private static final String PUBLISH_KEY = "pub-c-7b8779c0-5d71-42e8-9041-b91b538ec2b4";
    private static final String SUBSCRIBE_KEY = "sub-c-57d15590-2530-11e5-b6a9-0619f8945a4f";
    private static final String CHANNEL = "door_channel";

    private final PubNubCallback callback;

    private Pubnub delegate;
    private boolean streamingStarted = false;

    public PubNubPath() {
        this.callback = new PubNubCallback();
    }

    @Override
    protected void uncheckedTransfer(JSONObject jsonData) {
        delegate.publish(CHANNEL, jsonData, this.callback);
    }

    @Override
    public void startStreaming() {
        this.delegate = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY);
        this.delegate.time(this.callback);

    }

    @Override
    public boolean isStreamingStarted() {
        return this.streamingStarted;
    }

    @Override
    public void endStreaming() {
        this.streamingStarted = false;
        if (this.delegate != null) {
            this.delegate.shutdown();
            this.delegate = null;
        }
        this.statusListener.onEndpointDisconnected("PubNub disconnected");
    }

    private class PubNubCallback extends com.pubnub.api.Callback {
        @Override
        public void successCallback(String channel, Object response) {
            Log.d("pubnub success", response.toString());
            PubNubPath.this.streamingStarted = true;
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.d("pubnub", error.toString());
        }
    }
}
