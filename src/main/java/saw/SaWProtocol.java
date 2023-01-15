package saw;

import core.Configuration;
import core.Msg;
import core.Protocol;
import exceptions.IWProtocolException;
import exceptions.IllegalMsgException;
import slp.SLPConfiguration;
import slp.SLPProtocol;

import java.io.IOException;

public class SaWProtocol extends Protocol{

    private SLPProtocol slp;
    private SLPConfiguration slpConfig;
    private int seqNr;
    private int ackNr;


    public SaWProtocol(SLPProtocol slp) throws IWProtocolException {
        this.slp = slp;
        this.ackNr = 0;
        this.seqNr = 0;
    }

    @Override
    public void send(String s, Configuration config) throws IOException, IWProtocolException {

        SLPConfiguration slpConfig = new SLPConfiguration(((SaWConfiguration)config).getRemoteID(), slp);

        slp.enableTimeout();
        SaWDataMsg sawMessage = new SaWDataMsg();
        sawMessage.create(s, seqNr);

        int attempts = 0;
        //loop until the message has been successfully sent or the maximum number of 3 attempts has been reached
        while (attempts < 3) {
            attempts++;

            slp.send(new String(sawMessage.getDataBytes()), slpConfig);
            Msg in = null;
            try {
                in = slp.receive();
            } catch (IWProtocolException e) {
                continue;
            }
            try {
                in = (new SaWMsg()).parse(in.getData());
            } catch (IWProtocolException e) {
                continue;
            }

            //if the received message is no SaWAckMsg, continue to the next iteration of the loop
            if (!(in instanceof SaWAckMsg))
                continue;

            //if the acknowledgement number in the received message does not match the current sequence number, continue to the next iteration of the loop
            if (((SaWAckMsg)in).getAckNr() != this.seqNr)
                continue;

            //the message has been successfully sent

            //flips the seqNr to 0 if it is 1, and to 1 if it is 0
            this.seqNr = (this.seqNr + 1) % 2;

            return;

        }
        //no acknowledgment message is received for three
        //consecutive retransmissions
        throw new IllegalMsgException();

    }

    @Override
    public Msg receive() throws IOException, IWProtocolException {
        return null;
    }

    public Msg receive(SaWConfiguration configuration) throws IOException, IWProtocolException {

        SLPConfiguration slpConfig = new SLPConfiguration(configuration.getRemoteID() , slp);

        slp.disableTimeout();
        Msg in = null;

        try{
            in = slp.receive();
        } catch (IWProtocolException e) {
            e.printStackTrace();
        }

        //try to parse the message as a SaWMsg
        try {
            in = (new SaWMsg()).parse(in.getData());
        } catch (Exception e) {
            //if parsing fails, call the receive method again
            return this.receive();
        }

        //if the message is not an instance of SaWDataMsg, call the receive method again
        if (!(in instanceof SaWDataMsg))
            return this.receive();

        //if the sequence number of the message matches the expected ack number
        if ( ((SaWDataMsg) in).getSeqNr() == this.ackNr) {

            //create an ACK message with the current ack number
            SaWAckMsg ackMsg = new SaWAckMsg();
            ackMsg.create(ackNr);

            //flips the ackNr to 0 if it is 1, and to 1 if it is 0
            this.ackNr = (this.ackNr + 1) % 2;
            slp.send(new String(ackMsg.getDataBytes()), slpConfig);

            //return the received message
            return in;
        }

        //the message is not accepted, the receiver resends the last acknowledgment
        //current ackNr gets flipped to the last ackNr
        int lastAckNr = (this.ackNr + 1) % 2;


        //create an ACK message with the last ack number and send it
        SaWAckMsg ackMsg = new SaWAckMsg();
        ackMsg.create(lastAckNr);
        slp.send(new String(ackMsg.getDataBytes()), slpConfig);

        //receive again
        return this.receive();
    }



}
