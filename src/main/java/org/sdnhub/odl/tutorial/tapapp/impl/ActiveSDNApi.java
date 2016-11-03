package org.sdnhub.odl.tutorial.tapapp.impl;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.ActivesdnListener;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.ActivesdnService;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateDstOnlyTunnelInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateDstOnlyTunnelOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateDstOnlyTunnelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateSrcDstTunnelInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateSrcDstTunnelOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateSrcDstTunnelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateSrcOnlyTunnelInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateSrcOnlyTunnelOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.CreateSrcOnlyTunnelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.EventTriggered;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllFlowRulesFromASwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllFlowRulesFromASwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllFlowRulesFromASwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllHostsOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.GetAllHostsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallFlowRuleInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallFlowRuleInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallFlowRuleOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallFlowRuleOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallNetworkPathInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallNetworkPathOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.InstallNetworkPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.MigrateNetworkPathInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.MigrateNetworkPathOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.MigrateNetworkPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveAFlowRuleFromSwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveAFlowRuleFromSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveAFlowRuleFromSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveAllFlowsFromASwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveAllFlowsFromASwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveAllFlowsFromASwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveEventFromSwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveEventFromSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.RemoveEventFromSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SendPacketOutInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SendPacketOutOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SendPacketOutOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SubscribeEventInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SubscribeEventOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.SubscribeEventOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.get.all.flow.rules.from.a._switch.output.FlowRules;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.get.all.flow.rules.from.a._switch.output.FlowRulesBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.get.all.hosts.output.HostsInfo;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.activesdn.rev150601.get.all.hosts.output.HostsInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.AddTapInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.AddTapOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.ConnectedHosts;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.CookieToFlowid;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.CookieToFlowidBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.HostInfo;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallFlowOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallFlowRepository;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallPathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallPathOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.LocalIpv4Prefix;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MovePathInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MovePathOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MutateIpInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MutateIpOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.NodeNeighbors;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.NodeNeighborsBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAFlowFromSwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAFlowFromSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAFlowFromSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveATapFromSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveATapFromSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveFlowsFromSwitchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveFlowsFromSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TapService;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TapSpec;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TapSpecBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TrafficType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.add.tap.input.Tap1;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.add.tap.input.Tap1Builder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.connected.hosts.ConnectedHost;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.connected.hosts.ConnectedHostKey;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.event.actions.tap.actions.BlockCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.event.actions.tap.actions.DropAndNotifyCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.event.actions.tap.actions.NotifyCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.event.actions.tap.actions.block._case.BlockActionBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.event.actions.tap.actions.drop.and.notify._case.DropAndNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.event.actions.tap.actions.notify._case.NotifyActionBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.DropPacketCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.ForwardToControllerCase;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.ForwardToControllerCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.ForwardToFloodCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.ForwardToPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.SetDstIpv4AddressCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.SetIpv4TosCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.SetIpv4TtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.SetSourceIpv4AddressCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.SetTcpDstPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.SetTcpSrcPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.drop.packet._case.DropPacketBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.forward.to.controller._case.ForwardToController;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.forward.to.controller._case.ForwardToControllerBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.forward.to.flood._case.ForwardToFloodBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.forward.to.port._case.ForwardToPortBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.set.dst.ipv4.address._case.SetDstIpv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.set.ipv4.tos._case.SetIpv4TosBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.set.ipv4.ttl._case.SetIpv4TtlBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.set.source.ipv4.address._case.SetSourceIpv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.set.tcp.dst.port._case.SetTcpDstPortBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.set.tcp.src.port._case.SetTcpSrcPortBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.install.flow.input.AssociatedActions;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.install.flow.input.AssociatedActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.install.flow.input.NewFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mutate.ip.input.mutation.end.BothCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mutate.ip.input.mutation.end.DstOnlyCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mutate.ip.input.mutation.end.SourceOnlyCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mutate.ip.input.mutation.end.both._case.BothBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mutate.ip.input.mutation.end.dst.only._case.DstOnlyBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mutate.ip.input.mutation.end.source.only._case.SourceOnlyBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.tap.spec.TapBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.sdnhub.odl.tutorial.tapapp.impl.TutorialTapProvider;
import org.sdnhub.odl.tutorial.utils.GenericTransactionUtils;
import org.sdnhub.odl.tutorial.utils.inventory.InventoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class ActiveSDNApi implements ActivesdnService {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	private final static long FLOOD_PORT_NUMBER = 0xfffffffbL;
    private final static long TABLE_PORT_NUMBER = 0xfffffff9L;
    private final static long CONTROLLER_PORT_NUMBER = 0xfffffffdL;
	private final static String FLOOD = "FLOOD";
    private final static String DROP = "DROP";
    private final static String CONTROLLER = "CONTROLLER";
    private DataBroker dataBroker;
    private TapService tapService;
    public final AtomicLong eventID = new AtomicLong();
    private PacketProcessingService packetProcessingService;
    
	public ActiveSDNApi(DataBroker dataBroker, NotificationProviderService notificationService, RpcProviderRegistry rpcProviderRegistry) {
		//Store the data broker for reading/writing from inventory store
        this.dataBroker = dataBroker;
        //Object used for flow programming through RPC calls
        this.tapService = rpcProviderRegistry.getRpcService(TapService.class);
        rpcProviderRegistry.addRpcImplementation(ActivesdnService.class, this);
        this.packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);
	}

	@Override
	public Future<RpcResult<InstallFlowRuleOutput>> installFlowRule(
			InstallFlowRuleInput input) {
		NodeId nodeId = new NodeId("openflow:" + input.getSwitchId().toString());
		
		NewFlowBuilder newFlowBuilder = new NewFlowBuilder();
		if (input.getInPortId() != 0){
			newFlowBuilder.setInPort(InventoryUtils.getNodeConnectorId(nodeId, input.getInPortId()));
		}
		
		newFlowBuilder.setDstIpAddress((input.getDstIpAddress() != null ? 
				new Ipv4Prefix(input.getDstIpAddress()) : null));
		newFlowBuilder.setSrcIpAddress((input.getSrcIpAddress() != null ? 
				new Ipv4Prefix(input.getSrcIpAddress()) : null));
	
		
		newFlowBuilder.setDstMacAddress((input.getDstMacAddress() != null ? 
				new MacAddress(input.getDstMacAddress()) : null));
		newFlowBuilder.setSrcMacAddress((input.getSrcMacAddress() != null ? 
				new MacAddress(input.getSrcMacAddress()) : null));
		
		newFlowBuilder.setTrafficMatch(input.getTypeOfTraffic() != null ? input.getTypeOfTraffic() : null);
		newFlowBuilder.setIdleTimeout(input.getIdleTimeout());
		newFlowBuilder.setHardTimeout(input.getHardTimeout());
		newFlowBuilder.setFlowPriority(input.getFlowPriority());
		
		long actionIndex = 1;
		List<AssociatedActions> actionList = Lists.newArrayList();
		AssociatedActionsBuilder actionBuilder = new AssociatedActionsBuilder();
		
		if (input.getActionSetDstIpv4Address() != null){
			SetDstIpv4AddressBuilder setDstIPBuilder = new SetDstIpv4AddressBuilder();
			setDstIPBuilder.setValue(new Ipv4Prefix(input.getActionSetDstIpv4Address()));
			actionBuilder.setFlowActions(new SetDstIpv4AddressCaseBuilder().
					setSetDstIpv4Address(setDstIPBuilder.build()).build());
			actionBuilder.setId(actionIndex++);
			actionList.add(actionBuilder.build());
		}
		
		if (input.getActionSetSourceIpv4Address() != null){
			SetSourceIpv4AddressBuilder setSrcIPBuilder = new SetSourceIpv4AddressBuilder();
			setSrcIPBuilder.setValue(new Ipv4Prefix(input.getActionSetSourceIpv4Address()));
			actionBuilder.setFlowActions(new SetSourceIpv4AddressCaseBuilder().
					setSetSourceIpv4Address(setSrcIPBuilder.build()).build());
			actionBuilder.setId(actionIndex++);
			actionList.add(actionBuilder.build());
		}
		
		if (input.getActionSetIpv4Tos() != null){
			SetIpv4TosBuilder setTosBuilder = new SetIpv4TosBuilder();
			setTosBuilder.setValue(input.getActionSetIpv4Tos());
			actionBuilder.setFlowActions(new SetIpv4TosCaseBuilder().
					setSetIpv4Tos(setTosBuilder.build()).build());
			actionBuilder.setId(actionIndex++);
			actionList.add(actionBuilder.build());
		}
		
		if (input.getActionSetTcpSrcPort() != null) {
			SetTcpSrcPortBuilder setSrcPortBuilder = new SetTcpSrcPortBuilder();
			setSrcPortBuilder.setPortNumber(input.getActionSetTcpSrcPort());
			actionBuilder.setFlowActions(new SetTcpSrcPortCaseBuilder().
					setSetTcpSrcPort(setSrcPortBuilder.build()).build());
			actionBuilder.setId(actionIndex++);
			actionList.add(actionBuilder.build());
		}
		
		if (input.getActionSetTcpDstPort() != null) {
			SetTcpDstPortBuilder setDstPortBuilder = new SetTcpDstPortBuilder();
			setDstPortBuilder.setPortNumber(input.getActionSetTcpDstPort());
			actionBuilder.setFlowActions(new SetTcpDstPortCaseBuilder().
					setSetTcpDstPort(setDstPortBuilder.build()).build());
			actionBuilder.setId(actionIndex++);
			actionList.add(actionBuilder.build());
		}
		
		if (input.getActionSetIpv4Ttl() != null) {
			SetIpv4TtlBuilder ttlBuilder = new SetIpv4TtlBuilder();
			ttlBuilder.setTtlValue(input.getActionSetIpv4Ttl());
			actionBuilder.setFlowActions(new SetIpv4TtlCaseBuilder().
					setSetIpv4Ttl(ttlBuilder.build()).build());
			actionBuilder.setId(actionIndex++);
			actionList.add(actionBuilder.build());
		}
		
		if (input.getActionOutputPort() == FLOOD){
			ForwardToFloodBuilder floodBuilder = new ForwardToFloodBuilder();
			actionBuilder.setFlowActions(new ForwardToFloodCaseBuilder().
					setForwardToFlood(floodBuilder.build()).build());
			actionBuilder.setId(actionIndex++);
			actionList.add(actionBuilder.build());
		}
		else if (input.getActionOutputPort() == CONTROLLER) {
    		ForwardToControllerBuilder controllerBuilder = new ForwardToControllerBuilder();
    		actionBuilder.setFlowActions(new ForwardToControllerCaseBuilder().
    				setForwardToController(controllerBuilder.build()).build());
    		actionBuilder.setId(actionIndex++);
    		actionList.add(actionBuilder.build());
		}
		else if (input.getActionOutputPort() == DROP){
			DropPacketBuilder dropBuilder = new DropPacketBuilder();
			actionBuilder.setFlowActions(new DropPacketCaseBuilder().
					setDropPacket(dropBuilder.build()).build());
			actionBuilder.setId(actionIndex++);
			actionList.add(actionBuilder.build());
		}
		else 
		{
			ForwardToPortBuilder forwardBuilder = new ForwardToPortBuilder();
			NodeConnectorId outputPort = InventoryUtils.getNodeConnectorId(nodeId, (long) Integer.parseInt(input.getActionOutputPort()));
			forwardBuilder.setOutputNodeConnector(outputPort);
			actionBuilder.setFlowActions(new ForwardToPortCaseBuilder().
					setForwardToPort(forwardBuilder.build()).build());
			actionBuilder.setId(actionIndex++);
			actionList.add(actionBuilder.build());
		}
    	
		InstallFlowInputBuilder installFlowBuilder = new InstallFlowInputBuilder();
		installFlowBuilder.setNode(nodeId);
		installFlowBuilder.setNewFlow(newFlowBuilder.build());
		installFlowBuilder.setAssociatedActions(actionList);
		installFlowBuilder.setFlowId(BigInteger.valueOf(eventID.incrementAndGet()).toString());
		
		InstallFlowRuleOutputBuilder output = new InstallFlowRuleOutputBuilder();
		try {
			Future<RpcResult<InstallFlowOutput>> futureOutput;
			futureOutput = tapService.installFlow(installFlowBuilder.build());
					
			if (futureOutput != null){
				InstallFlowOutput installOutput = futureOutput.get().getResult();
				output.setFlowId(installFlowBuilder.getFlowId());
				output.setStatus(installOutput.getStatus());
				return RpcResultBuilder.success(output.build()).buildFuture();
			}
		} catch (Exception e) {
			LOG.error("Exception reached in InstallFlowRule RPC {} --------", e);
			return null;
		}
		return null;
	}

	@Override
	public Future<RpcResult<SubscribeEventOutput>> subscribeEvent(
			SubscribeEventInput input) {
		//LOG.debug("         ---------------------------------------------------------------------     ");
		// LOG.debug("Subscribe event is called..................");
		// LOG.debug("         ---------------------------------------------------------------------     ");
		Tap1Builder tapInputBuilder = new Tap1Builder();
		NodeId nodeId = new NodeId("openflow:" + input.getSwitchId().toString());
		tapInputBuilder.setNode(nodeId);
		if (input.getInPortId() != null) {
			tapInputBuilder.setInPortConnector(InventoryUtils.getNodeConnectorId(nodeId, input.getInPortId()));
		}
		
		tapInputBuilder.setId(BigInteger.valueOf(eventID.incrementAndGet()).toString());
		tapInputBuilder.setCount(input.getCount());
		tapInputBuilder.setDuration(input.getDuration());
		tapInputBuilder.setSourceIpAddress(input.getSrcIpAddress() != null ? 
				new LocalIpv4Prefix(input.getSrcIpAddress()) : null);
		tapInputBuilder.setDstIpAddress(input.getDstIpAddress() != null ? 
				new LocalIpv4Prefix(input.getDstIpAddress()) : null);
		tapInputBuilder.setSourceMacAddress(input.getSrcMacAddress() != null ? 
				new MacAddress(input.getSrcMacAddress()) : null);
		tapInputBuilder.setDstMacAddress(input.getDstMacAddress() != null ? 
				new MacAddress(input.getDstMacAddress()) : null);
		//tapInputBuilder.setId("1");
		if (input.getTrafficProtocol() != null){
			tapInputBuilder.setTrafficMatch(input.getTrafficProtocol());
		}
		switch (input.getEventAction()) {
			case DROP:
				BlockActionBuilder blockBuilder = new BlockActionBuilder();
				tapInputBuilder.setTapActions(new BlockCaseBuilder().
						setBlockAction(blockBuilder.build()).build());
				break;
			case NOTIFY:
				NotifyActionBuilder notifyBuilder = new NotifyActionBuilder();
				tapInputBuilder.setTapActions(new NotifyCaseBuilder().
						setNotifyAction(notifyBuilder.build()).build());
				break;
			case DROPANDNOTIFY:
				DropAndNotifyBuilder dropAndNotifyBuilder = new DropAndNotifyBuilder();
				dropAndNotifyBuilder.setHoldNotification(input.getHoldNotification());
				tapInputBuilder.setTapActions(new DropAndNotifyCaseBuilder().
						setDropAndNotify(dropAndNotifyBuilder.build()).build());
				break;
		}
		List<Tap1> tapList = Lists.newArrayList();
		tapList.add(tapInputBuilder.build());
		AddTapInputBuilder inputBuilder = new AddTapInputBuilder();
		inputBuilder.setTap1(tapList);
		
		SubscribeEventOutputBuilder eventOutputBuilder = new SubscribeEventOutputBuilder();
		try {
			Future<RpcResult<AddTapOutput>> tapFutureOutput = tapService.addTap(inputBuilder.build());
			if (tapFutureOutput != null){
				AddTapOutput tapOutput = tapFutureOutput.get().getResult();
				eventOutputBuilder.setStatus(tapOutput.getStatus());
				eventOutputBuilder.setEventId(tapInputBuilder.getId());
				return RpcResultBuilder.success(eventOutputBuilder.build()).buildFuture();
			}
		} catch (Exception e){
			LOG.error("Exception reached in Subscribe Event RPC {} --------", e);
			return null;
		}
		return null;
	}
	
	@Override
	public Future<RpcResult<InstallNetworkPathOutput>> installNetworkPath(
			InstallNetworkPathInput input) {
		InstallPathInputBuilder installPathBuilder = new InstallPathInputBuilder();
		try {
			if (input.getDstIpAddress() != null){
				installPathBuilder.setDstIpAddress(new Ipv4Prefix(input.getDstIpAddress()));
			}
			else {
				String exception = "No Destination IP is provided.";
				throw new Exception(exception);
			}
			if (input.getSrcIpAddress() != null) {
				installPathBuilder.setSrcIpAddress(new Ipv4Prefix(input.getSrcIpAddress()));
			}
			else {
				String exception = "No Source IP is provided.";
				throw new Exception(exception);
			}
			if (input.getSwitchesInPath()!= null){
				List<NodeId> nodeList = Lists.newArrayList();
				for (int node: input.getSwitchesInPath()){
					NodeId nodeId = new NodeId("openflow:" + Integer.toString(node));
					nodeList.add(nodeId);
				}
				installPathBuilder.setPathNodes(nodeList);
			}
			else {
				String exception = "No Path is provided.";
				throw new Exception(exception);
			}
			installPathBuilder.setFlowPriority(input.getFlowPriority());
			installPathBuilder.setIdleTimeout(input.getIdleTimeout());
			installPathBuilder.setHardTimeout(input.getHardTimeout());
			///-------------------------------
			Future<RpcResult<InstallPathOutput>> pathFutureOutput =  
			tapService.installPath(installPathBuilder.build());
			if (pathFutureOutput != null) {
				InstallNetworkPathOutput output = new InstallNetworkPathOutputBuilder().
						setStatus(pathFutureOutput.get().getResult().getStatus()).build();
				return RpcResultBuilder.success(output).buildFuture();
			}
			else {
				String exception = "No Path could be installed.";
				throw new Exception(exception);
			}
		} catch (Exception e){
			LOG.error("Exception reached in InstallNetworkPath RPC {} --------", e);
			return null;
		}
	}
	
	@Override
	public Future<RpcResult<CreateSrcOnlyTunnelOutput>> createSrcOnlyTunnel(
			CreateSrcOnlyTunnelInput input) {
		MutateIpInputBuilder mutateIpBuilder = new MutateIpInputBuilder();
		try {
			if (input.getDstIpAddress() == null || input.getCurrentSrcIpAddress() == null || 
					input.getNewSrcIpAddress() == null || input.getSwitchesInPath()== null) {
				String exception = "Incomplete Data is provided and some parameter has null value.";
				throw new Exception(exception);
			}
			else {
				mutateIpBuilder.setDstIpAddress(new Ipv4Prefix(input.getDstIpAddress()));
				mutateIpBuilder.setSrcIpAddress(new Ipv4Prefix(input.getCurrentSrcIpAddress()));
				List<NodeId> nodeList = Lists.newArrayList();
				for (int node: input.getSwitchesInPath()){
					NodeId nodeId = new NodeId("openflow:" + Integer.toString(node));
					nodeList.add(nodeId);
				}
				mutateIpBuilder.setPathNodes(nodeList);
				
				SourceOnlyBuilder srcOnlyBuilder = new SourceOnlyBuilder();
				srcOnlyBuilder.setNewSrcIpAddress(new Ipv4Prefix(input.getNewSrcIpAddress()));
				SourceOnlyCaseBuilder srcOnlyCaseBuilder = new SourceOnlyCaseBuilder();
				srcOnlyCaseBuilder.setSourceOnly(srcOnlyBuilder.build());
				mutateIpBuilder.setMutationEnd(srcOnlyCaseBuilder.build());
				mutateIpBuilder.setFlowPriority(input.getFlowPriority());
				mutateIpBuilder.setIdleTimeout(input.getIdleTimeout());
				mutateIpBuilder.setHardTimeout(input.getHardTimeout());
				///-------------------------------
				Future<RpcResult<MutateIpOutput>> mutateIpFutureOutput =  
				tapService.mutateIp(mutateIpBuilder.build());
				if (mutateIpFutureOutput != null) {
					CreateSrcOnlyTunnelOutput output = new CreateSrcOnlyTunnelOutputBuilder().
							setStatus(mutateIpFutureOutput.get().getResult().getStatus()).build();
					return RpcResultBuilder.success(output).buildFuture();
				}
				else {
					String exception = "No Tunnel could be installed.";
					throw new Exception(exception);
				}
			}
		} catch (Exception e){
			LOG.error("Exception reached in Create Source Only Tunnel RPC {} --------", e);
			return null;
		}
	}
	
	@Override
	public Future<RpcResult<CreateDstOnlyTunnelOutput>> createDstOnlyTunnel(
			CreateDstOnlyTunnelInput input) {
		MutateIpInputBuilder mutateIpBuilder = new MutateIpInputBuilder();
		try {
			if (input.getCurrentDstIpAddress() == null || input.getSrcIpAddress() == null || 
					input.getNewDstIpAddress() == null || input.getSwitchesInPath()== null) {
				String exception = "Incomplete Data is provided and some parameter has null value.";
				throw new Exception(exception);
			}
			else {
				mutateIpBuilder.setDstIpAddress(new Ipv4Prefix(input.getCurrentDstIpAddress()));
				mutateIpBuilder.setSrcIpAddress(new Ipv4Prefix(input.getSrcIpAddress()));
				List<NodeId> nodeList = Lists.newArrayList();
				for (int node: input.getSwitchesInPath()){
					NodeId nodeId = new NodeId("openflow:" + Integer.toString(node));
					nodeList.add(nodeId);
				}
				mutateIpBuilder.setPathNodes(nodeList);
				
				DstOnlyBuilder dstOnlyBuilder = new DstOnlyBuilder();
				dstOnlyBuilder.setNewDstIpAddress(new Ipv4Prefix(input.getNewDstIpAddress()));
				DstOnlyCaseBuilder dstOnlyCaseBuilder = new DstOnlyCaseBuilder();
				dstOnlyCaseBuilder.setDstOnly(dstOnlyBuilder.build());
				mutateIpBuilder.setMutationEnd(dstOnlyCaseBuilder.build());
				mutateIpBuilder.setFlowPriority(input.getFlowPriority());
				mutateIpBuilder.setIdleTimeout(input.getIdleTimeout());
				mutateIpBuilder.setHardTimeout(input.getHardTimeout());
				///-------------------------------
				Future<RpcResult<MutateIpOutput>> mutateIpFutureOutput =  
				tapService.mutateIp(mutateIpBuilder.build());
				if (mutateIpFutureOutput != null) {
					CreateDstOnlyTunnelOutput output = new CreateDstOnlyTunnelOutputBuilder().
							setStatus(mutateIpFutureOutput.get().getResult().getStatus()).build();
					return RpcResultBuilder.success(output).buildFuture();
				}
				else {
					String exception = "No Tunnel could be installed.";
					throw new Exception(exception);
				}
			}
		} catch (Exception e){
			LOG.error("Exception reached in Create Destination Only Tunnel RPC {} --------", e);
			return null;
		}
	}
	
	@Override
	public Future<RpcResult<CreateSrcDstTunnelOutput>> createSrcDstTunnel(
			CreateSrcDstTunnelInput input) {
		MutateIpInputBuilder mutateIpBuilder = new MutateIpInputBuilder();
		try {
			if (input.getCurrentDstIpAddress() == null || input.getCurrentSrcIpAddress() == null || 
					input.getNewDstIpAddress() == null || input.getNewSrcIpAddress() == null || 
					input.getSwitchesInPath()== null) {
				String exception = "Incomplete Data is provided and some parameter has null value.";
				throw new Exception(exception);
			}
			else {
				mutateIpBuilder.setDstIpAddress(new Ipv4Prefix(input.getCurrentDstIpAddress()));
				mutateIpBuilder.setSrcIpAddress(new Ipv4Prefix(input.getCurrentSrcIpAddress()));
				List<NodeId> nodeList = Lists.newArrayList();
				for (int node: input.getSwitchesInPath()){
					NodeId nodeId = new NodeId("openflow:" + Integer.toString(node));
					nodeList.add(nodeId);
				}
				mutateIpBuilder.setPathNodes(nodeList);
				
				BothBuilder bothBuilder = new BothBuilder();
				bothBuilder.setNewDstIpAddress(new Ipv4Prefix(input.getNewDstIpAddress()));
				bothBuilder.setNewSrcIpAddress(new Ipv4Prefix(input.getNewSrcIpAddress()));
				BothCaseBuilder bothCaseBuilder = new BothCaseBuilder();
				bothCaseBuilder.setBoth(bothBuilder.build());
				mutateIpBuilder.setMutationEnd(bothCaseBuilder.build());
				mutateIpBuilder.setFlowPriority(input.getFlowPriority());
				mutateIpBuilder.setIdleTimeout(input.getIdleTimeout());
				mutateIpBuilder.setHardTimeout(input.getHardTimeout());
				///-------------------------------
				Future<RpcResult<MutateIpOutput>> mutateIpFutureOutput =  
				tapService.mutateIp(mutateIpBuilder.build());
				if (mutateIpFutureOutput != null) {
					CreateSrcDstTunnelOutput output = new CreateSrcDstTunnelOutputBuilder().
							setStatus(mutateIpFutureOutput.get().getResult().getStatus()).build();
					return RpcResultBuilder.success(output).buildFuture();
				}
				else {
					String exception = "No Tunnel could be installed.";
					throw new Exception(exception);
				}
			}
		} catch (Exception e){
			LOG.error("Exception reached in Create Source Destination lTunnel RPC {} --------", e);
			return null;
		}
	}
	
	@Override
	public Future<RpcResult<MigrateNetworkPathOutput>> migrateNetworkPath(
			MigrateNetworkPathInput input) {
		MovePathInputBuilder movePathBuilder = new MovePathInputBuilder();
		try {
			if (input.getDstIpAddress() == null || input.getSrcIpAddress() == null || 
					input.getSwitchesInOldPath() == null || input.getSwitchesInNewPath()== null) {
				String exception = "Incomplete Data is provided and some parameter has null value.";
				throw new Exception(exception);
			}
			else {
				movePathBuilder.setDstIpAddress(new Ipv4Prefix(input.getDstIpAddress()));
				movePathBuilder.setSrcIpAddress(new Ipv4Prefix(input.getSrcIpAddress()));
				List<NodeId> oldNodeList = Lists.newArrayList();
				for (int node: input.getSwitchesInOldPath()){
					NodeId nodeId = new NodeId("openflow:" + Integer.toString(node));
					oldNodeList.add(nodeId);
				}
				movePathBuilder.setOldPathNodes(oldNodeList);
				
				List<NodeId> newNodeList = Lists.newArrayList();
				for (int node: input.getSwitchesInNewPath()){
					NodeId nodeId = new NodeId("openflow:" + Integer.toString(node));
					newNodeList.add(nodeId);
				}
				movePathBuilder.setNewPathNodes(newNodeList);
				movePathBuilder.setFlowPriority(input.getFlowPriority());
				movePathBuilder.setIdleTimeout(input.getIdleTimeout());
				movePathBuilder.setHardTimeout(input.getHardTimeout());
				///-------------------------------
				Future<RpcResult<MovePathOutput>> movePathFutureOutput =  
				tapService.movePath(movePathBuilder.build());
				if (movePathFutureOutput != null) {
					MigrateNetworkPathOutput output = new MigrateNetworkPathOutputBuilder().
							setStatus(movePathFutureOutput.get().getResult().getStatus()).build();
					return RpcResultBuilder.success(output).buildFuture();
				}
				else {
					String exception = "Path could not be migrated.";
					throw new Exception(exception);
				}
			}
		} catch (Exception e){
			LOG.error("Exception reached in Migrate Path RPC {} --------", e);
			return null;
		}
	}
	
	@Override
	public Future<RpcResult<RemoveAllFlowsFromASwitchOutput>> removeAllFlowsFromASwitch(
			RemoveAllFlowsFromASwitchInput input) {
		RemoveFlowsFromSwitchInputBuilder removeFlowsBuilder = new RemoveFlowsFromSwitchInputBuilder();
		NodeId nodeId = new NodeId("openflow:" + input.getSwitchId().toString());
		try {
			Future<RpcResult<RemoveFlowsFromSwitchOutput>> removeFlowsFutureOutput =
					tapService.removeFlowsFromSwitch(removeFlowsBuilder.build());
			if (removeFlowsFutureOutput != null) {
				RemoveAllFlowsFromASwitchOutput output = new RemoveAllFlowsFromASwitchOutputBuilder().
						setStatus(removeFlowsFutureOutput.get().getResult().getStatus()).build();
				return RpcResultBuilder.success(output).buildFuture();
			}
			else {
				String exception = "Flows couldn't be removed from switch " + input.getSwitchId().toString();
				throw new Exception(exception);
			}
		}catch (Exception e){
			LOG.error("Exception reached in RemoveAllFlowsFromASwitch RPC {} --------", e);
			return null;
		}
	}
	
	@Override
	public Future<RpcResult<GetAllHostsOutput>> getAllHosts() {
		InstanceIdentifier<ConnectedHosts> hostIID = InstanceIdentifier.builder(ConnectedHosts.class).build();
				
		ConnectedHosts hosts = GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.OPERATIONAL, hostIID);
		if (hosts != null){
			long hostId = 1;
			GetAllHostsOutputBuilder getHostOutputBuilder = new GetAllHostsOutputBuilder();
			List<HostsInfo> hostList = Lists.newArrayList();
			for (ConnectedHost host: hosts.getConnectedHost()){
				HostsInfoBuilder hostBuilder = new HostsInfoBuilder();
				hostBuilder.setHostIpAddress(host.getHostIpAddress());
				hostBuilder.setHostMacAddress(host.getHostMacAddress());
				hostBuilder.setNodeConnectedTo(host.getNodeConnectedTo());
				hostBuilder.setNodeConnectorConnectedTo(host.getNodeConnectorConnectedTo());
				hostBuilder.setId(hostId++);
				hostList.add(hostBuilder.build());
			}
			getHostOutputBuilder.setHostsInfo(hostList);
			return RpcResultBuilder.success(getHostOutputBuilder.build()).buildFuture();
		}
		return null;
	}
	
	@Override
	public Future<RpcResult<GetAllFlowRulesFromASwitchOutput>> getAllFlowRulesFromASwitch(
			GetAllFlowRulesFromASwitchInput input) {
		NodeId nodeId = new NodeId("openflow:" + input.getSwitchId().toString());
		TableKey tableKey = new TableKey((short)0);
		InstanceIdentifier<Table> tableIID = InstanceIdentifier.builder(Nodes.class)
        		.child(Node.class, new NodeKey(nodeId))
        		.augmentation(FlowCapableNode.class)
        		.child(Table.class, tableKey).build();
		
		Table table = GenericTransactionUtils.readData(this.dataBroker, LogicalDatastoreType.CONFIGURATION, tableIID);
		if (table != null){
			if (table.getFlow() != null) {
				int flowCount = 0;
				List<Flow> flows = table.getFlow();
				GetAllFlowRulesFromASwitchOutputBuilder flowRulesOutputBuilder = new GetAllFlowRulesFromASwitchOutputBuilder();
				List<FlowRules> flowRuleList = Lists.newArrayList();
				for (Iterator<Flow> iterator = flows.iterator(); iterator.hasNext();) {
					flowCount++;
					FlowKey flowKey = iterator.next().getKey();
			        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.create(Nodes.class)
			        		.child(Node.class, new NodeKey(nodeId))
			        		.augmentation(FlowCapableNode.class)
			        		.child(Table.class, tableKey)
			        		.child(Flow.class, flowKey);
			        Flow flow = GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID);
			        if (flow != null){
			        	FlowRulesBuilder flowBuilder = new FlowRulesBuilder();
			        	////---------Extract Flow properties information
			        	if (flow.getId() != null){
			        		flowBuilder.setFlowId(flow.getId().getValue());
			        	}
			        	flowBuilder.setFlowPriority(flow.getPriority());
			        	flowBuilder.setIdleTimeout(flow.getIdleTimeout());
			        	flowBuilder.setHardTimeout(flow.getHardTimeout());
			        	flowBuilder.setSwitchId(input.getSwitchId());
			        	///---------------Extract IP and MAC address information
			        	if (flow.getMatch().getEthernetMatch().getEthernetSource() != null){
			        		flowBuilder.setSrcMacAddress(
			        				flow.getMatch().getEthernetMatch().getEthernetSource().getAddress().getValue());
			        	}
			        	if (flow.getMatch().getEthernetMatch().getEthernetDestination() != null){
			        		flowBuilder.setDstMacAddress(
			        				flow.getMatch().getEthernetMatch().getEthernetDestination().getAddress().getValue());
			        	}
			        	if (flow.getMatch().getInPort() != null){
			        		flowBuilder.setInPortId((long)Integer.parseInt(flow.getMatch().getInPort().getValue().split(":")[2]));
			        	}
			        	if (flow.getMatch().getLayer3Match() != null){
			        		Ipv4Match ipv4Match = (Ipv4Match) flow.getMatch().getLayer3Match();
			        		if (ipv4Match.getIpv4Source() != null) {
			        			flowBuilder.setSrcIpAddress(ipv4Match.getIpv4Source().getValue());
			        		}
			        		if (ipv4Match.getIpv4Destination() != null) {
			        			flowBuilder.setDstIpAddress(ipv4Match.getIpv4Destination().getValue());
			        		}
			        	}
			        	//----------Extract Port Information
			        	if (flow.getMatch().getLayer4Match() != null) {
			        		TcpMatch tcpMatch = (TcpMatch) flow.getMatch().getLayer4Match();
			        		if (tcpMatch.getTcpDestinationPort() != null) {
			        			flowBuilder.setDstPort(tcpMatch.getTcpDestinationPort().getValue());
			        		}
			        		if (tcpMatch.getTcpSourcePort() != null) {
			        			flowBuilder.setSrcPort(tcpMatch.getTcpSourcePort().getValue());
			        		}
			        	}
			        	///---------------Extract Network Protocol information
			        	if (flow.getMatch().getIpMatch().getIpProto() != null){
			        		if (flow.getMatch().getIpMatch().getIpProto().getIntValue() == 1){
			        			flowBuilder.setTypeOfTraffic(TrafficType.ICMP);
			        		}
			        		if (flow.getMatch().getIpMatch().getIpProto().getIntValue() == 6){
			        			flowBuilder.setTypeOfTraffic(TrafficType.TCP);
			        			if (flow.getMatch().getEthernetMatch().getEthernetType() != null) {
					        		if (flow.getMatch().getEthernetMatch().getEthernetType().getType().getValue() == 0x0800){
					        			if (flow.getMatch().getLayer4Match() != null) {
					        				TcpMatch tcpMatch = (TcpMatch) flow.getMatch().getLayer4Match();
					        				if (tcpMatch.getTcpDestinationPort() != null) {
					        					if (tcpMatch.getTcpDestinationPort().getValue() == 80) {
					        						flowBuilder.setTypeOfTraffic(TrafficType.HTTP);
					        					}
					        					if (tcpMatch.getTcpDestinationPort().getValue() == 443) {
					        						flowBuilder.setTypeOfTraffic(TrafficType.HTTPS);
					        					}
					        				}
					        			}
					        		}
					        	}
			        		}
			        		if (flow.getMatch().getIpMatch().getIpProto().getIntValue() == 17){
			        			flowBuilder.setTypeOfTraffic(TrafficType.UDP);
			        			if (flow.getMatch().getEthernetMatch().getEthernetType() != null) {
					        		if (flow.getMatch().getEthernetMatch().getEthernetType().getType().getValue() == 0x0800){
					        			if (flow.getMatch().getLayer4Match() != null) {
					        				TcpMatch tcpMatch = (TcpMatch) flow.getMatch().getLayer4Match();
					        				if (tcpMatch.getTcpDestinationPort() != null) {
					        					if (tcpMatch.getTcpDestinationPort().getValue() == 53) {
					        						flowBuilder.setTypeOfTraffic(TrafficType.DNS);
					        					}
					        					if (tcpMatch.getTcpDestinationPort().getValue() == 67) {
					        						flowBuilder.setTypeOfTraffic(TrafficType.DHCP);
					        					}
					        				}
					        			}
					        		}
					        	}
			        		}
			        		
			        	} 
			        	//---------------Extract ARP Traffic Info ---------------
			        	if (flow.getMatch().getEthernetMatch().getEthernetType() != null) {
			        		if (flow.getMatch().getEthernetMatch().getEthernetType().getType().getValue() == 0x0806){
			        			flowBuilder.setTypeOfTraffic(TrafficType.ARP);
			        		}
			        	}
			        	//----------------------------Extract Action Information ---------------------
			        	Instructions instructions = flow.getInstructions();
			        	for (Instruction instruction : instructions.getInstruction()){
			        		ApplyActionsCase applyActionCase = (ApplyActionsCase) instruction.getInstruction();
			        		ApplyActions applyAction = applyActionCase.getApplyActions();
			        		for (Action action : applyAction.getAction()) {
			        			if (action.getAction() instanceof OutputActionCase) {
			        				OutputActionCase outputCase = (OutputActionCase) action.getAction();
			        				String outputPort = outputCase.getOutputAction().getOutputNodeConnector().getValue().split(":")[2];
			        				flowBuilder.setActionOutputPort(outputPort);
			        			} else if (action.getAction() instanceof DropActionCase){
			        				flowBuilder.setActionOutputPort("0");
			        			} else if (action.getAction() instanceof SetNwSrcActionCase) {
			        				SetNwSrcActionCase setNwSrcCase = (SetNwSrcActionCase) action.getAction();
			        				Ipv4 ipv4Address = (Ipv4) setNwSrcCase.getSetNwSrcAction().getAddress();
			        				flowBuilder.setActionSetSourceIpv4Address(ipv4Address.getIpv4Address().getValue());
			        				
			        			} else if (action.getAction() instanceof SetNwDstActionCase) {
			        				SetNwDstActionCase setNwDstCase = (SetNwDstActionCase) action.getAction();
			        				Ipv4 ipv4Address = (Ipv4) setNwDstCase.getSetNwDstAction().getAddress();
			        				flowBuilder.setActionSetDstIpv4Address(ipv4Address.getIpv4Address().getValue());
			        				
			        			} else if (action.getAction() instanceof SetTpSrcActionCase) {
			        				SetTpSrcActionCase setTpCase = (SetTpSrcActionCase) action.getAction();
			        				flowBuilder.setActionSetTcpSrcPort(setTpCase.getSetTpSrcAction().getPort().getValue());
			        				
			        			} else if (action.getAction() instanceof SetTpDstActionCase) {
			        				SetTpDstActionCase setTpCase = (SetTpDstActionCase) action.getAction();
			        				flowBuilder.setActionSetTcpDstPort(setTpCase.getSetTpDstAction().getPort().getValue());
			        				
			        			} else if (action.getAction() instanceof SetNwTosActionCase) {
			        				SetNwTosActionCase setNwTosCase = (SetNwTosActionCase) action.getAction();
			        				flowBuilder.setActionSetIpv4Tos(setNwTosCase.getSetNwTosAction().getTos());
			        				
			        			} else if (action.getAction() instanceof SetNwTtlActionCase) {
			        				SetNwTtlActionCase setNwTtlCase = (SetNwTtlActionCase) action.getAction();
			        				flowBuilder.setActionSetIpv4Ttl(setNwTtlCase.getSetNwTtlAction().getNwTtl());
			        			}
			        		}
			        	}
			        	flowRuleList.add(flowBuilder.build());
			        }///////////////////--------------------End of IF Flow != NULL
				} //...................End of For loop Iterating through Flows
				// ------------ Create output object of the RPC
				flowRulesOutputBuilder.setFlowRules(flowRuleList);
				return RpcResultBuilder.success(flowRulesOutputBuilder.build()).buildFuture();
			} // End of IF No Flows IN Table 0
		} //End of IF There Are no Tables in the Switch
		
		return null;
	}
	
	@Override
	public Future<RpcResult<RemoveAFlowRuleFromSwitchOutput>> removeAFlowRuleFromSwitch(
			RemoveAFlowRuleFromSwitchInput input) {
		RemoveAFlowFromSwitchInputBuilder flowRemoveInputBuilder = new RemoveAFlowFromSwitchInputBuilder();
		flowRemoveInputBuilder.setFlowKey(input.getFlowKey());
		flowRemoveInputBuilder.setTableId((short) 0);
		flowRemoveInputBuilder.setNodeId(new NodeId("openflow:" + Integer.toString(input.getSwitchId())));
		Future<RpcResult<RemoveAFlowFromSwitchOutput>> output = this.tapService.removeAFlowFromSwitch(flowRemoveInputBuilder.build());
		if (output != null) {
			RemoveAFlowRuleFromSwitchOutputBuilder outputBuilder = new RemoveAFlowRuleFromSwitchOutputBuilder();
			outputBuilder.setStatus("Flow with ID " + input.getFlowKey() + " is successfully removed from Switch " + Integer.toString(input.getSwitchId()));
			return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
		}
		return null;
	}

	
	@Override
	public Future<RpcResult<RemoveEventFromSwitchOutput>> removeEventFromSwitch(
			RemoveEventFromSwitchInput input) {
		RemoveATapFromSwitchInputBuilder removeTapInputBuilder = new RemoveATapFromSwitchInputBuilder();
		removeTapInputBuilder.setNode(new NodeId("openflow:" + input.getSwitchId()));
		removeTapInputBuilder.setTapId(input.getEventId());
		Future<RpcResult<RemoveATapFromSwitchOutput>> output = this.tapService.removeATapFromSwitch(removeTapInputBuilder.build());
		if (output != null) {
			RemoveEventFromSwitchOutputBuilder outputBuilder = new RemoveEventFromSwitchOutputBuilder();
			outputBuilder.setStatus("Event with ID" + input.getEventId() + " From switch " + Integer.toString(input.getSwitchId()));
			return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
		}
		return null;
	}

	
	@Override
	public Future<RpcResult<SendPacketOutOutput>> sendPacketOut(
			SendPacketOutInput input) {
		NodeId nodeId = new NodeId("openflow:" + Integer.toString(input.getSwitchId()));
		NodeRef ingressNodeRef = InventoryUtils.getNodeRef(nodeId);

		NodeConnectorId ingressPortId = new NodeConnectorId(nodeId.getValue() + 
				":" + Integer.toString((int)input.getInPortNumber()));
		NodeConnectorRef ingressPortRef = InventoryUtils.getNodeConnectorRef(ingressPortId);
		
		NodeConnectorId egressPortId;
		NodeConnectorRef egressPortRef;
		if (input.getOutputPort() == FLOOD){
			egressPortId = InventoryUtils.getNodeConnectorId(nodeId, FLOOD_PORT_NUMBER);
			egressPortRef = InventoryUtils.getNodeConnectorRef(egressPortId);
		}
		else if (input.getOutputPort() == CONTROLLER) {
			egressPortId = InventoryUtils.getNodeConnectorId(nodeId, CONTROLLER_PORT_NUMBER);
			egressPortRef = InventoryUtils.getNodeConnectorRef(egressPortId);
		} 
		else {
			egressPortId = new NodeConnectorId(nodeId.getValue() + 
					":" + input.getOutputPort());
			egressPortRef = InventoryUtils.getNodeConnectorRef(egressPortId);
		}
		if (input.getOutputPort() == DROP) {
			return null;
		}
		TransmitPacketInputBuilder inputbuilder = new TransmitPacketInputBuilder();
        inputbuilder.setPayload(input.getPayload());
        inputbuilder.setNode(ingressNodeRef);
        inputbuilder.setEgress(egressPortRef);
        inputbuilder.setIngress(ingressPortRef);
         
        packetProcessingService.transmitPacket(inputbuilder.build());
        SendPacketOutOutputBuilder outputBuilder = new SendPacketOutOutputBuilder();
        outputBuilder.setStatus("Packet out is sent.");
       
		return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
		
		//Handle the Drop Port Case explicitly not just by ignoring.
	}
	
	////////////////////////////-------------------------------/////////////////////////////////
	//////----------------------Events generated---------------------------------------/////////
	////////////////////////////---------------------------------///////////////////////////////

}
