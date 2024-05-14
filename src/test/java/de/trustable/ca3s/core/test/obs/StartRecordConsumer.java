package de.trustable.ca3s.core.test.obs;

import io.obswebsocket.community.client.message.response.record.StartRecordResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class StartRecordConsumer implements Consumer<StartRecordResponse>{

    Logger LOG = LoggerFactory.getLogger(StartRecordConsumer.class);

    private final Runnable runnable;

    public StartRecordConsumer(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
        public void accept(StartRecordResponse startRecordResponse) {
            LOG.info("StartRecordResponse: " + String.valueOf(startRecordResponse));
            if( runnable != null){
                runnable.run();
            }
        }

        @NotNull
        @Override
        public Consumer<StartRecordResponse> andThen(@NotNull Consumer<? super StartRecordResponse> after) {
            LOG.info("StartRecordResponse: andThen " + String.valueOf(after));
            return Consumer.super.andThen(after);
        }
}
