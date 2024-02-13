package de.trustable.ca3s.core.test.speech;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.JavaSoundAudioDeviceFactory;
import javazoom.jl.player.*;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONException;

import java.io.*;
import java.security.NoSuchAlgorithmException;

  public class SoundOutput {

    public static void main(String[] args) throws Exception {

        SoundOutput test = new SoundOutput("Navigate your browser to the start page of the application and login in as a simple user");
        test.play();
        test = new SoundOutput("Change to the directory where the key should be created. Consult the product documentation of your application for the appropriate directory. Fill out the required fields in the ca3s web form (subject, organization, SAN, ...)" );
        test.play();
    }

    private byte[] soundBytes;

    public SoundOutput(String text) throws Exception {

        SpeechifyControl speechifyControl = new SpeechifyControl();
        this.soundBytes = speechifyControl.getSoundBytes(text);
    }

    /**
     * Plays the audio from the given source
     */
    public final void play() throws IOException, JavaLayerException {
        FactoryRegistry r = FactoryRegistry.systemRegistry();
        AudioDevice audioDevice = r.createAudioDevice();
        try( InputStream is = new ByteArrayInputStream(soundBytes)) {
            Player mp3Player = new Player(is,audioDevice);
            mp3Player.play();
        }
    }

}
