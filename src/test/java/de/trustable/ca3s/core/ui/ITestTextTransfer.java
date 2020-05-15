package de.trustable.ca3s.core.ui;


import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.*;

public final class ITestTextTransfer implements ClipboardOwner {

  public static void main(String...  aArguments ){
    ITestTextTransfer ITestTextTransfer = new ITestTextTransfer();

    //display what is currently on the clipboard
    System.out.println("Clipboard contains:" + ITestTextTransfer.getClipboardContents());

    //change the contents and then re-display
    ITestTextTransfer.setClipboardContents("blah, blah, blah");
    System.out.println("Clipboard contains:" + ITestTextTransfer.getClipboardContents());
  }

   /**
   * Empty implementation of the ClipboardOwner interface.
   */
   @Override public void lostOwnership(Clipboard aClipboard, Transferable aContents){
     //do nothing
   }

  /**
  * Place a String on the clipboard, and make this class the
  * owner of the Clipboard's contents.
  */
  public void setClipboardContents(String aString){
    StringSelection stringSelection = new StringSelection(aString);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, this);
  }

  /**
  * Get the String residing on the clipboard.
  *
  * @return any text found on the Clipboard; if none found, return an
  * empty String.
  */
  public String getClipboardContents() {
    String result = "";
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    //odd: the Object param of getContents is not currently used
    Transferable contents = clipboard.getContents(null);
    boolean hasTransferableText =
      (contents != null) &&
      contents.isDataFlavorSupported(DataFlavor.stringFlavor)
    ;
    if (hasTransferableText) {
      try {
        result = (String)contents.getTransferData(DataFlavor.stringFlavor);
      }
      catch (UnsupportedFlavorException ex){
        System.out.println(ex);
        ex.printStackTrace();
      } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
    }
    return result;
  }
} 

