package org.sdnhub.odl.tutorial.tapapp.impl;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.net.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.ActivesdnListener;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.ActivesdnService;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.ConstructTopology;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateDstOnlyTunnelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateSrcOnlyTunnelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.EventTriggered;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.EventSpecs.EventAction;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.EventTriggered.TriggeredEventType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateSrcDstTunnelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllFlowRulesFromASwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllFlowRulesFromASwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllHostsOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallFlowRuleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallNetworkPathInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallNetworkPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.MigrateNetworkPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.NewHostFound;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveAFlowRuleFromSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveAllFlowsFromASwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveEventFromSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SendPacketOutInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SendPacketOutInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SubscribeEventInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.event.triggered.packet.type.ArpPacketType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.event.triggered.packet.type.IcmpPacketType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.event.triggered.packet.type.Ipv4PacketType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.get.all.flow.rules.from.a._switch.output.FlowRules;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.get.all.hosts.output.HostsInfo;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.AddTapOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetNetworkTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAFlowFromSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TapService;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TrafficType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.getnetworktopology.output.NetworkLinks;
import org.opendaylight.yangtools.yang.common.RpcResult;

//import org.graphstream.graph.*;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
///////////////////////////////////////////////////////////////////
class ConnectedHostInfo {
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

class LinkInfo {
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
///////////////////////////////////////////////////////////////////////

class NetworkGraph
{

	UndirectedGraph<String, DefaultEdge> networkTopology;
	HashMap<String, LinkInfo> links = new HashMap<String, LinkInfo>();
	NeighborIndex<String, DefaultEdge> neighborGraph;
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
    //~ Constructors ———————————————————

    public NetworkGraph() {
    	networkTopology = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
    }
    public NetworkGraph(GetNetworkTopologyOutput topologyOutput){
    	networkTopology = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
    	for (NetworkLinks link : topologyOutput.getNetworkLinks()){
    		int leftSwitch = Integer.parseInt(link.getSrcNode().getValue().split(":")[1]);
    		int rightSwitch = Integer.parseInt(link.getDstNode().getValue().split(":")[1]);
    		int leftSwitchPortNumber = Integer.parseInt(link.getSrcNodeConnector().getValue().split(":")[2]);
    		int rightSwitchPortNumber = Integer.parseInt(link.getDstNodeConnector().getValue().split(":")[2]);
    		
    		addLinkInfo(leftSwitch, rightSwitch, leftSwitchPortNumber, rightSwitchPortNumber);
    	}
    	neighborGraph = new NeighborIndex<String, DefaultEdge>(networkTopology);
    }

    //~ Methods ———————————————————
    public void addLinkInfo(int leftSwitch, int rightSwitch, int leftSwitchPortNumber, int rightSwitchPortNumber){
    	if (links.containsKey(Integer.toString(leftSwitch) + ":" + Integer.toString(rightSwitch)) == false){
    		LinkInfo link1 = new LinkInfo(leftSwitch, rightSwitch, leftSwitchPortNumber, rightSwitchPortNumber);
    		LinkInfo link2 = new LinkInfo(rightSwitch, leftSwitch, rightSwitchPortNumber, leftSwitchPortNumber);
   
        	links.put((Integer.toString(leftSwitch) + ":" + Integer.toString(rightSwitch)), link1);
        	links.put((Integer.toString(rightSwitch) + ":" + Integer.toString(leftSwitch)), link2);
        	
        	if (networkTopology.containsVertex(Integer.toString(leftSwitch)) == false){
        		networkTopology.addVertex(Integer.toString(leftSwitch));
        	}
        	if (networkTopology.containsVertex(Integer.toString(rightSwitch)) == false){
        		networkTopology.addVertex(Integer.toString(rightSwitch));
        	}
        	networkTopology.addEdge(Integer.toString(leftSwitch), Integer.toString(rightSwitch));
    	}
    }
    public LinkInfo findLink (int leftSwitch, int rightSwitch){
    	if (links.containsKey(Integer.toString(leftSwitch) + ":" + Integer.toString(rightSwitch)) == true) {
    		return links.get(Integer.toString(leftSwitch) + ":" + Integer.toString(rightSwitch));
    	}
    	return null;
    }

    public List findShortestPath (int leftSwitch, int rightSwitch){
    	List path = DijkstraShortestPath.findPathBetween(networkTopology, 
    			Integer.toString(leftSwitch), Integer.toString(rightSwitch));
    	List<String> networkPath = Lists.newArrayList();
    	//networkPath.add(leftSwitch);
    	for (Object node : path){
    		DefaultEdge dEdge = (DefaultEdge) node;
    		String link = dEdge.toString();
    		//String switchId = link.substring(link.indexOf("(") + 1, link.indexOf(":"));
    		
    		//LOG.debug("     ==================================================================     ");
    		//LOG.debug(link);
    		//LOG.debug("      ==================================================================     ");
    		//networkPath.add(Integer.parseInt(node.toString()));
    		networkPath.add(link);
    	}
    	return networkPath;
    }
    public int findPortID(int leftSwitch, int rightSwitch){
    	LinkInfo link = findLink(leftSwitch, rightSwitch);
    	if (link != null) {
    		return link.leftSwitchPortNumber;
    	}
    	return -1;
    }

    public List findNeighbors(int switchId) {
    	if (networkTopology.containsVertex(Integer.toString(switchId)) == false) {
    		return null;
    	}
    	else {
    		return this.neighborGraph.neighborListOf(Integer.toString(switchId));
    	}
    }
}
//////////////////////////////////////////////////////////////////////
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
    
    private boolean firstTime = true;
    private NetworkGraph topology;
    
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
	
	@Override
	public void onEventTriggered(EventTriggered notification) {
		LOG.debug("     ==================================================================     ");
		LOG.debug("                    Event Triggered is called.");
		LOG.debug("      ==================================================================     ");
		
		
		//============================================================================================
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
				
				if (icmpPacket.getSourceAddress().equals("10.0.0.1/32") && 
						icmpPacket.getDestinationAddress().equals("10.0.0.2/32")) {
					
					InstallFlowRuleInputBuilder flowRuleInputBuilder = new InstallFlowRuleInputBuilder();
					
					flowRuleInputBuilder.setSwitchId(3);
					flowRuleInputBuilder.setInPortId((long)notification.getInPortNumber());
					flowRuleInputBuilder.setDstIpAddress(icmpPacket.getDestinationAddress());
					///flowRuleInputBuilder.setDstIpAddress("10.0.0.1/32");
					flowRuleInputBuilder.setSrcIpAddress(icmpPacket.getSourceAddress());
					flowRuleInputBuilder.setFlowPriority(200);
					flowRuleInputBuilder.setIdleTimeout(0);
					flowRuleInputBuilder.setHardTimeout(0);
					//flowRuleInputBuilder.setSrcMacAddress("00:00:00:00:00:01");
					flowRuleInputBuilder.setSrcMacAddress(icmpPacket.getEthernetSrcMacAddress());
					flowRuleInputBuilder.setTypeOfTraffic(TrafficType.ICMP);
					flowRuleInputBuilder.setActionOutputPort("2");
					
					this.activeSDNService.installFlowRule(flowRuleInputBuilder.build());	
					//---------------
					SendPacketOutInputBuilder packetOutBuilder = new SendPacketOutInputBuilder();
					packetOutBuilder.setSwitchId(3);
					packetOutBuilder.setInPortNumber(notification.getInPortNumber());
					packetOutBuilder.setPayload(notification.getPayload()); //This sets the payload as received during PacketIn
					packetOutBuilder.setOutputPort("2"); 
				
					this.activeSDNService.sendPacketOut(packetOutBuilder.build());
					
				}
				if (icmpPacket.getSourceAddress().equals("10.0.0.2/32") && 
						icmpPacket.getDestinationAddress().equals("10.0.0.1/32")) {
					
					InstallFlowRuleInputBuilder flowRuleInputBuilder = new InstallFlowRuleInputBuilder();
					
					flowRuleInputBuilder.setSwitchId(3);
					flowRuleInputBuilder.setInPortId((long)notification.getInPortNumber());
					flowRuleInputBuilder.setDstIpAddress(icmpPacket.getDestinationAddress());
					flowRuleInputBuilder.setSrcIpAddress(icmpPacket.getSourceAddress());
					flowRuleInputBuilder.setFlowPriority(200);
					flowRuleInputBuilder.setIdleTimeout(0);
					flowRuleInputBuilder.setHardTimeout(0);
					//flowRuleInputBuilder.setSrcMacAddress("00:00:00:00:00:01");
					flowRuleInputBuilder.setSrcMacAddress(icmpPacket.getEthernetSrcMacAddress());
					flowRuleInputBuilder.setTypeOfTraffic(TrafficType.ICMP);
					flowRuleInputBuilder.setActionOutputPort("1");
					
					this.activeSDNService.installFlowRule(flowRuleInputBuilder.build());	
					
					//------------------------
					SendPacketOutInputBuilder packetOutBuilder = new SendPacketOutInputBuilder();
					packetOutBuilder.setSwitchId(3);
					packetOutBuilder.setInPortNumber(notification.getInPortNumber());
					packetOutBuilder.setPayload(notification.getPayload()); //This sets the payload as received during PacketIn
					packetOutBuilder.setOutputPort("1"); 
					
					this.activeSDNService.sendPacketOut(packetOutBuilder.build());
				
				}
				if (icmpPacket.getSourceAddress().equals("10.0.0.1/32") && 
						icmpPacket.getDestinationAddress().equals("10.0.0.8/32")) {

					SubscribeEventInputBuilder eventInputBuilder = new SubscribeEventInputBuilder();
					eventInputBuilder.setCount((long)12);
					eventInputBuilder.setDstIpAddress("10.0.0.8/32");
					eventInputBuilder.setSrcIpAddress("10.0.0.1/32");
					eventInputBuilder.setDuration((long)30);
					eventInputBuilder.setSwitchId(1);
					eventInputBuilder.setTrafficProtocol(TrafficType.ICMP);
					eventInputBuilder.setEventAction(EventAction.DROPANDNOTIFY);
					eventInputBuilder.setHoldNotification(5);
					//eventInputBuilder.setEventAction(EventAction.NOTIFY);
					
					this.activeSDNService.subscribeEvent(eventInputBuilder.build());
					
					
					InstallNetworkPathInputBuilder pathInputBuilder = new InstallNetworkPathInputBuilder();
					
					pathInputBuilder.setSrcIpAddress("10.0.0.1/32");
					pathInputBuilder.setDstIpAddress("10.0.0.8/32");
					pathInputBuilder.setFlowPriority(300);
					List<Integer> pathNodes = Lists.newArrayList();
					pathNodes.add(3);
					pathNodes.add(2);
					pathNodes.add(1);
					pathNodes.add(5);
					pathNodes.add(7);
					pathInputBuilder.setSwitchesInPath(pathNodes);
					
					this.activeSDNService.installNetworkPath(pathInputBuilder.build());
					///---------------------------------------------------------------------*/				
					
				}
				//////////////////////////////////////////////////////
				if (icmpPacket.getSourceAddress().equals("10.0.0.2/32") && 
						icmpPacket.getDestinationAddress().equals("10.0.0.7/32")) {
					SubscribeEventInputBuilder eventInputBuilder = new SubscribeEventInputBuilder();
					eventInputBuilder.setCount((long)12);
					eventInputBuilder.setDstIpAddress("10.0.0.7/32");
					eventInputBuilder.setSrcIpAddress("10.0.0.2/32");
					eventInputBuilder.setDuration((long)30);
					eventInputBuilder.setSwitchId(1);
					eventInputBuilder.setTrafficProtocol(TrafficType.ICMP);
					eventInputBuilder.setEventAction(EventAction.DROPANDNOTIFY);
					eventInputBuilder.setHoldNotification(5);
					//eventInputBuilder.setEventAction(EventAction.NOTIFY);
					
					this.activeSDNService.subscribeEvent(eventInputBuilder.build());
					
					
					InstallNetworkPathInputBuilder pathInputBuilder = new InstallNetworkPathInputBuilder();
					
					pathInputBuilder.setSrcIpAddress("10.0.0.2/32");
					pathInputBuilder.setDstIpAddress("10.0.0.7/32");
					pathInputBuilder.setFlowPriority(300);
					List<Integer> pathNodes = Lists.newArrayList();
					pathNodes.add(3);
					pathNodes.add(2);
					pathNodes.add(1);
					pathNodes.add(5);
					pathNodes.add(7);
					pathInputBuilder.setSwitchesInPath(pathNodes);
					
					this.activeSDNService.installNetworkPath(pathInputBuilder.build());
					
				}
			}
		}
		else if (notification.getTriggeredEventType() == TriggeredEventType.SubscribedEvent) {
			//If conditions checks if the Event is triggered because of a subscribed event is triggered 
			//you can find the event id from notification.getEventId()
			if (notification.getPacketType() instanceof Ipv4PacketType) {
				Ipv4PacketType ipv4Packet = (Ipv4PacketType) notification.getPacketType();
				
				
			}
			else if (notification.getPacketType() instanceof IcmpPacketType) {
				IcmpPacketType icmpPacket = (IcmpPacketType) notification.getPacketType();
				LOG.debug("         ---------------------------------------------------------------------     ");
       		 	LOG.debug("Subscried Event is called to Migrate the path");
       		 	LOG.debug("         ---------------------------------------------------------------------     ");
				
	       		 if (icmpPacket.getSourceAddress().equals("10.0.0.1/32") && 
							icmpPacket.getDestinationAddress().equals("10.0.0.8/32")) {
					MigrateNetworkPathInputBuilder migratePathInputBuilder = new MigrateNetworkPathInputBuilder();
					migratePathInputBuilder.setSrcIpAddress("10.0.0.1/32");
					migratePathInputBuilder.setDstIpAddress("10.0.0.8/32");
					migratePathInputBuilder.setFlowPriority(301);
					
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
					migratePathInputBuilder.setSwitchesInNewPath(newPathNodes);
					this.activeSDNService.migrateNetworkPath(migratePathInputBuilder.build());
	       		 }
	       		 /////////////////////////////////////////////////////////
	       		if (icmpPacket.getSourceAddress().equals("10.0.0.2/32") && 
						icmpPacket.getDestinationAddress().equals("10.0.0.7/32")) {
	       			
	       			CreateSrcDstTunnelInputBuilder srcDstTunnelBuilder = new CreateSrcDstTunnelInputBuilder();
	       			
					srcDstTunnelBuilder.setCurrentSrcIpAddress("10.0.0.2/32");
					srcDstTunnelBuilder.setNewSrcIpAddress("10.0.0.20/32");
					srcDstTunnelBuilder.setCurrentDstIpAddress("10.0.0.7/32");
					srcDstTunnelBuilder.setNewDstIpAddress("10.0.0.70/32");
					srcDstTunnelBuilder.setFlowPriority(400);
					srcDstTunnelBuilder.setIdleTimeout(0);
					srcDstTunnelBuilder.setHardTimeout(0);
					List<Integer> pathNodes = Lists.newArrayList();
					pathNodes.add(3);
					pathNodes.add(2);
					pathNodes.add(1);
					pathNodes.add(5);
					pathNodes.add(7);
					
					srcDstTunnelBuilder.setSwitchesInPath(pathNodes);
					this.activeSDNService.createSrcDstTunnel(srcDstTunnelBuilder.build());
	       		}
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
