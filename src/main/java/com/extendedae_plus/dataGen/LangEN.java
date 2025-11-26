package com.extendedae_plus.dataGen;

import appeng.core.definitions.AEItems;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.init.ModBlocks;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.util.UtilGetKey;
import com.glodblock.github.extendedae.common.EAESingletons;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Arrays;

public class LangEN extends LanguageProvider {
    public LangEN(PackOutput output) {
        super(output, ExtendedAEPlus.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        UtilGetKey.bindTranslator("en_us", this::add);

        this.addItem(ModItems.ENTITY_TICKER_PART_ITEM, "Entity Accelerator");
        this.addItem(ModItems.INFINITY_BIGINTEGER_CELL_ITEM, "§4De§cvou§6rer §eof §aCo§bsmic §dSilence");
        this.addItem(ModItems.PROVIDER_CONTROLLER, "Pattern Provider Controller");
        this.addItem(ModItems.CHANNEL_CARD, "Channel Card");
        this.addItem(ModItems.ENTITY_SPEED_CARD, "Entity Speed Card");
        new UtilGetKey(ModItems.ENTITY_SPEED_CARD)
                .addStr("multiplier").buildInto("Entity Speed Card (x%s)");

        this.addBlock(ModBlocks.WIRELESS_TRANSCEIVER, "Wireless Transceiver");
        this.addBlock(ModBlocks.ASSEMBLER_MATRIX_UPLOAD_CORE, "Assembly Matrix Upload Core");
        Arrays.stream(EAEPCraftingUnitType.values()).forEach(type ->
                this.addBlock(type.getBlock(), type.getAcceleratorThreads() + "x Crafting Accelerator"));


        new UtilGetKey(UtilGetKey.creativeTab)
                .addStr("main").buildInto("ExtendedAE Plus");

        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.CHANNEL_CARD)
                .addStr("frequency")
                .branch("unset", "Frequency: Unset")
                .branch("set_to", "Frequency set to %s")
                .branch("set_to.none", "Frequency Cleared")
                .buildInto("Frequency: %s");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.CHANNEL_CARD)
                .branch("name", "Bound to: %s")
                .branch("id", "Bound to UUID{%2$s}")
                .buildInto("Unbound");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.ENTITY_SPEED_CARD)
                .branch("multiplier", "Multiplier: %s")
                .branch("max", "Max: %s");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.ENTITY_TICKER_PART_ITEM)
                .buildInto("""
                        Put in the Entity Speed Card to enable acceleration
                        Up to 1024x acceleration
                        Accelerate will consume energy in ME network""");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(ModItems.INFINITY_BIGINTEGER_CELL_ITEM)
                .addStr("description")
                .buildInto("""
                        §5Per novem sacra, §dad vanum sonus§r
                        §8Iava, Lord of the Void§r, grants you this
                        §b—§4In§d fi§cnite§e space§a, §6in§bfi§5nite§9 worlds""");
        new UtilGetKey(UtilGetKey.tooltip)
                .item(AEItems.PROCESSING_PATTERN.get())
                .addStr("encoder")
                .buildInto("Encoded by %s");
        new UtilGetKey(UtilGetKey.tooltip)
                .addStr("bom")
                .addStr("help")
                .buildInto("""
                        \n---------§aExtendedAE Plus§r---------
                        Hold §6[Ctrl]§r and Right click a node,
                        to encode & upload a pattern automatically""");

        new UtilGetKey(UtilGetKey.screenTooltip)
                .item(ModItems.ENTITY_TICKER_PART_ITEM)
                .branch("blacklist", "§c§lTarget block disabled")
                .branch("enabled", "Enabled")
                .branch("disabled", "Disabled");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("smart_blocking")
                .branch("enabled", "The same recipe will no longer block (requires the original blocking mode)")
                .branch("disabled", "Open it please pwq")
                .branch("disabled_by_super", "You know why it doesn't work")
                .buildInto("Smart Blocking");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("smart_doubling")
                .branch("enabled", "Intelligently scale the processing pattern based on the volume of requests")
                .branch("disabled", "Nothing be to do")
                .buildInto("Smart Doubling");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("upload_button")
                .branch("auto_upload", "\n§a[Ctrl] §7Upload encoded pattern")
                .buildInto("§7Upload encoded pattern");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("toggle_slot_display")
                .branch("enabled", "Hide slots")
                .branch("disabled", "Display slots")
                .buildInto("Toggle slots display");
        new UtilGetKey(UtilGetKey.screenTooltip)
                .addStr("provider_list")
                .addStr("candidate_keywords")
                .buildInto("§f§lKeywords");

        new UtilGetKey(UtilGetKey.message)
                .addStr("provider_list")
                .branch("remap_success", "[EAEP] Succeed to remap")
                .branch("remap_failed", "[EAEP] Failed to remap");
        new UtilGetKey(UtilGetKey.message)
                .addStr("provider_list")
                .addStr("add_alias")
                .branch("empty_query", "[EAEP] Empty query, please input the KeywordToMap first")
                .branch("empty_alias", "[EAEP] Empty alias, please input the AliasToMap")
                .branch("success", "[EAEP] Alias{%s → %s}")
                .branch("failed", "[EAEP] Failed to map Alias{%s}");
        new UtilGetKey(UtilGetKey.message)
                .addStr("provider_list")
                .addStr("delete_alias")
                .branch("empty_alias", "[EAEP] Empty alias, please input the AliasToDelete")
                .branch("success", "[EAEP] Alias{%s × %s} was deleted")
                .branch("failed", "[EAEP] Failed to delete Alias{%s}");
        new UtilGetKey(UtilGetKey.message)
                .addStr("pattern_uploading")
                .addStr("duplicate_pattern")
                .buildInto("[EAEP] Duplicate patterns");
        new UtilGetKey(UtilGetKey.message)
                .addStr("provider_to_upload")
                .branch("selected", "[EAEP] PatternProvider{%s} was selected")
                .branch("unset", "[EAEP] Please select a provider first")
                .branch("failed", "[EAEP] Failed to upload patterns")
                .branch("invalid_pattern", "[EAEP] Can't upload Non-Processing patterns");
        new UtilGetKey(UtilGetKey.message)
                .item(ModItems.PROVIDER_CONTROLLER)
                .addStr("global_switch")
                .buildInto("[EAEP] The global setting is now in effect, affecting PatternProvider x%s");
        new UtilGetKey(UtilGetKey.message)
                .addStr("pattern_scaling")
                .branch("mul", "[EAEP] Multiplied to %s: T/S/F[%s/%s/%s]")
                .branch("div", "[EAEP] Divided to 1/%s: T/S/F[%s/%s/%s]");
        new UtilGetKey(UtilGetKey.message)
                .addStr("opened_provider_info")
                .buildInto("[EAEP] Now opening PatternProvider{Location[%s], Dimension[%s]}");

        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .branch("switch_locked", "Locked")
                .branch("switch_unlock", "Unlocked");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("locked")
                .buildInto("Transceiver is Locked");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("mode")
                .branch("master", "Switched to Master")
                .branch("slave", "Switched to Slave");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("frequency")
                .branch("unset", "Frequency Cleared")
                .buildInto("Frequency: %s");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(AEItems.CERTUS_QUARTZ_KNIFE.get())
                .addStr("block_name_coping")
                .branch("success", "Copied BlockName{%s} to the clipboard")
                .branch("failed", "Failed to copy BlockName{%s}");
        new UtilGetKey(UtilGetKey.actionBar)
                .item(ModItems.CHANNEL_CARD)
                .addStr("binding")
                .branch("clear", "Binding Cleared")
                .buildInto("Bound to: %s");

        new UtilGetKey(UtilGetKey.screen)
                .item(ModItems.ENTITY_TICKER_PART_ITEM)
                .branch("enabled", "§2§lRunning")
                .branch("blacklist", "§c§lDisabled")
                .branch("needs_energy", "§6§lEnergy Insufficient")
                .branch("speed", "Total multiplier: %s")
                .branch("energy", "Energy cost: %s/t")
                .branch("power_ratio", "Power ratio: %s")
                .branch("multiplier", "Additional consumption multiplier: %s");
        new UtilGetKey(UtilGetKey.screen)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("frequency_input")
                .branch("input_field", "Input to modify...")
                .branch("confirm", "confirm")
                .branch("cancel", "cancel")
                .buildInto("Input Frequency");
        new UtilGetKey(UtilGetKey.screen)
                .addStr("provider_list")
                .branch("query", "Input to search...")
                .branch("alias", "Input aliasToMap...")
                .branch("remap_aliases", "Remap")
                .branch("add_alias", "Add a map")
                .branch("delete_alias", "Delete a map")
                .buildInto("Select a Provider to Upload");
        new UtilGetKey(UtilGetKey.screen)
                .item(ModItems.PROVIDER_CONTROLLER)
                .branch("blocking", "Toggle Blocking")
                .branch("smart_blocking", "Toggle Smart Blocking")
                .branch("smart_doubling", "Toggle Smart Doubling")
                .branch("all_on", "All On")
                .branch("all_off", "All Off")
                .buildInto("Pattern Provider Management Panel");
        new UtilGetKey(UtilGetKey.screen)
                .item(EAESingletons.EX_PATTERN_PROVIDER.asItem())
                .addStr("pages")
                .buildInto("%s/%s Pages");

        new UtilGetKey(UtilGetKey.keywordGroup)
                .addStr("workstations")
                .buildInto("§Keyword Group {§rRecipe[§l%s§6]}");

        new UtilGetKey(UtilGetKey.config)
                .branch("title", "ExtendedAE Plus Configs")
                .branch("state_on", "True")
                .branch("state_off", "False")
                .branch("ae", "AE2")
                .branch("wireless", "Wireless")
                .branch("entitySpeedTicker", "Ticker");
        new UtilGetKey(UtilGetKey.config)
                .addStr("pageMultiplier")
                .branch("tooltip", """
                        Expand the multiplier of the total slot capacity of the provider
                        The base is 36, each page still displays 36 cells, and the magnification will increase the total number of pages/total capacity
                        Recommended range 1-16""")
                .buildInto("Ex Pattern Provider Slots Multiplier");
        new UtilGetKey(UtilGetKey.config)
                .addStr("overrideAE2WTPicking")
                .branch("tooltip", "There should have something? Sorry I forgot")
                .buildInto("Override AE2WT Picking");
        new UtilGetKey(UtilGetKey.config)
                .addStr("showEncoderPatternPlayer")
                .branch("tooltip", "If true, tooltips in patterns will show the encoder")
                .buildInto("Show Pattern Encoder");
        new UtilGetKey(UtilGetKey.config)
                .addStr("patternTerminalShowSlotsDefault")
                .branch("tooltip", "If true, pattern access menu will display provider slots by default")
                .buildInto("Show slots by default");
        new UtilGetKey(UtilGetKey.config)
                .addStr("independentUploadingButton")
                .branch("tooltip", "If true, a button will appear in the pattern coding terminal for uploading patterns.")
                .buildInto("Independent Upload Button");
        new UtilGetKey(UtilGetKey.config)
                .addStr("craftingPauseThreshold")
                .branch("tooltip", """
                        The larger the value, the fewer wait/notify times in the process of AE building the synthetic plan,
                        which improves throughput but reduces scheduling responsiveness.""")
                .buildInto("AE Composition Calculation Pause Check Threshold");
        new UtilGetKey(UtilGetKey.config)
                .addStr("smartScalingMaxMultiplier")
                .branch("tooltip", "The maximum multiplier for smart doubling (0 means no limit)")
                .buildInto("Smart Doubling Maximum Multiplier");
        new UtilGetKey(UtilGetKey.config)
                .addStr("providerRoundRobinEnable")
                .branch("tooltip", " Note: All related providers need to enable smart doubling, otherwise they may fail")
                .buildInto("Enable Pattern Provider Polling Assignment");
        new UtilGetKey(UtilGetKey.config)
                .addStr("wirelessMaxRange")
                .branch("tooltip", """
                        The straight-line distance between the slave end and the master end must be less than or equal to this value before a connection can be established
                        (0 means no limit)""")
                .buildInto("Wireless Maximum Distance");
        new UtilGetKey(UtilGetKey.config)
                .addStr("wirelessCrossDimEnable")
                .branch("tooltip", "There should have something? Sorry I forgot")
                .buildInto("Allow Transceiver Links Cross Dimensions");
        new UtilGetKey(UtilGetKey.config)
                .addStr("entityTickerCost")
                .buildInto("Base Ticker Energy Cost");
        new UtilGetKey(UtilGetKey.config)
                .addStr("entityTickerBlackList")
                .branch("tooltip", """
                        Supports Wildcards/Regex
                        For Example 'minecraft:chest', 'minecraft:*', 'mekanism:.*_factory'""")
                .buildInto("Ticker Blacklist");
        new UtilGetKey(UtilGetKey.config)
                .addStr("entityTickerMultipliers")
                .branch("tooltip", """
                        Set additional energy multipliers for certain blocks,
                        Template 'modid:blockid multiplier'，Supports Wildcards/Regex
                        For Example 'mekanism:.*_factory 2x'""")
                .buildInto("Ticker Additional Consumption Multiplier");
        new UtilGetKey(UtilGetKey.config)
                .addStr("prioritizeDiskEnergy")
                .branch("tooltip", """
                        If true，ticker will uses energy stored in disks first
                        Note: AppliedFlux only""")
                .buildInto("Prioritize Extraction of Energy from Disk");
        new UtilGetKey(UtilGetKey.config)
                .addStr("needsUploadingCore")
                .branch("tooltip", "If true, patterns can only be uploaded to assembly matrix with upload core")
                .buildInto("Needs Uploading Core");

        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("frequency")
                .branch("unset", "Frequency: Unset")
                .buildInto("Frequency: %s");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("mode")
                .branch("master", "Master Mode")
                .branch("slave", "Slave Mode");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("master_location")
                .branch("custom_name", "Master Node: %4$s{%1$s, %2$s, %3$s}")
                .buildInto("Master Node: Transceiver{%s, %s, %s}");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("master_location")
                .addStr("dim")
                .buildInto("Dimension: %s");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("locked")
                .buildInto("Locked");
        new UtilGetKey(UtilGetKey.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .branch("name", "Owner: %s")
                .branch("id", "Owner{%2$s}")
                .buildInto("Public Mode");

        new UtilGetKey(UtilGetKey.jadeConfig)
                .addStr("wireless_transceiver")
                .branch("channels", "Wireless Transceiver: Channels")
                .branch("frequency", "Wireless Transceiver: Frequency")
                .branch("master_mode", "Wireless Transceiver: Mode")
                .branch("master_location", "Wireless Transceiver: Master Location")
                .branch("locked", "Wireless Transceiver: Locked")
                .branch("placer", "Wireless Transceiver: Owner");

        UtilGetKey.destroy("en_us");
    }
}
