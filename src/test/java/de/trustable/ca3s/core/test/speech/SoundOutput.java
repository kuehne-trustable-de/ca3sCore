package de.trustable.ca3s.core.test.speech;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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

      public void _play() throws IOException, UnsupportedAudioFileException {

          try( InputStream is = new ByteArrayInputStream(soundBytes)) {
              AudioInputStream inputStream = AudioSystem.getAudioInputStream(is);

              Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
              for (int i = 0; i < mixerInfo.length; i++) {
                  Mixer.Info info = mixerInfo[i];

                  if( info.getDescription().contains("DirectSound Playback")) {
                      System.out.println(String.format("Name [%s] \n Description [%s]\n", info.getName(), info.getDescription()));
                      System.out.println(mixerInfo.getClass());

                      Mixer m = AudioSystem.getMixer(info);
                      Line.Info[] lineInfos = m.getSourceLineInfo();
                      for (Line.Info lineInfo:lineInfos){
                          System.out.println ("source line info: "+lineInfo);

                      }

                      try (Clip clip = AudioSystem.getClip(info)) {
                          System.out.println(" ---- clip format: " + clip.getFormat().toString());
                          clip.open(inputStream);
                          clip.start();
                          System.out.println("Clip succeeded !");
                      } catch (Throwable t) {
                          System.out.println(t);
                      }

                      break;
                  }

              }
          }
      }
}
