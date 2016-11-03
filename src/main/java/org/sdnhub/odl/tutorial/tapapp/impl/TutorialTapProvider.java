package org.sdnhub.odl.tutorial.tapapp.impl;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

import javax.xml.ws.handler.PortInfo;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
//import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
//import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.ControllerActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.FloodActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.controller.action._case.ControllerActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.flood.action._case.FloodActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
/*
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeterKey;
*/
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeaders;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Drop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.AddTapInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.AddTapOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.AddTapOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.CheckingInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.CheckingOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.CheckingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.ConnectedHosts;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.CookieToFlowid;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.CookieToFlowidBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.EventActions;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.FieldType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetAllHostsOnSwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetAllHostsOnSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetAllHostsOnSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetAllLinksOfSwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetAllLinksOfSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetAllLinksOfSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetAllSwitchesOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetAllSwitchesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetNetworkTopologyOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.GetNetworkTopologyOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.HostInfo;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallFlowInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallFlowOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallFlowRepository;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallPathBwNodesInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallPathBwNodesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallPathBwNodesOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallPathBwNodesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallPathInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallPathOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.InstallPathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.LocalIpv4Prefix;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MovePathInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MovePathOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MovePathOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MutateIpInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MutateIpOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MutateIpOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.NetworkLink;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.NodeNeighbors;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.NodeNeighborsBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAFlowFromSwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAFlowFromSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAFlowFromSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveATapFromSwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveATapFromSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveATapFromSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAllTapsFromSwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAllTapsFromSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveAllTapsFromSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveFlowsFromSwitchInput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveFlowsFromSwitchOutput;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.RemoveFlowsFromSwitchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TapSpecBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TrafficType;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.event.actions.TapActions;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.event.actions.tap.actions.BlockCase;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.event.actions.tap.actions.NotifyCase;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.forward.to.controller._case.*;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.forward.to.flood._case.ForwardToFloodBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.forward.to.port._case.ForwardToPortBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.set.dst.ipv4.address._case.SetDstIpv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.set.source.ipv4.address._case.SetSourceIpv4AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.flow.actions.*;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.*;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MalEvents;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.MalEventsBuilder;

//import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.ProtocolInfo;
//import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.ProtocolInfoBuilder;

import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TapService;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.TapSpec;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.add.tap.input.Tap1;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.connected.hosts.ConnectedHost;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.connected.hosts.ConnectedHostKey;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.cookie.to.flowid.Cookie;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.cookie.to.flowid.CookieBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.cookie.to.flowid.CookieKey;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.flow.actions.FlowActions;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.getallhostsonswitch.output.HostsInfo;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.getallhostsonswitch.output.HostsInfoBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.getnetworktopology.output.NetworkLinks;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.getnetworktopology.output.NetworkLinksBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.install.flow.input.AssociatedActions;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.install.flow.input.AssociatedActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.install.flow.input.NewFlow;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.install.flow.input.NewFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.install.flow.repository.NewFlow1;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mal.events.MalEvent;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mal.events.MalEventBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mutate.ip.input.mutation.end.BothCase;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mutate.ip.input.mutation.end.DstOnlyCase;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.mutate.ip.input.mutation.end.SourceOnlyCase;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.node.neighbors.CurrNode;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.node.neighbors.CurrNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.node.neighbors.CurrNodeKey;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.node.neighbors.curr.node.Neighbors;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.node.neighbors.curr.node.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.node.neighbors.curr.node.NeighborsKey;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.tap.spec.Tap;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.tap.spec.TapBuilder;
import org.opendaylight.yang.gen.v1.urn.sdnhub.tutorial.odl.tap.rev150601.tap.spec.TapKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.ConnectionCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;

//import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.arp.rev140528.ArpPacketReceived;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.sdnhub.odl.tutorial.utils.GenericTransactionUtils;
import org.sdnhub.odl.tutorial.utils.inventory.InventoryUtils;
import org.sdnhub.odl.tutorial.utils.openflow13.MatchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

interface RepeatFunction {
	InstallFlowInput performFunction (NodeConnectorId outputPort, Ipv4Prefix dstIp, NodeId nodeid);
	InstallFlowInput performFunction(NodeConnectorId outputPort, Ipv4Prefix curDstIp,
			Ipv4Prefix newDstIp, Ipv4Prefix curSrcIp, Ipv4Prefix newSrcIp,
			NodeId nodeid);
}

public class TutorialTapProvider implements AutoCloseable, DataChangeListener, OpendaylightInventoryListener, TapService{
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    //Members related to MD-SAL operations
    private List<Registration> registrations = Lists.newArrayList();
    private DataBroker dataBroker;
    private SalFlowService salFlowService;
    private final AtomicLong flowCookie = new AtomicLong();
    private HashMap<NodeId, List<String>> flowsInstalled = new HashMap<NodeId, List<String>>();
    private HashMap<NodeId, List<String>> tapsInstalled = new HashMap<NodeId, List<String>>();
    private TutorialL2Forwarding tutorialL2Forwarding;
    private ActiveSDNApi activeSDNApi;
	private ActiveSDNAssignment activeSDNAssignment;
    		
    public TutorialTapProvider(DataBroker dataBroker, NotificationProviderService notificationService, RpcProviderRegistry rpcProviderRegistry) {
    	this.activeSDNAssignment = new ActiveSDNAssignment(dataBroker, notificationService, rpcProviderRegistry);
    	this.tutorialL2Forwarding = new TutorialL2Forwarding(dataBroker, notificationService, rpcProviderRegistry, this.activeSDNAssignment);
    	this.activeSDNApi = new ActiveSDNApi(dataBroker, notificationService, rpcProviderRegistry);
    	
        //Store the data broker for reading/writing from inventory store
        this.dataBroker = dataBroker;
        //Object used for flow programming through RPC calls
        this.salFlowService = rpcProviderRegistry.getRpcService(SalFlowService.class);
        rpcProviderRegistry.addRpcImplementation(TapService.class, this);
        //initialize all containers in the data store to avoid initialization issues
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        InstanceIdentifier<TapSpec> newTapSpecIID = InstanceIdentifier.create(TapSpec.class);
        TapSpec tapSpec = new TapSpecBuilder().build();
        transaction.put(LogicalDatastoreType.CONFIGURATION, newTapSpecIID, tapSpec);
        
        InstanceIdentifier<CookieToFlowid> cookieIID = InstanceIdentifier.create(CookieToFlowid.class);
        CookieToFlowid cookie = new CookieToFlowidBuilder().build();
        transaction.put(LogicalDatastoreType.CONFIGURATION, cookieIID, cookie);
        
        InstanceIdentifier<NodeNeighbors> neighborsIID = InstanceIdentifier.create(NodeNeighbors.class);
        NodeNeighbors neighbor = new NodeNeighborsBuilder().build();
        transaction.put(LogicalDatastoreType.CONFIGURATION, neighborsIID, neighbor);
        //------------------------------------------------
        //List used to track notification (both data change and YANG-defined) listener registrations
        //this.registrations = registerDataChangeListeners();
        InstanceIdentifier<TapSpec> tapSpecIID = InstanceIdentifier.builder(TapSpec.class)
                .build();
        registerDataChangeListeners(tapSpecIID);
        
        InstanceIdentifier<InstallFlowRepository> flowIID = InstanceIdentifier.builder(InstallFlowRepository.class)
        		.build();
        registerDataChangeListeners(flowIID);

        //Register this object for receiving notifications when there are New switches
        registrations.add(notificationService.registerNotificationListener(this));
        
    }
 
    public void close() throws Exception {
        for (Registration registration : registrations) {
            registration.close();
        }
        for (NodeId nodeId : flowsInstalled.keySet()){
        	for (String flowId: flowsInstalled.get(nodeId)){
                String flowIdStr = flowId;
                	
            	FlowBuilder flowBuilder = new FlowBuilder();
            	FlowKey key = new FlowKey(new FlowId(flowIdStr));
            	flowBuilder.setFlowName(flowIdStr);
            	flowBuilder.setKey(key);
            	flowBuilder.setId(new FlowId(flowIdStr));
            	flowBuilder.setTableId((short)0);
                	
            	InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
            			.child(Node.class, new NodeKey(nodeId))
            			.augmentation(FlowCapableNode.class)
            			.child(Table.class, new TableKey(flowBuilder.getTableId()))
            			.child(Flow.class, new FlowKey(flowBuilder.getKey()))
            			.build();
                	
            	GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), false);
            }
        }
        for (NodeId nodeId : tapsInstalled.keySet()){
        	for (String tapId: tapsInstalled.get(nodeId)){
                String flowIdStr = "Tap_" + tapId + "_NodeID_" + nodeId.getValue();
                //remove tap from tap-spec datastore	
                InstanceIdentifier<Tap> tapIID = InstanceIdentifier.builder(TapSpec.class)
                		.child(Tap.class, new TapKey(tapId))
                		.build();
                TapBuilder tapB = new TapBuilder();
                tapB.setId(tapId);
                GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, tapIID, tapB.build(), false);
                
                //Remove Tap from the switch
            	FlowBuilder flowBuilder = new FlowBuilder();
            	FlowKey key = new FlowKey(new FlowId(flowIdStr));
            	flowBuilder.setFlowName(flowIdStr);
            	flowBuilder.setKey(key);
            	flowBuilder.setId(new FlowId(flowIdStr));
            	flowBuilder.setTableId((short)0);
                	
            	InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
            			.child(Node.class, new NodeKey(nodeId))
            			.augmentation(FlowCapableNode.class)
            			.child(Table.class, new TableKey(flowBuilder.getTableId()))
            			.child(Flow.class, new FlowKey(flowBuilder.getKey()))
            			.build();
                	
            	GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), false);
            }
        }
        flowsInstalled.clear();
        registrations.clear();
    }
    /////////////////////////////////////////////////////////////////////////////////////
    //private List<Registration> registerDataChangeListeners(InstanceIdentifier<?> iid) {
    private void registerDataChangeListeners(InstanceIdentifier<?> iid) {
        Preconditions.checkNotNull(dataBroker);
        //List<Registration> registrations = Lists.newArrayList();
        try {
            //Register listener for config updates and topology
            //InstanceIdentifier<TapSpec> tapSpecIID = InstanceIdentifier.builder(TapSpec.class)
            //        .build();
            ListenerRegistration<DataChangeListener> registration = dataBroker.registerDataChangeListener(
                    LogicalDatastoreType.CONFIGURATION,
                    iid, this, AsyncDataBroker.DataChangeScope.SUBTREE);
            LOG.debug("         ---------------------------------------------------------------------     ");
            LOG.debug("DataChangeListener registered with MD-SAL for path {}", iid);
            LOG.debug("         ---------------------------------------------------------------------     ");
            this.registrations.add(registration);
            //this.registrations.add(registration);

        } catch (Exception e) {
            LOG.error("Exception reached {}", e);
        }
        //return registrations;
    }
    /////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {   	
        LOG.debug("Data changed: {} created, {} updated, {} removed",
                change.getCreatedData().size(), change.getUpdatedData().size(), change.getRemovedPaths().size());
        DataObject dataObject;
       
        // Iterate over any created nodes or interfaces
        //change.get-- provides a set but here as we are using it as iterator therefore we use ().entrySet()
        
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change.getCreatedData().entrySet()) {
            dataObject = entry.getValue();
            LOG.debug("ADDED Path {}, Object {}", entry.getKey(), dataObject);
            if (dataObject instanceof Tap){
            	programTap((Tap)dataObject);
            }
            else if (dataObject instanceof NewFlow1){
            	callProgramFlow((NewFlow1)dataObject);
            }
        }

        // Iterate over any deleted nodes or interfaces
        Map<InstanceIdentifier<?>, DataObject> originalData = change.getOriginalData();
        for (InstanceIdentifier<?> path : change.getRemovedPaths()) {
            dataObject = originalData.get(path);
            LOG.debug("REMOVED Path {}, Object {}", path, dataObject);
            if (dataObject instanceof Tap){
            	removeTap((Tap)dataObject);
            }
            else if (dataObject instanceof NewFlow1){
            	removeFlow((NewFlow1)dataObject);
            }
        }

        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change.getUpdatedData().entrySet()) {
            dataObject = entry.getValue();
            DataObject originalDataObject = originalData.get(entry.getKey());
            LOG.debug("UPDATED Path {}, New Object {}, Old Object {}", entry.getKey(), dataObject, originalDataObject);
        }
       
    }
    /////////////////////////////////////////////////////////////////////////////////////    
    private void programTap(Tap tap) {

        NodeId nodeId = tap.getNode();
        String tapId = tap.getId();
        LOG.debug("         ---------------------------------------------------------------------     ");
		LOG.debug("ProgramTap is called for Node {}", nodeId);
		LOG.debug("         ---------------------------------------------------------------------     ");
        //Creating match object
        MatchBuilder matchBuilder = new MatchBuilder();   
        
        if (tap.getSourceMacAddress() != null){
			MatchUtils.createEthSrcMatch(matchBuilder, tap.getSourceMacAddress());
		}	
		if (tap.getDstMacAddress() != null){
			MatchUtils.createEthDstMatch(matchBuilder, tap.getDstMacAddress(), null);
		}
		if (tap.getSourceIpAddress() != null){
			MatchUtils.createSrcL3IPv4Match(matchBuilder, new Ipv4Prefix(tap.getSourceIpAddress().getValue()));
		}
		if (tap.getDstIpAddress() != null){
			MatchUtils.createDstL3IPv4Match(matchBuilder, new Ipv4Prefix(tap.getDstIpAddress().getValue()));
		}
        
        if (tap.getTrafficMatch() != null) {
            Integer dlType = null;
            Short nwProto = null;
            Integer tpPort = null;
            switch (tap.getTrafficMatch()) {
            case ARP:
                dlType = 0x806;
                break;
            case ICMP:
                dlType = 0x800;
                nwProto = 1;
                break;
            case TCP:
                dlType = 0x800;
                nwProto = 6;
                break;
            case HTTP:
                dlType = 0x800;
                nwProto = 6;
                tpPort = 80;
                break;
            case HTTPS:
                dlType = 0x800;
                nwProto = 6;
                tpPort = 443;
                break;
            case UDP:
                dlType = 0x800;
                nwProto = 0x11;
                break;
            case DNS:
                dlType = 0x800;
                nwProto = 0x11;
                tpPort = 53;
                break;
            case DHCP:
                dlType = 0x800;
                nwProto = 0x11;
                tpPort = 67;
            }
            if (dlType != null) {
                MatchUtils.createEtherTypeMatch(matchBuilder, dlType.longValue());
            }
            if (nwProto != null) {
                MatchUtils.createIpProtocolMatch(matchBuilder, nwProto);
                if (tpPort != null && nwProto == 6) {
                    MatchUtils.createSetDstTcpMatch(matchBuilder, new PortNumber(tpPort));
                } else if (tpPort != null && nwProto == 17) {
                    MatchUtils.createSetDstUdpMatch(matchBuilder, new PortNumber(tpPort));
                }
            }
        }
        MatchUtils.createIPv4BestEffortTOSMatch(matchBuilder, (short)0);
                
        NodeConnectorId inPortId1 = null;
		if (tap.getInPortConnector() != null){
			NodeConnectorId inPortId = tap.getInPortConnector();
		    MatchUtils.createInPortMatch(matchBuilder, inPortId);
		}

    	List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<Action> actionList = Lists.newArrayList();

		NodeConnectorRef controllerPortRef = InventoryUtils.getControllerNodeConnectorRef(nodeId);
        NodeConnectorId controllerPortId = InventoryUtils.getNodeConnectorId(controllerPortRef);
            
        int outputIndex = 0;
        OutputActionBuilder output = new OutputActionBuilder();
        output.setOutputNodeConnector(controllerPortId);
        output.setMaxLength(65535); //Send full packet and No buffer
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(outputIndex));
        ab.setOrder(outputIndex++);
        actionList.add(ab.build());

        // Create Apply Actions Instruction
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());
            
        // Instructions List Stores Individual Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        isb.setInstruction(instructions);
        
        //FlowCookie flowck = new FlowCookie(BigInteger.valueOf(flowCookie.incrementAndGet()));
        //String cookieID = nodeId.toString() + "." + flowck.toString();

        //String flowIdStr = "Tap_" + tapId + "_NodeID_" + nodeId.getValue();
        FlowCookie flowck = new FlowCookie(BigInteger.valueOf((long)Integer.parseInt(tapId)));
        String flowIdStr = tapId;
        
        FlowBuilder flowBuilder = new FlowBuilder();
        FlowKey key = new FlowKey(new FlowId(flowIdStr));

        flowBuilder.setKey(key);
        flowBuilder.setId(new FlowId(flowIdStr));
        flowBuilder.setFlowName(flowIdStr);
        flowBuilder.setTableId((short)0);
        flowBuilder.setBarrier(true);
        flowBuilder.setPriority(30000);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setCookie(flowck);
        flowBuilder.setCookieMask(flowck);
        
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setInstructions(isb.build());
     
        //Program flow by adding it to the flow table in the opendaylight-inventory
        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
        		.child(Node.class, new NodeKey(nodeId))
        		.augmentation(FlowCapableNode.class)
        		.child(Table.class, new TableKey(flowBuilder.getTableId()))
        		.child(Flow.class, new FlowKey(flowBuilder.getKey()))
        		.build();
        
        GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), true);

        CookieBuilder cookieBuilder = new CookieBuilder();
        cookieBuilder.setId(flowck.toString());
        //cookieBuilder.setId(flowIdStr);
        cookieBuilder.setTapid(tapId);
        cookieBuilder.setFlowid(flowIdStr);
        
        Cookie cookie = cookieBuilder.build();
        
        InstanceIdentifier<Cookie> cookieIID = InstanceIdentifier.builder(CookieToFlowid.class)
        		.child(Cookie.class, new CookieKey(flowck.toString()))
        		.build();
        
        boolean cookieAdded = GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, cookieIID, cookie, true);
        if (cookieAdded == true){
        	LOG.debug("         ---------------------------------------------------------------------     ");
    		LOG.debug("Cookied is successfully added {}", flowck.toString());
    		LOG.debug("         ---------------------------------------------------------------------     ");
        }
            
            /*
            CookieToFlowidBuilder cookieFlowBuilder = new CookieToFlowidBuilder();
            cookieFlowBuilder.setCookie(cookieID);
            cookieFlowBuilder.setTapid(tapId);
            cookieFlowBuilder.setFlowid(flowIdStr);
            CookieToFlowid cookieFlow = cookieFlowBuilder.build();
            
            InstanceIdentifier<CookieToFlowid> cookieFlowIID = InstanceIdentifier.builder(CookieToFlowid.class).build();
 
            MeterKey meterKey = new MeterKey(new MeterId((long) 2000));
            InstanceIdentifier<Meter> meterIID = InstanceIdentifier.builder(Nodes.class)
            		.child(Node.class, new NodeKey(nodeId))
            		.augmentation(FlowCapableNode.class)
            		.child(Meter.class, meterKey)
            		.build();
            
            long BURST_SIZE = 10;
            long DROP_RATE = 100;

            */
    }
    /////////////////////////////////////////////////////////////////////////////////////
    private void removeTap(Tap tap) {
    	NodeId nodeId = tap.getNode();
    	String tapId = tap.getId();
    	NodeConnectorId  srcNodeConnector = tap.getInPortConnector();
    	String flowIdStr = "Tap_" + tapId + "_NodeID_" + nodeId.getValue();
    	
    	FlowBuilder flowBuilder = new FlowBuilder();
    	FlowKey key = new FlowKey(new FlowId(flowIdStr));
    	flowBuilder.setFlowName(flowIdStr);
    	flowBuilder.setKey(key);
    	flowBuilder.setId(new FlowId(flowIdStr));
    	flowBuilder.setTableId((short)0);
    	
    	InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
    			.child(Node.class, new NodeKey(nodeId))
    			.augmentation(FlowCapableNode.class)
    			.child(Table.class, new TableKey(flowBuilder.getTableId()))
    			.child(Flow.class, new FlowKey(flowBuilder.getKey()))
    			.build();
    	
    	GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), false);
    	
    }
    /////////////////////////////////////////////////////////////////////////////////////   
    private void removeFlow(NewFlow1 flow1) {
    	//Fix this issue of passing nodeid to removeflow
    	//NodeId nodeId = flow1.getNode();
    
    	NodeId nodeId = new NodeId("openflow:1");
    	String flowId = flow1.getId();
    	NodeConnectorId  srcNodeConnector = flow1.getInPort();
        String flowIdStr = "Flow_" + flowId + "SrcPort" + srcNodeConnector.toString();
        	
    	FlowBuilder flowBuilder = new FlowBuilder();
    	FlowKey key = new FlowKey(new FlowId(flowIdStr));
    	flowBuilder.setFlowName(flowIdStr);
    	flowBuilder.setKey(key);
    	flowBuilder.setId(new FlowId(flowIdStr));
    	flowBuilder.setTableId((short)0);
        	
    	InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
    			.child(Node.class, new NodeKey(nodeId))
    			.augmentation(FlowCapableNode.class)
    			.child(Table.class, new TableKey(flowBuilder.getTableId()))
    			.child(Flow.class, new FlowKey(flowBuilder.getKey()))
    			.build();
        	
    	GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), false);
    	
    }
    /////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onNodeRemoved(NodeRemoved nodeRemoved) {
        LOG.debug("Node removed {}", nodeRemoved);
        
      //Remove all flows using RPC call to MD-SAL Flow Service
        //RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder()
        //    .setBarrier(true)
        //    .setNode(nodeRemoved.getNodeRef());
        //salFlowService.removeFlow(flowBuilder.build());
    }
    /////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onNodeConnectorRemoved(NodeConnectorRemoved nodeConnectorRemoved) {
        LOG.debug("Node connector removed {}", nodeConnectorRemoved);
    }
    /////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onNodeUpdated(NodeUpdated nodeUpdated) {
        LOG.debug("Node updated {}", nodeUpdated);
        NodeId nodeId = nodeUpdated.getId();
        
        FlowCapableNodeUpdated switchDesc = nodeUpdated.getAugmentation(FlowCapableNodeUpdated.class);
        if (switchDesc != null) {
            LOG.info("Node {}, OpenFlow description {}", nodeId, switchDesc);
        }

        //==================================================
    
        MatchBuilder matchBuilder = new MatchBuilder();
        //Integer dlType = 0x806;
        //MatchUtils.createEtherTypeMatch(matchBuilder, dlType.longValue());
        
        List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<Action> actionList = Lists.newArrayList();
        
		OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(65535);
        Uri value = new Uri("CONTROLLER");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());
        
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(1));
        
        instructions.add(ib.build());
        
        /* Instructions List Stores Individual Instructions
        ApplyActionsBuilder aab1 = new ApplyActionsBuilder();
        ActionBuilder ab1 = new ActionBuilder();
        List<Action> actionList1 = Lists.newArrayList();
        
		OutputActionBuilder output1 = new OutputActionBuilder();
        output1.setMaxLength(65535);
        Uri value1 = new Uri("FLOOD");
        output1.setOutputNodeConnector(value1);
        ab1.setAction(new OutputActionCaseBuilder().setOutputAction(output1.build()).build());
        ab1.setOrder(0);
        ab1.setKey(new ActionKey(0));
        actionList1.add(ab1.build());
        
        aab1.setAction(actionList1);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab1.build()).build());
        ib.setOrder(1);
        ib.setKey(new InstructionKey(2));
        instructions.add(ib.build());
        */
        InstructionsBuilder isb = new InstructionsBuilder();
        isb.setInstruction(instructions);
        
        String flowIdStr = "0";
        FlowBuilder flowBuilder = new FlowBuilder();
        FlowKey key = new FlowKey(new FlowId(flowIdStr));

        flowBuilder.setKey(key);
        flowBuilder.setId(new FlowId(flowIdStr));
        flowBuilder.setFlowName(flowIdStr);
        flowBuilder.setTableId((short)0);
        flowBuilder.setBarrier(true);
        flowBuilder.setPriority(150);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        
        //flowBuilder.setCookieMask(flowck);
        //flowBuilder.setStrict(false);
            
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setInstructions(isb.build());
        
        LOG.debug("         ---------------------------------------------------------------------     ");
		LOG.debug("Installing ARP flow Entry.");
		LOG.debug("         ---------------------------------------------------------------------     ");
		
        //Program flow by adding it to the flow table in the opendaylight-inventory
        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
        		.child(Node.class, new NodeKey(nodeId))
        		.augmentation(FlowCapableNode.class)
        		.child(Table.class, new TableKey(flowBuilder.getTableId()))
        		.child(Flow.class, new FlowKey(flowBuilder.getKey()))
        		.build();
        
        boolean status = GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), true);
        if (status == true){
        	LOG.debug("         ---------------------------------------------------------------------     ");
    		LOG.debug("					   Flow is added on node startup");
    		LOG.debug("         ---------------------------------------------------------------------     ");
        }
       
      /*
        InstallFlowInputBuilder flowInputBuilder = new InstallFlowInputBuilder();
        flowInputBuilder.setNode(nodeId);
        
        List<AssociatedActions> actionList = Lists.newArrayList();
		AssociatedActionsBuilder actionBuilder = new AssociatedActionsBuilder();
		ForwardToControllerBuilder controllerBuilder = new ForwardToControllerBuilder();
		actionBuilder.setFlowActions(new ForwardToControllerCaseBuilder().
				setForwardToController(controllerBuilder.build()).build());
		actionBuilder.setId((long)0);
		actionList.add(actionBuilder.build());
		flowInputBuilder.setAssociatedActions(actionList);
		
		NewFlowBuilder newFlowBuilder = new NewFlowBuilder();
		//newFlowBuilder.setTrafficMatch(TrafficType.ARP);
		newFlowBuilder.setFlowPriority(150);
		newFlowBuilder.setIdleTimeout(0);
		newFlowBuilder.setHardTimeout(0);
		flowInputBuilder.setNewFlow(newFlowBuilder.build());
		
		this.installFlow(flowInputBuilder.build());
 */
        //I assume that in my case it is always due to node added so we don't need to remove flows.
        //Remove all flows using RPC call to MD-SAL Flow Service
        //RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder()
        //    .setBarrier(true)
        //    .setNode(InventoryUtils.getNodeRef(nodeId));
        //salFlowService.removeFlow(flowBuilder.build());
    }
    /////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onNodeConnectorUpdated(NodeConnectorUpdated nodeConnectorUpdated) {
        LOG.debug("NodeConnector updated {}", nodeConnectorUpdated);
        NodeId nodeId = InventoryUtils.getNodeId(nodeConnectorUpdated.getNodeConnectorRef());
        FlowCapableNodeConnectorUpdated portDesc = nodeConnectorUpdated.getAugmentation(FlowCapableNodeConnectorUpdated.class);
        if (portDesc != null) {
            LOG.info("Node {}, OpenFlow Port description {}", nodeId, portDesc);
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Future<RpcResult<AddTapOutput>> addTap(AddTapInput input) {
		LOG.debug("         ---------------------------------------------------------------------     ");
		LOG.debug("Add Tap RPC is Called.");
		LOG.debug("         ---------------------------------------------------------------------     ");
		
		List<Tap1> tap1 = input.getTap1();
		Tap1 tappy = tap1.get(0);
		
		TapBuilder tapBuilder = new TapBuilder();
		tapBuilder.setId(tappy.getId());
		tapBuilder.setNode(tappy.getNode());
		if (tappy.getInPortConnector() != null){
			tapBuilder.setInPortConnector(tappy.getInPortConnector());
		}
		if (tappy.getSourceIpAddress() != null){
			tapBuilder.setSourceIpAddress(tappy.getSourceIpAddress());
		}
		if (tappy.getSourceMacAddress() != null){
			tapBuilder.setSourceMacAddress(tappy.getSourceMacAddress());
		}
		if (tappy.getDstIpAddress() != null){
			tapBuilder.setDstIpAddress(tappy.getDstIpAddress());
		}
		if (tappy.getDstMacAddress() != null){
			tapBuilder.setDstMacAddress(tappy.getDstMacAddress());
		}
		if (tappy.getTrafficMatch() != null){
			tapBuilder.setTrafficMatch(tappy.getTrafficMatch());
		}
		tapBuilder.setCount(tappy.getCount());
		tapBuilder.setDuration(tappy.getDuration());
		tapBuilder.setTapActions(tappy.getTapActions());
		
		

		LOG.debug("         ---------------------------------------------------------------------     ");
		LOG.debug("Finished building the TapBuilder.");
		LOG.debug("         ---------------------------------------------------------------------     ");
		Tap tap = tapBuilder.build();
		
		InstanceIdentifier<Tap> tapIID = InstanceIdentifier.create(TapSpec.class)
				.child(Tap.class, new TapKey(tappy.getId()));

		GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, tapIID, tap, true);
		
		String output_msg = "Tap is added to CONFIG Datastore";
		LOG.debug("         ---------------------------------------------------------------------     ");
		LOG.debug("Tap info added to CONFIG Datastore {}", tap);
		LOG.debug("         ---------------------------------------------------------------------     ");
		
		AddTapOutput output = new AddTapOutputBuilder()
        .setStatus(output_msg)
        .build();
		
		if (tapsInstalled.containsKey(tappy.getNode())){
        	tapsInstalled.get(tappy.getNode()).add(tappy.getId());
        }
        else {
        	List<String> list = Lists.newArrayList();
        	list.add(tappy.getId());
        	tapsInstalled.put(tappy.getNode(), list);
        }
		
		/*
		InstanceIdentifier<MalEvents> eventIID = InstanceIdentifier.builder(MalEvents.class).build();
		MalEventBuilder eventBuilder = new MalEventBuilder();
		eventBuilder.setCount((long)0);
		eventBuilder.setId(tappy.getId());
		MalEvent event = eventBuilder.build();
		MalEventsBuilder eventsBuilder = new MalEventsBuilder();
		eventsBuilder.setMalEvent(event);
		MalEvents malEvents = eventsBuilder.build();
		
		GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, eventIID, malEvents, true);
		LOG.debug("Event info added to CONFIG Datastore {}", malEvents);
		*/
		
		return RpcResultBuilder.success(output).buildFuture();	
	}	
    /////////////////////////////////////////////////////////////////////////////////////	
	private void callProgramFlow(NewFlow1 flow1){
		//Fix this as well
		/*
		NewFlowBuilder flowBuilder = new NewFlowBuilder();
		flowBuilder.setDstMacAddress(flow1.getDstMacAddress());
		flowBuilder.setSrcMacAddress(flow1.getSrcMacAddress());
		flowBuilder.setSrcIpAddress(flow1.getSrcIpAddress());
		flowBuilder.setDstIpAddress(flow1.getDstIpAddress());
		flowBuilder.setInPort(flow1.getInPort());
		flowBuilder.setId(flow1.getId());
		flowBuilder.setNode(flow1.getNode());
		flowBuilder.setOutPort(flow1.getOutPort());
		flowBuilder.setTrafficMatch(flow1.getTrafficMatch());
		flowBuilder.setCustomInfo(flow1.getCustomInfo());
		
		NewFlow flow = flowBuilder.build();
		boolean status = programFlow(flow);
		if (status == true){
			LOG.debug("Flow successfully installed {}", flow);
		}
		else {
			LOG.debug("Flow can't be installed.");
		}
		*/
	}
    /////////////////////////////////////////////////////////////////////////////////////	
	private String programFlow(NodeId nodeId, String flowKey, NewFlow flow, List<AssociatedActions> actions){
		
		//LOG.debug("         ---------------------------------------------------------------------     ");
		//LOG.debug("ProgramFlow is called for Node {}, Matching Criteria {}, Actions {}", nodeId, flow, actions);
		//LOG.debug("         ---------------------------------------------------------------------     ");
		long idleTimeOut = 0;
		long hardTimeOut = 0;
		long priority = 3000;
		MatchBuilder matchBuilder = new MatchBuilder();
        
		if (flow.getSrcMacAddress() != null){
			MatchUtils.createEthSrcMatch(matchBuilder, flow.getSrcMacAddress());
		}	
		if (flow.getDstMacAddress() != null){
			MatchUtils.createEthDstMatch(matchBuilder, flow.getDstMacAddress(), null);
		}
		if (flow.getSrcIpAddress() != null){
			MatchUtils.createSrcL3IPv4Match(matchBuilder, flow.getSrcIpAddress());
		}
		if (flow.getDstIpAddress() != null){
			MatchUtils.createDstL3IPv4Match(matchBuilder, flow.getDstIpAddress());
		}
		//LOG.debug("         ---------------------------------------------------------------------     ");
		//LOG.debug("Just finished creating source and destination IP and MAC addresses");
		//LOG.debug("         ---------------------------------------------------------------------     ");
		
        if (flow.getTrafficMatch() != null) {
            Integer dlType = null;
            Short nwProto = null;
            Integer tpPort = null;
            switch (flow.getTrafficMatch()) {
            case ARP:
                dlType = 0x806;
                idleTimeOut = 0;
                hardTimeOut = 0;
                break;
            case ICMP:
                dlType = 0x800;
                nwProto = 1;
                idleTimeOut = 0;
                hardTimeOut = 0;
                break;
            case TCP:
                dlType = 0x800;
                nwProto = 6;
                idleTimeOut = 0;
                hardTimeOut = 0;
                priority = 3000;
                break;
            case HTTP:
                dlType = 0x800;
                nwProto = 6;
                tpPort = 80;
                break;
            case HTTPS:
                dlType = 0x800;
                nwProto = 6;
                tpPort = 443;
                break;
            case UDP:
                dlType = 0x800;
                nwProto = 0x11;
                idleTimeOut = 0;
                hardTimeOut = 0;
                priority = 3000;
                break;
            case DNS:
                dlType = 0x800;
                nwProto = 0x11;
                tpPort = 53;
                break;
            case DHCP:
                dlType = 0x800;
                nwProto = 0x11;
                tpPort = 67;
                break;
            case CUSTOM:
            	dlType = flow.getCustomInfo().getDlType();
            	nwProto = flow.getCustomInfo().getNwProto();
            	tpPort = flow.getCustomInfo().getTpDst();
            	break;
            }
            if (dlType != null) {
                MatchUtils.createEtherTypeMatch(matchBuilder, dlType.longValue());
            }
            if (nwProto != null) {
                MatchUtils.createIpProtocolMatch(matchBuilder, nwProto);
                if (tpPort != null && nwProto == 6) {
                    MatchUtils.createSetDstTcpMatch(matchBuilder, new PortNumber(tpPort));
                } else if (tpPort != null && nwProto == 17) {
                    MatchUtils.createSetDstUdpMatch(matchBuilder, new PortNumber(tpPort));
                }
            }
        }
        
        //LOG.debug("         ---------------------------------------------------------------------     ");
		//LOG.debug("Just finished creating Traffic Type Information");
		//LOG.debug("         ---------------------------------------------------------------------     ");
		
		NodeConnectorId inPortId = null;
		if (flow.getInPort() != null){
			inPortId = flow.getInPort();
		    MatchUtils.createInPortMatch(matchBuilder, inPortId);
		}
		
        //LOG.debug("         ---------------------------------------------------------------------     ");
		//LOG.debug("Just starting creating Instruction and actions");
		//LOG.debug("         -------------------------------------------------------------------     ");
		
    	List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<Action> actionList = Lists.newArrayList();

        //For each sink node connector
        int outputIndex = 0;
        boolean setTos = true;
        boolean limitRate = true;
        int instructionIndex = 0;
        //meter-kbps
       
        for (AssociatedActions action: actions) {
        	if (action.getFlowActions() instanceof SetRateLimitCase) {
        		SetRateLimitCase rateLimitCase = (SetRateLimitCase) action.getFlowActions();
        		
        		MeterId mID = new MeterId((long) 1);
        		String meterName = "NodeId_" + nodeId.getValue() + "_Meter_" + mID.getValue();

        		switch (rateLimitCase.getSetRateLimit().getMeterType()){
        		case MeterKbps:
        			InventoryUtils.createMeter(dataBroker, nodeId, mID.getValue(), meterName, 
            				rateLimitCase.getSetRateLimit().getBandRate(), 
            				rateLimitCase.getSetRateLimit().getBandBurstSize(), 
            				rateLimitCase.getSetRateLimit().getDropRate(),
            				rateLimitCase.getSetRateLimit().getDropBurstSize(), InventoryUtils.EnumMeterFlags.KBPS);
        			break;
        		case MeterPktps:
        			InventoryUtils.createMeter(dataBroker, nodeId, mID.getValue(), meterName, 
            				rateLimitCase.getSetRateLimit().getBandRate(), 
            				rateLimitCase.getSetRateLimit().getBandBurstSize(), 
            				rateLimitCase.getSetRateLimit().getDropRate(),
            				rateLimitCase.getSetRateLimit().getDropBurstSize(), InventoryUtils.EnumMeterFlags.PKPTS);
        			break;
        		case MeterBurst:
        			InventoryUtils.createMeter(dataBroker, nodeId, mID.getValue(), meterName, 
            				rateLimitCase.getSetRateLimit().getBandRate(), 
            				rateLimitCase.getSetRateLimit().getBandBurstSize(), 
            				rateLimitCase.getSetRateLimit().getDropRate(),
            				rateLimitCase.getSetRateLimit().getDropBurstSize(), InventoryUtils.EnumMeterFlags.BURSTS);
        			break;
        		case MeterStats:
        			InventoryUtils.createMeter(dataBroker, nodeId, mID.getValue(), meterName, 
            				rateLimitCase.getSetRateLimit().getBandRate(), 
            				rateLimitCase.getSetRateLimit().getBandBurstSize(), 
            				rateLimitCase.getSetRateLimit().getDropRate(),
            				rateLimitCase.getSetRateLimit().getDropBurstSize(), InventoryUtils.EnumMeterFlags.STATS);
        			
        			break;      			
        		}
        		
                //org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.
                //instruction.instruction.meter._case.MeterBuilder meterB = new org.opendaylight.
                //yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder();
                //meterB.setMeterId(mID);
                
                //ib.setInstruction(new MeterCaseBuilder().setMeter(meterB.build()).build());
                //ib.setOrder(instructionIndex);
                //ib.setKey(new InstructionKey(instructionIndex++));
                //instructions.add(ib.build());
                
                //rateLimitCase.getSetRateLimit
                OutputActionBuilder output = new OutputActionBuilder();
                output.setOutputNodeConnector(rateLimitCase.getSetRateLimit().getEgressPort());
                output.setMaxLength(65535); //Send full packet and No buffer
                ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
                ab.setKey(new ActionKey(outputIndex));
                ab.setOrder(outputIndex++);
                actionList.add(ab.build());
                //break;
        	}
        	else if (action.getFlowActions() instanceof ForwardToPortCase) {
        		ForwardToPortCase forwardtoPortCase = (ForwardToPortCase) action.getFlowActions();
        		OutputActionBuilder output = new OutputActionBuilder();
                output.setOutputNodeConnector(forwardtoPortCase.getForwardToPort().getOutputNodeConnector());
                output.setMaxLength(65535); //Send full packet and No buffer
                ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
                ab.setKey(new ActionKey(outputIndex));
                ab.setOrder(outputIndex++);
                actionList.add(ab.build());
        	}
        	else if (action.getFlowActions() instanceof ForwardToControllerCase){
        		ForwardToControllerCase forwardtoControllerCase = (ForwardToControllerCase) action.getFlowActions();
        		OutputActionBuilder output = new OutputActionBuilder();
                output.setMaxLength(65535);
                Uri value = new Uri("CONTROLLER");
                output.setOutputNodeConnector(value);
                ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
                ab.setOrder(0);
                ab.setKey(new ActionKey(0));
                actionList.add(ab.build());
        	}
        	else if (action.getFlowActions() instanceof ForwardToFloodCase){
        		ForwardToFloodCase forwardtoFloodCase = (ForwardToFloodCase) action.getFlowActions();
        		OutputActionBuilder output = new OutputActionBuilder();
                output.setMaxLength(65535);
                Uri value = new Uri("FLOOD");
                output.setOutputNodeConnector(value);
                ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
                ab.setOrder(0);
                ab.setKey(new ActionKey(0));
                actionList.add(ab.build());
        	}
        	else if (action.getFlowActions() instanceof DropPacketCase){
        		DropPacketCase dropPacketCase = (DropPacketCase) action.getFlowActions();
        		DropActionBuilder dropAction = new DropActionBuilder();
        		ab.setAction(new DropActionCaseBuilder().setDropAction(dropAction.build()).build());
        		ab.setKey(new ActionKey(outputIndex));
        		ab.setOrder(outputIndex++);
        		actionList.add(ab.build());
        	}
        	else if (action.getFlowActions() instanceof SetSourceIpv4AddressCase){
        		SetSourceIpv4AddressCase sourceIpv4Case = (SetSourceIpv4AddressCase) action.getFlowActions();
        		SetNwSrcActionBuilder nwSrcActionB = new SetNwSrcActionBuilder();
        		Ipv4Builder ipv4CaseBuilder = new Ipv4Builder();
        		ipv4CaseBuilder.setIpv4Address(sourceIpv4Case.getSetSourceIpv4Address().getValue());
        		Ipv4 ipv4Case = ipv4CaseBuilder.build();
        		nwSrcActionB.setAddress(ipv4Case);
        		ab.setAction(new SetNwSrcActionCaseBuilder().setSetNwSrcAction(nwSrcActionB.build()).build());
        		ab.setKey(new ActionKey(outputIndex));
        		ab.setOrder(outputIndex++);
        		actionList.add(ab.build());
        	}
        	else if (action.getFlowActions() instanceof SetDstIpv4AddressCase){
        		SetDstIpv4AddressCase dstIpv4Case = (SetDstIpv4AddressCase) action.getFlowActions();
        		SetNwDstActionBuilder nwDstActionB = new SetNwDstActionBuilder();
        		Ipv4Builder ipv4CaseBuilder = new Ipv4Builder();
        		ipv4CaseBuilder.setIpv4Address(dstIpv4Case.getSetDstIpv4Address().getValue());
        		Ipv4 ipv4Case = ipv4CaseBuilder.build();
        		nwDstActionB.setAddress(ipv4Case);
        		ab.setAction(new SetNwDstActionCaseBuilder().setSetNwDstAction(nwDstActionB.build()).build());
        		ab.setKey(new ActionKey(outputIndex));
        		ab.setOrder(outputIndex++);
        		actionList.add(ab.build());
        	}
        	else if (action.getFlowActions() instanceof SetIpv4TosCase){
        		SetIpv4TosCase ipv4TosCase = (SetIpv4TosCase) action.getFlowActions();
        		SetNwTosActionBuilder nwTosCaseBuilder = new SetNwTosActionBuilder();
        		nwTosCaseBuilder.setTos(ipv4TosCase.getSetIpv4Tos().getValue());
        		ab.setAction(new SetNwTosActionCaseBuilder().setSetNwTosAction(nwTosCaseBuilder.build()).build());
        		ab.setKey(new ActionKey(outputIndex));
        		ab.setOrder(outputIndex++);
        		actionList.add(ab.build());
        		setTos = false;
        	}
        	else if (action.getFlowActions() instanceof SetTcpSrcPortCase){
        		SetTcpSrcPortCase tcpSrcPortCase = (SetTcpSrcPortCase) action.getFlowActions();
        		SetTpSrcActionBuilder tpSrcActionBuilder = new SetTpSrcActionBuilder();
        		tpSrcActionBuilder.setPort(new PortNumber(tcpSrcPortCase.getSetTcpSrcPort().getPortNumber()));
        		ab.setAction(new SetTpSrcActionCaseBuilder().setSetTpSrcAction(tpSrcActionBuilder.build()).build());
        		ab.setKey(new ActionKey(outputIndex));
        		ab.setOrder(outputIndex++);
        		actionList.add(ab.build());
        	}
        	else if (action.getFlowActions() instanceof SetTcpDstPortCase){
        		SetTcpDstPortCase tcpDstPortCase = (SetTcpDstPortCase) action.getFlowActions();
        		SetTpDstActionBuilder tpDstActionBuilder = new SetTpDstActionBuilder();
        		tpDstActionBuilder.setPort(new PortNumber(tcpDstPortCase.getSetTcpDstPort().getPortNumber()));
        		ab.setAction(new SetTpDstActionCaseBuilder().setSetTpDstAction(tpDstActionBuilder.build()).build());
        		ab.setKey(new ActionKey(outputIndex));
        		ab.setOrder(outputIndex++);
        		actionList.add(ab.build());
        	}
        	else if (action.getFlowActions() instanceof SetIpv4TtlCase){
        		SetIpv4TtlCase ipv4TtlCase = (SetIpv4TtlCase) action.getFlowActions();
        		SetNwTtlActionBuilder nwTtlActionBuilder = new SetNwTtlActionBuilder();
        		nwTtlActionBuilder.setNwTtl((short)ipv4TtlCase.getSetIpv4Ttl().getTtlValue());
        		ab.setAction(new SetNwTtlActionCaseBuilder().setSetNwTtlAction(nwTtlActionBuilder.build()).build());
        		ab.setKey(new ActionKey(outputIndex));
        		ab.setOrder(outputIndex++);
        		actionList.add(ab.build());
        	}
        }

        if (setTos == true){
    		SetNwTosActionBuilder nwTosCaseBuilder = new SetNwTosActionBuilder();
    		nwTosCaseBuilder.setTos((int)0);
    		ab.setAction(new SetNwTosActionCaseBuilder().setSetNwTosAction(nwTosCaseBuilder.build()).build());
    		ab.setKey(new ActionKey(outputIndex));
    		ab.setOrder(outputIndex++);
    		actionList.add(ab.build());
        }
        // Create Apply Actions Instruction
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(instructionIndex);
        ib.setKey(new InstructionKey(instructionIndex++));
        
        instructions.add(ib.build());
        // Instructions List Stores Individual Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        isb.setInstruction(instructions);
        
        //FlowCookie flowck = new FlowCookie(BigInteger.valueOf(flowCookie.incrementAndGet()));
        FlowCookie flowck = new FlowCookie(BigInteger.valueOf((long) Integer.parseInt(flowKey)));
        
        String cookieID = nodeId.toString() + "." + flowck.toString();

        //LOG.debug("         ---------------------------------------------------------------------     ");
		//LOG.debug("Just starting creating Flow Builder");
		//LOG.debug("         ---------------------------------------------------------------------     ");
		
        //String flowIdStr = "Node_" + nodeId.getValue() + "_Flow_" + flowck.getValue().toString();
		String flowIdStr = flowck.getValue().toString();
        
        /*
         * Write code here to insert this flow information in local repository
         */
		
        FlowBuilder flowBuilder = new FlowBuilder();
        FlowKey key = new FlowKey(new FlowId(flowIdStr));

        flowBuilder.setKey(key);
        flowBuilder.setId(new FlowId(flowIdStr));
        flowBuilder.setFlowName(flowIdStr);
        flowBuilder.setTableId((short)0);
        flowBuilder.setBarrier(true);
        flowBuilder.setPriority(flow.getFlowPriority());
        flowBuilder.setHardTimeout(flow.getHardTimeout());
        flowBuilder.setIdleTimeout(flow.getIdleTimeout());
        flowBuilder.setCookie(flowck);
        

        //flowBuilder.setCookieMask(flowck);
        //flowBuilder.setStrict(false);
            
        flowBuilder.setMatch(matchBuilder.build());
        flowBuilder.setInstructions(isb.build());
        
        //LOG.debug("         ---------------------------------------------------------------------     ");
		//LOG.debug("Installing Flow to the switch through node repository.");
		//LOG.debug("         ---------------------------------------------------------------------     ");
		
        //Program flow by adding it to the flow table in the opendaylight-inventory
        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
        		.child(Node.class, new NodeKey(nodeId))
        		.augmentation(FlowCapableNode.class)
        		.child(Table.class, new TableKey(flowBuilder.getTableId()))
        		.child(Flow.class, new FlowKey(flowBuilder.getKey()))
        		.build();
        if (flowsInstalled.containsKey(nodeId)){
        	flowsInstalled.get(nodeId).add(flowIdStr);
        }
        else {
        	List<String> list = Lists.newArrayList();
        	list.add(flowIdStr);
        	flowsInstalled.put(nodeId, list);
        }
        
        boolean result = GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), true);
        if (result == true) return flowIdStr;
        else return null; 
	}
    /////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public Future<RpcResult<InstallFlowOutput>> installFlow(InstallFlowInput input) {
		
		//LOG.debug("         ---------------------------------------------------------------------     ");
		//LOG.debug("InstallFlow RPC is called with Input Object {}", input);
		//LOG.debug("         ---------------------------------------------------------------------     ");
		NodeId nodeId = input.getNode();
		NewFlow flow = input.getNewFlow();
		List<AssociatedActions> actions = input.getAssociatedActions();

		String flowIdStr;
		if (input.getFlowId() == null) {
			String flowId = BigInteger.valueOf(this.activeSDNApi.eventID.incrementAndGet()).toString();
			flowIdStr = programFlow(nodeId, flowId, flow, actions);
		} 
		else {
			flowIdStr = programFlow(nodeId, input.getFlowId(), flow, actions);
		}
		
		
		if (flowIdStr != null){
			String output_msg = "Flow is Installed in the Switch";
			//LOG.debug("         ---------------------------------------------------------------------     ");
			//LOG.debug("Flow is Installed to the switch {}", flow);
			//LOG.debug("         ---------------------------------------------------------------------     ");
			
			InstallFlowOutputBuilder output = new InstallFlowOutputBuilder();
			output.setStatus(output_msg);
			output.setFlowId(flowIdStr);
			return RpcResultBuilder.success(output.build()).buildFuture();
		}
		else{
			return null;
		}
	}
    /////////////////////////////////////////////////////////////////////////////////////	
	private List<Node> getNodes() {
		  InstanceIdentifier<Nodes> nodesIID = InstanceIdentifier.create(Nodes.class);
		  Nodes nodes = GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.CONFIGURATION, 
				  nodesIID);
				  
		  if (nodes == null) {
		   throw new RuntimeException("nodes are not found, pls add the node.");
		  }
		  return nodes.getNode();
	}
    /////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Future<RpcResult<GetAllSwitchesOutput>> getAllSwitches() {
		LOG.debug("         ---------------------------------------------------------------------     \n");
		LOG.debug("getAllSwitches RPC is called {}");
		LOG.debug("         ---------------------------------------------------------------------     \n");
		InstanceIdentifier<Nodes> nodesIID = InstanceIdentifier.create(Nodes.class);
		  Nodes nodes = GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.OPERATIONAL, 
				  nodesIID);
				  
		  if (nodes == null) {
			  LOG.debug("nodes are not found, pls add the node.");
			  return null;
		  }
		  List<NodeId> switchIds = Lists.newArrayList();
		  List<Node> switches = nodes.getNode();
		  for (Iterator<Node> iterator = switches.iterator(); iterator.hasNext();) {
			  NodeKey nodeKey = iterator.next().getKey();
			  switchIds.add(nodeKey.getId());
		  }
		  
		  GetAllSwitchesOutputBuilder allSwitchesOutput = new GetAllSwitchesOutputBuilder();
		  allSwitchesOutput.setNodes(switchIds);
		  GetAllSwitchesOutput output = allSwitchesOutput.build();
		  return RpcResultBuilder.success(output).buildFuture();
	}
    /////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Future<RpcResult<GetAllLinksOfSwitchOutput>> getAllLinksOfSwitch(
			GetAllLinksOfSwitchInput input) {
		// Returns all the ports of a switch
		List<NodeConnectorId> portIds = Lists.newArrayList();
		
		NodeKey nodeKey = new NodeKey(input.getNode());
		InstanceIdentifier<Node> nodeRef = InstanceIdentifier.create(Nodes.class)
				.child(Node.class, nodeKey);
		
		Node node = GenericTransactionUtils.readData(dataBroker, 
				LogicalDatastoreType.OPERATIONAL, nodeRef);
		if (node != null){
			if (node.getNodeConnector() != null) {
			     List<NodeConnector> ports = node.getNodeConnector();
			     for (Iterator<NodeConnector> iterator2 = ports.iterator(); iterator2
			    	       .hasNext();) {
			    	 NodeConnectorKey nodeConnectorKey = iterator2.next().getKey();
			    	 //test(InventoryUtils.getNodeConnectorRef(nodeConnectorKey.getId()));
			    	 portIds.add(nodeConnectorKey.getId());
			     }
			     GetAllLinksOfSwitchOutputBuilder outputBuilder = new GetAllLinksOfSwitchOutputBuilder();
			     outputBuilder.setLinks(portIds);
			     return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
			}
			else{
				LOG.debug("There are no ports available on Node {}", input.getNode());
				return null;
			}
		}
		else{
			LOG.debug("There are no ports available on Node {}", input.getNode());
			return null;
		}
	}
    /////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public Future<RpcResult<GetNetworkTopologyOutput>> getNetworkTopology() {
		String topologyId = "flow:1";
		InstanceIdentifier<Topology> topologyInstanceIdentifier = InstanceIdentifier.builder(NetworkTopology.class)
        .child(Topology.class, new TopologyKey(new TopologyId(topologyId))).build();
		Topology topology = GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.OPERATIONAL, topologyInstanceIdentifier);
		long linkIndex = 0;
		List<NetworkLinks> networkLinks = new ArrayList<NetworkLinks>();
		List<String> addedLinks = new ArrayList<String>();
		if (topology != null){
			List<Link> links = topology.getLink();
	        if (links == null || links.isEmpty()) {
	        	LOG.debug("         ---------------------------------------------------------------------     \n");
	            LOG.debug("No Links in the Topology");
	            LOG.debug("         ---------------------------------------------------------------------     \n");
	        }
	        else {
		        List<Link> internalLinks = new ArrayList<>();
		        for (Link link : links) {
		            if (!(link.getLinkId().getValue().contains("host"))) {
		                internalLinks.add(link);
		                NodeId sourceNodeId = new NodeId(link.getSource().getSourceNode().getValue());
			            NodeRef sourceNodeRef = InventoryUtils.getNodeRef(sourceNodeId);
			            NodeConnectorId sourceNodConnectorId = new NodeConnectorId(link.getSource().getSourceTp().getValue());
			            NodeConnectorRef sourceNodeConnectorRef = InventoryUtils.getNodeConnectorRef(sourceNodConnectorId);
			            
			            NodeId dstNodeId = new NodeId(link.getDestination().getDestNode().getValue());
			            NodeRef dstNodeRef = InventoryUtils.getNodeRef(dstNodeId);
			            NodeConnectorId dstNodConnectorId = new NodeConnectorId(link.getDestination().getDestTp().getValue());
			            NodeConnectorRef dstNodeConnectorRef = InventoryUtils.getNodeConnectorRef(dstNodConnectorId);
			           
			            String linkId = sourceNodeId.getValue() + "_" + dstNodeId.getValue();
			            if (!addedLinks.contains(linkId)){
			            	addedLinks.add(linkId);
			            	NetworkLinksBuilder netLinksBuilder = new NetworkLinksBuilder();
				            netLinksBuilder.setId(linkId);
				            netLinksBuilder.setSrcNode(sourceNodeId);
				            netLinksBuilder.setSrcNodeConnector(sourceNodConnectorId);
				            netLinksBuilder.setDstNode(dstNodeId);
				            netLinksBuilder.setDstNodeConnector(dstNodConnectorId);
				            NetworkLinks netLinks = netLinksBuilder.build();
				            networkLinks.add(netLinks);
			            }
			            String linkId1 = dstNodeId.getValue() + "_" + sourceNodeId.getValue();
			            if (!addedLinks.contains(linkId1)){
			            	addedLinks.add(linkId1);
			            	NetworkLinksBuilder netLinksBuilder = new NetworkLinksBuilder();
				            netLinksBuilder.setId(linkId1);
				            netLinksBuilder.setSrcNode(dstNodeId);
				            netLinksBuilder.setSrcNodeConnector(dstNodConnectorId);
				            netLinksBuilder.setDstNode(sourceNodeId);
				            netLinksBuilder.setDstNodeConnector(sourceNodConnectorId);
				            NetworkLinks netLinks = netLinksBuilder.build();
				            networkLinks.add(netLinks);
			            }       
			            
			            LOG.debug("         ---------------------------------------------------------------------     \n");
			            LOG.debug("LinkId {},  linkSource {}, LinksrcTP {}, linkdst {}, linkdsttp {}", link.getLinkId().getValue(),
			            		link.getSource().getSourceNode().getValue(), link.getSource().getSourceTp().getValue(),
			            		link.getDestination().getDestNode().getValue(), link.getDestination().getDestNode().getValue());
			            LOG.debug("         ---------------------------------------------------------------------     \n");
		            }
		            LOG.debug("         ---------------------------------------------------------------------     \n");
		            LOG.debug("LinkId {},  linkSource {}, LinksrcTP {}, linkdst {}, linkdsttp {}", link.getLinkId().getValue(),
		            		link.getSource().getSourceNode().getValue(), link.getSource().getSourceTp().getValue(),
		            		link.getDestination().getDestNode().getValue(), link.getDestination().getDestNode().getValue());
		            LOG.debug("         ---------------------------------------------------------------------     \n");
		        }
		        ////////////
		        GetNetworkTopologyOutputBuilder output = new GetNetworkTopologyOutputBuilder();
		        output.setNetworkLinks(networkLinks);
		        this.populateNeighors(output.build());
		        return RpcResultBuilder.success(output.build()).buildFuture();
	        }
		}
		return null;
	}
    /////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Future<RpcResult<CheckingOutput>> checking(CheckingInput input) {
		LOG.debug("         ---------------------------------------------------------------------     \n");
		LOG.debug("getAllSwitches RPC is called {}");
		LOG.debug("         ---------------------------------------------------------------------     \n");
		String note = input.getNote();
		CheckingOutputBuilder output = new CheckingOutputBuilder();
		output.setMsg(note + "_ho gaya na");
		return RpcResultBuilder.success(output.build()).buildFuture();
	}
    /////////////////////////////////////////////////////////////////////////////////////
	public String convertTime(long time){
	    Date date = new Date(time);
	    Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");
	    return format.format(date);
	}
/*

	@Override
	public Future<RpcResult<GetAllHostsOnSwitchOutput>> getAllHostsOnSwitch(
			GetAllHostsOnSwitchInput input) {
		List<NodeConnectorId> portIds = Lists.newArrayList();
		List<HostsInfo> hosts = new ArrayList<HostsInfo>();
		
		
		NodeKey nodeKey = new NodeKey(input.getNode());
		InstanceIdentifier<Node> nodeRef = InstanceIdentifier.create(Nodes.class)
				.child(Node.class, nodeKey);
		
		Node node = GenericTransactionUtils.readData(dataBroker, 
				LogicalDatastoreType.OPERATIONAL, nodeRef);
		List<Addresses> addresses = null;
		if (node != null){
			if (node.getNodeConnector() != null) {
			     List<NodeConnector> ports = node.getNodeConnector();
			     long hostIndex = 0;
			     for (Iterator<NodeConnector> iterator2 = ports.iterator(); iterator2
			    	       .hasNext();) {
			    	 NodeConnectorKey nodeConnectorKey = iterator2.next().getKey();
			    	 //iterator2 is the nodeconnetor
			    	 portIds.add(nodeConnectorKey.getId());
			    	 NodeConnector nc = null;
			    	 nc = (NodeConnector) GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.OPERATIONAL, 
			    			 (InstanceIdentifier<NodeConnector>) iterator2);
			    	 if (nc != null) {
			    		 AddressCapableNodeConnector acnc = (AddressCapableNodeConnector) nc
				                    .getAugmentation(AddressCapableNodeConnector.class);
			    		 if (acnc != null && acnc.getAddresses() != null) {
			                 // Search for this mac-ip pair in the existing address
			                 // observations & update last-seen timestamp
			    			 
			                 addresses = acnc.getAddresses();
			                 MacAddress hostMacAddress = addresses.get(0).getMac();
			                 IpAddress hostIpAddress = addresses.get(0).getIp();
			                 String firstSeen = convertTime((long) addresses.get(0).getFirstSeen());
			                 String lastSeen = convertTime((long)addresses.get(0).getLastSeen());
			                 
			                 HostsInfoBuilder hostInfoBuilder = new HostsInfoBuilder();
			                 hostInfoBuilder.setHostFirstSeen(firstSeen);
			                 hostInfoBuilder.setHostLastSeen(lastSeen);
			                 hostInfoBuilder.setHostIpAddress(hostIpAddress.getIpv4Address().getValue());
			                 hostInfoBuilder.setHostMacAddress(hostMacAddress.getValue());
			                 hostInfoBuilder.setId(++hostIndex);
			                 
			                 HostsInfo hostInfo = hostInfoBuilder.build();
			                 hosts.add(hostInfo);
			    		 }
			            
			    	 }
			            
			     }
			     GetAllHostsOnSwitchOutputBuilder output = new GetAllHostsOnSwitchOutputBuilder();
			     output.setHostsInfo(hosts);
			     return RpcResultBuilder.success(output.build()).buildFuture();
			     
			}
			else{
				LOG.debug("         ---------------------------------------------------------------------     \n");
				LOG.debug("There are no Hosts available on Node ");
				LOG.debug("         ---------------------------------------------------------------------     \n");
				return null;
			}
		}
		else{
			LOG.debug("         ---------------------------------------------------------------------     \n");
			LOG.debug("There are no ports available on Node ");
			LOG.debug("         ---------------------------------------------------------------------     \n");
			return null;
			
		}
	}
	


    private void test(NodeConnectorRef nodeConnectorRef){
    	

       // Read existing address observations from data tree
        ReadWriteTransaction readWriteTransaction = dataBroker.newReadWriteTransaction();

        NodeConnector nc = null;
        try {
          Optional<DataObject> dataObjectOptional = (Optional<DataObject>) readWriteTransaction.read(LogicalDatastoreType.OPERATIONAL, 
        		  nodeConnectorRef.getValue()).get();// not recommended option, one should implement listener on retuned ListenableFuture
          if(dataObjectOptional.isPresent())
            nc = (NodeConnector) dataObjectOptional.get(); 
        } catch(Exception e) {
        	LOG.debug("something");
          readWriteTransaction.commit();
          throw new RuntimeException("Error reading from operational store, node connector : " + nodeConnectorRef, e);
        }
        if(nc == null) {
          readWriteTransaction.commit();
          return;
        }
        InstanceIdentifier<Addresses> addrCapableNodeConnectors=InstanceIdentifier.builder(Nodes.class)
        		.child(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node.class)
        		.child(NodeConnector.class).augmentation(AddressCapableNodeConnector.class)
        		.child(Addresses.class).build();
    }
    */
    /////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public Future<RpcResult<GetAllHostsOnSwitchOutput>> getAllHostsOnSwitch(
			GetAllHostsOnSwitchInput input) {
		List<NodeConnectorId> portIds = Lists.newArrayList();
		List<HostsInfo> hosts = new ArrayList<HostsInfo>();
		
		
			  
		return null;
	}
	/////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public Future<RpcResult<RemoveFlowsFromSwitchOutput>> removeFlowsFromSwitch(
			RemoveFlowsFromSwitchInput input) {
		NodeId nodeId = input.getNode();
		if (flowsInstalled.containsKey(nodeId)){
			for (String flowId: flowsInstalled.get(nodeId)){
                String flowIdStr = flowId;
                	
            	FlowBuilder flowBuilder = new FlowBuilder();
            	FlowKey key = new FlowKey(new FlowId(flowIdStr));
            	flowBuilder.setFlowName(flowIdStr);
            	flowBuilder.setKey(key);
            	flowBuilder.setId(new FlowId(flowIdStr));
            	flowBuilder.setTableId((short)0);
                	
            	InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
            			.child(Node.class, new NodeKey(nodeId))
            			.augmentation(FlowCapableNode.class)
            			.child(Table.class, new TableKey(flowBuilder.getTableId()))
            			.child(Flow.class, new FlowKey(flowBuilder.getKey()))
            			.build();
                	
            	GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), false);
            }
			flowsInstalled.remove(nodeId);
			RemoveFlowsFromSwitchOutputBuilder output = new RemoveFlowsFromSwitchOutputBuilder();
			output.setStatus("Flows removed from " + nodeId.getValue());
			
			return RpcResultBuilder.success(output.build()).buildFuture();
		}
		else {
			RemoveFlowsFromSwitchOutputBuilder output = new RemoveFlowsFromSwitchOutputBuilder();
			output.setStatus("No flows Installed by Agile Api on " + nodeId.getValue());
			
			return RpcResultBuilder.success(output.build()).buildFuture();
		}
		
		
	}
	/////////////////////////////////////////////////////////////////////////////////////
	private Neighbors getPortInformation (NodeId currNode, NodeId neighborNode){
		InstanceIdentifier<Neighbors> neighborIID = InstanceIdentifier.builder(NodeNeighbors.class)
				.child(CurrNode.class, new CurrNodeKey(currNode))
				.child(Neighbors.class, new NeighborsKey(neighborNode))
				.build();
		Neighbors neighbor = GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.CONFIGURATION
				, neighborIID);
		if (neighbor != null){
			return neighbor;
		}
		else{
			LOG.debug("         -------------------------------------------             ");
			LOG.debug("NodeID {} is not Neighbor of {} .", neighborNode, currNode);
			LOG.debug("         -------------------------------------------             ");
			return null;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////
	private ConnectedHost getHostInfo(String ipAddress){
		LocalIpv4Prefix srcipPrefix = new LocalIpv4Prefix(ipAddress);
		InstanceIdentifier<ConnectedHost> hostIID = InstanceIdentifier.builder(ConnectedHosts.class)
				.child(ConnectedHost.class, new ConnectedHostKey(srcipPrefix))
				.build();
		ConnectedHost host = GenericTransactionUtils.readData(dataBroker, LogicalDatastoreType.OPERATIONAL, hostIID);
		if (host != null){
			return host;
		}
		else {
			LOG.debug("         -------------------------------------------             ");
			LOG.debug("Host with IP {} not found in the repository.", ipAddress);
			LOG.debug("         -------------------------------------------             ");
			return null;
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////
	private void populateNeighors(GetNetworkTopologyOutput topology){
		HashMap<String, CurrNodeBuilder> hpnodeBuilders = new HashMap<String, CurrNodeBuilder>();
		List<NetworkLinks> links = topology.getNetworkLinks();
		for (NetworkLinks link : links){
			String linkId = link.getId();
			NodeId currentNodeId = link.getSrcNode();
			if (hpnodeBuilders.containsKey(currentNodeId.getValue())){
				NeighborsBuilder neighBuilder = new NeighborsBuilder();
				neighBuilder.setNeighborNodeId(link.getDstNode());
				neighBuilder.setSrcPort(link.getSrcNodeConnector());
				neighBuilder.setNeighPort(link.getDstNodeConnector());
				hpnodeBuilders.get(currentNodeId.getValue()).getNeighbors().add(neighBuilder.build());
			}
			else {
				CurrNodeBuilder cNodeBuilder = new CurrNodeBuilder();
				cNodeBuilder.setCurrNodeId(currentNodeId);
				List<Neighbors> neighList = Lists.newArrayList();
				cNodeBuilder.setNeighbors(neighList);
				
				NeighborsBuilder neighBuilder = new NeighborsBuilder();
				neighBuilder.setNeighborNodeId(link.getDstNode());
				neighBuilder.setSrcPort(link.getSrcNodeConnector());
				neighBuilder.setNeighPort(link.getDstNodeConnector());
				cNodeBuilder.getNeighbors().add(neighBuilder.build());
				hpnodeBuilders.put(currentNodeId.getValue(), cNodeBuilder);
			}
		} //end of for
		
		for (String nodeIdKey: hpnodeBuilders.keySet()){
			InstanceIdentifier<CurrNode> currNodeIID = InstanceIdentifier.builder(NodeNeighbors.class)
					.child(CurrNode.class, new CurrNodeKey(new NodeId(nodeIdKey)))
					.build();
			GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION,
					currNodeIID, hpnodeBuilders.get(nodeIdKey).build(), true);
		}
	}
	/////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Future<RpcResult<InstallPathOutput>> installPath(
			final InstallPathInput input) {
		// This RPC installs a path between a source-destination pair
		List<NodeId> pathNodes = input.getPathNodes();
		InstallPathOutputBuilder output = new InstallPathOutputBuilder();
		
		int nodeIndex = 0;
		RepeatFunction func = new RepeatFunction() {	
			@Override
			public InstallFlowInput performFunction(NodeConnectorId outputPort, Ipv4Prefix dstIp,
					NodeId nodeid) {
				AssociatedActionsBuilder actionBuilder = new AssociatedActionsBuilder();
				ForwardToPortBuilder forwardBuilder = new ForwardToPortBuilder();
				forwardBuilder.setOutputNodeConnector(outputPort);
				
				actionBuilder.setFlowActions(new ForwardToPortCaseBuilder().
						setForwardToPort(forwardBuilder.build()).build());
				actionBuilder.setId((long)1);
				List<AssociatedActions> actionList = Lists.newArrayList();
				actionList.add(actionBuilder.build());
				
				NewFlowBuilder newFlowBuilder = new NewFlowBuilder();
				newFlowBuilder.setDstIpAddress(dstIp);
				newFlowBuilder.setFlowPriority(input.getFlowPriority());
				newFlowBuilder.setIdleTimeout(input.getIdleTimeout());
				newFlowBuilder.setHardTimeout(input.getHardTimeout());
				
				InstallFlowInputBuilder installFlowBuilder = new InstallFlowInputBuilder();
				installFlowBuilder.setNode(nodeid);
				
				installFlowBuilder.setNewFlow(newFlowBuilder.build());
				installFlowBuilder.setAssociatedActions(actionList);
				return installFlowBuilder.build();
				
			}

			@Override
			public InstallFlowInput performFunction(NodeConnectorId outputPort,
					Ipv4Prefix curDstIp, Ipv4Prefix newDstIp,
					Ipv4Prefix curSrcIp, Ipv4Prefix newSrcIp, NodeId nodeid) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		int index = 0;
		for (; index < pathNodes.size(); index++){
			if (index == 0){
				ConnectedHost host = getHostInfo(input.getSrcIpAddress().getValue());
				if (host != null){
					this.installFlow(func.performFunction(host.getNodeConnectorConnectedTo(), 
							input.getSrcIpAddress(), pathNodes.get(index)));
				}
				if (index + 1 < pathNodes.size()){
					//forwarding direction rule i.e., src -> dst
					Neighbors neighbor = getPortInformation(pathNodes.get(index), pathNodes.get(index+1));
					if (neighbor != null){
						this.installFlow(func.performFunction(neighbor.getSrcPort(), 
								input.getDstIpAddress(), pathNodes.get(index)));
					}
				}
				else {
					ConnectedHost host1 = getHostInfo(input.getDstIpAddress().getValue());
					if (host1 != null){
						this.installFlow(func.performFunction(host1.getNodeConnectorConnectedTo(), 
								input.getDstIpAddress(), pathNodes.get(index)));
					}
				}
			}/////////////////////////////////////////////////////////////////
			else if (index - 1 >= 0 && index + 1 < pathNodes.size()){
				//forwarding direction rule i.e., src -> dst
				Neighbors neighbor = getPortInformation(pathNodes.get(index), pathNodes.get(index+1));
				if (neighbor != null){
					this.installFlow(func.performFunction(neighbor.getSrcPort(), 
							input.getDstIpAddress(), pathNodes.get(index)));
				}
				//Reverse direction rule  i.e., dst -> src
				Neighbors neighbor1 = getPortInformation(pathNodes.get(index), pathNodes.get(index-1));
				if (neighbor1 != null){
					this.installFlow(func.performFunction(neighbor1.getSrcPort(), 
							input.getSrcIpAddress(), pathNodes.get(index)));
				}
			}/////////////////////////////////////////////////////////////////
			else if (index + 1 == pathNodes.size()){
				ConnectedHost host = getHostInfo(input.getDstIpAddress().getValue());
				if (host != null){
					this.installFlow(func.performFunction(host.getNodeConnectorConnectedTo(), 
							input.getDstIpAddress(), pathNodes.get(index)));
				}
				if (index - 1 >= 0){
					//forwarding direction rule i.e., src -> dst
					Neighbors neighbor = getPortInformation(pathNodes.get(index), pathNodes.get(index-1));
					if (neighbor != null){
						this.installFlow(func.performFunction(neighbor.getSrcPort(), 
								input.getSrcIpAddress(), pathNodes.get(index)));
					}
				}
			}
		}
		LOG.debug("         -------------------------------------------             ");
		LOG.debug("Path is successfully installed.");
		LOG.debug("         -------------------------------------------             ");
		
		output.setStatus("Path is installed.");
		return RpcResultBuilder.success(output).buildFuture();
	}
	//////////////////////////////////////////////////////////////////////////////////
	@Override
	public Future<RpcResult<MovePathOutput>> movePath(final MovePathInput input) {
		// This function migrates the traffic from one path to another
		List<NodeId> oldpathNodes = input.getOldPathNodes();
		List<NodeId> newpathNodes = input.getNewPathNodes();
		//Validation Check-----First and last nodes of both paths should be same
		MovePathOutputBuilder output = new MovePathOutputBuilder();
		if ((oldpathNodes.get(0).equals(newpathNodes.get(0)) == false) || 
				(oldpathNodes.get(oldpathNodes.size()-1).equals(newpathNodes.get(newpathNodes.size()-1)) == false)){
			
			LOG.debug("         -------------------------------------------             ");
			LOG.debug("Invalid Migration: First and Last nodes are not identical.");
			LOG.debug("         -------------------------------------------             ");
			
			output.setStatus("Invalid Migration: First and Last nodes are not identical.");
			return RpcResultBuilder.success(output).buildFuture();
		}
		///////////////////////////////////
		try {
			RepeatFunction func = new RepeatFunction() {	
				@Override
				public InstallFlowInput performFunction(NodeConnectorId outputPort, Ipv4Prefix dstIp,
						NodeId nodeid) {
					AssociatedActionsBuilder actionBuilder = new AssociatedActionsBuilder();
					ForwardToPortBuilder forwardBuilder = new ForwardToPortBuilder();
					forwardBuilder.setOutputNodeConnector(outputPort);
					
					actionBuilder.setFlowActions(new ForwardToPortCaseBuilder().
							setForwardToPort(forwardBuilder.build()).build());
					actionBuilder.setId((long)1);
					List<AssociatedActions> actionList = Lists.newArrayList();
					actionList.add(actionBuilder.build());
					
					NewFlowBuilder newFlowBuilder = new NewFlowBuilder();
					newFlowBuilder.setDstIpAddress(dstIp);
					newFlowBuilder.setFlowPriority(input.getFlowPriority());
					newFlowBuilder.setIdleTimeout(input.getIdleTimeout());
					newFlowBuilder.setHardTimeout(input.getHardTimeout());
					
					InstallFlowInputBuilder installFlowBuilder = new InstallFlowInputBuilder();
					installFlowBuilder.setNode(nodeid);
					installFlowBuilder.setNewFlow(newFlowBuilder.build());
					installFlowBuilder.setAssociatedActions(actionList);
					return installFlowBuilder.build();
					
				}

				@Override
				public InstallFlowInput performFunction(
						NodeConnectorId outputPort, Ipv4Prefix curDstIp,
						Ipv4Prefix newDstIp, Ipv4Prefix curSrcIp,
						Ipv4Prefix newSrcIp, NodeId nodeid) {
					// TODO Auto-generated method stub
					return null;
				}
			};
			int index = 0;
			for (; index < newpathNodes.size(); index++){
				if (index == 0 || index == newpathNodes.size() - 1) continue;
				
				//forwarding direction rule i.e., src -> dst
				LOG.debug("         ---------------------------------------------------------------------     ");
       		 	LOG.debug(newpathNodes.get(index).getValue() + "  index & index+1     " + newpathNodes.get(index + 1));
       		 	LOG.debug("         ---------------------------------------------------------------------     ");
				Neighbors neighbor = getPortInformation(newpathNodes.get(index), newpathNodes.get(index+1));
				if (neighbor != null){
					this.installFlow(func.performFunction(neighbor.getSrcPort(), 
							input.getDstIpAddress(), newpathNodes.get(index)));
					LOG.debug("         ---------------------------------------------------------------------     ");
	       		 	LOG.debug(neighbor.getSrcPort().getValue() + "     " + input.getDstIpAddress().getValue());
	       		 	LOG.debug("         ---------------------------------------------------------------------     ");
				} else {
					String exception = "Neighbor " + newpathNodes.get(index).getValue() + 
							" is not available for Node " + newpathNodes.get(index+1).getValue();
					throw new Exception(exception);
				}
				//Reverse direction rule  i.e., dst -> src
				LOG.debug("         ---------------------------------------------------------------------     ");
       		 	LOG.debug(newpathNodes.get(index).getValue() + "   index & index-1    " + newpathNodes.get(index - 1));
       		 	LOG.debug("         ---------------------------------------------------------------------     ");
				Neighbors neighbor1 = getPortInformation(newpathNodes.get(index), newpathNodes.get(index-1));
				if (neighbor1 != null){
					this.installFlow(func.performFunction(neighbor1.getSrcPort(), 
							input.getSrcIpAddress(), newpathNodes.get(index)));
					LOG.debug("         ---------------------------------------------------------------------     ");
	       		 	LOG.debug(neighbor1.getSrcPort().getValue() + "     " + input.getSrcIpAddress().getValue());
	       		 	LOG.debug("         ---------------------------------------------------------------------     ");
				} else {
					String exception = "Neighbor " + newpathNodes.get(index).getValue() + 
							" is not available for Node " + newpathNodes.get(index-1).getValue();
					throw new Exception(exception);
				}
			}
			//Switch for for both First and Last nodes
			Neighbors neighbor = getPortInformation(newpathNodes.get(0), newpathNodes.get(1));
			Neighbors neighbor1 = getPortInformation(newpathNodes.get(newpathNodes.size()-1), newpathNodes.get(newpathNodes.size()-2));
			
			if (neighbor != null){
				this.installFlow(func.performFunction(neighbor.getSrcPort(), 
						input.getDstIpAddress(), newpathNodes.get(0)));
			} else {
				String exception = "Neighbor " + newpathNodes.get(0).getValue() + 
						" is not available for Node " + newpathNodes.get(1).getValue();
				throw new Exception(exception);
			}
			if (neighbor1 != null){
				this.installFlow(func.performFunction(neighbor1.getSrcPort(), 
						input.getSrcIpAddress(), newpathNodes.get(newpathNodes.size()-1)));
			} else {
				String exception = "Neighbor " + newpathNodes.get(newpathNodes.size()-1).getValue() + 
						" is not available for Node " + newpathNodes.get(newpathNodes.size()-2).getValue();
				throw new Exception(exception);
			}
			////////////////////////////////////////////////////////////////////////////
			
		} catch (Exception e) {
            LOG.error("Exception reached in MovePath RPC {} --------", e);
            output.setStatus("Path Couldn't be migrated.");
    		return RpcResultBuilder.success(output).buildFuture();
        }
		/*
		 * ------Do something for old path, one thing that we can readjust their idle timeout to small value
		 *  we can start this to begin-with. So, for each migration, the nodes in the oldPath are put to 
		 *  no flight zone for this src-destination pair. We can also extend the matching criteria from only
		 *  destination to src-destination both. So, whenever a new flow-removed msg is received then we can
		 *  do a simple look-up to confirm if flow nees to be restored or just leave it.
		 *  
		 *  Also Check what should be the flags of these newly installed flows
		 */
		
		output.setStatus("Path is successfully migrated.");
		return RpcResultBuilder.success(output).buildFuture();
		//return null;
	}
	///////////////////////////////////////////////////////////////////////////
	@Override
	public Future<RpcResult<InstallPathBwNodesOutput>> installPathBwNodes(
			final InstallPathBwNodesInput input) {
		// This will install the path between any two nodes and don't care about hosts connected to edge nodes
		
		List<NodeId> pathNodes = input.getPathNodes();
		InstallPathBwNodesOutputBuilder output = new InstallPathBwNodesOutputBuilder();
		if (pathNodes.size() <= 2 ){
			LOG.debug("         -------------------------------------------             ");
			LOG.debug("Short Path: No nodes in between edge nodes.");
			LOG.debug("         -------------------------------------------             ");
			
			output.setStatus("Short Path: No nodes in between edge nodes");
			return RpcResultBuilder.success(output).buildFuture();
		}
		///////////////////////////////////
		try {
			RepeatFunction func = new RepeatFunction() {	
				@Override
				public InstallFlowInput performFunction(NodeConnectorId outputPort, Ipv4Prefix dstIp,
						NodeId nodeid) {
					AssociatedActionsBuilder actionBuilder = new AssociatedActionsBuilder();
					ForwardToPortBuilder forwardBuilder = new ForwardToPortBuilder();
					forwardBuilder.setOutputNodeConnector(outputPort);
					
					actionBuilder.setFlowActions(new ForwardToPortCaseBuilder().
							setForwardToPort(forwardBuilder.build()).build());
					actionBuilder.setId((long)1);
					List<AssociatedActions> actionList = Lists.newArrayList();
					actionList.add(actionBuilder.build());
					
					NewFlowBuilder newFlowBuilder = new NewFlowBuilder();
					newFlowBuilder.setDstIpAddress(dstIp);
					newFlowBuilder.setFlowPriority(input.getFlowPriority());
					newFlowBuilder.setIdleTimeout(input.getIdleTimeout());
					newFlowBuilder.setHardTimeout(input.getHardTimeout());
					
					InstallFlowInputBuilder installFlowBuilder = new InstallFlowInputBuilder();
					installFlowBuilder.setNode(nodeid);
					installFlowBuilder.setNewFlow(newFlowBuilder.build());
					installFlowBuilder.setAssociatedActions(actionList);
					return installFlowBuilder.build();
					
				}

				@Override
				public InstallFlowInput performFunction(
						NodeConnectorId outputPort, Ipv4Prefix curDstIp,
						Ipv4Prefix newDstIp, Ipv4Prefix curSrcIp,
						Ipv4Prefix newSrcIp, NodeId nodeid) {
					// TODO Auto-generated method stub
					return null;
				}
			};
			int index = 0;
			for (; index < pathNodes.size(); index++){
				if (index == 0 || index == pathNodes.size() - 1) continue;
				
				//forwarding direction rule i.e., src -> dst
				Neighbors neighbor = getPortInformation(pathNodes.get(index), pathNodes.get(index+1));
				if (neighbor != null){
					this.installFlow(func.performFunction(neighbor.getSrcPort(), 
							input.getDstIpAddress(), pathNodes.get(index)));
				} else {
					String exception = "Neighbor " + pathNodes.get(index).getValue() + 
							" is not available for Node " + pathNodes.get(index+1).getValue();
					throw new Exception();
				}
				//Reverse direction rule  i.e., dst -> src
				Neighbors neighbor1 = getPortInformation(pathNodes.get(index), pathNodes.get(index-1));
				if (neighbor1 != null){
					this.installFlow(func.performFunction(neighbor1.getSrcPort(), 
							input.getSrcIpAddress(), pathNodes.get(index)));
				} else {
					String exception = "Neighbor " + pathNodes.get(index).getValue() + 
							" is not available for Node " + pathNodes.get(index-1).getValue();
					throw new Exception();
				}
			}
			////////////////////////////////////////////////////////////////////////////
			
		} catch (Exception e) {
            LOG.error("Exception reached in InstallPathBwNodes RPC {} --------", e);
            output.setStatus("Path Couldn't be Installed between Nodes.");
    		return RpcResultBuilder.success(output).buildFuture();
            
        }
		/*
		 * ------Do something for old path, one thing that we can readjust their idle timeout to small value
		 *  we can start this to begin-with. So, for each migration, the nodes in the oldPath are put to 
		 *  no flight zone for this src-destination pair. We can also extend the matching criteria from only
		 *  destination to src-destination both. So, whenever a new flow-removed msg is received then we can
		 *  do a simple look-up to confirm if flow nees to be restored or just leave it.
		 *  
		 *  Also Check what should be the flags of these newly installed flows
		 */
		
		output.setStatus("Path is successfully migrated.");
		return RpcResultBuilder.success(output).buildFuture();
	}
	///////////////////////////////////////////////////////////////////////////
	@Override
	public Future<RpcResult<MutateIpOutput>> mutateIp(final MutateIpInput input) {
		// This function will mutate the Ip addresses
		RepeatFunction func = new RepeatFunction() {	
			@Override
			public InstallFlowInput performFunction(NodeConnectorId outputPort, Ipv4Prefix curDstIp,
					Ipv4Prefix newDstIp, Ipv4Prefix curSrcIp, Ipv4Prefix newSrcIp,
					NodeId nodeid) {
				List<AssociatedActions> actionList = Lists.newArrayList();
				AssociatedActionsBuilder actionBuilder = new AssociatedActionsBuilder();
				long actionKey = 1;
				if (curSrcIp.equals(newSrcIp) == false){
					SetSourceIpv4AddressBuilder srcIpBuilder = new SetSourceIpv4AddressBuilder();
					srcIpBuilder.setValue(newSrcIp);
					actionBuilder.setFlowActions(new SetSourceIpv4AddressCaseBuilder().
							setSetSourceIpv4Address(srcIpBuilder.build()).build());
					actionBuilder.setId(actionKey++);
					actionList.add(actionBuilder.build());
				}
				if (curDstIp.equals(newDstIp) == false){
					SetDstIpv4AddressBuilder dstIpBuilder = new SetDstIpv4AddressBuilder();
					dstIpBuilder.setValue(newDstIp);
					actionBuilder.setFlowActions(new SetDstIpv4AddressCaseBuilder().
							setSetDstIpv4Address(dstIpBuilder.build()).build());
					actionBuilder.setId(actionKey++);
					actionList.add(actionBuilder.build());
				}
				
				ForwardToPortBuilder forwardBuilder = new ForwardToPortBuilder();
				forwardBuilder.setOutputNodeConnector(outputPort);
				
				actionBuilder.setFlowActions(new ForwardToPortCaseBuilder().
						setForwardToPort(forwardBuilder.build()).build());
				actionBuilder.setId(actionKey++);
				actionList.add(actionBuilder.build());
				
				NewFlowBuilder newFlowBuilder = new NewFlowBuilder();
				newFlowBuilder.setDstIpAddress(curDstIp);
				newFlowBuilder.setSrcIpAddress(curSrcIp);
				newFlowBuilder.setFlowPriority(input.getFlowPriority());
				newFlowBuilder.setIdleTimeout(input.getIdleTimeout());
				newFlowBuilder.setHardTimeout(input.getHardTimeout());
				
				InstallFlowInputBuilder installFlowBuilder = new InstallFlowInputBuilder();
				installFlowBuilder.setNode(nodeid);
				installFlowBuilder.setNewFlow(newFlowBuilder.build());
				
				installFlowBuilder.setAssociatedActions(actionList);
				return installFlowBuilder.build();
				
			}

			@Override
			public InstallFlowInput performFunction(NodeConnectorId outputPort,
					Ipv4Prefix dstIp, NodeId nodeid) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		MutateIpOutputBuilder output = new MutateIpOutputBuilder();
		try{
			//LOG.debug("  ===========================================   ");
			//LOG.debug(input.getPathNodes().toString());
			//LOG.debug("  ===========================================   ");
			
			ConnectedHost srcHost = getHostInfo(input.getSrcIpAddress().getValue());
			if (srcHost == null){
				String exception = "Source Host " + input.getSrcIpAddress().getValue() + 
						" is not known to controller ";
				throw new Exception(exception);
			} else if (srcHost.getNodeConnectedTo().equals(input.getPathNodes().get(0)) == false ){
				//LOG.debug("  ===========================================   ");
				//LOG.debug(input.getPathNodes().get(0).getValue());
				//LOG.debug("  ===========================================   ");
				String exception = "Source Host " + input.getSrcIpAddress().getValue() + 
						" is not known connected to node " + input.getPathNodes().get(0).getValue();
				throw new Exception(exception);
			}
			/////////////////
			ConnectedHost dstHost = getHostInfo(input.getDstIpAddress().getValue());
			if (dstHost == null){
				String exception = "Dst Host " + input.getDstIpAddress().getValue() + 
						" is not known to controller ";
				throw new Exception(exception);
			} else if (dstHost.getNodeConnectedTo().equals(input.getPathNodes().get(input.getPathNodes().size()-1)) == false ){
				String exception = "Dst Host " + input.getDstIpAddress().getValue() + 
						" is not known connected to node " + input.getPathNodes().get(input.getPathNodes().size()-1).getValue();
				throw new Exception(exception);
			}
			//////////////////////
			Neighbors srcEdgeNeighbor = getPortInformation(input.getPathNodes().get(0), input.getPathNodes().get(1));
			if (srcEdgeNeighbor == null){
				String exception = "Soure Edge Node" + input.getPathNodes().get(0).getValue() + 
						" is not neighbor with " + input.getPathNodes().get(1).getValue();
				throw new Exception(exception);
			}
			Neighbors dstEdgeNeighbor = getPortInformation(input.getPathNodes().get(input.getPathNodes().size()-1), 
					input.getPathNodes().get(input.getPathNodes().size()-2));
			if (dstEdgeNeighbor == null){
				String exception = "Dst Edge Node" + input.getPathNodes().get(input.getPathNodes().size()-1).getValue() + 
						" is not neighbor with " + input.getPathNodes().get(input.getPathNodes().size()-2).getValue();
				throw new Exception(exception);
			}
			////////////////////////////////////////////////
			if (input.getMutationEnd() instanceof SourceOnlyCase){
				SourceOnlyCase srcOnlyCase = (SourceOnlyCase) input.getMutationEnd();
				InstallPathBwNodesInputBuilder pathInputBuilder = new InstallPathBwNodesInputBuilder();
				pathInputBuilder.setDstIpAddress(input.getDstIpAddress());
				pathInputBuilder.setSrcIpAddress(srcOnlyCase.getSourceOnly().getNewSrcIpAddress());
				pathInputBuilder.setPathNodes(input.getPathNodes());
				pathInputBuilder.setFlowPriority(input.getFlowPriority());
				pathInputBuilder.setIdleTimeout(input.getIdleTimeout());
				pathInputBuilder.setHardTimeout(input.getHardTimeout());
				this.installPathBwNodes(pathInputBuilder.build());
				
				//On source edge node, SrcHost will be the destination and its traffic will reach with changed DstIp 
				//so we have to revert it back to original srcHost IP
				this.installFlow(func.performFunction(srcHost.getNodeConnectorConnectedTo(), 
						srcOnlyCase.getSourceOnly().getNewSrcIpAddress(),
						input.getSrcIpAddress(), input.getDstIpAddress(), input.getDstIpAddress(), 
						input.getPathNodes().get(0)));
				
				//On source edge node, if SrcHost is the source then going to dstHost then we need to change its identity 
				//for the network and change the srcIp of packet to newSrcIP and leave the dstHost intact
				this.installFlow(func.performFunction(srcEdgeNeighbor.getSrcPort(), 
						input.getDstIpAddress(), input.getDstIpAddress(), 
						input.getSrcIpAddress(), srcOnlyCase.getSourceOnly().getNewSrcIpAddress(), 
						input.getPathNodes().get(0)));
				
				//On Destination edge node, if newSrcHostIP is the source then going to dstHost then we need to change its identity 
				//back to original srcHost IP so that the dstHost can recognize it
				this.installFlow(func.performFunction(dstHost.getNodeConnectorConnectedTo(), 
						input.getDstIpAddress(), input.getDstIpAddress(),
						srcOnlyCase.getSourceOnly().getNewSrcIpAddress(),
						input.getSrcIpAddress(),
						input.getPathNodes().get(input.getPathNodes().size()-1)));
				
				//On Destination edge node, SrcHostIP will be the destination and its traffic Identity should be hidden from the  
				//network, so the DstIP of the packet will be changed from actualSrcHostIP to newIP
				this.installFlow(func.performFunction(dstEdgeNeighbor.getSrcPort(), 
						input.getSrcIpAddress(), 
						srcOnlyCase.getSourceOnly().getNewSrcIpAddress(),
						input.getDstIpAddress(), input.getDstIpAddress(), 
						input.getPathNodes().get(input.getPathNodes().size()-1)));
				
			} ///////////////////////////////
			else if (input.getMutationEnd() instanceof DstOnlyCase){
				DstOnlyCase dstOnlyCase = (DstOnlyCase) input.getMutationEnd();
				InstallPathBwNodesInputBuilder pathInputBuilder = new InstallPathBwNodesInputBuilder();
				pathInputBuilder.setDstIpAddress(dstOnlyCase.getDstOnly().getNewDstIpAddress());
				pathInputBuilder.setSrcIpAddress(input.getSrcIpAddress());
				pathInputBuilder.setPathNodes(input.getPathNodes());
				pathInputBuilder.setFlowPriority(input.getFlowPriority());
				pathInputBuilder.setIdleTimeout(input.getIdleTimeout());
				pathInputBuilder.setHardTimeout(input.getHardTimeout());
				this.installPathBwNodes(pathInputBuilder.build());
				
				//On source edge node, dstHostIP is the actual destination, however its traffic should be hidden from the network 
				//so we change the IP destination of packets from actual dstHostIP to new IP
				this.installFlow(func.performFunction(srcEdgeNeighbor.getSrcPort(), 
						input.getDstIpAddress(),
						dstOnlyCase.getDstOnly().getNewDstIpAddress(),
						input.getSrcIpAddress(),  input.getSrcIpAddress(), 
						input.getPathNodes().get(0)));
				
				//On source edge node, if SrcHost is the destination then all packets coming from actual dstHostIP are mutated 
				//in their srcIP address field, so before we forward these packets to srcHost, we need to switch them back original dstIP
				this.installFlow(func.performFunction(srcHost.getNodeConnectorConnectedTo(), 
						input.getSrcIpAddress(),  input.getSrcIpAddress(),
						dstOnlyCase.getDstOnly().getNewDstIpAddress(),
						input.getDstIpAddress(),					 
						input.getPathNodes().get(0)));
				
				//on Dst Edge node, if traffic coming from SrcHost for dstHost will be mutated in dstIP as we hide the identity of dstHost  
				//from network. So, we have to now change the mutated dstIP to original dstIp of the dstHost
				this.installFlow(func.performFunction(dstHost.getNodeConnectorConnectedTo(), 
						dstOnlyCase.getDstOnly().getNewDstIpAddress(),
						input.getDstIpAddress(),
						input.getSrcIpAddress(), input.getSrcIpAddress(),
						input.getPathNodes().get(input.getPathNodes().size()-1)));
				
				//On Destination edge node, any traffic originating from dstHost will have its IP address and we have to hide its identity from   
				//the network. So we change the srcIP of packets from actual dstHost to mutated IP
				this.installFlow(func.performFunction(dstEdgeNeighbor.getSrcPort(), 
						input.getSrcIpAddress(), input.getSrcIpAddress(),
						input.getDstIpAddress(),
						dstOnlyCase.getDstOnly().getNewDstIpAddress(),
						input.getPathNodes().get(input.getPathNodes().size()-1)));
			}/////////////////////////////////////////////////////////////////
			else if (input.getMutationEnd() instanceof BothCase){
				BothCase bothCase = (BothCase) input.getMutationEnd();
				InstallPathBwNodesInputBuilder pathInputBuilder = new InstallPathBwNodesInputBuilder();
				pathInputBuilder.setDstIpAddress(bothCase.getBoth().getNewDstIpAddress());
				pathInputBuilder.setSrcIpAddress(bothCase.getBoth().getNewSrcIpAddress());
				pathInputBuilder.setPathNodes(input.getPathNodes());
				pathInputBuilder.setFlowPriority(input.getFlowPriority());
				pathInputBuilder.setIdleTimeout(input.getIdleTimeout());
				pathInputBuilder.setHardTimeout(input.getHardTimeout());
				this.installPathBwNodes(pathInputBuilder.build());
				
				//On source edge node, traffic generated by actual srcHost for actual dstHost but we have to 
				//mutate their identities
				this.installFlow(func.performFunction(srcEdgeNeighbor.getSrcPort(), 
						input.getDstIpAddress(),
						bothCase.getBoth().getNewDstIpAddress(),
						input.getSrcIpAddress(), 
						bothCase.getBoth().getNewSrcIpAddress(), 
						input.getPathNodes().get(0)));
				
				//On source edge node, traffic coming from mutated dstHost IP to mutated srcHost IP should be 
				//reverted back to their identities
				this.installFlow(func.performFunction(srcHost.getNodeConnectorConnectedTo(), 
						bothCase.getBoth().getNewSrcIpAddress(),
						input.getSrcIpAddress(),
						bothCase.getBoth().getNewDstIpAddress(),
						input.getDstIpAddress(), 
						input.getPathNodes().get(0)));
				
				//On dst edge node, traffic coming for actual dstHost is mutated in both src and dst IPs
				//so we have to revert it back before we hand this over to dstHost
				this.installFlow(func.performFunction(dstHost.getNodeConnectorConnectedTo(), 
						bothCase.getBoth().getNewDstIpAddress(),
						input.getDstIpAddress(),
						bothCase.getBoth().getNewSrcIpAddress(),
						input.getSrcIpAddress(),
						input.getPathNodes().get(input.getPathNodes().size()-1)));
				
				//On dst edge node, traffic originated from dstHost for srcHost, we have to mutate both IPs 
				//to hide their identities from the network
				this.installFlow(func.performFunction(dstEdgeNeighbor.getSrcPort(), 
						input.getSrcIpAddress(),
						bothCase.getBoth().getNewSrcIpAddress(),
						input.getDstIpAddress(),
						bothCase.getBoth().getNewDstIpAddress(),
						input.getPathNodes().get(input.getPathNodes().size()-1)));
				
			}
			
			
		} catch (Exception e) {
            LOG.error("Exception reached in MutateIP RPC {} --------", e);
            output.setStatus("IP couldn't be mutated.");
    		return RpcResultBuilder.success(output).buildFuture();
        }
		
		output.setStatus("IP is successfully mutated.");
		return RpcResultBuilder.success(output).buildFuture();
	}

	@Override
	public Future<RpcResult<RemoveAllTapsFromSwitchOutput>> removeAllTapsFromSwitch(
			RemoveAllTapsFromSwitchInput input) {
		NodeId nodeId = input.getNode();
		if (tapsInstalled.containsKey(nodeId)){
			for (String tapId: tapsInstalled.get(nodeId)){
				String flowIdStr = "Tap_" + tapId + "_NodeID_" + nodeId.getValue();
                //remove tap from tap-spec datastore	
                InstanceIdentifier<Tap> tapIID = InstanceIdentifier.builder(TapSpec.class)
                		.child(Tap.class, new TapKey(tapId))
                		.build();
                TapBuilder tapB = new TapBuilder();
                tapB.setId(tapId);
                GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, tapIID, tapB.build(), false);
            }
			RemoveAllTapsFromSwitchOutputBuilder output = new RemoveAllTapsFromSwitchOutputBuilder();
			output.setStatus("Taps removed from " + nodeId.getValue());
			
			return RpcResultBuilder.success(output.build()).buildFuture();
		}
		else {
			RemoveAllTapsFromSwitchOutputBuilder output = new RemoveAllTapsFromSwitchOutputBuilder();
			output.setStatus("No Taps Installed on " + nodeId.getValue());
			
			return RpcResultBuilder.success(output.build()).buildFuture();
		}
	}

	@Override
	public Future<RpcResult<RemoveAFlowFromSwitchOutput>> removeAFlowFromSwitch(
			RemoveAFlowFromSwitchInput input) {
		
		FlowKey key = new FlowKey(new FlowId(input.getFlowKey()));
		InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
        		.child(Node.class, new NodeKey(input.getNodeId()))
        		.augmentation(FlowCapableNode.class)
        		.child(Table.class, new TableKey(input.getTableId()))
        		.child(Flow.class, new FlowKey(key))
        		.build();
		
		FlowBuilder flowBuilder = new FlowBuilder();
    	flowBuilder.setFlowName(key.getId().getValue());
    	flowBuilder.setKey(key);
    	flowBuilder.setId(new FlowId(input.getFlowKey()));
    	flowBuilder.setTableId((short)0);
    	
		boolean status = GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), false);
		RemoveAFlowFromSwitchOutputBuilder outputBuilder = new RemoveAFlowFromSwitchOutputBuilder(); 
		if (status == true) {
			outputBuilder.setStatus("Flow with ID " + input.getFlowKey() + " is successfully removed from Switch " + input.getNodeId().getValue());
			return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
		}
		return null;
	}

	
	@Override
	public Future<RpcResult<RemoveATapFromSwitchOutput>> removeATapFromSwitch(
			RemoveATapFromSwitchInput input) {
		NodeId nodeId = input.getNode();
		if (tapsInstalled.containsKey(nodeId)){
			List<String> taps = tapsInstalled.get(nodeId);
			if (taps.contains(input.getTapId())) {
				String tapId = taps.get(taps.indexOf(input.getTapId()));
				String flowIdStr = "Tap_" + tapId + "_NodeID_" + nodeId.getValue();
                //remove tap from tap-spec datastore	
                InstanceIdentifier<Tap> tapIID = InstanceIdentifier.builder(TapSpec.class)
                		.child(Tap.class, new TapKey(tapId))
                		.build();
                TapBuilder tapB = new TapBuilder();
                tapB.setId(tapId);
                GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, tapIID, tapB.build(), false);
                RemoveATapFromSwitchOutputBuilder outputBuilder = new RemoveATapFromSwitchOutputBuilder();
                outputBuilder.setStatus("Tap is removed");
                return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
			}
		}
		return null;
	}
	
}
