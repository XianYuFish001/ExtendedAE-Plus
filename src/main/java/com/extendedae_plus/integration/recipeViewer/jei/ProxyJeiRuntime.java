package com.extendedae_plus.integration.recipeViewer.jei;

import com.extendedae_plus.mixin.core.recipeViewer.jei.accessor.AccessorBookmarkOverlay;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.gui.bookmarks.BookmarkList;
import mezz.jei.gui.bookmarks.IngredientBookmark;
import mezz.jei.gui.overlay.elements.IElement;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 线程安全地缓存并访问 JEI Runtime。
 */
public final class ProxyJeiRuntime {
    private static volatile IJeiRuntime RUNTIME;

    private ProxyJeiRuntime() {}

    static void setRuntime(IJeiRuntime runtime) {
        RUNTIME = runtime;
    }

    @Nullable
    public static IJeiRuntime get() {
        return RUNTIME;
    }

    public static Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
        IJeiRuntime rt = RUNTIME;
        if (rt == null) return Optional.empty();

        IIngredientListOverlay list = rt.getIngredientListOverlay();
        if (list != null) {
            var ing = list.getIngredientUnderMouse();
            if (ing.isPresent()) return ing;
        }
        IBookmarkOverlay bm = rt.getBookmarkOverlay();
        if (bm != null) {
            var ing = bm.getIngredientUnderMouse();
            if (ing.isPresent()) return ing;
        }
        return Optional.empty();
    }

    /**
     * 检测 JEI 是否开启了作弊模式（给物品）。
     * 使用 JEI 内部开关，若 JEI 未初始化或异常则返回 false。
     */
    public static boolean isJeiCheatModeEnabled() {
        try {
            // 使用完全限定名以避免在源码缺失时的编译依赖问题
            return mezz.jei.common.Internal.getClientToggleState().isCheatItemsEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 将文本写入 JEI 的搜索过滤框。
     * 若 JEI runtime 不可用则静默返回。
     */
    public static void setIngredientFilterText(String text) {
        IJeiRuntime rt = RUNTIME;
        if (rt == null) return;
        try {
            rt.getIngredientFilter().setFilterText(text == null ? "" : text);
        } catch (Throwable ignored) {
            // 兼容不同 JEI 版本或在启动阶段尚未就绪
        }
    }

    /**
     * 获取JEI书签列表
     */
    public static List<? extends ITypedIngredient<?>> getBookmarkList() {
        IJeiRuntime rt = RUNTIME;
        if (rt == null) return Collections.emptyList();
        IBookmarkOverlay bookmarkOverlay = rt.getBookmarkOverlay();
        if (bookmarkOverlay instanceof AccessorBookmarkOverlay accessor) {
            BookmarkList bookmarkList = accessor.getBookmarkList();
            return bookmarkList.getElements().stream().map(IElement::getTypedIngredient).toList();
        }
        return Collections.emptyList();
    }

    public static <TStack> void addFavorite(TStack stack, IIngredientType<TStack> type) {
        var runtime = RUNTIME;
        if (runtime == null) return;
        if (!(runtime.getBookmarkOverlay() instanceof AccessorBookmarkOverlay accessor)) return;

        runtime.getIngredientManager().createTypedIngredient(type, stack)
                .ifPresent(ingredient -> accessor.getBookmarkList().add(
                        IngredientBookmark.create(ingredient, runtime.getIngredientManager())));
    }
}