package me.codexadrian.spirit.compat.jei.categories;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import me.codexadrian.spirit.Spirit;
import me.codexadrian.spirit.compat.jei.multiblock.SoulEngulfingRecipeWrapper;
import me.codexadrian.spirit.recipe.SoulEngulfingRecipe;
import me.codexadrian.spirit.registry.SpiritItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class SoulEngulfingCategory extends BaseCategory<SoulEngulfingRecipe> {

    public static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(Spirit.MODID, "textures/gui/soul_engulfing.png");
    public static final ResourceLocation ID = new ResourceLocation(Spirit.MODID, "soul_engulfing");
    public static final RecipeType<SoulEngulfingRecipe> RECIPE = new RecipeType<>(ID, SoulEngulfingRecipe.class);
    private static final double OFFSET = Math.sqrt(512) * .5;
    private int scale = 10;
    public long lastTime;
    private final BlockRenderDispatcher dispatcher;
    private SoulEngulfingRecipeWrapper wrapper;

    public SoulEngulfingCategory(IGuiHelper guiHelper) {
        super(guiHelper,
                RECIPE,
                Component.translatable("spirit.jei.soul_engulfing.title"),
                guiHelper.drawableBuilder(GUI_BACKGROUND, 0, 0, 150, 100).build(),
                guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, SpiritItems.SOUL_CRYSTAL.get().getDefaultInstance()));

        lastTime = System.currentTimeMillis();
        dispatcher = Minecraft.getInstance().getBlockRenderer();

    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, SoulEngulfingRecipe recipe, @NotNull IFocusGroup focuses) {
        List<ItemStack> items = new ArrayList<>();
        var blocks = recipe.input().multiblock().keys().values();
        var holderSets = blocks.stream().flatMap(predicate -> {
            if (predicate.blocks().isPresent()) return predicate.blocks().get().stream();
            return Stream.of();
        }).toList();
        for (Holder<Block> holderSet : holderSets) {
            items.add(holderSet.value().asItem().getDefaultInstance());
        }
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addIngredients(Ingredient.of(items.stream()));
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 2).addIngredients(recipe.input().item());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 133, 83).addIngredient(VanillaTypes.ITEM_STACK, recipe.getResultItem());
    }

    @Override
    public List<Component> getTooltipStrings(SoulEngulfingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        updateWrapper(recipe);

        List<Component> components = new ArrayList<>();
        var tempBlockMap = new ArrayList<>(wrapper.blockMap);
        Collections.reverse(tempBlockMap);
        if (mouseX > 1 && mouseX < 105 && mouseY > 27 && mouseY < 99) {
            for (int i = 0; i < tempBlockMap.size(); i++) {
                components.add(Component.translatable("spirit.jei.soul_engulfing.layer", (i + 1)).withStyle(ChatFormatting.DARK_GRAY));
                for (SoulEngulfingRecipeWrapper.BlockMap blockMap : tempBlockMap.get(i)) {
                    components.add(Component.literal("  ").append(blockMap.blocks().getCurrent().getName()).withStyle(ChatFormatting.GRAY));
                }
            }
            if (recipe.breaksBlocks())
                components.add(Component.translatable("spirit.jei.soul_engulfing.consumes").withStyle(ChatFormatting.RED));
        }
        if (mouseX > 107 && mouseX < 129 && mouseY > 83 && mouseY < 98) {
            components.add(Component.translatable("spirit.jei.soul_engulfing.duration", recipe.duration() * 0.05));
        }
        return components;
    }

    @Override
    public void draw(SoulEngulfingRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        updateWrapper(recipe);

        long l = System.currentTimeMillis();

        if (lastTime + 1500 <= l && !Screen.hasShiftDown()) {
            wrapper.tick();
            lastTime = l;
        }
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        Matrix4f pose = stack.last().pose();
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
        pose.store(floatBuffer);
        Vec3 translation = new Vec3(floatBuffer.get(12) * guiScale, floatBuffer.get(13) * guiScale, floatBuffer.get(14) * guiScale);
        RenderSystem.enableScissor((int) (translation.x + 2 * guiScale), (int) (Minecraft.getInstance().getWindow().getHeight() - 27 * guiScale - translation.y - 73 * guiScale), (int) (103 * guiScale), (int) (73 * guiScale));
        stack.pushPose();
        Lighting.setupForFlatItems();
        float scaled = 1.6F * scale;
        double width = wrapper.getMultiblock().pattern().get(0).size() * OFFSET * (scaled/16f);
        double height = wrapper.blockMap.size() * 16 + (66 - wrapper.blockMap.size() * OFFSET);
        stack.translate(52 - width, height, 100);
        stack.scale(scaled, -scaled, 1);
        stack.mulPose(Vector3f.XP.rotationDegrees(45));
        stack.mulPose(Vector3f.YP.rotationDegrees(45));
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        for (int i = 0; i < Math.min(wrapper.blockMap.size(), wrapper.layer); i++) {
            for (SoulEngulfingRecipeWrapper.BlockMap blockMap : wrapper.blockMap.get(i)) {
                stack.pushPose();
                stack.translate(blockMap.pos().getX(), blockMap.pos().getY(), blockMap.pos().getZ());
                dispatcher.renderSingleBlock(blockMap.blocks().getCurrent().defaultBlockState(), stack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                stack.popPose();
            }
        }
        bufferSource.endBatch();
        stack.popPose();
        RenderSystem.disableScissor();
    }

    @Override
    public boolean handleInput(SoulEngulfingRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
        updateWrapper(recipe);

        if (input.getValue() == InputConstants.KEY_UP) {
            wrapper.layer = Math.min(wrapper.layer + 1, wrapper.blockMap.size());
            return true;
        }
        if (input.getValue() == InputConstants.KEY_DOWN) {
            wrapper.layer = Math.max(wrapper.layer - 1, 0);
            return true;
        }
        if(input.getValue() == InputConstants.KEY_MINUS) {
            scale = Math.max(scale - 1, 1);
            return true;
        }
        if(input.getValue() == InputConstants.KEY_EQUALS) {
            scale = Math.min(scale + 1, 20);
            return true;
        }
        return false;
    }

    private void updateWrapper(SoulEngulfingRecipe recipe) {
        if (this.wrapper == null || recipe != this.wrapper.getRecipe()) {
            this.wrapper = new SoulEngulfingRecipeWrapper(recipe);
        }
    }
}
