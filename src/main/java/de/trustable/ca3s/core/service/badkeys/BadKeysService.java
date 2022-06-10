package de.trustable.ca3s.core.service.badkeys;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class BadKeysService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BadKeysService.class);

    private final String badkeysExecutable;
    private final File badkeysDirectory;

    private boolean isInstalled = false;

    public BadKeysService(@Value("${ca3s.badkeys.executable:badkeys-cli}") String badkeysExecutable,
                          @Value("${ca3s.badkeys.directory:/var/opt/badkeys}") File badkeysDirectory) {
        this.badkeysExecutable = badkeysExecutable;
        this.badkeysDirectory = badkeysDirectory;

        if( badkeysDirectory.exists() && badkeysDirectory.canRead()){
            isInstalled = true;
        }
    }

    public BadKeysResult checkCSR(final String pemCSR) {

        if( !badkeysDirectory.exists() ){
            return new BadKeysResult(false, false, "badkeysDirectory '"+badkeysDirectory.getAbsolutePath()+"' does not exist");
        }
        if( !badkeysDirectory.canRead() ){
            return new BadKeysResult(false, false, "cannot access badkeysDirectory '"+badkeysDirectory.getAbsolutePath()+"', check access rights.");
        }

        File inputFile = null;
        try {
            inputFile = File.createTempFile("badKeysInput", "pem");
            try (FileOutputStream fos = new FileOutputStream(inputFile)) {
                fos.write(pemCSR.getBytes());
                fos.flush();
                return invokeBadKeys(inputFile);
            }
        } catch (IOException e) {
            LOGGER.warn("problem occurred writing file temporarily", e);
            return new BadKeysResult(false, "problem occurred writing temp file ");
        } finally {
            if (inputFile != null) {
                LOGGER.debug("deleting temp file {}", inputFile.getAbsolutePath());
                inputFile.delete();
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
                LOGGER.info("badkeys process returns with code {}", exitValue);
                BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
                JsonObject jsonObject = JsonParser.parseReader(output).getAsJsonObject();
                LOGGER.info("badkeys process returns json:\n{}",jsonObject.toString());

                Response response = new Response(jsonObject);
                return new BadKeysResult(response);
            } catch (IllegalThreadStateException illegalThreadStateException) {
                try {Thread.sleep(100L);} catch (InterruptedException ignore) {}
            }
        }
        process.destroyForcibly();
        return new BadKeysResult(false, "problem calling / controlling badkeys process ");
    }

    public boolean isInstalled() {
        return isInstalled;
    }
}
