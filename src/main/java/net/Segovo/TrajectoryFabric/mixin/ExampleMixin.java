package net.Segovo.TrajectoryFabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


public class ExampleMixin {

}

//@Mixin(WorldRenderer.class)
//public class ExampleMixin {

//	private final MinecraftClient client = MinecraftClient.getInstance();

	//private PlayerEntity getCameraPlayer() {
	//	if (!(this.client.getCameraEntity() instanceof PlayerEntity))
	//		return null;
	//	return (PlayerEntity)this.client.getCameraEntity();
	//}

//	@Inject(at = @At("HEAD"), method = "render()V")
	//public void render(CallbackInfo info) {
		//System.out.println("Hello from VF!");
		//PlayerEntity playerEntity = getCameraPlayer();
		//System.out.print("rendering");
		/*
		RenderSystem.lineWidth(2.0f);
		RenderSystem.disableDepthTest();
		RenderSystem.disableTexture();
		RenderSystem.disableBlend();

		final Tessellator tess = Tessellator.getInstance();
		final BufferBuilder buffer = tess.getBuffer();

		buffer.begin(1, VertexFormats.POSITION_COLOR);
		//buffer.vertex(playerEntity.getX(), playerEntity.getEyeY(), playerEntity.getZ()).color(1.0f, 0.0f, 0.0f, 1.0f).next();
		buffer.vertex(5.0, 75.0, 20.0).color(1.0f, 0.0f, 0.0f, 1.0f).next();
		buffer.vertex(10.0, 75.0, 40.0).color(1.0f, 0.0f, 0.0f, 1.0f).next();

		tess.draw();
		RenderSystem.enableTexture();
		RenderSystem.lineWidth(1.0f);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		*/
//	}

//}
