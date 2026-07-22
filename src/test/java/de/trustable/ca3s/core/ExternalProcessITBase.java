package de.trustable.ca3s.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ExternalProcessITBase {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalProcessITBase.class);


    public boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public boolean isInstalled(final String executableName) {
        return isInstalled(executableName, (String) null);
    }

    public boolean isInstalled(final String executableName, final String noopArgs) {

        ProcessBuilder builderExecutabelExixts = new ProcessBuilder();
        if (isWindows) {
            if( noopArgs == null) {
                builderExecutabelExixts.command(executableName);
            }else{
                builderExecutabelExixts.command(executableName, noopArgs);
            }
        } else {
            if( noopArgs == null) {
                builderExecutabelExixts.command("which", executableName);
            }else {
                builderExecutabelExixts.command("which", executableName, noopArgs);
            }
        }

        return isInstalled(executableName, builderExecutabelExixts);
    }

    public boolean isInstalled(final String executableName, final ProcessBuilder builderExecutabelExixts) {
        if (isWindows) {
            int status = executeExternalProcess(builderExecutabelExixts);
            if (status == 9009 || status == -1) {
                LOG.info("'{]' missing, please install and rerun.", executableName);
                return false;
            }
        } else {
            if (executeExternalProcess(builderExecutabelExixts) != 0) {
                LOG.info("'{]' missing, please install and rerun.", executableName);
                return false;
            }
        }
        return true;
    }
        /**
         * @param builder
         */
    protected int executeExternalProcess(ProcessBuilder builder) {

        int exitCode = -1;

        String cmd = "";
        for( String s:builder.command()) {
            cmd += s + " ";
        }
        LOG.debug("external process command '"+ cmd +"' " );

        try {

//			builder.directory(new File(System.getProperty("user.home")));
            builder.inheritIO();

            Process process = builder.start();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            ExecutorService execSrv = Executors.newSingleThreadExecutor();
            execSrv.submit(streamGobbler);

            exitCode = process.waitFor();
            LOG.debug("external process exitCode '" + exitCode + "' ");

            execSrv.shutdownNow();

        }catch(InterruptedException | IOException ex) {
            LOG.error("executing external process failed with exception", ex);
        }

        return exitCode;
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                .forEach(consumer);
        }
    }

}
