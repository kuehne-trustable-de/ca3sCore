package de.trustable.ca3s.core.test.speech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpeechifyTest {

    private final String[] speechifyApiTokenArr;

    final static URI targetUri = URI.create("https://api.sws.speechify.com/v1/audio/speech");
    String soundFormat = "mp3";
    String voiceName;
    String voiceEngine;
    String voicelanguageCode;

    String soundFilePath = "./src/test/resources/pr/sound";

    public SpeechifyTest(String[] speechifyApiTokenArr, String locale){

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
        SpeechifyTest speechifyControl = new SpeechifyTest(speechifyApiTokenArr, "en");

        System.out.println("#### Plain text");
        String soundBytes = speechifyControl.speechifyOAS(
            "This is a normal speech pattern." +
                "  I'm speaking with a higher pitch, faster than usual, and louder!" +
                "  Back to normal speech pattern.");
        if( soundBytes != null && soundBytes.length() >100 ){
            System.out.println("valid response, " + soundBytes.length() + " chars.");
        }

        System.out.println("\n#### Plain text with newline");
        try {
            speechifyControl.speechifyOAS(
                "This is a normal speech pattern. \n" +
                    "  I'm speaking with a higher pitch, faster than usual, and louder!" +
                    "  Back to normal speech pattern.");
        } catch (Exception ex) {
            System.out.println("Text contains newline. Message " + ex.getMessage());
        }

        System.out.println("\n#### Escaped SSML tags ");
            soundBytes = speechifyControl.speechifyOAS(
                "This is a normal speech pattern." +
                    "    &lt;prosody pitch='high' rate='fast' volume='+20%'&gt;" +
                    "        I'm speaking with a higher pitch, faster than usual, and louder!" +
                    "    &lt;/prosody&gt;" +
                    "    Back to normal speech pattern.");
        System.out.println("quoted tags, read as text. " + soundBytes.length() + " Chars.");

        System.out.println("\n#### SSML tags");
        try {
            speechifyControl.speechifyOAS(
                "This is a normal speech pattern." +
                    "    <prosody pitch=\"high\" rate=\"fast\" volume=\"+20%\">" +
                    "        I am speaking with a higher pitch, faster than usual, and louder!" +
                    "    </prosody>" +
                    "    Back to normal speech pattern.");
        } catch (Exception ex) {
            System.out.println("Exception message : " + ex.getMessage());
        }

    }

    public String speechifyOAS(final String text) throws Exception{

        String jsonRequest = String.format(oasJSONRequestPattern, soundFormat, text, voiceName);

        String authorization = "Bearer " + speechifyApiTokenArr[0];
        HttpRequest request = HttpRequest.newBuilder()
            .uri(targetUri)
            .header("accept", "*/*")
            .header("content-type", "application/json")
            .header("Authorization", authorization)
            .method("POST", HttpRequest.BodyPublishers.ofString(jsonRequest))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();

    }

}

