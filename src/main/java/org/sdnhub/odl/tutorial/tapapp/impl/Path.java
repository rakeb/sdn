package org.sdnhub.odl.tutorial.tapapp.impl;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

public class Path {
	private String start;
	private String end;
	private ArrayList<Integer> orderedNodeListInInteger;
	private ArrayList<String> unorderedEdgeListInString;

	public Path(String start, String end) {
		this.start = start;
		this.end = end;
		orderedNodeListInInteger = new ArrayList<Integer>();
		unorderedEdgeListInString = new ArrayList<String>();
	}

	public void genOrderedNodeListInInteger(Stack<String> path) {
		for (String node : path) {
			orderedNodeListInInteger.add(Integer.parseInt(node));
		}
	}
	
	/**
	 * Takes path as string like 6:5, 5:3 or 6:5, 3:5
	 * @param path
	 */
	public void genOrderedNodeListInInteger(ArrayList<String> path) {
		
		boolean isFirst = true;
		for(String edge : path) {
			String[] nodeList = edge.replaceAll("\\s+","").split(":");
    		int left = Integer.parseInt(nodeList[0]);
    		int right = Integer.parseInt(nodeList[1]);
    		
    		if (isFirst) {
    			isFirst = false;
    			if (Integer.parseInt(this.start.replaceAll("\\s+","")) == left) {
        			orderedNodeListInInteger.add(left);
        		} else {
        			orderedNodeListInInteger.add(right);
    			}
			}
    		
    		if (!orderedNodeListInInteger.contains(left)) {
    			orderedNodeListInInteger.add(left);
			}
    		
    		if (!orderedNodeListInInteger.contains(right)) {
    			orderedNodeListInInteger.add(right);
			}
		}
	}

	/**
	 * Generates unordered edge list as String and stores into unorderedEdgeListInString
	 * @param orderedPath
	 */
	public void genUnorderedEdgeListInString(Stack<String> orderedPath) {
		
		Stack<String> nodesInStack = new Stack<String>();
		String nodeInStack;
		String edge;

		for (String node : orderedPath) {
			try {
				nodeInStack = nodesInStack.pop();
				edge = Utilities.createEdge(nodeInStack, node);
				unorderedEdgeListInString.add(edge);
				nodesInStack.push(node);
			} catch (EmptyStackException e) {
				nodesInStack.push(node);
			}
		}
	}

	
	
	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public ArrayList<Integer> getOrderedNodeListInInteger() {
		return orderedNodeListInInteger;
	}

	public void setOrderedNodeListInInteger(
			ArrayList<Integer> orderedNodeListInInteger) {
		this.orderedNodeListInInteger = orderedNodeListInInteger;
	}

	public ArrayList<String> getUnorderedEdgeListInString() {
		return unorderedEdgeListInString;
	}

	public void setUnorderedEdgeListInString(
			ArrayList<String> unorderedEdgeListInString) {
		this.unorderedEdgeListInString = unorderedEdgeListInString;
	}

	@Override
	public String toString() {
		return "[start=" + start + ", end=" + end
				+ ", orderedNodeListInInteger=" + orderedNodeListInInteger
				+ ", unorderedEdgeListInString=" + unorderedEdgeListInString
				+ "]";
	}
	
	

}
