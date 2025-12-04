package com.extendedae_plus.dataGen;

import appeng.core.definitions.AEItems;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.block.EAEPCraftingUnitType;
import com.extendedae_plus.common.init.ModBlocks;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.util.UtilKeyBuilder;
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
        UtilKeyBuilder.BuilderDataGen.bindTranslator("en_us", this::add);

        this.addItem(ModItems.PART_TICKER, "Ticker");
        this.addItem(ModItems.INFINITY_BIGINTEGER_CELL_ITEM, "§4De§cvou§6rer §eof §aCo§bsmic §dSilence");
        this.addItem(ModItems.PROVIDER_CONTROLLER, "Provider Controller");
        this.addItem(ModItems.CHANNEL_CARD, "Channel Card");
        this.addItem(ModItems.CARD_AUTO_COMPLETION, "Auto Completion Card");
        this.addItem(ModItems.PRIORITY_TOOL, "Priority Override Tool");
        this.addItem(ModItems.TICKING_CARD, "Ticking Card");
        UtilKeyBuilder.ofDataGen(ModItems.TICKING_CARD)
                .addStr("multiplier")
                .buildInto("Ticking Card (x%s)");

        this.addBlock(ModBlocks.WIRELESS_TRANSCEIVER, "Wireless Transceiver");
        this.addBlock(ModBlocks.PORT_UPLOAD, "Assembly Matrix Upload Port");
        this.addBlock(ModBlocks.CORE_ADVANCED_CRAFTER, "Assembler Matrix Advanced Craft Core");
        this.addBlock(ModBlocks.CORE_ADVANCED_PATTERN, "Assembler Matrix Advanced Pattern Core");
        this.addBlock(ModBlocks.CORE_ADVANCED_SPEED, "Assembler Matrix Advanced Speed Core");
        Arrays.stream(EAEPCraftingUnitType.values()).forEach(type ->
                this.addBlock(type.getBlock(), type.getAcceleratorThreads() + "x Crafting Accelerator"));

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.creativeTab)
                .addStr("main")
                .buildInto("ExtendedAE Plus");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .item(ModItems.CHANNEL_CARD)
                .addStr("frequency")
                .branch("unset", "Frequency: Unset")
                .branch("set_to", "Frequency set to %s")
                .branch("set_to.none", "Frequency Cleared")
                .buildInto("Frequency: %s");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .item(ModItems.CHANNEL_CARD)
                .branch("name", "Bound to: %s")
                .branch("id", "Bound to UUID{%2$s}")
                .buildInto("Unbound");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .item(ModItems.TICKING_CARD)
                .branch("multiplier", "Multiplier: %s")
                .branch("max", "Max: %s");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .item(ModItems.PART_TICKER)
                .branch("advanced_tip", """
                        Ticking multiplier calculation formula:
                        §2§oBaseTickerEnergyCost * ((2147483647 / BaseTickerEnergyCost) ^ 0.1) ^ (log2(SpeedMultiplier))
                        §rEnergy card energy consumption reduction calculation formula:
                        §2§o0.9 * (0.5 / 0.9)^((EnergyCardCount - 1) / 7)""")
                .buildInto("""
                        Apply the Ticking Card to enable acceleration
                        Up to 1024x acceleration
                        Accelerate will consume energy in ME network""");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .item(ModItems.INFINITY_BIGINTEGER_CELL_ITEM)
                .addStr("description")
                .buildInto("""
                        §5Per novem sacra, §dad vanum sonus§r
                        §8Iava, Lord of the Void§r, grants you this
                        §b—§4In§d fi§cnite§e space§a, §6in§bfi§5nite§9 worlds""");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .item(AEItems.PROCESSING_PATTERN.get())
                .addStr("encoder")
                .buildInto("Encoded by %s");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .addStr("bom")
                .addStr("help")
                .buildInto("""
                        \n---------§aExtendedAE Plus§r---------
                        Hold §6[Ctrl]§r and Right click a node,
                        to encode & upload a pattern automatically""");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .item(ModItems.CARD_AUTO_COMPLETION)
                .branch("advanced_tip", "What you need is just copy one from NAE2")
                .buildInto("Cancel the crafting task automatically when items in patterns be pushed");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .item(ModItems.PRIORITY_TOOL)
                .buildInto("Simple Tool for machine priority overriding");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("state_ticker")
                .branch("blacklist", "§c§lTarget block blacklisted")
                .branch("enabled", "Enabled")
                .branch("disabled", "Disabled")
                .buildInto("Ticker State");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("smart_blocking")
                .branch("enabled", "The same recipe will no longer block (requires the original blocking mode)")
                .branch("disabled", "Open it please pwq")
                .branch("disabled_by_super", "You know why it doesn't work")
                .buildInto("Smart Blocking");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("smart_doubling")
                .branch("enabled", "Intelligently scale the processing pattern based on the volume of requests")
                .branch("disabled", "Nothing be to do")
                .buildInto("Smart Doubling");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("upload_button")
                .branch("auto_upload", "\n§a[Ctrl] §7Upload encoded pattern")
                .buildInto("§7Upload encoded pattern");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("toggle_slot_display")
                .branch("enabled", "Hide slots")
                .branch("disabled", "Display slots")
                .buildInto("Toggle slots display");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("provider_list")
                .addStr("candidate_keywords")
                .buildInto("§f§lKeywords");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("priority_tool")
                .branch("keep", "Keep")
                .branch("increment", "Increment per apply")
                .branch("decrement", "Decrement per apply")
                .buildInto("Tool Mode");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.message)
                .addStr("provider_list")
                .branch("remap_success", "[EAEP] Succeed to remap")
                .branch("remap_failed", "[EAEP] Failed to remap");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.message)
                .addStr("provider_list")
                .addStr("add_alias")
                .branch("empty_query", "[EAEP] Empty query, please input the KeywordToMap first")
                .branch("empty_alias", "[EAEP] Empty alias, please input the AliasToMap")
                .branch("success", "[EAEP] Alias{%s → %s}")
                .branch("failed", "[EAEP] Failed to map Alias{%s}");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.message)
                .addStr("provider_list")
                .addStr("delete_alias")
                .branch("empty_alias", "[EAEP] Empty alias, please input the AliasToDelete")
                .branch("success", "[EAEP] Alias{%s × %s} was deleted")
                .branch("failed", "[EAEP] Failed to delete Alias{%s}");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.message)
                .addStr("pattern_uploading")
                .addStr("duplicate_pattern")
                .buildInto("[EAEP] Duplicate patterns");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.message)
                .addStr("provider_to_upload")
                .branch("selected", "[EAEP] PatternProvider{%s} was selected")
                .branch("unset", "[EAEP] Please select a provider first")
                .branch("failed", "[EAEP] Failed to upload patterns")
                .branch("invalid_pattern", "[EAEP] Can't upload Non-Processing patterns");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.message)
                .item(ModItems.PROVIDER_CONTROLLER)
                .addStr("global_switch")
                .buildInto("[EAEP] The global setting is now in effect, affecting PatternProvider x%s");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.message)
                .addStr("pattern_scaling")
                .branch("mul", "[EAEP] Multiplied to %s: T/S/F[%s/%s/%s]")
                .branch("div", "[EAEP] Divided to 1/%s: T/S/F[%s/%s/%s]");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.message)
                .addStr("opened_provider_info")
                .buildInto("[EAEP] Now opening PatternProvider{Location[%s], Dimension[%s]}");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .branch("switch_locked", "Locked")
                .branch("switch_unlock", "Unlocked");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("locked")
                .buildInto("Transceiver is Locked");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("mode")
                .branch("master", "Switched to Master")
                .branch("slave", "Switched to Slave");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.actionBar)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("frequency")
                .branch("unset", "Frequency Cleared")
                .buildInto("Frequency: %s");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.actionBar)
                .item(AEItems.CERTUS_QUARTZ_KNIFE.get())
                .addStr("block_name_coping")
                .branch("success", "Copied BlockName{%s} to the clipboard")
                .branch("failed", "Failed to copy BlockName{%s}");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.actionBar)
                .item(ModItems.CHANNEL_CARD)
                .addStr("binding")
                .branch("clear", "Binding Cleared")
                .buildInto("Bound to: %s");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screen)
                .item(ModItems.PART_TICKER)
                .branch("enabled", "§2§lTicker Enabled")
                .branch("needs_energy", "§6§lEnergy Insufficient")
                .branch("disabled", "§0§lTicker Disabled")
                .branch("blacklisted", "§c§lTarget Block Blacklisted")
                .branch("speed_multiplier", "Speed Multiplier: %s")
                .branch("energy_cost", "Energy Cost: %s/t")
                .branch("power_ratio", "Power Ratio: %s")
                .branch("cost_multiplier", "Additional Cost Multiplier: %s");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screen)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("frequency_input")
                .branch("input_field", "Input to modify...")
                .branch("confirm", "confirm")
                .branch("cancel", "cancel")
                .buildInto("Input Frequency");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screen)
                .addStr("provider_list")
                .branch("query", "Input to search...")
                .branch("alias", "Input aliasToMap...")
                .branch("remap_aliases", "Remap")
                .branch("add_alias", "Add a map")
                .branch("delete_alias", "Delete a map")
                .buildInto("Select a Provider to Upload");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screen)
                .item(ModItems.PROVIDER_CONTROLLER)
                .branch("blocking", "Toggle Blocking")
                .branch("smart_blocking", "Toggle Smart Blocking")
                .branch("smart_doubling", "Toggle Smart Doubling")
                .branch("all_on", "All On")
                .branch("all_off", "All Off")
                .buildInto("Pattern Provider Management Panel");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screen)
                .item(EAESingletons.EX_PATTERN_PROVIDER.asItem())
                .addStr("pages")
                .buildInto("%s/%s Pages");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screen)
                .item(ModItems.PRIORITY_TOOL)
                .buildInto("Override Priority");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.keywordGroup)
                .addStr("workstations")
                .buildInto("§Keyword Group {§rRecipe[§l%s§6]}");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .branch("title", "ExtendedAE Plus Config")
                .branch("ae", "AE2")
                .branch("wireless", "Wireless")
                .branch("ticker", "Ticker");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("pageMultiplier")
                .branch("tooltip", """
                        Expand the multiplier of the total slot capacity of the provider
                        The base is 36, each page still displays 36 cells, and the magnification will increase the total number of pages/total capacity
                        Recommended range 1-16""")
                .buildInto("Ex Pattern Provider Slots Multiplier");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("overrideAE2WTPicking")
                .branch("tooltip", "There should have something? Sorry I forgot")
                .buildInto("Override AE2WT Picking");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("showEncoderPatternPlayer")
                .branch("tooltip", "If true, tooltips in patterns will show the encoder")
                .buildInto("Show Pattern Encoder");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("patternTerminalShowSlotsDefault")
                .branch("tooltip", "If true, pattern access menu will display provider slots by default")
                .buildInto("Show slots by default");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("independentUploadingButton")
                .branch("tooltip", "If true, a button will appear in the pattern coding terminal for uploading patterns.")
                .buildInto("Independent Upload Button");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("craftingPauseThreshold")
                .branch("tooltip", """
                        The larger the value, the fewer wait/notify times in the process of AE building the synthetic plan,
                        which improves throughput but reduces scheduling responsiveness.""")
                .buildInto("AE Composition Calculation Pause Check Threshold");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("smartScalingMaxMultiplier")
                .branch("tooltip", "The maximum multiplier for smart doubling (0 means no limit)")
                .buildInto("Smart Doubling Maximum Multiplier");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("providerRoundRobinEnable")
                .branch("tooltip", " Note: All related providers need to enable smart doubling, otherwise they may fail")
                .buildInto("Enable Pattern Provider Polling Assignment");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("wirelessMaxRange")
                .branch("tooltip", """
                        The straight-line distance between the slave end and the master end must be less than or equal to this value before a connection can be established
                        (0 means no limit)""")
                .buildInto("Wireless Maximum Distance");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("wirelessCrossDimEnable")
                .branch("tooltip", "There should have something? Sorry I forgot")
                .buildInto("Allow Transceiver Links Cross Dimensions");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("tickerBaseCost")
                .buildInto("Base Ticker Energy Cost");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("tickerBlacklist")
                .branch("tooltip", """
                        Use Block Description Name/Block Tag
                        For Example 'mekanism:enrichment_chamber', '#c:storage_blocks/unobtainium'""")
                .buildInto("Ticker Blacklist");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("tickerExternalMultipliers")
                .branch("tooltip", """
                        Set additional energy multipliers for certain blocks,
                        Template '<entry>\\[<multiplier>\\]'
                        Use Block Description Name/Block Tag on <entry>
                        For Example 'mekanism:enrichment_chamber[1.14]', '#c:storage_blocks/unobtainium[5.14]'""")
                .buildInto("Ticker Additional Cost Multiplier");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("allowDiskEnergy")
                .branch("tooltip", """
                        If true，ticker will uses energy stored in disks first
                        Note: AppliedFlux only""")
                .buildInto("Allow Disk Energy");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("needsUploadingPort")
                .branch("tooltip", "If true, patterns can only be uploaded to assembly matrix with upload port")
                .buildInto("Needs Uploading Port");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("frequency")
                .branch("unset", "Frequency: Unset")
                .buildInto("Frequency: %s");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("mode")
                .branch("master", "Master Mode")
                .branch("slave", "Slave Mode");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("master_location")
                .branch("custom_name", "Master Node: %4$s{%1$s, %2$s, %3$s}")
                .buildInto("Master Node: Transceiver{%s, %s, %s}");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("master_location")
                .addStr("dim")
                .buildInto("Dimension: %s");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("locked")
                .buildInto("Locked");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .branch("name", "Owner: %s")
                .branch("id", "Owner{%2$s}")
                .buildInto("Public Mode");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.jadeConfig)
                .addStr("wireless_transceiver")
                .branch("channels", "Wireless Transceiver: Channels")
                .branch("frequency", "Wireless Transceiver: Frequency")
                .branch("master_mode", "Wireless Transceiver: Mode")
                .branch("master_location", "Wireless Transceiver: Master Location")
                .branch("locked", "Wireless Transceiver: Locked")
                .branch("placer", "Wireless Transceiver: Owner");

        UtilKeyBuilder.BuilderDataGen.destroy("en_us");
    }
}
