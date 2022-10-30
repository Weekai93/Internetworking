package phy;

import apps.SimplexPhyServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class PhyMsgTest {
    @Test
    @DisplayName("Phy message creation test")
    void createTest() {
        PhyConfiguration config;
        PhyMsg msg;
        try {
            config = new PhyConfiguration(InetAddress.getByName("localhost"), 1000);
            msg = new PhyMsg(config);
            msg.create("Hello World");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        assertEquals("phy Hello World", new String(msg.getDataBytes()));
    }
}
