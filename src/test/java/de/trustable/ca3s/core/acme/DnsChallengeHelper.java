package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.challenge.ChallengeValidator;
import de.trustable.ca3s.core.web.rest.acme.ChallengeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

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

    List<EntryAndValue> entryAndValueList = new ArrayList<>();

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

        // Add answers as needed
        response.addRecord(org.xbill.DNS.Record.fromString(Name.root, Type.A, DClass.IN, 86400, "1.2.3.4", Name.root), Section.ANSWER);
        for(EntryAndValue entryAndValue : entryAndValueList) {
            response.addRecord(org.xbill.DNS.Record.fromString(entryAndValue.getEntryName(), Type.TXT, DClass.IN, 86400, entryAndValue.getTextValue(), Name.root), Section.ANSWER);
            LOG.info("Serving DNS TXT record for {} with value {}", entryAndValue.getEntryName().toString(false), entryAndValue.getTextValue());
        }

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

    public void setDNSChallengeDetails(String textValue, String identifier) throws TextParseException, NameTooLongException {

        entryAndValueList = new ArrayList<>();

        Name nameOfIdentifier = fromString(identifier, root);
        this.entryAndValueList.add(new EntryAndValue(concatenate(ChallengeController.ACME_CHALLENGE_PREFIX, nameOfIdentifier), textValue));
    }

    public void addDNSPersistChallengeDetails(String textValue, String identifier) throws TextParseException, NameTooLongException {
        Name nameOfIdentifier = fromString(identifier, root);
        this.entryAndValueList.add(new EntryAndValue(concatenate(ChallengeValidator.ACME_DNS_PERSIST_CHALLENGE_PREFIX, nameOfIdentifier), textValue));

    }
    public void setDNSPersistChallengeDetails(String textValue, String identifier) throws TextParseException, NameTooLongException {
        entryAndValueList = new ArrayList<>();
        addDNSPersistChallengeDetails(textValue, identifier);
    }

    static class EntryAndValue{

        private final Name entryName;

        private final String textValue;

        EntryAndValue(Name entryName, String textValue) {
            this.entryName = entryName;
            this.textValue = textValue;
        }

        public Name getEntryName() {
            return entryName;
        }

        public String getTextValue() {
            return textValue;
        }
    }
}
