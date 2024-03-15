package de.trustable.ca3s.core.test.obs;

import io.obswebsocket.community.client.message.response.record.StartRecordResponse;
import io.obswebsocket.community.client.message.response.record.StopRecordResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class StopRecordConsumer implements Consumer<StopRecordResponse> {

    Logger LOG = LoggerFactory.getLogger(StopRecordConsumer.class);

    private final RunnableWithFilename runnable;

    public StopRecordConsumer(RunnableWithFilename runnable) {
        this.runnable = runnable;
    }

    @Override
    public void accept(StopRecordResponse stopRecordResponse) {
        LOG.info("StopRecordResponse: " + String.valueOf(stopRecordResponse));
        if (runnable != null) {
            runnable.run(stopRecordResponse.getOutputPath());
        }
    }

    @NotNull
    @Override
    public Consumer<StopRecordResponse> andThen(@NotNull Consumer<? super StopRecordResponse> after) {
        LOG.info("StartRecordResponse: andThen " + String.valueOf(after));
        return Consumer.super.andThen(after);
    }
}
