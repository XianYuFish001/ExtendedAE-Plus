package com.extendedae_plus.dataGen;

import appeng.core.definitions.AEItems;
import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.init.ModBlocks;
import com.extendedae_plus.common.init.ModItems;
import com.extendedae_plus.common.registry.block.EAEPCraftingUnitType;
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
        this.addItem(ModItems.INFINITY_BIGINTEGER_CELL_ITEM, "Devourer of Cosmic Silence");
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
                .addStr("label")
                .branch("unset", "Link Label: Unset")
                .buildInto("Link Label: %s");
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
                        §fEnergy card energy consumption reduction calculation formula:
                        §2§o0.9 * (0.5 / 0.9)^((EnergyCardCount - 1) / 7)""")
                .buildInto("""
                        Apply the Ticking Card to enable acceleration
                        Up to 1024x acceleration
                        Accelerate will consume energy in ME network""");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.tooltip)
                .item(ModItems.INFINITY_BIGINTEGER_CELL_ITEM)
                .addStr("description")
                .branch("colored", "——Infinite space, infinite worlds")
                .buildInto("""
                        §5Per novem sacra, §dad vanum sonus§r
                        §8Iava, Lord of the Void§r, grants you this
                        """);
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
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("recipe_alias")
                .branch("add", "Add a Mapping")
                .branch("remove", "Remove Mappings")
                .buildInto("Alias Actions");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("row_slots_visible")
                .branch("visible", "Visible")
                .branch("invisible", "Invisible")
                .buildInto("Pattern Slot Visibility");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("label_link")
                .addStr("info_label")
                .branch("public", "Public")
                .buildInto("Owner: %s{%s}");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("label_link")
                .addStr("label_description")
                .branch("empty", "Desc: Empty")
                .buildInto("Desc: ");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("label_type")
                .branch("frequency", "Frequency")
                .branch("label", "String Label")
                .buildInto("Link Label Type");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("label_mode")
                .branch("public", "Public")
                .branch("private", "Private/Team")
                .buildInto("Link Label Mode");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("label_add")
                .buildInto("Reg Link Label");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("label_locked")
                .branch("locked", "Locked")
                .branch("unlocked", "Unlocked")
                .buildInto("Lock Device Label");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("transceiver_mode")
                .branch("master", "Master")
                .branch("slave", "Slave")
                .buildInto("Device Mode");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screenTooltip)
                .addStr("transfer_mode")
                .branch("none", "None")
                .branch("merge_adjacency", "Merge Adjacent Items")
                .branch("independence", "Full Independence")
                .buildInto("Recipe Transfer Merge Mode");

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
                .addStr("provider_list")
                .branch("query", "Input to search...")
                .branch("alias", "Input aliasToMap...")
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
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screen)
                .addStr("stacks_rename")
                .buildInto("Rename");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.screen)
                .addStr("label_link")
                .branch("register", "Reg Link Label")
                .branch("label_value", "Link Label")
                .branch("label_description", "Link Label Description")
                .buildInto("Choose a Link Label");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.keywordGroup)
                .addStr("workstations")
                .buildInto("§Keyword Group {§rRecipe[§l%s§6]}");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .branch("title", "ExtendedAE Plus Config")
                .branch("ae", "AE2")
                .branch("ticker", "Ticker")
                .branch("assembler_matrix", "Assembler Matrix");
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
                .addStr("baseCoreCrafterThreads")
                .buildInto("Advanced Craft Core Base Threads");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("coreCrafterThreadAmplification")
                .branch("tooltip", """
                        Advanced Craft Core Thread Amplification per 5 speed multiplier(see GuideME),
                        0 means disabled""")
                .buildInto("Advanced Craft Core Thread Amplification");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("maximumCoreCrafterThreads")
                .branch("tooltip", "Tip: The maximum threads cannot be less than the base threads")
                .buildInto("Advanced Craft Core Maximum Threads");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("corePatternSlotMultiplier")
                .buildInto("Advanced Pattern Core Slot Multiplier");
        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.config)
                .addStr("needsUploadingPort")
                .branch("tooltip", "If true, patterns can only be uploaded to assembly matrix with upload port")
                .buildInto("Needs Uploading Port");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.jadeInfo)
                .item(ModItems.WIRELESS_TRANSCEIVER)
                .addStr("label")
                .branch("unset", "Link Label: Unset")
                .buildInto("Link Label: %s");
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
                .buildInto("Public");

        UtilKeyBuilder.ofDataGen(UtilKeyBuilder.jadeConfig)
                .addStr("wireless_transceiver")
                .branch("channels", "Wireless Transceiver: Channels")
                .branch("label", "Wireless Transceiver: Link Label")
                .branch("master_mode", "Wireless Transceiver: Mode")
                .branch("master_location", "Wireless Transceiver: Master Location")
                .branch("locked", "Wireless Transceiver: Locked")
                .branch("placer", "Wireless Transceiver: Owner");

        UtilKeyBuilder.BuilderDataGen.destroy("en_us");
    }
}
