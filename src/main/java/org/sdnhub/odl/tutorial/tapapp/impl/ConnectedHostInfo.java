package org.sdnhub.odl.tutorial.tapapp.impl;

public class ConnectedHostInfo {
	public String hostMac;
	public String hostIP;
	public int switchConnectedTo;
	public int portConnectedTo;
	
	ConnectedHostInfo(String hostMac, String hostIP, 
			int switchConnectedTo, int portConnectedTo){
		this.hostIP = hostIP;
		this.hostMac = hostMac;
		this.switchConnectedTo = switchConnectedTo;
		this.portConnectedTo = portConnectedTo;
	}
	
	public String getHostMac(){
		return this.hostMac;
	}
	public String getHostIP(){
		return this.hostIP;
	}
	public int getSwitchConnectedTo(){
		return this.switchConnectedTo;
	}
	public int getPortConnectedTo(){
		return this.portConnectedTo;
	}
	
}