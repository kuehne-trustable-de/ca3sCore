package de.trustable.ca3s.core.test.obs;

import com.google.gson.JsonObject;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.request.inputs.CreateInputRequest;
import io.obswebsocket.community.client.message.request.record.StartRecordRequest;
import io.obswebsocket.community.client.message.request.record.StopRecordRequest;
import io.obswebsocket.community.client.message.response.inputs.*;
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

    public static final String SPOKEN_TEXT_INPUT = "spokenTextInput";
    public static final String TEXT_INPUT_KIND = "text_gdiplus_v3";

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

        OBSRemoteController testController = connectToRemoteOBS();

        GetSceneListResponse getSceneListResponse = testController.getSceneList(1000);
        if(getSceneListResponse != null) {
            LOG.info("getSceneListResponse: {}", getSceneListResponse);
        }

        testController.disconnect();

        if( getSceneListResponse != null) {
            LOG.info("OBS instance available");
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

            getController().getInputKindList(true, 2000L);
        }

/*
        GetInputKindListResponse getInputKindListResponse = getController().getInputKindList(true, 10000L);
        LOG.info( "getInputKindListResponse: {} ", getInputKindListResponse);

        GetInputListResponse inputListResponse = getController().getInputList(supportedKind, 1000);

        GetInputSettingsResponse inputSettingsResponse = getController().getInputSettings(SPOKEN_TEXT_INPUT,1000);

        if( inputSettingsResponse.getMessageData().getRequestStatus().getResult()){
            LOG.debug( "Text input already present.");
        }else {
            controller = null;

            CreateInputRequest createInputRequest = CreateInputRequest.builder()
                .inputName(SPOKEN_TEXT_INPUT)
                .inputKind(TEXT_INPUT_KIND)
                .sceneName(sceneName)
                .inputSettings(buildTextSettings("dummy Text"))
                .sceneItemEnabled(true)
                .build();

            Consumer<CreateInputResponse> callback = new Consumer<>() {
                @Override
                public void accept(CreateInputResponse createInputResponse) {
                    LOG.info("createInputResponse.isSuccessful() : {}", createInputResponse.isSuccessful());
                    if (!createInputResponse.isSuccessful()) {
                        LOG.info("create text input failed : {}", createInputResponse.getMessageData().getRequestStatus());
                    }
                }
            };
            getController().sendRequest(createInputRequest, callback);
        }

 */
    }

    public void setSpokenText(String textContent) {
        SetInputSettingsResponse setInputSettingsResponse = getController().setInputSettings(
            SPOKEN_TEXT_INPUT,
            buildTextSettings(textContent ),
            false,
            1000);
        LOG.info("setInputSettingsResponse.isSuccessful() : {}", setInputSettingsResponse.isSuccessful());
    }

    JsonObject buildTextSettings(final String textValue){
        /*

        {"align":"center","font":{"face":"Arial","flags":0,"size":14,"style":"Standard"},"text":"Test Text","valign":"bottom"}

         */
        JsonObject inputSettings = new JsonObject();
        inputSettings.addProperty("text", textValue);
        inputSettings.addProperty("align", "center");
        inputSettings.addProperty("valign", "bottom");
        inputSettings.addProperty("outline", "true");
        inputSettings.addProperty("color", "4278190080");
        inputSettings.addProperty("bk_color", "4294967295");
        inputSettings.addProperty("extents", "true");
        inputSettings.addProperty("extents_cx", 700);
        inputSettings.addProperty("extents_cy", 50);

        JsonObject fontElement = new JsonObject();
        fontElement.addProperty("face", "Calibri");
        fontElement.addProperty("flags",0);
        fontElement.addProperty("size",16);
        fontElement.addProperty("style","Regular");
        inputSettings.add("font", fontElement);
        return inputSettings;
    }

    private synchronized OBSRemoteController getController(){

        if( controller == null){
            controller = connectToRemoteOBS();
        }
        return controller;
    }

    private OBSRemoteController connectToRemoteOBS() {

        OBSRemoteController con = OBSRemoteController.builder()
            .host(host)
            .port(port)
            .password(password)
            .connectionTimeout(30)
            .build();

        con.connect();
        return con;
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
