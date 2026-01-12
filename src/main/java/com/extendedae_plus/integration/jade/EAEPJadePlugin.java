package com.extendedae_plus.integration.jade;

import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockEntityUpload;
import com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload.BlockUpload;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.registry.block.wirelessTransceiver.BlockWirelessTransceiver;
import com.extendedae_plus.integration.jade.portUpload.ProviderPortUpload;
import com.extendedae_plus.integration.jade.portUpload.TooltipPortUpload;
import com.extendedae_plus.integration.jade.wirelessTransceiver.ProviderWirelessTransceiver;
import com.extendedae_plus.integration.jade.wirelessTransceiver.TooltipWirelessTransceiver;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.*;

import java.util.List;

@WailaPlugin("extendedae_plus") // 你的 mod ID
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
                        ProviderWirelessTransceiver.class,
                        TooltipWirelessTransceiver.class,
                        BlockEntityWirelessTransceiver.class,
                        BlockWirelessTransceiver.class
                ),
                new InfoBlock<>(
                        "provider_port_upload",
                        ProviderPortUpload.class,
                        TooltipPortUpload.class,
                        BlockEntityUpload.class,
                        BlockUpload.class
                )
        );
    }

    private record InfoBlock<
            TProvider extends Enum<TProvider> & ObjectedProvider.IObjectedProvider<BlockAccessor>,
            TConsumer extends Enum<TConsumer> & IBlockComponentProvider>(
            String uid,
            Class<TProvider> clazzProvider,
            Class<TConsumer> clazzConsumer,
            Class<? extends BlockEntity> clazzBlockEntity,
            Class<? extends Block> clazzBlock
    ) {
        public void registerProvider(IWailaCommonRegistration registration) {
            registration.registerBlockDataProvider(
                    ObjectedProvider.create(this.uid,
                            ObjectedProvider.IObjectedProvider.getProviders(this.clazzProvider)),
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