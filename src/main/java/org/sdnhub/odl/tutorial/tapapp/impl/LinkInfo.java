package org.sdnhub.odl.tutorial.tapapp.impl;

public class LinkInfo {
	public int leftSwitch;
	public int rightSwitch;
	public int leftSwitchPortNumber;
	public int rightSwitchPortNumber;
	
	LinkInfo (int leftSwitch, int rightSwitch, int leftSwitchPortNumber, int rightSwitchPortNumber){
		this.leftSwitch = leftSwitch;
		this.rightSwitch = rightSwitch;
		this.leftSwitchPortNumber =leftSwitchPortNumber;
		this.rightSwitchPortNumber = rightSwitchPortNumber;
	}
}
