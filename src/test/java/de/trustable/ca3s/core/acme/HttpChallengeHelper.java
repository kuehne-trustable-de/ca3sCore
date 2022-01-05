package de.trustable.ca3s.core.acme;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.takes.Take;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.Exit;
import org.takes.http.FtBasic;

import java.io.IOException;
import java.net.BindException;

public class HttpChallengeHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HttpChallengeHelper.class);

    final int callbackPort;

    public HttpChallengeHelper(int callbackPort) {
        this.callbackPort = callbackPort;
    }

    public Thread provideAuthEndpoint(String fileName, String fileContent, Boolean terminate) throws IOException, InterruptedException {

        final String fileNameRegEx = "/\\.well-known/acme-challenge/" + fileName;

        LOG.debug("Handling authorization for {} on port {}", fileNameRegEx, callbackPort);

        Take tk = new TkFork(new FkRegex(fileNameRegEx, fileContent));

        FtBasic webBasicTmp;
        try {
            webBasicTmp = new FtBasic(tk, callbackPort);
        }catch(BindException be) {
            LOG.warn("BindException for port " + callbackPort, be);
            Thread.sleep(1000L);
            webBasicTmp = new FtBasic(tk, callbackPort);
        }
        final FtBasic webBasic = webBasicTmp;

        final Exit exitOnValid = new Exit() {
            @Override
            public boolean ready() {
                LOG.info("exitOnValid by Boolean {}", terminate.booleanValue());
                return (terminate.booleanValue());
            }
        };

        Thread webThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOG.debug("ACME callback webserver started for {}", fileNameRegEx);
                    webBasic.start(exitOnValid);
                    LOG.debug("ACME callback webserver finished for {}", fileNameRegEx);
                } catch (IOException ioe) {
                    LOG.warn("exception occur running webserver in extra thread", ioe);
                }
            }
        });

        webThread.start();

        LOG.debug("started ACME callback webserver for {} on port {}", fileNameRegEx, callbackPort);

        return webThread;
    }

}
