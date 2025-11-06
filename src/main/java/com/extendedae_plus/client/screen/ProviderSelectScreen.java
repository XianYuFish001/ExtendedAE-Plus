package com.extendedae_plus.client.screen;

import com.extendedae_plus.network.UploadEncodedPatternToProviderC2SPacket;
import com.extendedae_plus.util.PatternAliasing;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单的供应器选择弹窗。
 * 展示若干个可点击的供应器条目，点击后发送带 providerId 的上传请求。
 */
public class ProviderSelectScreen extends Screen {
    private final Screen parent;
    // 原始数据
    private final List<Long> ids;
    private final List<String> names;
    private final List<String> i18nKeys;
    private final List<Integer> emptySlots;

    // 分组后的数据（同名合并）
    private final List<Long> gIds = new ArrayList<>();            // 代表条目使用的 providerId：选择空位数最多的那个
    private final List<String> gNames = new ArrayList<>();        // 分组名（供应器名称）
    private final List<String> gI18nKeys = new ArrayList<>();     // 分组名（供应器Key）
    private final List<Integer> gTotalSlots = new ArrayList<>();  // 该名称下供应器空位总和
    private final List<Integer> gCount = new ArrayList<>();       // 该名称下供应器数量

    // 过滤后的数据（由查询生成）
    private final List<Long> fIds = new ArrayList<>();
    private final List<String> fNames = new ArrayList<>();
    private final List<Integer> fTotalSlots = new ArrayList<>();
    private final List<Integer> fCount = new ArrayList<>();

    // 搜索框
    private EditBox searchBox;
    // 中文名输入框（用于添加映射）
    private EditBox aliasInput;
    private PatternAliasing.KeywordGroup selectedQuery = PatternAliasing.KeywordGroup.EMPTY;
    private List<PatternAliasing.KeywordGroup> query = new ArrayList<>();
    private boolean needsRefresh = false;

    private int page = 0;
    private static final int PAGE_SIZE = 6;

    private final List<Button> entryButtons = new ArrayList<>();

    public ProviderSelectScreen(Screen parent, List<Long> ids, List<String> names, List<String> i18nKeys, List<Integer> emptySlots) {
        super(Component.translatable("extendedae_plus.screen.choose_provider.title"));
        this.parent = parent;
        this.ids = ids;
        this.names = names;
        this.i18nKeys = i18nKeys;
        this.emptySlots = emptySlots;
        // 如果有来自 JEI 的最近处理名称，则作为初始查询
        try {
            var recent = PatternAliasing.recipeKeywords;
            if (recent != null && !recent.isEmpty()) {
                this.query = new ArrayList<>(recent);
                this.selectedQuery = query.getFirst();

                PatternAliasing.recipeKeywords.clear();
            }
        } catch (Throwable ignored) {
        }
        buildGroups();
        applyFilter();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        entryButtons.clear();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 70;

        // 搜索框（置于条目上方）
        if (searchBox == null) {
            searchBox = new EditBox(this.font, centerX - 120, startY - 25, 240, 18, Component.translatable("extendedae_plus.screen.search"));
        } else {
            // 重新定位，保持输入值
            searchBox.setX(centerX - 120);
            searchBox.setY(startY - 25);
            searchBox.setWidth(240);
        }
        searchBox.setValue(selectedQuery.getDescription().getString());

        // 为多个候选的情况添加Tooltip指示
        if (query.size() > 1) {
            MutableComponent candidateQuery = Component.literal("候选关键词");
            query.forEach(q -> {
                if (q.equals(selectedQuery)) candidateQuery
                        .append(Component.literal("\n→ ").withStyle(ChatFormatting.GREEN))
                        .append(q.getDescription());
                else candidateQuery.append("\n").append(q.getDescription().copy().withStyle(ChatFormatting.GRAY));
            });
            searchBox.setTooltip(Tooltip.create(candidateQuery));
        }

        searchBox.setResponder(text -> {
            // 只有当输入真正发生变化时，才重置页码与过滤
            if (text.equals(selectedQuery.getDescription().getString())) return;
            selectedQuery = new PatternAliasing.KeywordGroup(text);
            // 切换候选词不触发, 手动输入时重置query
            query.clear();
            page = 0;
            searchBox.setTooltip(Tooltip.create(Component.empty()));
            applyFilter();
            // 避免在回调中直接重建 UI，延迟到下一次 tick
            needsRefresh = true;
        });
        this.addRenderableWidget(searchBox);

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, fIds.size());

        int buttonWidth = 240;
        int buttonHeight = 20;
        int gap = 5;

        for (int i = start; i < end; i++) {
            int idx = i;
            String label = buildLabel(idx);
            Button btn = Button.builder(Component.literal(label), b -> onChoose(idx))
                    .bounds(centerX - buttonWidth / 2, startY + (i - start) * (buttonHeight + gap), buttonWidth, buttonHeight)
                    .build();
            entryButtons.add(btn);
            this.addRenderableWidget(btn);
        }

        // 分页按钮
        int navY = startY + PAGE_SIZE * (buttonHeight + gap) + 10;
        Button prev = Button.builder(Component.literal("<"), b -> changePage(-1))
                .bounds(centerX - 60, navY, 20, 20)
                .build();
        Button next = Button.builder(Component.literal(">"), b -> changePage(1))
                .bounds(centerX + 40, navY, 20, 20)
                .build();
        prev.active = page > 0;
        next.active = (page + 1) * PAGE_SIZE < fIds.size();
        this.addRenderableWidget(prev);
        this.addRenderableWidget(next);

        // 重载映射按钮（热重载 recipe_type_names.json）——移至下一行，与关闭按钮并排
        Button reload = Button.builder(Component.translatable("extendedae_plus.screen.reload_mapping"), b -> reloadMapping())
                .bounds(centerX - 130, navY + 30, 80, 20)
                .build();
        this.addRenderableWidget(reload);

        // 中文名输入框（用于新增映射的值）
        if (aliasInput == null) {
            aliasInput = new EditBox(this.font, centerX + 50, navY + 30, 120, 20, Component.translatable("extendedae_plus.screen.cn_name"));
        } else {
            aliasInput.setX(centerX + 50);
            aliasInput.setY(navY + 30);
            aliasInput.setWidth(120);
        }
        this.addRenderableWidget(aliasInput);

        // 增加映射按钮（使用当前搜索关键字 -> 中文）
        Button addMap = Button.builder(Component.translatable("extendedae_plus.screen.add_mapping"), b -> addMappingFromUI())
                .bounds(centerX + 175, navY + 30, 60, 20)
                .build();
        this.addRenderableWidget(addMap);

        // 删除映射（按中文值精确匹配删除）按钮
        Button delByCn = Button.builder(Component.literal("删除映射"), b -> deleteMappingByCnFromUI())
                .bounds(centerX + 240, navY + 30, 60, 20)
                .build();
        this.addRenderableWidget(delByCn);

        // 关闭按钮
        Button close = Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(centerX - 40, navY + 30, 80, 20)
                .build();
        this.addRenderableWidget(close);
    }

    private void changePage(int delta) {
        int newPage = page + delta;
        if (newPage < 0) return;
        if (newPage * PAGE_SIZE >= fIds.size()) return;
        page = newPage;
        // 避免在回调中直接重建 UI，改为下帧刷新
        needsRefresh = true;
    }

    private void reloadMapping() {
        try {
            PatternAliasing.loadAliases();
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.sendSystemMessage(Component.literal("ExtendedAE Plus: 已重载映射表"));
            }
            // 重载后不强制刷新筛选，但如需立即应用到名称匹配，可手动编辑搜索框或翻页
        } catch (Throwable t) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.sendSystemMessage(Component.literal("ExtendedAE Plus: 重载映射表失败: " + t.getClass().getSimpleName()));
            }
        }
    }

    private String buildLabel(int idx) {
        String name = fNames.get(idx);
        int totalSlots = fTotalSlots.get(idx);
        int count = fCount.get(idx);
        // 不显示具体 id，显示合并统计：名称（总空位）x数量
        return name + "  (" + totalSlots + ")  x" + count;
    }

    private void onChoose(int idx) {
        if (idx < 0 || idx >= fIds.size()) return;
        long providerId = fIds.get(idx);
        var conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            conn.send(new UploadEncodedPatternToProviderC2SPacket(providerId));
        }
        this.onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void buildGroups() {
        // 使用 LinkedHashMap 保持首次出现顺序
        Map<String, Group> nameMap = new LinkedHashMap<>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String i18nKey = i18nKeys.get(i);
            long id = ids.get(i);
            int slots = emptySlots.get(i);
            Group g = nameMap.computeIfAbsent(name, k -> new Group());
            g.count++;
            g.totalSlots += Math.max(0, slots);
            // 挑选空位最多的作为代表 id；若并列，保留先到者
            if (slots > g.bestSlots) {
                g.bestSlots = slots;
                g.bestId = id;
            }
            if (!i18nKey.isBlank()) g.i18nKey = i18nKey;
        }
        for (Map.Entry<String, Group> e : nameMap.entrySet()) {
            String name = e.getKey();
            Group g = e.getValue();
            gNames.add(name);
            gI18nKeys.add(g.i18nKey);
            gIds.add(g.bestId);
            gTotalSlots.add(g.totalSlots);
            gCount.add(g.count);
        }
    }

    private static class Group {
        long bestId = Long.MIN_VALUE;
        int bestSlots = Integer.MIN_VALUE;
        int totalSlots = 0;
        int count = 0;
        String i18nKey = "";
    }

    private void applyFilter() {
        fIds.clear();
        fNames.clear();
        fTotalSlots.clear();
        fCount.clear();
        for (int i = 0; i < gIds.size(); i++) {
            String name = gNames.get(i);
            String i18nKey = gI18nKeys.get(i);
            if (selectedQuery.match(name, i18nKey)) {
                fIds.add(gIds.get(i));
                fNames.add(name);
                fTotalSlots.add(gTotalSlots.get(i));
                fCount.add(gCount.get(i));
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox != null && searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 右键点击搜索框区域时，清空搜索框内容并刷新
        if (button == 1 && this.searchBox != null) {
            if (this.searchBox.isMouseOver(mouseX, mouseY) || this.aliasInput.isMouseOver(mouseX, mouseY)) {
                if (Minecraft.getInstance().player != null)
                    Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.1F, 1.0F);

                if (this.searchBox.isMouseOver(mouseX, mouseY)) {
                    if (!this.searchBox.getValue().isEmpty()) this.searchBox.setValue("");
                    this.selectedQuery = PatternAliasing.KeywordGroup.EMPTY;
                    this.query.clear();
                    this.page = 0;
                    searchBox.setTooltip(Tooltip.create(Component.empty()));
                    applyFilter();
                    this.needsRefresh = true;
                } else {
                    if (!this.aliasInput.getValue().isEmpty()) this.aliasInput.setValue("");
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (query.size() <= 1 || this.searchBox == null)
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if (Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.1F, 1.0F);

        boolean direction = scrollY > 0;
        int boxX = this.searchBox.getX();
        int boxY = this.searchBox.getY();
        int boxWidth = this.searchBox.getWidth();
        int boxHeight = this.searchBox.getHeight();

        // 滚轮切换候选词
        if (mouseX >= boxX && mouseX <= boxX + boxWidth &&
                mouseY >= boxY && mouseY <= boxY + boxHeight &&
                query.contains(selectedQuery)) {
            int index = query.indexOf(selectedQuery);

            if (direction && index == 0) selectedQuery = query.getLast();
            else if (direction) selectedQuery = query.get(index - 1);
            else if (index == query.size() - 1) selectedQuery = query.getFirst();
            else selectedQuery = query.get(index + 1);

            this.page = 0;
            applyFilter();
            this.needsRefresh = true;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void tick() {
        super.tick();
        if (needsRefresh) {
            needsRefresh = false;
            // 重新构建当前屏幕内容
            init();
        }
    }

    private void addMappingFromUI() {
        String searchKey = selectedQuery.getDescription().getString();
        String aliasToSet = aliasInput == null ? "" : aliasInput.getValue().trim();
        var player = Minecraft.getInstance().player;

        if (selectedQuery.isEmpty()) {
            if (player != null) player.displayClientMessage(Component.literal("请输入搜索关键字后再添加映射"), false);
            return;
        }
        if (aliasToSet.isEmpty()) {
            if (player != null) player.displayClientMessage(Component.literal("请输入待映射别名"), false);
            return;
        }

        if (PatternAliasing.addOrUpdateAlias(searchKey, aliasToSet)) {
            if (player != null) player.displayClientMessage(
                    Component.literal("已添加/更新映射: " + searchKey + " -> " + aliasToSet), false);

            // 将刚添加的中文名写入搜索框，作为当前查询
            this.query.remove(selectedQuery);

            var newAliasGroup = new PatternAliasing.KeywordGroup(aliasToSet);
            this.query.addFirst(newAliasGroup);
            this.selectedQuery = newAliasGroup;

            if (this.searchBox != null)
                this.searchBox.setValue(aliasToSet);
            applyFilter();
            page = 0;
            needsRefresh = true;
        } else {
            if (player != null) player.displayClientMessage(Component.literal("写入映射失败"), false);
        }
    }

    // 使用中文值精确匹配删除映射
    private void deleteMappingByCnFromUI() {
        String val = aliasInput == null ? "" : aliasInput.getValue().trim();
        var player = Minecraft.getInstance().player;
        if (val.isEmpty()) {
            if (player != null) player.sendSystemMessage(Component.literal("请输入存在别名后再删除映射"));
            return;
        }
        int removed = PatternAliasing.removeAliases(val);
        if (removed > 0) {
            if (player != null) player.sendSystemMessage(Component.literal("已删除 " + removed + " 条映射，别名: [" + val + "]"));
            applyFilter();
            needsRefresh = true;
        } else {
            if (player != null) player.sendSystemMessage(Component.literal("未找到别名为 '" + val + "' 的映射"));
        }
    }
}
