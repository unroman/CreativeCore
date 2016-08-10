package com.creativemd.creativecore.client.rendering.model;

import java.util.Arrays;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.RenderCubeObject;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class CreativeBakedQuad extends BakedQuad {
	
	public static CreativeBakedQuad lastRenderedQuad = null;
	public final RenderCubeObject cube;
	
	public CreativeBakedQuad(BakedQuad quad, RenderCubeObject cube, int tintedColor, boolean shouldOverrideColor)
    {
        super(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length), shouldOverrideColor ? tintedColor : quad.getTintIndex(), FaceBakery.getFacingFromVertexData(quad.getVertexData()), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
        this.cube = cube;
    }
	
	@Override
    public void pipe(net.minecraftforge.client.model.pipeline.IVertexConsumer consumer)
    {
		lastRenderedQuad = this;
        net.minecraftforge.client.model.pipeline.LightUtil.putBakedQuad(consumer, this);
        lastRenderedQuad = null;
    }
	
}