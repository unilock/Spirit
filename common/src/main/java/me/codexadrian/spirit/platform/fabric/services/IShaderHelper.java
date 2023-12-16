package me.codexadrian.spirit.platform.fabric.services;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface IShaderHelper {

    void setSoulShader(ShaderInstance shader);

    <T extends Entity> RenderType getSoulShader(T entity, ResourceLocation texture);
}
