package edu.ntust.dmlab.footbridge.app.backend.streaming;

import edu.ntust.dmlab.footbridge.app.backend.streaming.path.StreamPath;
import edu.ntust.dmlab.footbridge.app.backend.streaming.source.StreamSource;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public abstract class TransferBridge {
    protected StreamSource source;
    protected StreamPath path;
    protected StreamSource.AsyncConsumer transferTrigger;
    private EndpointStatusListener sourceStatusListener;
    private EndpointStatusListener pathStatusListener;

    protected void initCallbacks(StreamSource.AsyncConsumer transferTrigger, EndpointStatusListener sourceStatusListener, EndpointStatusListener pathStatusListener) {
        this.transferTrigger = transferTrigger;
        this.sourceStatusListener = sourceStatusListener;
        this.pathStatusListener = pathStatusListener;
    }

    public void setSource(StreamSource source) {
        if (this.source != null) {
            this.source.endStreaming();
        }
        source.setStatusListener(this.sourceStatusListener);
        source.setConsumer(this.transferTrigger);
        this.source = source;
        this.source.startStreaming();
    }

    public StreamSource getSource() {
        return this.source;
    }

    public void setPath(StreamPath path) {
        if (this.path != null) {
            this.path.endStreaming();
        }
        path.setStatusListener(this.pathStatusListener);
        this.path = path;
        this.path.startStreaming();
    }

    public StreamPath getPath() {
        return this.path;
    }

    public void breakBridge() {
        this.source.endStreaming();
        this.path.endStreaming();
    }

}
