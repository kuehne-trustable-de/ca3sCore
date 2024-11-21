package de.trustable.ca3s.core.service.util;

import java.util.List;

public class LDHUtil {

    public static boolean isLDHCharsOnly(final String name) {
        return isLDHCharsOnly(name, null);
    }

    public static boolean isLDHCharsOnly(final String name, List<String> msgList){

        boolean ret = true;
//        String[] parts = name.split(".\u3002\uFF0E\uFF61");
//        String[] parts = name.split("\\.。．｡");
        String[] parts = name.split("\\.");
        for( String part: parts){
            for (int i = 0; i < part.length(); i++) {
                if (isNonLDHAsciiCodePoint(part.charAt(i))) {
                    if( msgList != null) {
                        String msg = String.format("character '%c' (of part '%s') is a non-LDH character! ", part.charAt(i), part);
                        if(!msgList.contains(msg)) {
                            msgList.add(msg);
                        }
                    }
                    ret = false;
                }
            }
        }
        return ret;
    }

    //
    // LDH stands for "letter/digit/hyphen", with characters restricted to the
    // 26-letter Latin alphabet <A-Z a-z>, the digits <0-9>, and the hyphen
    // <->.
    // Non LDH refers to characters in the ASCII range, but which are not
    // letters, digits or the hyphen.
    //
    // non-LDH = 0..0x2C, 0x2E..0x2F, 0x3A..0x40, 0x5B..0x60, 0x7B..0x7F
    //
    private static boolean isNonLDHAsciiCodePoint(int ch){
        return (0x0000 <= ch && ch <= 0x002C) ||
            (0x002E <= ch && ch <= 0x002F) ||
            (0x003A <= ch && ch <= 0x0040) ||
            (0x005B <= ch && ch <= 0x0060) ||
            (0x007B <= ch && ch <= 0x007F);
    }

}
