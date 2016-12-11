package org.sdnhub.odl.tutorial.tapapp.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.event.triggered.packet.type.IcmpPacketType;

public class Utilities {
	public static String createEdge(String letfNode, String rightNode) {
		int letf = Integer.parseInt(letfNode.replaceAll("\\s+",""));
		int right = Integer.parseInt(rightNode.replaceAll("\\s+",""));
		String edge;

		if (letf <= right) {
			edge = letf + ":" + right;
		} else {
			edge = right + ":" + letf;
		}
		return edge;
	}
	
    public static Vector<Integer> nodesFromEdge(String edge) {
        String[] nodes = edge.split(":");
        int start = Integer.parseInt(nodes[0].replaceAll("\\s+",""));
        int end = Integer.parseInt(nodes[1].replaceAll("\\s+",""));

        Vector<Integer> nodesFromEdge = new Vector<Integer>(2);
        nodesFromEdge.addElement(new Integer(start));
        nodesFromEdge.addElement(new Integer(end));

        return nodesFromEdge;
    }
    
    public static Vector<String> getSrcDestIpAddress(IcmpPacketType icmpPacket) {
    	
    	String srcIpAddress;
		String dstIpAddress;

		if (icmpPacket.getType() == (short) 0x08) {
			srcIpAddress = icmpPacket.getSourceAddress();
			dstIpAddress = icmpPacket.getDestinationAddress();
			
		} else {
			dstIpAddress = icmpPacket.getSourceAddress();
			srcIpAddress = icmpPacket.getDestinationAddress();
		}
    	
        Vector<String> srcDestIpAddress = new Vector<String>(2);
        srcDestIpAddress.addElement(srcIpAddress);
        srcDestIpAddress.addElement(dstIpAddress);

        return srcDestIpAddress;
    }

	public static ArrayList<Integer> getSwitchInterface(ArrayList<Path> graphActiveTraffic, ArrayList<String> betweennessCentrality, boolean isHighToLow) {
		ArrayList<Integer> switchInterfaceList = new ArrayList<Integer>();
		ArrayList<Integer> orderedNodeListInInteger;
		ArrayList<String> unorderedEdgeListInString;
		
		Vector<Integer> nodesFromEdge;
		int startNodeWins;
		int endNodeWins;
		Integer start = null;
		Integer end = null;
		
		for (String edge : betweennessCentrality) {
			startNodeWins = 0;
			endNodeWins = 0;
			nodesFromEdge = Utilities.nodesFromEdge(edge);
			start = nodesFromEdge.firstElement();
			end = nodesFromEdge.lastElement();
			
			for (Path path : graphActiveTraffic) {
				orderedNodeListInInteger = path.getOrderedNodeListInInteger();
				unorderedEdgeListInString = path.getUnorderedEdgeListInString();
				
				if (unorderedEdgeListInString.contains(edge)) {
					if (orderedNodeListInInteger.indexOf(start) < orderedNodeListInInteger.indexOf(end)) {
						startNodeWins++;
					} else {
						endNodeWins++;
					}
				}
			}
			
			if (isHighToLow) {
				if (startNodeWins > endNodeWins) {
					switchInterfaceList.add(start);
				} else {
					switchInterfaceList.add(end);
				}
			} else {
				if (startNodeWins < endNodeWins) {
					switchInterfaceList.add(start);
				} else {
					switchInterfaceList.add(end);
				}
			}
		}
		
		return switchInterfaceList;
	}
	
//	public static boolean equalLists(List<String> one, List<String> two){     
//	    if (one == null && two == null){
//	        return true;
//	    }
//
//	    if((one == null && two != null) 
//	      || one != null && two == null
//	      || one.size() != two.size()){
//	        return false;
//	    }
//
//	    //to avoid messing the order of the lists we will use a copy
//	    one = new ArrayList<String>(one); 
//	    two = new ArrayList<String>(two);   
//
//	    Collections.sort(one);
//	    Collections.sort(two);      
//	    return one.equals(two);
//	}
	
	public static boolean equalLists(List<Integer> one, List<Integer> two){     
	    if (one == null && two == null){
	        return true;
	    }

	    if((one == null && two != null) 
	      || one != null && two == null
	      || one.size() != two.size()){
	        return false;
	    }

	    //to avoid messing the order of the lists we will use a copy
	    one = new ArrayList<Integer>(one); 
	    two = new ArrayList<Integer>(two);   

	    Collections.sort(one);
	    Collections.sort(two);      
	    return one.equals(two);
	}
	
	
	public static boolean isPathContainInGraph(Path givenPath, ArrayList<Path> graphActiveTraffic){     
	    for (Path path : graphActiveTraffic) {
			return Utilities.equalLists(path.getOrderedNodeListInInteger(), givenPath.getOrderedNodeListInInteger());
		}
	    return false;
	}
	
	public static void removePath(Path givenPath, ArrayList<Path> graphActiveTraffic) {
		graphActiveTraffic.remove(givenPath);
	}
	
}
