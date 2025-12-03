package com.extendedae_plus.common.impl.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.AESmithingTablePattern;
import appeng.crafting.pattern.AEStonecuttingPattern;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.implementations.PatternAccessTermMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;
import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.common.block.assemblerMatrix.coreUpload.UploadCoreBlockEntity;
import com.extendedae_plus.mixin.core.ae2.accessor.PatternEncodingTermMenuAccessor;
import com.extendedae_plus.util.UtilKeyBuilder;
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ExtendedAE扩展样板管理终端专用的样板上传工具类
 * 兼容ExtendedAE的ContainerExPatternTerminal和原版AE2的PatternAccessTermMenu
 */
public class PatternUploader {
    /**
     * 获取玩家当前的样板访问终端菜单（支持ExtendedAE和原版AE2）
     * 
     * @param player 玩家
     * @return PatternAccessTermMenu实例，如果玩家没有打开则返回null
     */
    public static PatternAccessTermMenu getPatternAccessMenu(ServerPlayer player) {
        if (player == null) return null;
        // 优先检查ExtendedAE的扩展样板管理终端（使用类名检查避免直接导入）
        String containerClassName = player.containerMenu.getClass().getName();
        if (containerClassName.equals("com.glodblock.github.extendedae.container.ContainerExPatternTerminal")) {
            // ExtendedAE的容器继承自PatternAccessTermMenu，可以安全转换
            return (PatternAccessTermMenu) player.containerMenu;
        }
        // 兼容原版AE2的样板访问终端
        if (player.containerMenu instanceof PatternAccessTermMenu) {
            return (PatternAccessTermMenu) player.containerMenu;
        }
        return null;
    }

    /**
     * 从 AE2 的图样编码终端菜单上传当前“已编码图样”至 ExtendedAE 装配矩阵（仅合成图样）。
     * 不会处理“处理图样”。
     *
     * @param player 服务器玩家
     * @param menu   PatternEncodingTermMenu
     */
    public static void uploadFromEncodingMenuToMatrix(ServerPlayer player, PatternEncodingTermMenu menu) {
        if (player == null || menu == null) {
            return;
        }

        // 读取已编码槽位的物品
        var encodedSlot = ((PatternEncodingTermMenuAccessor) menu)
                .eap$getEncodedPatternSlot();
        ItemStack stack = encodedSlot.getItem();
        if (stack.isEmpty() || !PatternDetailsHelper.isEncodedPattern(stack)) {
            return;
        }

        // 仅允许“合成/锻造台/切石机图样”
        IPatternDetails details = PatternDetailsHelper.decodePattern(stack, player.level());
        if (!(details instanceof AECraftingPattern
                || details instanceof AESmithingTablePattern
                || details instanceof AEStonecuttingPattern)) {
            return;
        }

        // 获取 AE 网络
        IGrid grid = null;
        try {
            if (menu.getTarget() instanceof IActionHost host && host.getActionableNode() != null)
                grid = host.getActionableNode().getGrid();
        } catch (Throwable ignored) {}
        if (grid == null) {
            return;
        }

        // 在尝试上传之前，检查装配矩阵是否已经存在相同样板（物品与NBT完全一致）
        if (matrixContainsPattern(grid, stack)) {
            // 直接提醒并跳过上传，并将同等数量的空白样板放回空白样板槽，否则退回玩家背包
            player.sendSystemMessage(UtilKeyBuilder.of(UtilKeyBuilder.message)
                    .addStr("pattern_uploading")
                    .addStr("duplicate_pattern")
                    .build());
            try {
                var accessor = (PatternEncodingTermMenuAccessor) menu;
                var blankSlot = accessor.eap$getBlankPatternSlot();
                ItemStack blanks = AEItems.BLANK_PATTERN.stack(stack.getCount());
                if (blankSlot != null && blankSlot.mayPlace(blanks)) {
                    ItemStack remain = blankSlot.safeInsert(blanks);
                    if (!remain.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(remain, false);
                    }
                } else {
                    player.getInventory().placeItemBackInInventory(blanks, false);
                }
            } catch (Throwable t) {
                // 兜底：直接还给玩家背包
                player.getInventory().placeItemBackInInventory(AEItems.BLANK_PATTERN.stack(stack.getCount()), false);
            }
            // 清空编码样板槽，防止再次输出
            encodedSlot.set(ItemStack.EMPTY);
            return;
        }

        // 收集所有可用的装配矩阵（图样模块）内部库存并逐一尝试（遵循其过滤规则）
        List<InternalInventory> inventories = findAllMatrixPatternInventories(grid);
        if (!inventories.isEmpty()) {
            for (InternalInventory inv : inventories) {
                ItemStack toInsert = stack.copy();
                ItemStack remain = inv.addItems(toInsert);
                if (remain.getCount() < stack.getCount()) {
                    int inserted = stack.getCount() - remain.getCount();
                    stack.shrink(inserted);
                    if (stack.isEmpty()) {
                        encodedSlot.set(ItemStack.EMPTY);
                    }
                    return;
                }
            }
            // 所有内部库存都无法接收 -> 尝试 capability 回退
        }
    }

    /**
     * 在给定 AE Grid 中收集所有已成型且在线的装配矩阵“图样模块”的用于外部插入的内部库存。
     * 优先使用 TileAssemblerMatrixPattern#getExposedInventory（仅允许插入，且已带AE过滤规则）。
     */
    private static List<InternalInventory> findAllMatrixPatternInventories(IGrid grid) {
        List<InternalInventory> result = new ArrayList<>();
        try {
            var tiles = grid.getMachines(TileAssemblerMatrixPattern.class);
            for (TileAssemblerMatrixPattern tile : tiles) {
                if (tile != null && tile.isFormed() && tile.getMainNode().isActive() && clusterHasSingleUploadCore(tile)) {
                    var inv = tile.getExposedInventory();
                    if (inv != null) {
                        result.add(inv);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return result;
    }

    private static boolean matrixContainsPattern(IGrid grid, ItemStack pattern) {
        if (grid == null || pattern == null || pattern.isEmpty()) return false;
        try {
            // 先检查提供外部插入视图的内部库存
            List<InternalInventory> inventories = findAllMatrixPatternInventories(grid);
            for (InternalInventory inv : inventories) {
                if (inv == null) continue;
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack s = inv.getStackInSlot(i);
                    if (!s.isEmpty() && net.minecraft.world.item.ItemStack.isSameItemSameComponents(s, pattern)) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        // 1.21 暂不检查聚合能力视图，能力系统适配后再补充
        return false;
    }

    /**
     * 将玩家背包中的样板上传到指定的样板供应器
     * 兼容ExtendedAE和原版AE2
     *
     * @param player          玩家
     * @param playerSlotIndex 玩家背包槽位索引
     * @param providerId      目标样板供应器的服务器ID
     */
    public static void uploadPatternToProvider(ServerPlayer player, int playerSlotIndex, long providerId) {
        // 1. 验证玩家是否打开了样板访问终端
        PatternAccessTermMenu menu = getPatternAccessMenu(player);
        if (menu == null) {
            return;
        }

        // 2. 获取玩家背包中的物品
        ItemStack playerItem = player.getInventory().getItem(playerSlotIndex);
        if (playerItem.isEmpty()) {
            return;
        }

        // 3. 验证是否是编码样板
        if (!PatternDetailsHelper.isEncodedPattern(playerItem)) {
            return;
        }

        // 4. 获取目标样板供应器
        PatternContainer patternContainer = getPatternContainerById(menu, providerId);
        if (patternContainer == null) {
            return;
        }

        // 5. 获取样板供应器的库存
        InternalInventory patternInventory = patternContainer.getTerminalPatternInventory();
        if (patternInventory == null) {
            return;
        }

        // 6. 使用AE2的标准样板过滤器进行插入
        var patternFilter = new ExtendedAEPatternFilter();
        var filteredInventory = new FilteredInternalInventory(patternInventory, patternFilter);

        // 7. 尝试插入样板
        ItemStack itemToInsert = playerItem.copy();
        ItemStack remaining = filteredInventory.addItems(itemToInsert);

        if (remaining.getCount() < itemToInsert.getCount()) {
            // 插入成功（部分或全部）
            int insertedCount = itemToInsert.getCount() - remaining.getCount();
            playerItem.shrink(insertedCount);
            
            if (playerItem.isEmpty()) {
                player.getInventory().setItem(playerSlotIndex, ItemStack.EMPTY);
            }
        }
    }

    /**
     * 获取样板供应器中的空槽位数量
     * 
     * @param providerId 供应器ID
     * @param menu 样板访问终端菜单（支持ExtendedAE）
     * @return 空槽位数量，如果无法访问则返回-1
     */
    public static int getAvailableSlots(long providerId, PatternAccessTermMenu menu) {
        PatternContainer container = getPatternContainerById(menu, providerId);
        if (container == null) {
            return -1;
        }

        InternalInventory inventory = container.getTerminalPatternInventory();
        if (inventory == null) {
            return -1;
        }

        int availableSlots = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                availableSlots++;
            }
        }

        return availableSlots;
    }

    /**
     * 通过服务器ID获取PatternContainer
     * 兼容ExtendedAE的ContainerExPatternTerminal和原版PatternAccessTermMenu
     * 
     * @param menu 样板访问终端菜单
     * @param providerId 供应器服务器ID
     * @return PatternContainer实例，如果不存在则返回null
     */
    private static PatternContainer getPatternContainerById(PatternAccessTermMenu menu, long providerId) {
        try {
            // 通过反射访问byId字段（ExtendedAE继承了这个字段）
            Field byIdField = findByIdField(menu.getClass());
            if (byIdField == null) return null;
            
            byIdField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            Map<Long, Object> byId = (Map<Long, Object>) byIdField.get(menu);
            
            Object containerTracker = byId.get(providerId);
            if (containerTracker == null) {
                return null;
            }

            // 从ContainerTracker中获取PatternContainer
            Field containerField = findContainerField(containerTracker.getClass());
            if (containerField == null) return null;
            
            containerField.setAccessible(true);
            return (PatternContainer) containerField.get(containerTracker);
            
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 在类层次结构中查找byId字段
     */
    private static Field findByIdField(Class<?> clazz) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField("byId");
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 在类层次结构中查找container字段
     */
    private static Field findContainerField(Class<?> clazz) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField("container");
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    /**
     * ExtendedAE兼容的样板过滤器
     * 使用AE2的PatternDetailsHelper进行样板验证
     */
    private static class ExtendedAEPatternFilter implements IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && PatternDetailsHelper.isEncodedPattern(stack);
        }
    }

    /**
     * 获取样板供应器的显示名称
     * 
     * @param providerId 供应器ID
     * @param menu 样板访问终端菜单
     * @return 显示名称，如果无法获取则返回"未知供应器"
     */
    public static String getProviderDisplayName(long providerId, PatternAccessTermMenu menu) {
        PatternContainer container = getPatternContainerById(menu, providerId);
        if (container == null) {
            return "未知供应器";
        }

        try {
            // 尝试获取供应器的组信息来构建显示名称
            var group = container.getTerminalGroup();
            if (group != null) {
                return group.name().getString();
            }
        } catch (Exception e) {
            // 忽略异常，使用默认名称
        }

        return "样板供应器 #" + providerId;
    }

    /** 获取供应器显示名（优先组名） */
    public static String getProviderDisplayName(PatternContainer container) {
        if (container == null) return "未知供应器";
        try {
            var group = container.getTerminalGroup();
            if (group != null) return group.name().getString();
        } catch (Throwable ignored) {
        }
        return "样板供应器";
    }

    public static String getProviderI18nName(Long providerId, PatternAccessTermMenu menu) {
        return getProviderI18nName(getPatternContainerById(menu, providerId));
    }

    /** 获取样板供应器默认选取的方块名称 */
    public static String getProviderI18nName(PatternContainer container) {
        if (container == null) return "";
        try {
            var group = container.getTerminalGroup();
            var name = group.name();
            return name.toString().contains("literal") ? "" : name.toString();
        } catch (Throwable ignored) {}
        return "";
    }

    /**
     * 验证样板供应器是否可用
     *
     * @param providerId 供应器ID
     * @param menu 样板访问终端菜单
     * @return 是否可用
     */
    public static boolean isProviderAvailable(long providerId, PatternAccessTermMenu menu) {
        PatternContainer container = getPatternContainerById(menu, providerId);
        if (container == null) {
            return false;
        }

        // 检查是否在终端中可见
        if (!container.isVisibleInTerminal()) {
            return false;
        }

        // 检查是否连接到网络
        return container.getGrid() != null;
    }

    /**
     * 将图样编码终端的“已编码图样”上传到指定的样板供应器（通过 providerId 定位）。
     */
    public static void uploadFromEncodingMenuToProvider(ServerPlayer player, PatternEncodingTermMenu menu, long providerId) {
        if (player == null || menu == null) {
            return;
        }
        var encodedSlot = ((PatternEncodingTermMenuAccessor) menu)
                .eap$getEncodedPatternSlot();
        ItemStack stack = encodedSlot.getItem();
        if (stack.isEmpty() || !PatternDetailsHelper.isEncodedPattern(stack)) {
            return;
        }

        PatternAccessTermMenu accessMenu = getPatternAccessMenu(player);
        if (accessMenu == null) {
            return;
        }
        // 先确定目标容器名称，用于同名回退
        String targetName = getProviderDisplayName(providerId, accessMenu);
        // 构建尝试顺序：先指定ID，其次同名的其他ID
        java.util.List<Long> tryIds = new java.util.ArrayList<>();
        tryIds.add(providerId);
        try {
            java.util.List<Long> all = getAllProviderIds(accessMenu);
            for (Long id : all) {
                if (id == null || id == providerId) continue;
                String name = getProviderDisplayName(id, accessMenu);
                if (name.equals(targetName)) {
                    tryIds.add(id);
                }
            }
        } catch (Throwable ignored) {}

        // 按顺序逐个尝试插入
        for (Long id : tryIds) {
            PatternContainer c = getPatternContainerById(accessMenu, id);
            if (c == null || !c.isVisibleInTerminal()) continue;
            InternalInventory inv = c.getTerminalPatternInventory();
            if (inv == null || inv.size() <= 0) continue;

            var filtered = new FilteredInternalInventory(inv, new ExtendedAEPatternFilter());
            ItemStack toInsert = stack.copy();
            ItemStack remain = filtered.addItems(toInsert);
            if (remain.getCount() < toInsert.getCount()) {
                int inserted = toInsert.getCount() - remain.getCount();
                stack.shrink(inserted);
                if (stack.isEmpty()) {
                    encodedSlot.set(ItemStack.EMPTY);
                } else {
                    encodedSlot.set(stack);
                }
                return;
            }
        }
    }

    /**
     * 列出当前菜单中所有供应器的服务器ID（原样返回 byId 的 key 集合）。
     */
    public static java.util.List<Long> getAllProviderIds(PatternAccessTermMenu menu) {
        java.util.List<Long> result = new java.util.ArrayList<>();
        if (menu == null) return result;
        try {
            java.lang.reflect.Field byIdField = findByIdField(menu.getClass());
            if (byIdField == null) return result;
            byIdField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Long, Object> byId = (java.util.Map<Long, Object>) byIdField.get(menu);
            if (byId != null) {
                result.addAll(byId.keySet());
            }
        } catch (Throwable ignored) {
        }
        return result;
    }

    /**
     * 基于编码终端菜单的 AE Grid 遍历，列出“可在终端中可见且有空位”的供应器容器。
     * 返回顺序稳定：按 grid 的 machineClasses 顺序，再按 activeMachines 迭代顺序。
     */
    public static List<PatternContainer> listAvailableProvidersFromGrid(PatternEncodingTermMenu menu) {
        List<PatternContainer> list = new ArrayList<>();
        if (menu == null) return list;
        try {
            IGrid grid = null;
            Object target = menu.getTarget();
            if (target instanceof IActionHost host && host.getActionableNode() != null) {
                grid = host.getActionableNode().getGrid();
            }
            if (grid == null) return list;
            for (var machineClass : grid.getMachineClasses()) {
                if (PatternContainer.class.isAssignableFrom(machineClass)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends PatternContainer> containerClass = (Class<? extends PatternContainer>) machineClass;
                    for (var container : grid.getActiveMachines(containerClass)) {
                        if (container == null || !container.isVisibleInTerminal()) continue;
                        InternalInventory inv = container.getTerminalPatternInventory();
                        if (inv == null || inv.size() <= 0) continue;
                        boolean hasEmpty = false;
                        for (int i = 0; i < inv.size(); i++) {
                            if (inv.getStackInSlot(i).isEmpty()) {
                                hasEmpty = true;
                                break;
                            }
                        }
                        if (hasEmpty) list.add(container);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return list;
    }

    /** 计算供应器空槽位数量 */
    public static int getAvailableSlots(PatternContainer container) {
        if (container == null) return -1;
        InternalInventory inv = container.getTerminalPatternInventory();
        if (inv == null) return -1;
        int available = 0;
        for (int i = 0; i < inv.size(); i++) {
            if (inv.getStackInSlot(i).isEmpty()) available++;
        }
        return available;
    }

    /**
     * 基于“索引”的定向上传：使用 listAvailableProvidersFromGrid(menu) 的顺序，
     * 将编码槽样板插入到第 index 个供应器。
     */
    public static void uploadFromEncodingMenuToProviderByIndex(ServerPlayer player, PatternEncodingTermMenu menu, int index) {
        if (player == null || menu == null || index < 0) return;
        List<PatternContainer> list = listAvailableProvidersFromGrid(menu);
        if (index >= list.size()) return;
        var container = list.get(index);
        if (container == null) return;

        var encodedSlot = ((PatternEncodingTermMenuAccessor) menu)
                .eap$getEncodedPatternSlot();
        ItemStack stack = encodedSlot.getItem();
        if (stack.isEmpty() || !PatternDetailsHelper.isEncodedPattern(stack)) {
            return;
        }

        // 以名称为键，同名供应器依次尝试：先 index 指定的，再同名的其他
        String targetName = getProviderDisplayName(container);
        java.util.List<PatternContainer> tryList = new java.util.ArrayList<>();
        tryList.add(container);
        try {
            for (PatternContainer c : list) {
                if (c == null || c == container) continue;
                String name = getProviderDisplayName(c);
                if (name.equals(targetName)) {
                    tryList.add(c);
                }
            }
        } catch (Throwable ignored) {}

        for (PatternContainer c : tryList) {
            InternalInventory inv = c.getTerminalPatternInventory();
            if (inv == null || inv.size() <= 0) continue;
            var filtered = new FilteredInternalInventory(inv, new ExtendedAEPatternFilter());
            ItemStack toInsert = stack.copy();
            ItemStack remain = filtered.addItems(toInsert);
            if (remain.getCount() < toInsert.getCount()) {
                int inserted = toInsert.getCount() - remain.getCount();
                stack.shrink(inserted);
                if (stack.isEmpty()) {
                    encodedSlot.set(ItemStack.EMPTY);
                } else {
                    encodedSlot.set(stack);
                }
                return;
            }
        }
    }

    /**
     * 判断给定矩阵集群中是否存在"装配矩阵上传核心"。
     * 要求：至少存在 1 个即可，不限制数量。
     * 传入任意属于该集群的 Tile（如 Pattern/Crafter/Frame 等）。
     */
    private static boolean clusterHasSingleUploadCore(com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixBase any) {
        if (!EAEPConfig.NEEDS_UPLOADING_CORE.getAsBoolean()) return true;
        try {
            if (any == null || any.getCluster() == null) return false;
            int cores = 0;
            var it = any.getCluster().getBlockEntities();
            while (it.hasNext()) {
                var te = it.next();
                if (te instanceof UploadCoreBlockEntity) {
                    cores++;
                }
            }
            return cores >= 1; // 至少一个即可
        } catch (Throwable t) {
            return false;
        }
    }
}
