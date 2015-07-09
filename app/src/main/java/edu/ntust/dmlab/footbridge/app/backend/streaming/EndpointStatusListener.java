package edu.ntust.dmlab.footbridge.app.backend.streaming;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public interface EndpointStatusListener {
    public void onEndpointConnected(String message);
    public void onEndpointDisconnected(String message);
}
