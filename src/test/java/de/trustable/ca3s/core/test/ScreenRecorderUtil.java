package de.trustable.ca3s.core.test;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.monte.media.AudioFormatKeys;
import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import javax.sound.sampled.AudioFormat;

import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class ScreenRecorderUtil extends ScreenRecorder {
 public static ScreenRecorder screenRecorder;
 public String name;
 public ScreenRecorderUtil(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
   Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder, String name)
     throws IOException, AWTException {
  super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
  this.name = name;
 }

 @Override
 protected File createMovieFile(Format fileFormat) throws IOException {

  if (!movieFolder.exists()) {
   movieFolder.mkdirs();
  } else if (!movieFolder.isDirectory()) {
   throw new IOException("\"" + movieFolder + "\" is not a directory.");
  }
  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
  return new File(movieFolder,
    name + "-" + dateFormat.format(new Date()) + "." + Registry.getInstance().getExtension(fileFormat));
 }

 public static void startRecord(String methodName) throws Exception {
  File file = new File("./test-recordings/");
 //    int width = 2000;
 //    int height =768;
  Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  int width = screenSize.width;
  int height = screenSize.height;

  Rectangle captureSize = new Rectangle(0, 0, width, height);

  GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
    getDefaultScreenDevice()
    .getDefaultConfiguration();
  Format fileformat = new Format(MediaTypeKey, MediaType.FILE,
      MimeTypeKey, MIME_AVI);
  Format fileformatquick = new Format(MediaTypeKey, MediaType.FILE,
         MimeTypeKey, MIME_QUICKTIME);
  Format screenformat = new Format(MediaTypeKey, MediaType.VIDEO,
      EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
      CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
      DepthKey, 24,
      FrameRateKey, Rational.valueOf(15),
      QualityKey, 1.0f,
      KeyFrameIntervalKey, 15 * 60);
  Format screenformatquick = new Format(MediaTypeKey, MediaType.VIDEO,
      EncodingKey, ENCODING_QUICKTIME_CINEPAK,
      CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_CINEPAK,
      DepthKey, 24,
      FrameRateKey, new Rational(15, 1));
  Format mouseformat = new Format(MediaTypeKey, MediaType.VIDEO,
      EncodingKey, "black",
      FrameRateKey, Rational.valueOf(60));
  Format mouseformatquick = new Format(MediaTypeKey, MediaType.VIDEO,
      EncodingKey, ENCODING_BLACK_CURSOR,
      FrameRateKey, new Rational(30, 1));
  AudioFormat audioformat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000,16,
     1, 2, 48000, false);

  Format audioformatquick = new Format(MediaTypeKey, MediaType.AUDIO,
      EncodingKey, ENCODING_MP3,
      FrameRateKey, new Rational(48000, 1),
      SampleSizeInBitsKey, 16,
      ChannelsKey, 2,
      SampleRateKey, new Rational(48000, 1),
      SignedKey, true,
      ByteOrderKey, ByteOrder.BIG_ENDIAN);
   Format audioformat2 = new Format(MediaTypeKey, MediaType.AUDIO,
         EncodingKey, ENCODING_ALAW,
         FrameRateKey, new Rational(44100, 1),
         SampleSizeInBitsKey, 16,
         ChannelsKey, 1,
         SampleRateKey, new Rational(44100, 1),
         SignedKey, true,
         ByteOrderKey, ByteOrder.LITTLE_ENDIAN);
  Format audioformat3 = new Format(MediaTypeKey, MediaType.AUDIO,
         //EncodingKey, ENCODING_PCM_SIGNED,
         SampleRateKey, new Rational(44100, 1),
      SampleSizeInBitsKey, 16);


  screenRecorder = new ScreenRecorderUtil(gc, captureSize,
      fileformatquick,
      screenformat,
      mouseformat,
      audioformatquick,
      file, methodName);
  screenRecorder.start();
 }

 public static void stopRecord() throws Exception {
  screenRecorder.stop();
 }
}
