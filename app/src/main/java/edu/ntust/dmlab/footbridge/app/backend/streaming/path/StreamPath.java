package edu.ntust.dmlab.footbridge.app.backend.streaming.path;

import edu.ntust.dmlab.footbridge.app.backend.streaming.EndpointStatusListener;
import edu.ntust.dmlab.footbridge.app.backend.streaming.StreamEndpoint;
import org.json.JSONObject;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public abstract class StreamPath extends StreamEndpoint {
    public abstract void transfer(JSONObject jsonData);
}
