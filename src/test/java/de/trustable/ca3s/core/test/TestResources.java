package de.trustable.ca3s.core.test;

import javax.sound.sampled.*;
import java.util.Arrays;

class TestResources {
    public static void main(String args[]) {
        try {
            Mixer.Info [] mixers = AudioSystem.getMixerInfo();
            for(int i = 0 ; i< mixers.length; i ++) {
                System.out.println((i+1)+". " + mixers[i].getName() + " --> " + mixers[i].getDescription() );

                Line.Info [] sourceLines = AudioSystem.getMixer(mixers[i]).getSourceLineInfo();
                System.out.println("\tSource Lines:" );
                for(int j = 0; j< sourceLines.length; j++) {
                    System.out.println("\tS" + (j+1) + ". " + sourceLines[j].toString() );

                }
                System.out.println();

                Line.Info [] targetLines = AudioSystem.getMixer(mixers[i]).getTargetLineInfo();
                System.out.println("\tTarget Lines:" );
                for(int j = 0; j< targetLines.length; j++) {
                    System.out.println("\tT" + (j+1) + ". " + targetLines[j].toString() );
                    showLineInfoFormats(targetLines[j]);
                }
                System.out.println("\n" );
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    static void showLineInfoFormats(final Line.Info lineInfo)
    {
        if (lineInfo instanceof DataLine.Info)
        {
            final DataLine.Info dataLineInfo = (DataLine.Info)lineInfo;

            Arrays.stream(dataLineInfo.getFormats())
                .forEach(format -> System.out.println("    " + format.toString()));
        }
    }

}


