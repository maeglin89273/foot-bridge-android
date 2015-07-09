package edu.ntust.dmlab.footbridge.app.backend.streaming.source;

import android.os.Bundle;
import edu.ntust.dmlab.footbridge.app.backend.streaming.EndpointStatusListener;
import edu.ntust.dmlab.footbridge.app.backend.streaming.StreamEndpoint;

/**
 * Created by maeglin89273 on 7/9/15.
 */
public abstract class StreamSource extends StreamEndpoint {
    protected AsyncConsumer consumer;


    public void setConsumer(AsyncConsumer consumer) {
        this.consumer = consumer;
    }

    public interface AsyncConsumer {
        public void consumeData(Bundle data);
    }
}
