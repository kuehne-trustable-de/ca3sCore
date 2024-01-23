package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.web.rest.acme.ChallengeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static org.xbill.DNS.Name.*;

public class DnsChallengeHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DnsChallengeHelper.class);

    final int port;

    public DnsChallengeHelper(int port) {
        this.port = port;
    }

    private Thread thread = null;
    private volatile boolean running = false;
    private static final int UDP_SIZE = 512;
    private int requestCount = 0;

    String token = "token";
    String identifier = "localhost";

    public void start() {
        running = true;
        thread = new Thread(() -> {
            try {
                serve();
            } catch (IOException ex) {
                stop();
                throw new RuntimeException(ex);
            }
        });
        thread.start();
    }

    public void stop() {
        running = false;
        if( thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
    public int getRequestCount() {
        return requestCount;
    }
    private void serve() throws IOException {
        DatagramSocket socket = new DatagramSocket(port);
        while (running) {
            process(socket);
        }
    }
    private void process(DatagramSocket socket) throws IOException {

        byte[] in = new byte[UDP_SIZE];

        // Read the request
        DatagramPacket indp = new DatagramPacket(in, UDP_SIZE);
        socket.receive(indp);
        ++requestCount;

        LOG.info(String.format("processing request #%d", requestCount));
        // Build the response
        Message request = new Message(in);
        Message response = new Message(request.getHeader().getID());
        response.addRecord(request.getQuestion(), Section.QUESTION);

        final Name nameOfIdentifier = fromString(identifier, root);
        final Name nameToLookup = concatenate(ChallengeController.ACME_CHALLENGE_PREFIX, nameOfIdentifier);

        // Add answers as needed
        response.addRecord(org.xbill.DNS.Record.fromString(Name.root, Type.A, DClass.IN, 86400, "1.2.3.4", Name.root), Section.ANSWER);
        response.addRecord(org.xbill.DNS.Record.fromString(nameToLookup, Type.TXT, DClass.IN, 86400, token, Name.root), Section.ANSWER);
        LOG.info("Serving DNS TXT record for {}", nameToLookup.toString(false));

        /*
        // Make it timeout, comment this section if a success response is needed
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            LOG.error("Interrupted");
            return;
        }
        */

        byte[] resp = response.toWire();
        DatagramPacket outdp = new DatagramPacket(resp, resp.length, indp.getAddress(), indp.getPort());
        socket.send(outdp);
    }

    public void setChallengeDetails(String token, String authorization) {
        this.token = token;
        this.identifier = authorization;
    }
}
