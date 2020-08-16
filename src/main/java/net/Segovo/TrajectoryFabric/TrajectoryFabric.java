package net.Segovo.TrajectoryFabric;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
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
	static double angleToHit = 0;

	public static FileConfig getConfigRef() {
		return config;
	}

	public static void remoteLoadConfig() {
		config.load();
	}

	public boolean checkConfig() {
		config.load();

		//Bow update
		Optional<Integer> trajectory = config.getOptional("arrowTrajectory");
		if(!trajectory.isPresent()) {
			System.out.println("arrowTrajectory missing from Config!");
			config.set("arrowTrajectory", true);
			config.save();
			config.load();
		} else {

		}

		Optional<Integer> version = config.getOptional("version");
		if(!version.isPresent()) {
			System.out.println("No Config!");
			config.set("version", 1);
			config.set("lineColorR", 255);
			config.set("lineColorG", 255);
			config.set("lineColorB", 255);
			config.set("lineColorA", 100);
			config.set("arrowTrajectory", true);
			config.save();
			config.load();
			return false;
		} else {
			System.out.println("Config Found!");
			return true;
		}



	}

	public static void setVelocity() {

	}

	public int[] getConfigColor() {
		return new int[] {config.get("lineColorR"), config.get("lineColorG"), config.get("lineColorB"), config.get("lineColorA")};
	}

	public static void renderBox(double x1, double y1, double z1, double x2, double y2, double z2) {
		GL11.glVertex3d(x1, y1, z1);
		GL11.glVertex3d(x1, y2, z1);

		GL11.glVertex3d(x2, y1, z1);
		GL11.glVertex3d(x2, y2, z1);

		GL11.glVertex3d(x1, y1, z2);
		GL11.glVertex3d(x1, y2, z2);

		GL11.glVertex3d(x2, y1, z2);
		GL11.glVertex3d(x2, y2, z2);
		//
		GL11.glVertex3d(x1, y1, z1);
		GL11.glVertex3d(x2, y1, z1);

		GL11.glVertex3d(x1, y2, z1);
		GL11.glVertex3d(x2, y2, z1);

		GL11.glVertex3d(x1, y1, z2);
		GL11.glVertex3d(x2, y1, z2);

		GL11.glVertex3d(x1, y2, z2);
		GL11.glVertex3d(x2, y2, z2);
		//
		GL11.glVertex3d(x1, y1, z1);
		GL11.glVertex3d(x1, y1, z2);

		GL11.glVertex3d(x2, y1, z1);
		GL11.glVertex3d(x2, y1, z2);

		GL11.glVertex3d(x1, y2, z1);
		GL11.glVertex3d(x1, y2, z2);

		GL11.glVertex3d(x2, y2, z1);
		GL11.glVertex3d(x2, y2, z2);;

	}

	public static void renderCurve(Camera camera, World world, BlockPos pos, float pitch, float yaw, double eye, PlayerEntity player, int[] color, float speed, float gravity) {
		double d0 = camera.getPos().x;
		double d1 = camera.getPos().y - .005D;
		double d2 = camera.getPos().z;
		int pX = pos.getX();
		int pY = pos.getY();
		int pZ = pos.getZ();
		double accurateX = player.getX();
		double accurateY = player.getY();
		double accurateZ = player.getZ();

		// Simulation
		RenderSystem.color4f(color[0] / 255f, color[1] / 255f, color[2] / 255f, (float)color[3]/100);

		//float ngravity = 0.03f;
		float drag = 0.99f;

		float entityVelX = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		float entityVelY = -MathHelper.sin(pitch * 0.017453292F);
		float entityVelZ = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
		Vec3d vec3d = (new Vec3d(entityVelX, entityVelY, entityVelZ)).normalize().multiply(speed);
		Vec3d playerVelocity = player.getVelocity();
		vec3d = vec3d.add(playerVelocity.x, player.isOnGround() ? 0.0D : playerVelocity.y, playerVelocity.z);
		Vec3d entityVelocity = new Vec3d(vec3d.x, vec3d.y, vec3d.z);
		Vec3d entityPosition = new Vec3d(0, 0 + 1.5, 0);

		double offsetX = 0 * Math.cos(Math.toRadians((yaw+90)*-1)) + 1 * Math.sin(Math.toRadians((yaw+90)*-1));
		double offsetZ = 0 * Math.sin(Math.toRadians((yaw+90)*-1)) + 1 * Math.cos(Math.toRadians((yaw+90)*-1));
		double prevX = 0;
		double prevZ = 0;

		SnowballEntity tempEntity = new SnowballEntity(world, player);

		for (int i=0; i < 100; i++) {
			HitResult hitResult = world.rayTrace(new RayTraceContext(new Vec3d(accurateX + entityPosition.x, accurateY + entityPosition.y, accurateZ + entityPosition.z), new Vec3d(accurateX + entityPosition.x, accurateY + entityPosition.y, accurateZ + entityPosition.z).add(entityVelocity), RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, tempEntity));
			if (hitResult.getType() != HitResult.Type.MISS) {
				double hitDistance = hitResult.getPos().distanceTo(player.getPos());
				double boxSize = hitDistance > 30 ? hitDistance/70 : 0.5;
				double defaultBoxSize = 0.5; // 0.5 = full block
				renderBox(hitResult.getPos().x - defaultBoxSize - d0, hitResult.getPos().y - defaultBoxSize - d1, hitResult.getPos().z - defaultBoxSize - d2, hitResult.getPos().x + defaultBoxSize - d0, hitResult.getPos().y + defaultBoxSize - d1, hitResult.getPos().z + defaultBoxSize - d2);
				renderBox(hitResult.getPos().x - boxSize - d0, hitResult.getPos().y - boxSize - d1, hitResult.getPos().z - boxSize - d2, hitResult.getPos().x + boxSize - d0, hitResult.getPos().y + boxSize - d1, hitResult.getPos().z + boxSize - d2);
				angleToHit = Math.acos(1 / hitDistance);
				break;
			}


			GL11.glVertex3d(prevX + offsetX + (accurateX-d0), entityPosition.y + (accurateY - d1), prevZ + offsetZ + (accurateZ - d2));

			entityPosition = entityPosition.add(entityVelocity);
			entityVelocity = entityVelocity.multiply(drag);
			entityVelocity = new Vec3d(entityVelocity.x, entityVelocity.y - gravity, entityVelocity.z);
			double newX = entityPosition.x * Math.cos(angleToHit-1.5708) - entityPosition.z * Math.sin(angleToHit-1.5708); // Rotation to point trajectory curve towards hit mark.
			double newZ = entityPosition.x * Math.sin(angleToHit-1.5708) + entityPosition.z * Math.cos(angleToHit-1.5708); //

			GL11.glVertex3d(newX + offsetX + (accurateX-d0), entityPosition.y + (accurateY - d1), newZ + offsetZ + (accurateZ - d2));
			prevX = newX;
			prevZ = newZ;
		}

	}

	@Override
	public void onInitializeClient() {

		//Night Config
		System.out.println(FabricLoader.getInstance().getConfigDirectory());
		checkConfig();
		config.load();

		// Rendering
		Set<Item> itemsSimple = new HashSet<Item>();
		itemsSimple.add(Items.ENDER_PEARL.asItem());
		itemsSimple.add(Items.SNOWBALL.asItem());
		//items.add(Items.SPLASH_POTION.asItem());
		itemsSimple.add(Items.EGG.asItem());

		Set<Item> itemsComplex = new HashSet<Item>();
		itemsComplex.add(Items.BOW.asItem());

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
			float pitch = playerEntity.pitch;
			float yaw = playerEntity.yaw;
			double eye = playerEntity.getEyeY();
			ItemStack itemStack = playerEntity.getMainHandStack();

			if (itemsSimple.contains(itemStack.getItem())) {
				float speed = 1.5f;
				int[] color = getConfigColor();
				TrajectoryFabric.renderCurve(camera, world, blockPos, pitch, yaw, eye, playerEntity, color, speed, 0.03f);
			} else if (itemsComplex.contains(itemStack.getItem()) && (boolean)config.get("arrowTrajectory")) {
				float bowMultiplier = (72000.0f - playerEntity.getItemUseTimeLeft()) / 20.0f;
				bowMultiplier = (bowMultiplier * bowMultiplier + bowMultiplier * 2.0f) / 3.0f;
				if (bowMultiplier > 1.0f) {
					bowMultiplier = 1.0f;
				}

				float speed = bowMultiplier * 3.0f;
				int[] color = getConfigColor();
				TrajectoryFabric.renderCurve(camera, world, blockPos, pitch, yaw, eye, playerEntity, color, speed, 0.05f);
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
