package net.Segovo.TrajectoryFabric;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

//Code snippets from https://github.com/shedaniel/LightOverlay/blob/1.16/src/main/java/me/shedaniel/lightoverlay/fabric/LightOverlay.java
public class TrajectoryFabric implements ClientModInitializer {

	final boolean smoothLines = true;
	static File configFile = new File(FabricLoader.getInstance().getConfigDirectory() + "/trajectoryconfig.json");
	static FileConfig config = FileConfig.of(configFile);

	public static FileConfig getConfigRef() {
		return config;
	}

	public static void remoteLoadConfig() {
		config.load();
	}

	public boolean checkConfig() {
		config.load();

		Optional<Integer> version = config.getOptional("version");
		if(!version.isPresent()) {
			System.out.println("No Config!");
			config.set("version", 1);
			config.set("lineColorR", 255);
			config.set("lineColorG", 255);
			config.set("lineColorB", 255);
			config.set("lineColorA", 100);
			config.set("lineRes", 8);
			config.save();
			config.load();
			return false;
		} else {
			System.out.println("Config Found!");
			return true;
		}

	}

	public static void renderCurve(Camera camera, World world, BlockPos pos, float pitch, float yaw, double eye, PlayerEntity player, int[] color, double lineRes) {
		double d0 = camera.getPos().x;
		double d1 = camera.getPos().y - .005D;
		double d2 = camera.getPos().z;
		int pX = pos.getX();
		int pY = pos.getY();
		int pZ = pos.getZ();

		double angle = yaw+90;
		double gravity = 0.180;
		double velocity = 1.5;
		double xdistance = 0;
		double ydistance = 1; // + xdistance * Math.tan(Math.toRadians(pitch*-1)) - Math.pow((gravity * xdistance), 2)/2*Math.pow(velocity*Math.cos(Math.toRadians(pitch*-1)), 2);
		double zdistance = 0;
		double prevX = 0;
		double prevY = 0 + xdistance * Math.tan(Math.toRadians(pitch*-1)) - (Math.pow((gravity * xdistance), 2))/(2*Math.pow(velocity*Math.cos(Math.toRadians(pitch*-1)), 2));
		double prevZ = 0;
		boolean trigger = false;
		int extrasteps = 0;
		BlockPos projectileHit = null;

		RenderSystem.color4f(color[0] / 255f, color[1] / 255f, color[2] / 255f, (float)color[3]/100);

		// Find enderpearl hit location
		for (int i = 1; i < (100); i++) {
			xdistance = i;

			double straightX = xdistance * Math.cos(Math.toRadians(angle)) + zdistance * Math.sin(Math.toRadians(angle));
			double straightZ = xdistance * Math.sin(Math.toRadians(angle)) + zdistance * Math.cos(Math.toRadians(angle));
			ydistance = 0 + xdistance * Math.tan(Math.toRadians(pitch * -1)) - (Math.pow((gravity * xdistance), 2)) / (2 * Math.pow(velocity * Math.cos(Math.toRadians(pitch * -1)), 2));

			BlockPos futureBlock = new BlockPos(.01 + straightX + pX, .01 + ydistance + pY + 1, .01 + straightZ + pZ);
			BlockState futureBlockState = world.getBlockState(futureBlock);

			if (futureBlockState.isFullCube(world, futureBlock) & trigger == false) {
				GL11.glVertex3d(futureBlock.getX() - d0, futureBlock.getY() + 1.1 - d1, futureBlock.getZ() + 1 - d2);
				GL11.glVertex3d(futureBlock.getX() + 1 - d0, futureBlock.getY() + 1.1 - d1, futureBlock.getZ() - d2);
				GL11.glVertex3d(futureBlock.getX() + 1 - d0, futureBlock.getY() + 1.1 - d1, futureBlock.getZ() + 1 - d2);
				GL11.glVertex3d(futureBlock.getX() - d0, futureBlock.getY() + 1.1 - d1, futureBlock.getZ() - d2);
				projectileHit = futureBlock;
				break;
			}
		}

		if (projectileHit == null) {
			return;
		} else {
			double offsetX = 0 * Math.cos(Math.toRadians(angle * -1)) + 1 * Math.sin(Math.toRadians(angle * -1));
			double offsetZ = 0 * Math.sin(Math.toRadians(angle * -1)) + 1 * Math.cos(Math.toRadians(angle * -1));
			double angleToHit = Math.atan2(projectileHit.getZ() - (pZ + offsetZ), projectileHit.getX() - (pX + offsetX));
			//Draw line to location
			for (int i = 1; i < (lineRes * 25); i++) {
				xdistance = Double.valueOf(i) / (Double.valueOf(lineRes) / 4.0D);


				double straightX = xdistance * Math.cos(Math.toRadians(angle)) + zdistance * Math.sin(Math.toRadians(angle));
				double straightZ = xdistance * Math.sin(Math.toRadians(angle)) + zdistance * Math.cos(Math.toRadians(angle));
				double newX = xdistance * Math.cos(angleToHit) + zdistance * Math.sin(angleToHit);
				double newZ = xdistance * Math.sin(angleToHit) + zdistance * Math.cos(angleToHit);

				ydistance = 0 + xdistance * Math.tan(Math.toRadians(pitch * -1)) - (Math.pow((gravity * xdistance), 2)) / (2 * Math.pow(velocity * Math.cos(Math.toRadians(pitch * -1)), 2));

				GL11.glVertex3d(.01 + prevX + offsetX, .01 + prevY, .01 + prevZ + offsetZ);
				GL11.glVertex3d(.01 + newX + offsetX, .01 + ydistance, .01 + newZ + offsetZ);

				prevX = newX;
				prevY = ydistance;
				prevZ = newZ;
			}
		}
	}

	@Override
	public void onInitializeClient() {

		//Night Config
		System.out.println(FabricLoader.getInstance().getConfigDirectory());
		checkConfig();
		config.load();

		// Rendering
		Set<Item> items = new HashSet<Item>();
		items.add(Items.ENDER_PEARL.asItem());
		items.add(Items.SNOWBALL.asItem());
		//items.add(Items.SPLASH_POTION.asItem());
		items.add(Items.EGG.asItem());

		MinecraftClient client = MinecraftClient.getInstance();
		ClothClientHooks.DEBUG_RENDER_PRE.register(() -> {
			RenderSystem.enableDepthTest();
			RenderSystem.disableTexture();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
			if (smoothLines) GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glLineWidth(2.0f);
			GL11.glBegin(GL11.GL_LINES);

			PlayerEntity playerEntity = client.player;
			World world = client.world;
			BlockPos blockPos = new BlockPos(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ());
			Camera camera = client.gameRenderer.getCamera();
			float pitch = playerEntity.getPitch(MinecraftClient.getInstance().getTickDelta());
			float yaw = playerEntity.getYaw(MinecraftClient.getInstance().getTickDelta());
			double eye = playerEntity.getEyeY();
			ItemStack itemStack = playerEntity.getMainHandStack();

			if (items.contains(itemStack.getItem())) {
				int lineRes = config.get("lineRes");
				int[] color = {config.get("lineColorR"), config.get("lineColorG"), config.get("lineColorB"), config.get("lineColorA")};
				TrajectoryFabric.renderCurve(camera, world, blockPos, pitch, yaw, eye, playerEntity, color, lineRes);
			}


			GL11.glEnd();
			RenderSystem.disableBlend();
			RenderSystem.enableTexture();
			if (smoothLines) GL11.glDisable(GL11.GL_LINE_SMOOTH);
		});

		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
		});

		System.out.println("Hello from TrajectoryFabric!");
	}



}
