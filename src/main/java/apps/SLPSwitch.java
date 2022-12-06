package apps;

import java.io.IOException;

import phy.PhyProtocol;
import slp.*;
import exceptions.*;

public class SLPSwitch {

	protected static final int SWITCHPORT = 3027;

	public static void main(String[] args) throws IWProtocolException, IOException {
		/* Task 4: Setup protocol stack and call storeAndForward() method of SLPProtocol */
		PhyProtocol phy = new PhyProtocol(4000);
		SLPProtocol protocolStack = new SLPProtocol(SWITCHPORT,true, phy);
		protocolStack.storeAndForward();

	}
}
