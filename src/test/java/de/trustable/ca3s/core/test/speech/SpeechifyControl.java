package de.trustable.ca3s.core.test.speech;

import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;

public class SpeechifyControl {

    private final String[] speechifyApiTokenArr;
    Logger LOG = LoggerFactory.getLogger(SpeechifyControl.class);

    final static URI targetUri = URI.create("https://api.sws.speechify.com/v1/audio/speech");
    String soundFormat = "mp3";
//    String soundFormat = "wav";
    String voiceName;
    String voiceEngine;
    String voicelanguageCode;

    String soundFilePath = "./src/test/resources/pr/sound";

    public SpeechifyControl(String[] speechifyApiTokenArr, String locale){

        this.speechifyApiTokenArr = speechifyApiTokenArr;

        if("de".equalsIgnoreCase(locale)) {
            voiceName = "andra";
            voicelanguageCode = "de-DE";
            voiceEngine = "azure";
        }else{
            voiceName = "henry";
            voicelanguageCode = "en-US";
            voiceEngine = "neural";
        }

        try {
            Files.createDirectories(Paths.get(soundFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    final String oasJSONRequestPattern = "{" +
        "\"audio_format\": \"%s\"," +
        "\"model\": \"simba-multilingual\"," +
        "\"input\": \"<speak>%s</speak>\"," +
        "\"voice_id\": \"%s\" }";



    public static void main(String[] args) throws Exception {

        String[] speechifyApiTokenArr = new String[]{"P6mt1FQ-BTh7GgI6ttepSGJsxeNa9M8E8HrntEdwHtU="};

        String speakableText =
/*
            "This is a normal speech pattern." +
                "        I'm speaking with a higher pitch, faster than usual, and louder!" +
                "    Back to normal speech pattern.";
*/
            /*
        "This is a normal speech pattern." +
            "    &lt;prosody pitch=\"high\" rate=\"fast\" volume=\"+20%\"&gt;" +
            "        I'm speaking with a higher pitch, faster than usual, and louder!" +
            "    &lt;/prosody&gt;" +
            "    Back to normal speech pattern.";
*/

        "This is a normal speech pattern." +
 //           "    <prosody pitch='high' rate='fast' volume='+20%'>" +
            "        I'm speaking with a higher pitch, faster than usual, and louder!" +
//            "    </prosody>" +
            "    Back to normal speech pattern.";


        SpeechifyControl speechifyControl = new SpeechifyControl(speechifyApiTokenArr, "en");

        byte[] soundBytes = speechifyControl.getSoundBytes(speakableText);

        FactoryRegistry r = FactoryRegistry.systemRegistry();
        AudioDevice audioDevice = r.createAudioDevice();
        try( InputStream is = new ByteArrayInputStream(soundBytes)) {
            Player mp3Player = new Player(is,audioDevice);
            mp3Player.play();
        }
    }


    public File getSoundFile(final String text) throws Exception {

        String jsonRequest = String.format(oasJSONRequestPattern,soundFormat, text, voiceName);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String base64FileName =
            Base64.getEncoder().encodeToString(digest.digest(jsonRequest.getBytes(StandardCharsets.UTF_8)))
                .replace('+', '_')
                .replace('/', '_')
                .replace('=', '_');

        File soundFile = new File(soundFilePath, base64FileName + ".json" );
        if( !soundFile.exists()){
            String json = speechifyOAS(jsonRequest);

            try(FileWriter fw = new FileWriter(soundFile)){
                fw.write(json);
            }
        }
        return soundFile;
    }

    public String speechifyOAS(final String body) throws Exception{

        String authorization = "Bearer " + speechifyApiTokenArr[0];
        HttpRequest request = HttpRequest.newBuilder()
            .uri(targetUri)
            .header("accept", "*/*")
            .header("content-type", "application/json")
            .header("Authorization", authorization)
            .method("POST", HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        LOG.debug(response.body());
        return response.body();

    }

    public byte[] getSoundBytes(final String text) throws Exception {

        File file = getSoundFile(text);
        String content = Files.readString(file.toPath());

        try {
            JSONObject jsonResponse = new JSONObject(content);

            String audioBytesBase64 = jsonResponse.getString("audio_data");
            String format = jsonResponse.getString("audio_format");

            //LOG.debug("Format '" + format + "', content :" + audioBytesBase64);

            return Base64.getDecoder().decode(audioBytesBase64);
        }catch(JSONException jsonException){
            file.delete();
            throw new IOException("problem reading content for '"+text+"'" );
        }
    }

}

