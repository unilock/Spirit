package me.codexadrian.spirit.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipe;
import me.codexadrian.spirit.registry.SpiritMisc;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public record MobTraitData(ResourceLocation id, EntityType<?> entity, List<MobTrait<?>> traits) implements CodecRecipe<Container> {

    public static Codec<MobTraitData> codec(ResourceLocation id) {
        return RecordCodecBuilder.create(instance -> instance.group(
                RecordCodecBuilder.point(id),
                Registry.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(MobTraitData::entity),
                MobTraitRegistry.CODEC.listOf().fieldOf("traits").forGetter(MobTraitData::traits)
        ).apply(instance, MobTraitData::new));
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ResourceLocation getId() {
        return id();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SpiritMisc.MOB_TRAIT_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return SpiritMisc.MOB_TRAIT.get();
    }

    public static Optional<MobTraitData> getEffectForEntity(EntityType<?> entityType, RecipeManager manager) {
        return manager.getAllRecipesFor(SpiritMisc.MOB_TRAIT.get()).stream().filter(recipe -> recipe.entity.equals(entityType)).findFirst();
    }

    @SuppressWarnings("ConstantConditions")
    public static Optional<MobTraitData> getEffect(String id, RecipeManager manager) {
        return (Optional<MobTraitData>) manager.byKey(ResourceLocation.tryParse(id));
    }
}
