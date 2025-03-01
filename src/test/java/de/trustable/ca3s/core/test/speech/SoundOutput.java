package de.trustable.ca3s.core.test.speech;

import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

  public class SoundOutput {

      private final String[] speechifyApiTokenArr;

      public SoundOutput(String[] speechifyApiTokenArr) {
          this.speechifyApiTokenArr = speechifyApiTokenArr;
      }

      public static void main(String[] args) throws Exception {

        /*
         test.play();
        test = new SoundOutput("Change to the directory where the key should be created. Consult the product " +
            "documentation of your application for the appropriate directory. Fill out the required fields in the " +
            "ca3s web form (subject, organization, SAN, ...)", "de" );
        test.play();

        test = new SoundOutput( );
        test.play("Unabhängig von der Schlüssellänge wählen Sie 'Zertifikat anfordern' aus der Menüleiste aus. 2", "de");

         */
    }

    /**
     * Plays the audio from the given source
     */
    public final void play(String text, String locale) throws Exception{

        SpeechifyControl speechifyControl = new SpeechifyControl(speechifyApiTokenArr, locale);
        String speakableText = text
            .replaceAll("CMP","C M P")
            .replaceAll("SCEP","esszepp")
            .replaceAll("ACME","ackmi")
            .replaceAll("ca3s","C A 3 S");

        byte[] soundBytes = speechifyControl.getSoundBytes(speakableText);

        FactoryRegistry r = FactoryRegistry.systemRegistry();
        AudioDevice audioDevice = r.createAudioDevice();
        try( InputStream is = new ByteArrayInputStream(soundBytes)) {
            Player mp3Player = new Player(is,audioDevice);
            mp3Player.play();
        }
    }
}
