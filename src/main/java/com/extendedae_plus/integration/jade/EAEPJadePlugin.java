package com.extendedae_plus.integration.jade;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockEntityUpload;
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver;
import com.extendedae_plus.integration.jade.helper.IObjectedProvider;
import com.extendedae_plus.integration.jade.helper.WrapperObjectedProvider;
import com.extendedae_plus.integration.jade.implementation.PortUpload;
import com.extendedae_plus.integration.jade.implementation.WirelessTransceiver;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.*;

import java.util.List;

@WailaPlugin(ExtendedAEPlus.MODID)
public class EAEPJadePlugin implements IWailaPlugin {
    private static final List<InfoBlock<?, ?>> blocks;

    @Override
    public void register(IWailaCommonRegistration registration) {
        // 注册服务端数据提供者（用于同步数据）
        blocks.forEach(info -> info.registerProvider(registration));
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // 遍历组件常量，逐一注册
        blocks.forEach(info -> info.registerConsumer(registration));
    }

    static {
        blocks = List.of(
                new InfoBlock<>(
                        "provider_wireless_transceiver",
                        WirelessTransceiver.Provider.class,
                        WirelessTransceiver.Tooltip.class,
                        BlockEntityWirelessTransceiver.class,
                        BlockWirelessTransceiver.class
                ),
                new InfoBlock<>(
                        "provider_port_upload",
                        PortUpload.Provider.class,
                        PortUpload.Tooltip.class,
                        BlockEntityUpload.class,
                        BlockUpload.class
                )
        );
    }

    private record InfoBlock<
            TProvider extends Enum<TProvider> & IObjectedProvider<BlockAccessor>,
            TConsumer extends Enum<TConsumer> & IBlockComponentProvider
            >(
            String uid,
            Class<TProvider> clazzProvider,
            Class<TConsumer> clazzConsumer,
            Class<? extends BlockEntity> clazzBlockEntity,
            Class<? extends Block> clazzBlock
    ) {
        public void registerProvider(IWailaCommonRegistration registration) {
            registration.registerBlockDataProvider(
                    WrapperObjectedProvider.create(this.uid, this.clazzProvider),
                    this.clazzBlockEntity
            );
        }

        public void registerConsumer(IWailaClientRegistration registration) {
            for (var entry : this.clazzConsumer.getEnumConstants()) {
                registration.registerBlockComponent(entry, this.clazzBlock);
            }
        }
    }
}