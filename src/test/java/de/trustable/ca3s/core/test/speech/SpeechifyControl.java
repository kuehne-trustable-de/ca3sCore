package de.trustable.ca3s.core.test.speech;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SpeechifyControl {

    Logger LOG = LoggerFactory.getLogger(SpeechifyControl.class);

    String targetUrl = "https://audio.api.speechify.com/generateAudioFiles";

    String soundFormat = "mp3";
    String voiceName = "Matthew";
    String voiceEngine = "neural";
    String voicelanguageCode = "en-US";

    String soundFilePath = "./src/test/resources/pr/sound";

    final String jsonRequestPattern = "{\"audioFormat\":\"%s\"," +
        "\"paragraphChunks\":[" +
        "\"%s\"" +
        "]," +
        "\"voiceParams\":{\"name\":\"%s\",\"engine\":\"%s\",\"languageCode\":\"%s\"}}";

    public File getSoundFile(final String text) throws JSONException, IOException, ParseException, NoSuchAlgorithmException {

        String jsonRequest = String.format(jsonRequestPattern,soundFormat, text, voiceName, voiceEngine, voicelanguageCode);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String base64FileName =
            Base64.getEncoder().encodeToString(digest.digest(jsonRequest.getBytes(StandardCharsets.UTF_8)))
                .replace('+', '_')
                .replace('/', '_')
                .replace('=', '_');

        File soundFile = new File(soundFilePath, base64FileName + ".json"  );
        if( !soundFile.exists()){
            String json = sendRequest(jsonRequest);

            try(FileWriter fw = new FileWriter(soundFile)){
                fw.write(json);
            }
        }
        return soundFile;
    }

    public byte[] getSoundBytes(final String text) throws JSONException, IOException, ParseException, NoSuchAlgorithmException {

        File file = getSoundFile(text);
        String content = Files.readString(file.toPath());

        JSONObject jsonResponse = new JSONObject(content);

        String audioBytesBase64 = jsonResponse.getString("audioStream");
        String format = jsonResponse.getString("format");

        LOG.debug("Format '" + format + "', content :" + audioBytesBase64);

        return Base64.getDecoder().decode(audioBytesBase64);
    }

    public String sendRequest(final String jsonRequest) throws JSONException, IOException, ParseException {

        final String[] audioBytesEncoded = new String[1];

        final HttpPost httpPost = new HttpPost(targetUrl);
        httpPost.setEntity(new StringEntity(jsonRequest));

        httpPost.setHeader(":authority:", "audio.api.speechify.com");
        httpPost.setHeader(":method:", "POST");
        httpPost.setHeader(":scheme:", "https");

        httpPost.setHeader("Accept", "*/*");
        httpPost.setHeader("Content-type", "application/json");

        httpPost.setHeader("Accept-Base64", "true");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate");
        httpPost.setHeader("Accept-Language", "de,de-DE;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setHeader("Origin", "https://speechify.com");
        httpPost.setHeader("Referer", "https://speechify.com/text-to-speech-online/");
        httpPost.setHeader("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Microsoft Edge\";v=\"120\"");
        httpPost.setHeader("Sec-Ch-Ua-Mobile", "?0");
        httpPost.setHeader("Sec-Ch-Ua-Platform", "\"Windows\"");
        httpPost.setHeader("Sec-Fetch-Dest", "empty");
        httpPost.setHeader("Sec-Fetch-Mode", "cors");
        httpPost.setHeader("Sec-Fetch-Site", "same-site");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0");
        httpPost.setHeader("X-Speechify-Client", "API");
        httpPost.setHeader("X-Speechify-Client-Version", "0.1.297");

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = (CloseableHttpResponse) client.execute(httpPost,
                 new HttpClientResponseHandler<ClassicHttpResponse>() {
                     @Override
                     public ClassicHttpResponse handleResponse(ClassicHttpResponse response) throws IOException {
                         final int statusCode = response.getCode();
                         if (statusCode == 200) {
                             LOG.debug("successful response");

                             audioBytesEncoded[0] = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

                         } else {
                             LOG.error("Status code:", statusCode);
                         }

                         return response;
                     }
                 })) {

            final int statusCode = response.getCode();
            if (statusCode == 200) {
                LOG.debug("successful response");
                return audioBytesEncoded[0];
            } else {
                LOG.error("Status code:", statusCode);
            }
        }
        throw new IOException("no content");
    }

    class CustomHttpClientResponseHandler implements HttpClientResponseHandler<ClassicHttpResponse> {
        Logger LOG = LoggerFactory.getLogger(CustomHttpClientResponseHandler.class);

        @Override
        public ClassicHttpResponse handleResponse(ClassicHttpResponse response) {
            final int statusCode = response.getCode();
            if (statusCode == 200) {
                LOG.debug("successful response");

                try {
                    String jsonResponseString = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject jsonResponse = new JSONObject(jsonResponseString);
                    String audio = jsonResponse.getString("audioStream");
                    String format = jsonResponse.getString("format");

                    LOG.debug("Format '" + format + "', content :" + audio);

                    ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(audio));
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(bais);

                }catch( Exception ex){
                    LOG.error("problem reading content", ex);
                }
            } else {
                LOG.error("Status code:", statusCode);
            }

            return response;
        }
    }

}

