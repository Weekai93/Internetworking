package phy;

import java.net.InetAddress;
import java.net.UnknownHostException;

import core.Configuration;

public class PhyConfiguration extends Configuration{
	protected int remotePort;
	protected InetAddress remoteIPAddress;
	protected boolean isClient;
	
	public PhyConfiguration(InetAddress rip, int rp) throws UnknownHostException {
		super(null);
		this.remotePort = rp;
		this.remoteIPAddress = rip;
		this.isClient = true;
	}

	public int getRemotePort() {
		return this.remotePort;
	}
	
	public InetAddress getRemoteIPAddress () {
		return this.remoteIPAddress;
	}
}
