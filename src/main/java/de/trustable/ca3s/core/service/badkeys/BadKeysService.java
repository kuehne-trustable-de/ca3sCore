package de.trustable.ca3s.core.service.badkeys;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class BadKeysService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BadKeysService.class);

    private final boolean useBadkeys;
    private final String badkeysExecutable;
    private final File badkeysDirectory;

    private List<String> availableChecks;

    private boolean isInstalled = false;

    public BadKeysService(@Value("${ca3s.badkeys.use:false}") boolean useBadkeys,
                          @Value("${ca3s.badkeys.executable:badkeys-cli}") String badkeysExecutable,
                          @Value("${ca3s.badkeys.directory:/var/opt/badkeys}") File badkeysDirectory) {
        this.useBadkeys = useBadkeys;
        this.badkeysExecutable = badkeysExecutable;
        this.badkeysDirectory = badkeysDirectory;

        if( useBadkeys && badkeysDirectory.exists() && badkeysDirectory.canRead()){

            try {
                availableChecks = invokeBadKeysInfo();
                if( availableChecks.size() > 5) {
                    isInstalled = true;
                }else{
                    LOGGER.info("Too few ({}) checks available, check your BadKeys installation!", availableChecks.size());
                }
            } catch (IOException e) {
                LOGGER.info("BadKeysService cTor", e);
            }
        }
    }

    public BadKeysResult checkContent(final String pemContent) {

        if( !useBadkeys ){
            return new BadKeysResult(false, false, "useBadkeys == false");
        }
        if( !badkeysDirectory.exists() ){
            return new BadKeysResult(false, false, "badkeysDirectory '"+badkeysDirectory.getAbsolutePath()+"' does not exist");
        }
        if( !badkeysDirectory.canRead() ){
            return new BadKeysResult(false, false, "cannot access badkeysDirectory '"+badkeysDirectory.getAbsolutePath()+"', check access rights.");
        }

        File inputFile = null;
        try {
            inputFile = File.createTempFile("badKeysInput_", ".pem");
            try (FileOutputStream fos = new FileOutputStream(inputFile)) {
                fos.write(pemContent.getBytes());
                fos.flush();
                return invokeBadKeys(inputFile);
            }
        } catch (IOException e) {
            LOGGER.warn("problem occurred writing file temporarily", e);
            return new BadKeysResult(false, "problem occurred writing temp file ");
        } finally {
            if (inputFile != null) {
                LOGGER.debug("deleting temp file '{}'", inputFile.getAbsolutePath());
                if( !inputFile.delete()){
                    LOGGER.warn("deleting temp file '{}' failed, please cleanup manually!", inputFile.getAbsolutePath());
                }
            }
        }
    }

    private BadKeysResult invokeBadKeys(File inputFile) throws IOException {

        // badkeys-cli -v test.pem
        String badKeyArg = badkeysExecutable + " -j -v " + inputFile.getAbsolutePath();
        Process process = Runtime.getRuntime().exec(badKeyArg, null, badkeysDirectory);

        for (int i = 0; i < 20; i++) {
            try {
                int exitValue = process.exitValue();
                LOGGER.debug("badkeys process returns with code {}", exitValue);
                BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
                if( exitValue != 0){
                    String errMsg = output.readLine();
                    LOGGER.warn("badkeys failed with message: {}",errMsg);
                    return new BadKeysResult(false, errMsg);
                }else {
                    JsonObject jsonObject = JsonParser.parseReader(output).getAsJsonObject();
                    LOGGER.debug("badkeys process returns json:\n{}", jsonObject.toString());
                    BadKeysResultResponse response = new BadKeysResultResponse(jsonObject);
                    return new BadKeysResult(response);
                }

            } catch (IllegalThreadStateException illegalThreadStateException) {
                try {Thread.sleep(100L);} catch (InterruptedException ignore) {}
            }
        }
        process.destroyForcibly();
        return new BadKeysResult(false, "problem calling / controlling badkeys process ");
    }

    private List<String> invokeBadKeysInfo() throws IOException {

        // badkeys-cli --list
        String badKeyArg = badkeysExecutable + " --list";
        Process process = Runtime.getRuntime().exec(badKeyArg, null, badkeysDirectory);

        ArrayList<String> checkList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            try {
                int exitValue = process.exitValue();
                LOGGER.info("badkeys info process returns with code {}", exitValue);
                if( exitValue != 0){
                    throw new IOException("invocation of badkeys failed with prcess status " + exitValue);
                }

                BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String check;
                while( (check = output.readLine()) != null) {
                    LOGGER.debug("badkeys offers check :{}", check);
                    checkList.add(check);
                }
                return checkList;
            } catch (IllegalThreadStateException illegalThreadStateException) {
                try {Thread.sleep(100L);} catch (InterruptedException ignore) {}
            }
        }
        process.destroyForcibly();
        throw new IOException("invocation of badkeys failed");
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public List<String> getAvailableChecks() {
        return availableChecks;
    }

}
