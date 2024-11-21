package de.trustable.ca3s.core.service.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LDHUtilTest {

    private static final Logger log = LoggerFactory.getLogger(LDHUtilTest.class);

    static ArgsAndResult[] testSet = {
        new ArgsAndResult("name", true),
        new ArgsAndResult("name.de", true),
        new ArgsAndResult("foo.name.de", true),

        new ArgsAndResult(" name.de", false),
        new ArgsAndResult("name.de:80", false),
        new ArgsAndResult("http://name.de", false)
    };

    @Test
    void isLDHCharsOnly() {

        for(ArgsAndResult argsAndResult: testSet) {

            List<String> msgList = new ArrayList<>();
            boolean isLDHCharsOnly = LDHUtil.isLDHCharsOnly(argsAndResult.name, msgList);
            if(!isLDHCharsOnly){
                for (String msg: msgList){
                    log.info("msg : '{}'", msg);
                }
            }
            assertEquals( argsAndResult.result, isLDHCharsOnly );
        }
    }
}

class ArgsAndResult{
    public ArgsAndResult(String name, boolean result){
        this.name = name;
        this.result = result;
        this.msgs = new ArrayList<>();
    }
    String name;
    boolean result;
    List<String> msgs;
}
