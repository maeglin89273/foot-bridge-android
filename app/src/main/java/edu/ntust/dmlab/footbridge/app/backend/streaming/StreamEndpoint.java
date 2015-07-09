package edu.ntust.dmlab.footbridge.app.backend.streaming;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public abstract class StreamEndpoint {
    protected EndpointStatusListener statusListener;

    public void setStatusListener(EndpointStatusListener statusListener) {
        this.statusListener = statusListener;
    }
    public abstract void startStreaming();
    public abstract boolean isStreamingStarted();
    public abstract void endStreaming();
}
