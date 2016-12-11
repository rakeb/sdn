package org.sdnhub.odl.tutorial.tapapp.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.ActivesdnListener;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.ActivesdnService;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.ConstructTopology;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.EventSpecs.EventAction;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.EventTriggered;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.EventTriggered.TriggeredEventType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllFlowRulesFromASwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllFlowRulesFromASwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallNetworkPathInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallNetworkPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.MigrateNetworkPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.NewHostFound;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveAFlowRuleFromSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveEventFromSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SendPacketOutInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SubscribeEventInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SubscribeEventOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.event.triggered.packet.type.IcmpPacketType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.event.triggered.packet.type.Ipv4PacketType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.get.all.flow.rules.from.a._switch.output.FlowRules;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetNetworkTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TapService;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TrafficType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveSDNAssignment implements ActivesdnListener{
	
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	private final static String FLOOD = "FLOOD";
    private final static String DROP = "DROP";
    private final static String CONTROLLER = "CONTROLLER";
    private DataBroker dataBroker;
    private ActivesdnService activeSDNService;
    private TapService tapService;
    private final AtomicLong eventID = new AtomicLong();
    private HashMap<String, ConnectedHostInfo> hostTable = new HashMap<String, ConnectedHostInfo>();
    private HashMap<Integer, List<FlowRules>> networkConfiguration = new HashMap<Integer, List<FlowRules>>();
    private ArrayList<String> betweennessCentrality;
    
    private boolean firstTime = true;
    private NetworkGraph topology;
    
    private ArrayList<ArrayList<String>> criticalLinks =  new ArrayList<ArrayList<String>>();
    private ArrayList<Path> graphActiveTraffic =  new ArrayList<Path>();
    ArrayList<Integer> switchInterfaceList;
    private Path currentPath;
	private Future<RpcResult<SubscribeEventOutput>> subscribeEventOutput;
	private boolean isReactiveDevense = false;
	private final String srcIpReactiveDevense = "10.0.0.6/32";
	private final String dstIpReactiveDevense = "10.0.0.11/32";
	private String specificLinkReactiveDevense;
    
	public ActiveSDNAssignment(DataBroker dataBroker, NotificationProviderService notificationService, RpcProviderRegistry rpcProviderRegistry) {
		//Store the data broker for reading/writing from inventory store
        this.dataBroker = dataBroker;
        //Object used for flow programming through RPC calls
        this.activeSDNService = rpcProviderRegistry.getRpcService(ActivesdnService.class);
        this.tapService = rpcProviderRegistry.getRpcService(TapService.class);
        notificationService.registerNotificationListener(this);
	}

	@Override
	public void onNewHostFound(NewHostFound notification) {
		ConnectedHostInfo connectedHost = new ConnectedHostInfo(
				notification.getHostMacAddress(), notification.getHostIpAddress(), 
				notification.getSwitchConnectedTo(), notification.getPortConnectedTo());
		hostTable.put(notification.getHostIpAddress(), connectedHost);
	}
	@Override
	public void onConstructTopology(ConstructTopology notification) {
		try {
			Future<RpcResult<GetNetworkTopologyOutput>> topologyFutureOutput = tapService.getNetworkTopology();
			if (topologyFutureOutput != null){
				GetNetworkTopologyOutput topologyOutput = topologyFutureOutput.get().getResult();
				topology = new NetworkGraph(topologyOutput);
			}
		} catch (Exception e){
			LOG.debug("Exception reached." , e);
		}
	}
	
	private void getSwitchFlowTable (int switchId) {
		GetAllFlowRulesFromASwitchInputBuilder getFlowsInputBuilder = new GetAllFlowRulesFromASwitchInputBuilder();
		getFlowsInputBuilder.setSwitchId(switchId);
	
		try {
			Future<RpcResult<GetAllFlowRulesFromASwitchOutput>> flowsOutputFuture = 
					this.activeSDNService.getAllFlowRulesFromASwitch(getFlowsInputBuilder.build());
			if (flowsOutputFuture != null){
				GetAllFlowRulesFromASwitchOutput flowsOutput = flowsOutputFuture.get().getResult();
				if (networkConfiguration.containsKey(switchId)){
					networkConfiguration.remove(switchId);
					networkConfiguration.put(switchId, flowsOutput.getFlowRules());
				} else {
					networkConfiguration.put(switchId,  flowsOutput.getFlowRules());
				}
			}
		} catch (Exception e){
			LOG.error("Exception reached in getting switch flow table {} ", e);
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////--------------------Students should write code below this ---------------------------------------////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Find and stores all possible critical links from the current graphs
	 * @return betweennessCentrality
	 */
	public ArrayList<String> findCriticalLinks() {
		Integer nodePresent;
		HashMap<String, Integer> centrality = new HashMap<String, Integer>();
		ArrayList<String> betweennessCentrality = new ArrayList<String>();
		ArrayList<String> unorderedEdgeListInString;
		for (Path path : graphActiveTraffic) {
			unorderedEdgeListInString = path.getUnorderedEdgeListInString();
			for (String node : unorderedEdgeListInString) {
				nodePresent = centrality.get(node);
				if (nodePresent != null) {
					centrality.put(node, nodePresent + 1);
				} else {
					centrality.put(node, 1);
				}
			}
		}
		
		int maxValueInMap = Collections.max(centrality.values());  // This will return max value in the Hashmap
        for (Entry<String, Integer> entry : centrality.entrySet()) {  // Iterate through hashmap
            if (entry.getValue()== maxValueInMap) {
                betweennessCentrality.add(entry.getKey());
            }
        }
        
        LOG.debug("     ==================================================================     ");
		LOG.debug("     		Critical Links {}", betweennessCentrality);
		LOG.debug("     ==================================================================     ");
        return betweennessCentrality;
	}
	
	
	/**
	 * Install network path
	 * @param icmpPacket
	 * @param priority
	 * @return
	 */
	public Path installNetworkPath(IcmpPacketType icmpPacket, int priority) {
		Vector<String> ipAddress = Utilities.getSrcDestIpAddress(icmpPacket);
		String srcIpAddress = ipAddress.firstElement();
		String dstIpAddress = ipAddress.lastElement();
		int srcSwitchNumber = hostTable.get(srcIpAddress).getSwitchConnectedTo();
		int dstSwitchNumber = hostTable.get(dstIpAddress).getSwitchConnectedTo();
		String srcSwitch = Integer.toString(srcSwitchNumber);
		String dstSwitch = Integer.toString(dstSwitchNumber);
		
		Path path = topology.dijkstraShortestPath(srcSwitch, dstSwitch);
		
		List<Integer> orderedNodeListInInteger = path.getOrderedNodeListInInteger();
		
		LOG.debug("     ==================================================================     ");
		LOG.debug("     		Dijkstra Shortest Ordered Nodes {}", orderedNodeListInInteger);
		LOG.debug("     ==================================================================     ");
		
		LOG.debug("     ==================================================================     ");
		LOG.debug("     		Dijkstra Shortest Unordered Edges {}", path.getUnorderedEdgeListInString());
		LOG.debug("     ==================================================================     ");
		
		InstallNetworkPathInput installNetworkPathInput = new InstallNetworkPathInputBuilder()
																.setSrcIpAddress(srcIpAddress)
																.setDstIpAddress(dstIpAddress)
																.setFlowPriority(priority)
																.setSwitchesInPath(orderedNodeListInInteger)
																.build();
		
		activeSDNService.installNetworkPath(installNetworkPathInput);
		
		return path; 
	}
	
	
	/**
	 * Subscribe and event
	 * @param icmpPacket
	 * @return 
	 */
	public Future<RpcResult<SubscribeEventOutput>> subscribeEvent(IcmpPacketType icmpPacket) {
		
		SubscribeEventInputBuilder eventInputBuilder = new SubscribeEventInputBuilder();
		
		if (isReactiveDevense) {
			int switchId = Integer.parseInt(specificLinkReactiveDevense.split(":")[0].replaceAll("\\s+",""));
			LOG.debug("     ==================================================================     ");
			LOG.debug("     		Event subscription started for Reactive Defense in switch: {}", switchId);
			LOG.debug("     ==================================================================     ");
			
			eventInputBuilder.setCount((long)1);
			eventInputBuilder.setSrcIpAddress(Utilities.getSrcDestIpAddress(icmpPacket).firstElement());
			eventInputBuilder.setDstIpAddress(Utilities.getSrcDestIpAddress(icmpPacket).lastElement());
			eventInputBuilder.setDuration((long)2);
			eventInputBuilder.setSwitchId(switchId);
			eventInputBuilder.setTrafficProtocol(TrafficType.ICMP);
			eventInputBuilder.setEventAction(EventAction.DROPANDNOTIFY);
			eventInputBuilder.setHoldNotification(0);
			
		} else {
			
			LOG.debug("     ==================================================================     ");
			LOG.debug("     		Event subscription started for Switch Number: {}", switchInterfaceList.get(0));
			LOG.debug("     ==================================================================     ");
			
			eventInputBuilder.setCount((long)10);
			eventInputBuilder.setSrcIpAddress(Utilities.getSrcDestIpAddress(icmpPacket).firstElement());
			eventInputBuilder.setDstIpAddress(Utilities.getSrcDestIpAddress(icmpPacket).lastElement());
			eventInputBuilder.setDuration((long)20);
			eventInputBuilder.setSwitchId(switchInterfaceList.get(0));
			eventInputBuilder.setTrafficProtocol(TrafficType.ICMP);
			eventInputBuilder.setEventAction(EventAction.DROPANDNOTIFY);
			eventInputBuilder.setHoldNotification(5);
		}
		
		
		
		return this.activeSDNService.subscribeEvent(eventInputBuilder.build());
	}
	
	/**
	 * Packet out
	 * @param notification
	 */
	public void packetOut(EventTriggered notification) {
		IcmpPacketType icmpPacket = (IcmpPacketType) notification.getPacketType();
		
		int firstSwitchId = currentPath.getOrderedNodeListInInteger().get(0);
		int secondSwitchId;
		int outputPort;
		
		if (currentPath.getOrderedNodeListInInteger().size() == 1) {
			outputPort = hostTable.get(Utilities.getSrcDestIpAddress(icmpPacket).lastElement()).getPortConnectedTo();
		} else {
			secondSwitchId = currentPath.getOrderedNodeListInInteger().get(1);
			outputPort = topology.findPortID(firstSwitchId, secondSwitchId);
		}
		
		SendPacketOutInputBuilder packetOutBuilder = new SendPacketOutInputBuilder();
		packetOutBuilder.setSwitchId(firstSwitchId);
		packetOutBuilder.setInPortNumber(notification.getInPortNumber());
		packetOutBuilder.setPayload(notification.getPayload()); //This sets the payload as received during PacketIn
		packetOutBuilder.setOutputPort(Integer.toString(outputPort)); 
	
		LOG.debug("     ==================================================================     ");
		LOG.debug("     		Packet Out is called for switch: {}, inPort: {}, outPort: {}", firstSwitchId, notification.getInPortNumber(), outputPort);
		LOG.debug("     ==================================================================     ");
		
		this.activeSDNService.sendPacketOut(packetOutBuilder.build());
	}
	
	/**
	 * Removes the last subscribed event event
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void removeEvent() throws InterruptedException, ExecutionException, NullPointerException {
		int switchId = switchInterfaceList.get(0);
		LOG.debug("     ==================================================================     ");
		LOG.debug("     		Remove Event called for Switch Number: {}", switchId);
		LOG.debug("     ==================================================================     ");
		
		//--------- to print all the flow rules --------//
		getSwitchFlowTable(switchId);
		LOG.debug("		======================= BEFORE Event Removal ================================     ");
		StringBuilder allFlowRule = new StringBuilder();
		List<FlowRules> flowrules = networkConfiguration.get(switchId);
		for (FlowRules flowRule: flowrules) {
			allFlowRule.append(flowRule.getFlowId());
		}
		LOG.debug("     		Total FlowRules {}", flowrules.size());
		LOG.debug("     		All FlowRule {}", allFlowRule.toString());
		LOG.debug("     ==================================================================     ");
		//--------- to print all the flow rules --------//
		
		
		
		RpcResult<SubscribeEventOutput> output = subscribeEventOutput.get();

		RemoveEventFromSwitchInputBuilder removeEvent = new RemoveEventFromSwitchInputBuilder();
		removeEvent.setEventId(output.getResult().getEventId());
		removeEvent.setSwitchId(switchInterfaceList.get(0));
		
		this.activeSDNService.removeEventFromSwitch(removeEvent.build());
		
		//--------- to print all the flow rules --------//
		getSwitchFlowTable(switchId);
		LOG.debug("		======================= AFTER Event Removal ================================     ");
		allFlowRule = new StringBuilder();
		flowrules = networkConfiguration.get(switchId);
		for (FlowRules flowRule: flowrules) {
			allFlowRule.append(flowRule.getFlowId());
		}
		LOG.debug("     		Total FlowRules {}", flowrules.size());
		LOG.debug("     		All FlowRule {}", allFlowRule.toString());
		LOG.debug("     ==================================================================     ");
		//--------- to print all the flow rules --------//
	}
	
	
	/**
	 * This will log the current graph
	 */
	private void logGraph() {
		LOG.debug("		==================================================================     ");
		StringBuilder logGraph = new StringBuilder(); 
		for (Path pathLogger: graphActiveTraffic) {
			 logGraph.append(pathLogger.toString());
		}
		LOG.debug("     		Graph {}", logGraph.toString());
		LOG.debug("     ==================================================================     ");
	}
	
	private void proactiveDefense(IcmpPacketType icmpPacket) {
		logGraph();
		betweennessCentrality = findCriticalLinks();
		switchInterfaceList = Utilities.getSwitchInterface(graphActiveTraffic, betweennessCentrality, false);
		LOG.debug("     ==================================================================     ");
		LOG.debug("     		Switch Interface {}", switchInterfaceList);
		LOG.debug("     ==================================================================     ");
		
		//Subscribe Drop Event
		if (switchInterfaceList.size() == 1 && currentPath.getOrderedNodeListInInteger().size() > 1) {
			subscribeEventOutput = subscribeEvent(icmpPacket);
		}
	}
	
	
	private void reactiveDefense(IcmpPacketType icmpPacket) {
		logGraph();
		//Subscribe Drop Event
		subscribeEvent(icmpPacket);
	}
	
	
	private void findIfReactiveDefense(IcmpPacketType icmpPacket) {
		String srcIpAddress = icmpPacket.getSourceAddress();
		String dstIpAddress = icmpPacket.getDestinationAddress();
		
		for (String edge: currentPath.getUnorderedEdgeListInString()) {
			specificLinkReactiveDevense = edge;
			break;
		}
		
		if ((srcIpAddress.equals(srcIpReactiveDevense) && dstIpAddress.equals(dstIpReactiveDevense)
						|| (srcIpAddress.equals(dstIpReactiveDevense) && dstIpAddress.equals(srcIpReactiveDevense)))) {
			isReactiveDevense = true;
			
			LOG.debug("     ==================================================================     ");
			LOG.debug("     		Reactive Defense will be started");
			LOG.debug("     ==================================================================     ");
			
		} else {
			isReactiveDevense = false;
			specificLinkReactiveDevense = null;
		}
	}
	
	
	/**
	 * Main functionality starts from here
	 */
	@Override
	public void onEventTriggered(EventTriggered notification) {
		LOG.debug("     ==================================================================     ");
		LOG.debug("                    Event Triggered is called.");
		LOG.debug("      ==================================================================     ");
		
		if (notification.getPacketType() instanceof Ipv4PacketType) {
			Ipv4PacketType ipv4Packet = (Ipv4PacketType) notification.getPacketType();
			if (!(hostTable.containsKey(ipv4Packet.getSourceAddress()) && 
					hostTable.containsKey(ipv4Packet.getDestinationAddress()))){
				LOG.debug("     ==================================================================     ");
				LOG.debug("Some host information is missing so skipping rest of the event trigger.");
				LOG.debug("      ==================================================================     ");
				return;
			}
		}
		else if (notification.getPacketType() instanceof IcmpPacketType) {
			IcmpPacketType icmpPacket = (IcmpPacketType) notification.getPacketType();
			if (!(hostTable.containsKey(icmpPacket.getSourceAddress()) && 
					hostTable.containsKey(icmpPacket.getDestinationAddress()))){
				LOG.debug("     ==================================================================     ");
				LOG.debug("Some host information is missing so skipping rest of the event trigger.");
				LOG.debug("      ==================================================================     ");
				return;
			}
		}
		//============================================================================================
		if (notification.getTriggeredEventType() == TriggeredEventType.NoFlowRuleEvent) {
			//If conditions checks if the Event is triggered because the switch couldn't find any flow rule
			if (notification.getPacketType() instanceof Ipv4PacketType) {
				//This condition checks if the the Packet received in the notification is of an IP Version 4 Packet
				//you can get IP packet fields by using .get methods, e.g., ipv4Packet.getDestinationAddress()
				Ipv4PacketType ipv4Packet = (Ipv4PacketType) notification.getPacketType();
	
			}
			else if (notification.getPacketType() instanceof IcmpPacketType) {
				//This condition checks if the the Packet received in the notification is of an ICMP Packet
				//you can get ICMP packet fields by using .get methods, e.g., icmpPacket.getCrc()
				IcmpPacketType icmpPacket = (IcmpPacketType) notification.getPacketType();
				
			}
		} 
		else if (notification.getTriggeredEventType() == TriggeredEventType.ControllerFlowRuleEvent) {
			//If conditions checks if the Event is triggered because of a flow rule that 
			//explicitly forwards the packet to the controller. You can get the flow rule id from notification.getEventId()
			if (notification.getPacketType() instanceof Ipv4PacketType) {
				Ipv4PacketType ipv4Packet = (Ipv4PacketType) notification.getPacketType();
				
			}
			else if (notification.getPacketType() instanceof IcmpPacketType) {
				IcmpPacketType icmpPacket = (IcmpPacketType) notification.getPacketType();
				
				LOG.debug("     ==================================================================     ");
				LOG.debug("     		Src Host {}, Dest Host {}", icmpPacket.getSourceAddress(), icmpPacket.getDestinationAddress());
				LOG.debug("     ==================================================================     ");
				
				currentPath = installNetworkPath(icmpPacket, 200);
				if (!Utilities.isPathContainInGraph(currentPath, graphActiveTraffic)) {
					graphActiveTraffic.add(currentPath);
				}
				
				findIfReactiveDefense(icmpPacket);
				
				if (isReactiveDevense) {
					reactiveDefense(icmpPacket);
				} else {
					//pro-active defense
					proactiveDefense(icmpPacket);
				}
				
				//Packet out
				packetOut(notification);
			}
		}
		else if (notification.getTriggeredEventType() == TriggeredEventType.SubscribedEvent) {
			//If conditions checks if the Event is triggered because of a subscribed event is triggered 
			//you can find the event id from notification.getEventId()
			if (notification.getPacketType() instanceof Ipv4PacketType) {
				Ipv4PacketType ipv4Packet = (Ipv4PacketType) notification.getPacketType();
				
				LOG.debug("         ---------------------------------------------------------------------     ");
			 	LOG.debug("			Subscried Event is called");
			 	LOG.debug("         ---------------------------------------------------------------------     ");
			}
			else if (notification.getPacketType() instanceof IcmpPacketType) {

				IcmpPacketType icmpPacket = (IcmpPacketType) notification.getPacketType();
				findIfReactiveDefense(icmpPacket);
				
				if (isReactiveDevense) {
					LOG.debug("     ==================================================================     ");
					LOG.debug("			Reactive defense Subscried Event");
					LOG.debug("     ==================================================================     ");				
				 	
				 	LOG.debug("     ==================================================================     ");
					LOG.debug("     		Src Host {}, Dest Host {}", icmpPacket.getSourceAddress(), icmpPacket.getDestinationAddress());
					LOG.debug("     ==================================================================     ");
					
					
					topology.getAllPaths(currentPath.getStart(), currentPath.getEnd(), specificLinkReactiveDevense, 1);
					
					Path oldPath = currentPath;
					Path currentPath = topology.allPaths.get(0);
					
					LOG.debug("     ==================================================================     ");
					LOG.debug("     		Old Path {}", oldPath.toString());
					LOG.debug("     ==================================================================     ");
					
					LOG.debug("     ==================================================================     ");
					LOG.debug("     		New Path {}", currentPath.toString());
					LOG.debug("     ==================================================================     ");
				 	
					MigrateNetworkPathInputBuilder migratePathInputBuilder = new MigrateNetworkPathInputBuilder();
					migratePathInputBuilder.setSrcIpAddress(Utilities.getSrcDestIpAddress(icmpPacket).firstElement());
					migratePathInputBuilder.setDstIpAddress(Utilities.getSrcDestIpAddress(icmpPacket).lastElement());
					migratePathInputBuilder.setFlowPriority(301);
					
					migratePathInputBuilder.setSwitchesInOldPath(oldPath.getOrderedNodeListInInteger()); //list of switches along the new path
					
					migratePathInputBuilder.setSwitchesInNewPath(currentPath.getOrderedNodeListInInteger());
					this.activeSDNService.migrateNetworkPath(migratePathInputBuilder.build());
					
				} else {
					LOG.debug("     ==================================================================     ");
					LOG.debug("			Subscried Event triggered Migrate Path");
					LOG.debug("     ==================================================================     ");				
				 	
				 	LOG.debug("     ==================================================================     ");
					LOG.debug("     		Src Host {}, Dest Host {}", icmpPacket.getSourceAddress(), icmpPacket.getDestinationAddress());
					LOG.debug("     ==================================================================     ");
					
					
					topology.getAllPaths(currentPath.getStart(), currentPath.getEnd(), betweennessCentrality.get(0), 1);
					
					Path oldPath = currentPath;
					currentPath = topology.allPaths.get(0);
					
					Utilities.removePath(oldPath, graphActiveTraffic);
					graphActiveTraffic.add(currentPath);
					
					
					LOG.debug("     ==================================================================     ");
					LOG.debug("     		Old Path {}", oldPath.toString());
					LOG.debug("     ==================================================================     ");
					
					LOG.debug("     ==================================================================     ");
					LOG.debug("     		New Path {}", currentPath.toString());
					LOG.debug("     ==================================================================     ");
				 	
					MigrateNetworkPathInputBuilder migratePathInputBuilder = new MigrateNetworkPathInputBuilder();
					migratePathInputBuilder.setSrcIpAddress(Utilities.getSrcDestIpAddress(icmpPacket).firstElement());
					migratePathInputBuilder.setDstIpAddress(Utilities.getSrcDestIpAddress(icmpPacket).lastElement());
					migratePathInputBuilder.setFlowPriority(301);
					
					migratePathInputBuilder.setSwitchesInOldPath(oldPath.getOrderedNodeListInInteger()); //list of switches along the new path
					
					migratePathInputBuilder.setSwitchesInNewPath(currentPath.getOrderedNodeListInInteger());
					this.activeSDNService.migrateNetworkPath(migratePathInputBuilder.build());
				 	
					//currently there is a bug inside getSwitchFlowTable(switchId);
//				 	try {
//						removeEvent();
//					} catch (InterruptedException | ExecutionException | NullPointerException e) {
//						e.printStackTrace();
//					}
				 	
				 	//again run pro-active defense
				 	LOG.debug("         ---------------------------------------------------------------------     ");
				 	LOG.debug("			Calling proactive Defense again...");
				 	LOG.debug("         ---------------------------------------------------------------------     ");
				 	proactiveDefense(icmpPacket);
				}
				
				//Packet out
//				packetOut(notification);
			 	
			}
		}

		/*
		if (notification.getPacketType() instanceof ArpPacketType){
			//This condition checks if the the Packet received in the notification is of an ARP Packet
			//you can get ARP packet fields by using .get methods, e.g., arpPacket.getEthernetSrcMacAddress()
			ArpPacketType arpPacket = (ArpPacketType) notification.getPacketType();
		*/	
		
	}

	/*
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////------------------------------subscribeEvent() Function example  ----------------------------------------////
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 SubscribeEventInputBuilder eventInputBuilder = new SubscribeEventInputBuilder();
		eventInputBuilder.setSwitchId(1);
		
		eventInputBuilder.setDstMacAddress("00:00:00:00:00:02");
		eventInputBuilder.setSrcMacAddress("00:00:00:00:00:01");
		eventInputBuilder.setDstIpAddress("10.0.0.2/32");
		eventInputBuilder.setSrcIpAddress("10.0.0.1/32");
		eventInputBuilder.setTrafficProtocol(TrafficType.ICMP);
		//eventInputBuilder.setTrafficProtocol(TrafficType.HTTP);
		//eventInputBuilder.setTrafficProtocol(TrafficType.DNS);
		eventInputBuilder.setInPortId((long)1);
		
		eventInputBuilder.setCount((long)10);
		eventInputBuilder.setDuration((long)20);

		//eventInputBuilder.setEventAction(EventAction.DROP); //OR
		eventInputBuilder.setEventAction(EventAction.NOTIFY); //OR
		eventInputBuilder.setEventAction(EventAction.DROPANDNOTIFY);
		eventInputBuilder.setHoldNotification(5); //number of instances e.g., 5 pings
		//This value is used only for Drop & Notify case where if you want to 
		 * hold notification immediately after drop. You could use this to create a malicious behavior where sowe 
		 * switch started dropping packets and then you either migratePath or create a tunnel to escape
		 * Please check the example for clarification.
		
		this.activeSDNService.subscribeEvent(eventInputBuilder.build());
	 
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////------------------------------installFlowRule() Function example  ---------------------------------------////
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 InstallFlowRuleInputBuilder flowRuleInputBuilder = new InstallFlowRuleInputBuilder();
		
		flowRuleInputBuilder.setSwitchId(1);
		flowRuleInputBuilder.setInPortId((long)1);
		flowRuleInputBuilder.setSrcIpAddress("10.0.0.1/32");
		flowRuleInputBuilder.setDstIpAddress("10.0.0.2/32");
		flowRuleInputBuilder.setSrcMacAddress("00:00:00:00:00:01");
		flowRuleInputBuilder.setDstMacAddress("00:00:00:00:00:02");
		flowRuleInputBuilder.setTypeOfTraffic(TrafficType.ICMP); //this is basically the traffic protocol
		flowRuleInputBuilder.setTypeOfTraffic(TrafficType.HTTP); //and more types
		flowRuleInputBuilder.setSrcPort(10000); //Source Port
		flowRuleInputBuilder.setDstPort(10000); //Destination Port
		
		flowRuleInputBuilder.setFlowPriority(100);
		flowRuleInputBuilder.setIdleTimeout(300);
		flowRuleInputBuilder.setHardTimeout(1000);
		
		//You can perform multiple actions
		//This will change the destination IP address field of the packet to new IP
		flowRuleInputBuilder.setActionSetDstIpv4Address("10.0.0.20/32");
		//This will change the source IP address field of the packet to new IP
		flowRuleInputBuilder.setActionSetSourceIpv4Address("10.0.0.10/32");
		//This will change the TTL value of the IP header to new value
		flowRuleInputBuilder.setActionSetIpv4Ttl((short)1);
		//This will change the Destination port value of the TCP header to new value
		flowRuleInputBuilder.setActionSetTcpDstPort(8000);
		//This will change the Source port value of the TCP header to new value
		flowRuleInputBuilder.setActionSetTcpSrcPort(8000);
		//This will change the TOS field value of the IP header. Please read online about the function of this field
		flowRuleInputBuilder.setActionSetIpv4Tos(4);
		//This will set the output port value for the flow rule
		flowRuleInputBuilder.setActionOutputPort("2");
		//This will set the output port to Flood so that packet belongs to this flow will be flooded.
		flowRuleInputBuilder.setActionOutputPort(FLOOD);
		//This will set the output port to Controller so that packet belongs to this flow will be sent to controller.
		flowRuleInputBuilder.setActionOutputPort(CONTROLLER);
		//This will set the output port to Drop so that packet belongs to this flow will be immediately dropped.
		flowRuleInputBuilder.setActionOutputPort(DROP);
		
		this.activeSDNService.installFlowRule(flowRuleInputBuilder.build());
		
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 ////------------------------------sendPacketOut() Function example  -----------------------------------------////
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 SendPacketOutInputBuilder packetOutBuilder = new SendPacketOutInputBuilder();
		packetOutBuilder.setSwitchId(1);
		packetOutBuilder.setInPortNumber(1);
		packetOutBuilder.setPayload(notification.getPayload()); //This sets the payload as received during PacketIn
		packetOutBuilder.setOutputPort("2"); // OR 
		packetOutBuilder.setOutputPort(FLOOD); // OR
		packetOutBuilder.setOutputPort(DROP); // OR
		packetOutBuilder.setOutputPort(CONTROLLER);

		this.activeSDNService.sendPacketOut(packetOutBuilder.build());
					
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////------------------------------installNetworkPath() Function example  ------------------------------------////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	InstallNetworkPathInputBuilder pathInputBuilder = new InstallNetworkPathInputBuilder();
		pathInputBuilder.setSrcIpAddress("10.0.0.1/32");
		pathInputBuilder.setDstIpAddress("10.0.0.8/32");
		pathInputBuilder.setFlowPriority(300);
		pathInputBuilder.setIdleTimeout(60);
		pathInputBuilder.setHardTimeout(400); //values in seconds
		List<Integer> pathNodes = Lists.newArrayList(); //list of switches along the path
		pathNodes.add(1);
		pathNodes.add(2);
		pathNodes.add(3);
		pathNodes.add(4);
		pathNodes.add(5);
		pathNodes.add(6);
		pathNodes.add(7);
		pathNodes.add(8);
		pathInputBuilder.setSwitchesInPath(pathNodes);
		
		this.activeSDNService.installNetworkPath(pathInputBuilder.build());
		
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////------------------------------migrateNetworkPath() Function example  ------------------------------------////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	MigrateNetworkPathInputBuilder migratePathInputBuilder = new MigrateNetworkPathInputBuilder();
		migratePathInputBuilder.setSrcIpAddress("10.0.0.1/32");
		migratePathInputBuilder.setDstIpAddress("10.0.0.8/32");
		migratePathInputBuilder.setFlowPriority(300);
		migratePathInputBuilder.setIdleTimeout(90);
		migratePathInputBuilder.setHardTimeout(0);
		
		List<Integer> oldPathNodes = Lists.newArrayList(); //List of switches along the old path
		oldPathNodes.add(3);
		oldPathNodes.add(2);
		oldPathNodes.add(1);
		oldPathNodes.add(5);
		oldPathNodes.add(7);
		migratePathInputBuilder.setSwitchesInOldPath(oldPathNodes); //list of switches along the new path
		
		List<Integer> newPathNodes = Lists.newArrayList();
		newPathNodes.add(3);
		newPathNodes.add(2);
		newPathNodes.add(5);
		newPathNodes.add(7);
		migratePathInputBuilder.setSwitchesInNewPath(oldPathNodes);
		this.activeSDNService.migrateNetworkPath(migratePathInputBuilder.build());
						
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////------------------------------createSrcDstTunnel() Function example  ------------------------------------////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	CreateSrcDstTunnelInputBuilder srcDstTunnelBuilder = new CreateSrcDstTunnelInputBuilder();
		srcDstTunnelBuilder.setCurrentSrcIpAddress("10.0.0.1/32");
		srcDstTunnelBuilder.setNewSrcIpAddress("10.0.0.10/32");
		srcDstTunnelBuilder.setCurrentDstIpAddress("10.0.0.8/32");
		srcDstTunnelBuilder.setNewDstIpAddress("10.0.0.80/32");
		srcDstTunnelBuilder.setFlowPriority(400);
		srcDstTunnelBuilder.setIdleTimeout(0);
		srcDstTunnelBuilder.setHardTimeout(0); //if you want the flow to remain forever in the switch
		
		List<Integer> pathNodes = Lists.newArrayList();
		pathNodes.add(1);
		pathNodes.add(2);
		pathNodes.add(3);
		pathNodes.add(4);
		pathNodes.add(5);
		pathNodes.add(6);
		pathNodes.add(7);
		pathNodes.add(8);
		srcDstTunnelBuilder.setSwitchesInPath(pathNodes);
		
		this.activeSDNService.createSrcDstTunnel(srcDstTunnelBuilder.build());
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////------------------------------createSrcOnlyTunnel() Function example  -----------------------------------////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	CreateSrcOnlyTunnelInputBuilder srcOnlyTunnelBuilder = new CreateSrcOnlyTunnelInputBuilder();
		srcOnlyTunnelBuilder.setCurrentSrcIpAddress("10.0.0.1/32");
		srcOnlyTunnelBuilder.setNewSrcIpAddress("10.0.0.10/32");
		srcOnlyTunnelBuilder.setDstIpAddress("10.0.0.8/32");
		srcOnlyTunnelBuilder.setFlowPriority(400);
		srcOnlyTunnelBuilder.setIdleTimeout(0);
		srcOnlyTunnelBuilder.setHardTimeout(0); //if you want the flow to remain forever in the switch
		
		List<Integer> pathNodes = Lists.newArrayList();
		pathNodes.add(1);
		pathNodes.add(2);
		pathNodes.add(3);
		pathNodes.add(4);
		pathNodes.add(5);
		pathNodes.add(6);
		pathNodes.add(7);
		pathNodes.add(8);
		srcOnlyTunnelBuilder.setSwitchesInPath(pathNodes);
		
		this.activeSDNService.createSrcOnlyTunnel(srcOnlyTunnelBuilder.build());
		
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////------------------------------createDstOnlyTunnel() Function example  -----------------------------------////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	CreateDstOnlyTunnelInputBuilder dstOnlyTunnelBuilder = new CreateDstOnlyTunnelInputBuilder();
		dstOnlyTunnelBuilder.setSrcIpAddress("10.0.0.1/32");
		dstOnlyTunnelBuilder.setCurrentDstIpAddress("10.0.0.8/32");
		dstOnlyTunnelBuilder.setNewDstIpAddress("10.0.0.80/32");
		dstOnlyTunnelBuilder.setFlowPriority(400);
		dstOnlyTunnelBuilder.setIdleTimeout(0);
		dstOnlyTunnelBuilder.setHardTimeout(0); //if you want the flow to remain forever in the switch
		
		List<Integer> pathNodes = Lists.newArrayList();
		pathNodes.add(1);
		pathNodes.add(2);
		pathNodes.add(3);
		pathNodes.add(4);
		pathNodes.add(5);
		pathNodes.add(6);
		pathNodes.add(7);
		pathNodes.add(8);
		dstOnlyTunnelBuilder.setSwitchesInPath(pathNodes);
		
		this.activeSDNService.createDstOnlyTunnel(dstOnlyTunnelBuilder.build());
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////------------------------------removeAllFlowsFromASwitch() Function example  -----------------------------////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	RemoveAllFlowsFromASwitchInputBuilder removeFlowsInputBuilder = new RemoveAllFlowsFromASwitchInputBuilder();
		removeFlowsInputBuilder.setSwitchId(1);
		
		this.activeSDNService.removeAllFlowsFromASwitch(removeFlowsInputBuilder.build()); 
		
		
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////------------------------------removeAFlowRuleFromSwitch() Function example  -----------------------------////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	RemoveAFlowRuleFromSwitchInputBuilder removeFlowInputBuilder = new RemoveAFlowRuleFromSwitchInputBuilder();
		//you have to provide a flowkey/flowID value. When you use function to get all flows from a switch
		//each flow will have a unique id and you can use that to delete a flow from the switch
		removeFlowInputBuilder.setFlowKey("value"); 
		removeFlowInputBuilder.setSwitchId(1);
		this.activeSDNService.removeAFlowRuleFromSwitch(removeFlowInputBuilder.build());
	
	 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////------------------------------removeEventFromSwitch() Function example  -----------------------------////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	RemoveEventFromSwitchInputBuilder removeEvent = new RemoveEventFromSwitchInputBuilder();
		//When you create an event you should keep a copy of that event locally.
		removeEvent.setEventId("value");
		removeEvent.setSwitchId(1);
		this.activeSDNService.removeEventFromSwitch(removeEvent.build());
		
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////------------------------------getSwitchFlowTable() Function example  -----------------------------////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	you can call this function to get all flows rules from a switch. This function will automatically populate a local 
	HashMap data structure called networkConfiguration<SwitchID, ListOfFlowRules>  
	
		getSwitchFlowTable (int switchId)
		
		
	 */


}
