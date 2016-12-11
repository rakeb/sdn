package org.sdnhub.odl.tutorial.tapapp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetNetworkTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.getnetworktopology.output.NetworkLinks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class NetworkGraph {

	UndirectedGraph<String, DefaultEdge> networkTopology;
	HashMap<String, LinkInfo> links = new HashMap<String, LinkInfo>();
	NeighborIndex<String, DefaultEdge> neighborGraph;
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	// -------------- All paths -----------------//
	int totalPath;
	private Stack<String> path; // the current path
	private Set<String> onPath; // the set of vertices on the path
	public ArrayList<Path> allPaths; // Stores all possible paths
	private String src;
	private String dst;

	// ~ Constructors ———————————————————
	public NetworkGraph() {
		networkTopology = new SimpleGraph<String, DefaultEdge>(
				DefaultEdge.class);
	}

	public NetworkGraph(GetNetworkTopologyOutput topologyOutput) {
		networkTopology = new SimpleGraph<String, DefaultEdge>(
				DefaultEdge.class);
		for (NetworkLinks link : topologyOutput.getNetworkLinks()) {
			int leftSwitch = Integer.parseInt(link.getSrcNode().getValue()
					.split(":")[1]);
			int rightSwitch = Integer.parseInt(link.getDstNode().getValue()
					.split(":")[1]);
			int leftSwitchPortNumber = Integer.parseInt(link
					.getSrcNodeConnector().getValue().split(":")[2]);
			int rightSwitchPortNumber = Integer.parseInt(link
					.getDstNodeConnector().getValue().split(":")[2]);

			addLinkInfo(leftSwitch, rightSwitch, leftSwitchPortNumber,
					rightSwitchPortNumber);
		}
		neighborGraph = new NeighborIndex<String, DefaultEdge>(networkTopology);
	}

	// ~ Methods ———————————————————
	public void addLinkInfo(int leftSwitch, int rightSwitch,
			int leftSwitchPortNumber, int rightSwitchPortNumber) {
		if (links.containsKey(Integer.toString(leftSwitch) + ":"
				+ Integer.toString(rightSwitch)) == false) {
			LinkInfo link1 = new LinkInfo(leftSwitch, rightSwitch,
					leftSwitchPortNumber, rightSwitchPortNumber);
			LinkInfo link2 = new LinkInfo(rightSwitch, leftSwitch,
					rightSwitchPortNumber, leftSwitchPortNumber);

			links.put((Integer.toString(leftSwitch) + ":" + Integer
					.toString(rightSwitch)), link1);
			links.put((Integer.toString(rightSwitch) + ":" + Integer
					.toString(leftSwitch)), link2);

			if (networkTopology.containsVertex(Integer.toString(leftSwitch)) == false) {
				networkTopology.addVertex(Integer.toString(leftSwitch));
			}
			if (networkTopology.containsVertex(Integer.toString(rightSwitch)) == false) {
				networkTopology.addVertex(Integer.toString(rightSwitch));
			}
			networkTopology.addEdge(Integer.toString(leftSwitch),
					Integer.toString(rightSwitch));
		}
	}

	public LinkInfo findLink(int leftSwitch, int rightSwitch) {
		if (links.containsKey(Integer.toString(leftSwitch) + ":"
				+ Integer.toString(rightSwitch)) == true) {
			return links.get(Integer.toString(leftSwitch) + ":"
					+ Integer.toString(rightSwitch));
		}
		return null;
	}

	public List<String> findShortestPath(int leftSwitch, int rightSwitch) {
		List<DefaultEdge> path = DijkstraShortestPath.findPathBetween(
				networkTopology, Integer.toString(leftSwitch),
				Integer.toString(rightSwitch));
		List<String> networkPath = Lists.newArrayList();
		for (Object node : path) {
			DefaultEdge dEdge = (DefaultEdge) node;
			String link = dEdge.toString();
			networkPath.add(link);
		}
		return networkPath;
	}

	public int findPortID(int leftSwitch, int rightSwitch) {
		LinkInfo link = findLink(leftSwitch, rightSwitch);
		if (link != null) {
			return link.leftSwitchPortNumber;
		}
		return -1;
	}

	public List<String> findNeighbors(int switchId) {
		if (networkTopology.containsVertex(Integer.toString(switchId)) == false) {
			return null;
		} else {
			return this.neighborGraph
					.neighborListOf(Integer.toString(switchId));
		}
	}

	// -------------- All paths --------------//

	/**
	 * Generates all possible paths between s and t. Sets the global allPaths which is a list of Path objects. Algorithm taken from
	 * http://introcs.cs.princeton.edu/java/45graph/AllPaths.java.html
	 * 
	 * @param s
	 * @param t
	 */
	public void getAllPaths(String s, String t) {
		src = s;
		dst = t;
		enumerate(s, t, "", "", Integer.MAX_VALUE);
	}
	
	/**
	 * Generates all possible paths between s and t. Sets the global allPaths which is a list of Path objects. Algorithm taken from
	 * http://introcs.cs.princeton.edu/java/45graph/AllPaths.java.html
	 * @param s	source
	 * @param t	destination
	 * @param willNotContain	an edge which will not contain in the path
	 * @param howMany	how many path user wants to create
	 */
	public void getAllPaths(String s, String t, String willNotContain, int howMany) {
		src = s;
		dst = t;
		path = new Stack<String>();
		onPath = new HashSet<String>();
		allPaths = new ArrayList<Path>();
		totalPath = 0;
		String[] nodes = willNotContain.split(":");
		String startNotContain = nodes[0].replaceAll("\\s+",""); 
		String endNotContain = nodes[1].replaceAll("\\s+","");
		enumerate(s, t, startNotContain, endNotContain, howMany);
	}

	/**
	 * DFS to find all paths between v and t
	 * 
	 * @param v
	 * @param t
	 */
	private void enumerate(String v, String t, String startNotContain, String endNotContain, int howMany) {
		if (howMany == totalPath) {
			return;
		}
		// add node v to current path from s
		path.push(v);
		onPath.add(v);

		// found path from s to t - currently prints in reverse order because of
		// stack
		if (v.equals(t)) {
			addPath();
		}

		// consider all neighbors that would continue path with repeating a node
		else {
			for (String w : neighborGraph.neighborListOf(v)) {
				if (!onPath.contains(w) && !w.equals(startNotContain) && !w.equals(endNotContain))
					enumerate(w, t, startNotContain, endNotContain, howMany);
			}
		}

		// done exploring from v, so remove from path
		path.pop();
		onPath.remove(v);
	}

	private void addPath() {
		Path pathObject = new Path(src, dst);
		pathObject.genOrderedNodeListInInteger(path);
		pathObject.genUnorderedEdgeListInString(path);
		allPaths.add(pathObject);
		totalPath++;
	}

	public Path dijkstraShortestPath(String leftSwitch, String rightSwitch) {
		
		Path pathObject = new Path(leftSwitch, rightSwitch);
		ArrayList<String> shortestPath = new ArrayList<String>();
		
		if (leftSwitch.equals(rightSwitch)) {
			String edge = Utilities.createEdge(leftSwitch, rightSwitch);
			shortestPath.add(edge);
			pathObject.setUnorderedEdgeListInString(shortestPath);
			pathObject.genOrderedNodeListInInteger(shortestPath);
			return pathObject;
		}
		
		List<DefaultEdge> path = DijkstraShortestPath.findPathBetween( networkTopology, leftSwitch, rightSwitch);

		for (Object node : path) {
			DefaultEdge dEdge = (DefaultEdge) node;
			String link = dEdge.toString();
			String leftSwitchId = link.substring(link.indexOf("(") + 1,
					link.indexOf(":"));
			String rightSwitchId = link.substring(link.indexOf(":") + 1,
					link.indexOf(")"));

			String edge = Utilities.createEdge(leftSwitchId, rightSwitchId);
			shortestPath.add(edge);
		}

		
		pathObject.setUnorderedEdgeListInString(shortestPath);
		pathObject.genOrderedNodeListInInteger(shortestPath);
		
		return pathObject;
	}
}
