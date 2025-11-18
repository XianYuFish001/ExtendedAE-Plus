package com.extendedae_plus.integration.jade;

import com.extendedae_plus.common.block.wirelessTransceiver.BlockEntityWirelessTransceiver;
import com.extendedae_plus.common.block.wirelessTransceiver.BlockWirelessTransceiver;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin("extendedae_plus") // 你的 mod ID
public class EAEPJadePlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		// 注册服务端数据提供者（用于同步数据）
		registration.registerBlockDataProvider(
                CommonProvider.create("provider_wireless_transceiver", ProviderWirelessTransceiver.getProviders()),
                BlockEntityWirelessTransceiver.class
        );
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		// 遍历组件常量，逐一注册
		for (var component : TooltipWirelessTransceiver.values()) {
			registration.registerBlockComponent(component, BlockWirelessTransceiver.class);
		}
	}
}