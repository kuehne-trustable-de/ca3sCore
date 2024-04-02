package de.trustable.ca3s.core.test.obs;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.request.record.StartRecordRequest;
import io.obswebsocket.community.client.message.request.record.StopRecordRequest;
import io.obswebsocket.community.client.message.response.record.StartRecordResponse;
import io.obswebsocket.community.client.message.response.record.StopRecordResponse;
import io.obswebsocket.community.client.message.response.scenes.GetSceneListResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class OBSClient {

    Logger LOG = LoggerFactory.getLogger(OBSClient.class);

    private final String host;
    private final int port;
    private final String password;

    private OBSRemoteController controller = null;

    Process process = null;

    public static final void main(String[] args) throws InterruptedException, IOException {

        OBSClient obsClient = new OBSClient("localhost", 4455, "S3cr3t!S");
        Thread.sleep(5000);
        obsClient.connect();
        Thread.sleep(5000);

        obsClient.startRecord();
        Thread.sleep(5000);
        obsClient.stopRecord();

        obsClient.close();

    }

    public OBSClient(String host, int port, String password) throws IOException {
        this.host = host;
        this.port = port;
        this.password = password;


        OBSRemoteController testController = OBSRemoteController.builder()
            .host(host)
            .port(port)
            .password(password)
            .connectionTimeout(30)
            .build();

        testController.connect();
        GetSceneListResponse getSceneListResponse = testController.getSceneList(1000);
        LOG.info( "getSceneListResponse: " + getSceneListResponse);

        if( getSceneListResponse != null) {
            testController.disconnect();
        }else{
            File obsFile = new File("C:\\Program Files\\obs-studio\\bin\\64bit\\obs64.exe");

            if (!obsFile.exists()) {

                System.err.println("OBS not present at path '" + obsFile.getPath() + "', download it from https://obsproject.com/de/download and install it");
                System.err.println("Configure OBS:\nSet sources to 'audio output' and 'screen recording'");
                System.err.println("Lower the mixer level of 'audio output' and 'desktop audio' to -15 dB to avoid distortions");
                System.err.println("Lower the mixer level of 'microphone' to '-inf dB' (left boundary) to mute the microphone");
                System.err.println("Open the menue Tools/WebSocket settings:\nActivate the websocket server checkbox\nset server port to 4455\nset a server passwort to 'S3cr3t!S'");

                throw new IOException("OBS not present at path '" + obsFile.getPath() + "' !");
            }
            if (!obsFile.canExecute()) {
                throw new IOException("Program at path '" + obsFile.getPath() + "' is not executable. ");
            }

            process = new ProcessBuilder("C:\\Program Files\\obs-studio\\bin\\64bit\\obs64.exe",
                "--websocket_port", "" + port,
                "--websocket_password", password,
                "--websocket_debug", "false")
                .directory(obsFile.getParentFile())
                .start();

            LOG.info("obs process: " + process.info());
        }

    }

    private synchronized OBSRemoteController getController(){

        if( controller == null){
            controller = OBSRemoteController.builder()
                .host(host)
                .port(port)
                .password(password)
                .connectionTimeout(30)
                .build();

            controller.connect();
        }
        return controller;
    }

    public void await() throws InterruptedException {
        LOG.info("await ...");
        getController().await();
        LOG.info("... await succeeded");
    }

    public void close(){
        getController().disconnect();
    }

    public void connect(){
        getController();
    }

    public void startRecord( StartRecordConsumer startRecordConsumer) {
        getController().sendRequest(StartRecordRequest.builder().build(), startRecordConsumer);
    }
    public void stopRecord( StopRecordConsumer stopRecordConsumer) {
        getController().sendRequest(StopRecordRequest.builder().build(), stopRecordConsumer);
    }



    public void startRecord(){
        LOG.info("startRecord");
        getController().sendRequest( StartRecordRequest.builder().build(), new Consumer<StartRecordResponse>(){
            @Override
            public void accept(StartRecordResponse startRecordResponse) {
                LOG.info("StartRecordResponse: " + startRecordResponse);
            }

            @NotNull
            @Override
            public Consumer<StartRecordResponse> andThen(@NotNull Consumer<? super StartRecordResponse> after) {
                LOG.info("StartRecordResponse: andThen " + after);
                return Consumer.super.andThen(after);
            }
        });
    }

    public void stopRecord(){

        LOG.info("stopRecord");
        getController().sendRequest( StopRecordRequest.builder().build(), new Consumer<StopRecordResponse>(){
            @Override
            public void accept(StopRecordResponse stopRecordResponse) {
                LOG.info("StopRecordResponse: " + String.valueOf(stopRecordResponse));
                LOG.info("created file: " + stopRecordResponse.getOutputPath());
            }

            @NotNull
            @Override
            public Consumer<StopRecordResponse> andThen(@NotNull Consumer<? super StopRecordResponse> after) {
                LOG.info("StopRecordResponse: andThen " + String.valueOf(after));
                return Consumer.super.andThen(after);
            }
        });
    }

}
