package edu.ntust.dmlab.footbridge.app.backend.streaming.path;

import edu.ntust.dmlab.footbridge.app.backend.streaming.EndpointStatusListener;
import edu.ntust.dmlab.footbridge.app.backend.streaming.StreamEndpoint;
import org.json.JSONObject;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public abstract class StreamPath extends StreamEndpoint {
    public void transfer(JSONObject jsonData) {
        if (this.isStreamingStarted()) {
            uncheckedTransfer(jsonData);
        }
    }
    protected abstract void uncheckedTransfer(JSONObject jsonData);
}
