package com.extendedae_plus.common.init;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.network.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public class ModNetwork {
    @SubscribeEvent
    public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(ExtendedAEPlus.MODID);
        registrar.playToServer(ToggleEntityTickerC2SPacket.TYPE, ToggleEntityTickerC2SPacket.STREAM_CODEC, ToggleEntityTickerC2SPacket::handle);
        registrar.playToServer(ToggleAdvancedBlockingC2SPacket.TYPE, ToggleAdvancedBlockingC2SPacket.STREAM_CODEC, ToggleAdvancedBlockingC2SPacket::handle);
        registrar.playToServer(ToggleSmartDoublingC2SPacket.TYPE, ToggleSmartDoublingC2SPacket.STREAM_CODEC, ToggleSmartDoublingC2SPacket::handle);
        registrar.playToServer(CPacketScalePatterns.TYPE, CPacketScalePatterns.STREAM_CODEC, CPacketScalePatterns::handle);
        registrar.playToServer(CPacketInterfaceScaling.TYPE, CPacketInterfaceScaling.STREAM_CODEC, CPacketInterfaceScaling::handle);
        registrar.playToClient(SetPatternHighlightS2CPacket.TYPE, SetPatternHighlightS2CPacket.STREAM_CODEC, SetPatternHighlightS2CPacket::handle);
        registrar.playToClient(AdvancedBlockingSyncS2CPacket.TYPE, AdvancedBlockingSyncS2CPacket.STREAM_CODEC, AdvancedBlockingSyncS2CPacket::handle);
        registrar.playToClient(ProvidersListS2CPacket.TYPE, ProvidersListS2CPacket.STREAM_CODEC, ProvidersListS2CPacket::handle);
        registrar.playToServer(RequestUploadingC2SPacket.TYPE, RequestUploadingC2SPacket.STREAM_CODEC, RequestUploadingC2SPacket::handle);
        registrar.playToClient(SetProviderPageS2CPacket.TYPE, SetProviderPageS2CPacket.STREAM_CODEC, SetProviderPageS2CPacket::handle);
        registrar.playToServer(CPacketProviderControllerOperation.TYPE, CPacketProviderControllerOperation.STREAM_CODEC, CPacketProviderControllerOperation::handle);
        registrar.playToServer(CraftingMonitorJumpC2SPacket.TYPE, CraftingMonitorJumpC2SPacket.STREAM_CODEC, CraftingMonitorJumpC2SPacket::handle);
        registrar.playToServer(CraftingMonitorOpenProviderC2SPacket.TYPE, CraftingMonitorOpenProviderC2SPacket.STREAM_CODEC, CraftingMonitorOpenProviderC2SPacket::handle);
        registrar.playToServer(UploadEncodedPatternToProviderC2SPacket.TYPE, UploadEncodedPatternToProviderC2SPacket.STREAM_CODEC, UploadEncodedPatternToProviderC2SPacket::handle);
        registrar.playToServer(UploadInventoryPatternToProviderC2SPacket.TYPE, UploadInventoryPatternToProviderC2SPacket.STREAM_CODEC, UploadInventoryPatternToProviderC2SPacket::handle);

        registrar.playToServer(CPacketPickFromNetwork.TYPE,
                CPacketPickFromNetwork.STREAM_CODEC,
                CPacketPickFromNetwork::handle);
        registrar.playToServer(CPacketPullFromNetwork.TYPE,
                CPacketPullFromNetwork.STREAM_CODEC,
                CPacketPullFromNetwork::handle);
        registrar.playToServer(CPacketChannelCardBind.TYPE,
                CPacketChannelCardBind.STREAM_CODEC,
                CPacketChannelCardBind::handle);
        registrar.playToServer(SetWirelessFrequencyC2SPacket.TYPE,
                SetWirelessFrequencyC2SPacket.STREAM_CODEC,
                SetWirelessFrequencyC2SPacket::handle);

        registrar.playToClient(CPacketEncodeFinished.TYPE,
                CPacketEncodeFinished.STREAM_CODEC,
                CPacketEncodeFinished::handle);
        registrar.playToServer(CPacketTargetKeyTriggered.TYPE,
                CPacketTargetKeyTriggered.STREAM_CODEC,
                CPacketTargetKeyTriggered::handle);
        registrar.playToServer(CPacketStoneCuttingID.TYPE,
                CPacketStoneCuttingID.STREAM_CODEC,
                CPacketStoneCuttingID::handle);
    }
}
