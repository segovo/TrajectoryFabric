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

		Optional<Integer> version = config.getOptional("version");
		if(!version.isPresent()) {
			System.out.println("No Config!");
			config.set("version", 1);
			config.set("lineColorR", 255);
			config.set("lineColorG", 255);
			config.set("lineColorB", 255);
			config.set("lineColorA", 100);
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

	public static void renderCurve(Camera camera, World world, BlockPos pos, float pitch, float yaw, double eye, PlayerEntity player, int[] color) {
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

		float speed = 1.5f;
		//float divergence = 1.0f;
		float ngravity = 0.03f;
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
	//	double prevZ = accurateZ;

		SnowballEntity tempEntity = new SnowballEntity(world, player);

		for (int i=0; i < 100; i++) {
			HitResult hitResult = world.rayTrace(new RayTraceContext(new Vec3d(accurateX + entityPosition.x, accurateY + entityPosition.y, accurateZ + entityPosition.z), new Vec3d(accurateX + entityPosition.x, accurateY + entityPosition.y, accurateZ + entityPosition.z).add(entityVelocity), RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, tempEntity));
			if (hitResult.getType() != HitResult.Type.MISS) {
				double hitDistance = hitResult.getPos().distanceTo(player.getPos());
				double boxSize = hitDistance > 20 ? hitDistance/40 : 0.5;
				double defaultBoxSize = 0.5; // 0.5 = full block
				renderBox(hitResult.getPos().x - defaultBoxSize - d0, hitResult.getPos().y - defaultBoxSize - d1, hitResult.getPos().z - defaultBoxSize - d2, hitResult.getPos().x + defaultBoxSize - d0, hitResult.getPos().y + defaultBoxSize - d1, hitResult.getPos().z + defaultBoxSize - d2);
				renderBox(hitResult.getPos().x - boxSize - d0, hitResult.getPos().y - boxSize - d1, hitResult.getPos().z - boxSize - d2, hitResult.getPos().x + boxSize - d0, hitResult.getPos().y + boxSize - d1, hitResult.getPos().z + boxSize - d2);
				angleToHit = Math.acos(1 / hitDistance);
				break;
			}


			GL11.glVertex3d(prevX + offsetX + (accurateX-d0), entityPosition.y + (accurateY - d1), prevZ + offsetZ + (accurateZ - d2));

			entityPosition = entityPosition.add(entityVelocity);
			entityVelocity = entityVelocity.multiply(drag);
			entityVelocity = new Vec3d(entityVelocity.x, entityVelocity.y - ngravity, entityVelocity.z);
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
			float pitch = playerEntity.pitch;
			float yaw = playerEntity.yaw;
			double eye = playerEntity.getEyeY();
			ItemStack itemStack = playerEntity.getMainHandStack();

			if (items.contains(itemStack.getItem())) {
				int[] color = {config.get("lineColorR"), config.get("lineColorG"), config.get("lineColorB"), config.get("lineColorA")};
				TrajectoryFabric.renderCurve(camera, world, blockPos, pitch, yaw, eye, playerEntity, color);
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
//double angleToHit = Math.atan2(projectileHit.getZ() - (pZ + offsetZ), projectileHit.getX() - (pX + offsetX));
			/*double newX = entityPosition.x * Math.cos(Math.toRadians(angle+20)) + zdistance * Math.sin(Math.toRadians(angle+20));
			double newZ = entityPosition.x * Math.sin(Math.toRadians(angle+20)) + zdistance * Math.cos(Math.toRadians(angle+20));
			GL11.glVertex3d(.01 + newX + offsetX, .01 + entityPosition.y, .01 + newZ + offsetZ);

			entityPosition = entityPosition.add(entityVelocity);

			entityVelocity = entityVelocity.multiply(drag);
			entityVelocity = new Vec3d(entityVelocity.x, entityVelocity.y - ngravity, entityVelocity.z);

			newX = entityPosition.x * Math.cos(Math.toRadians(angle+20)) + zdistance * Math.sin(Math.toRadians(angle+20));
			newZ = entityPosition.x * Math.sin(Math.toRadians(angle+20)) + zdistance * Math.cos(Math.toRadians(angle+20));
			GL11.glVertex3d(.01 + newX + offsetX, .01 + entityPosition.y, .01 + newZ + offsetZ);
			*/
				/*
		// formula
		RenderSystem.color4f(color[0] / 255f, color[1] / 255f, color[2] / 255f, (float)color[3]/100);
		if (projectileHit == null) {
			return;
		} else {
		//	double offsetX = 0 * Math.cos(Math.toRadians(angle * -1)) + 1 * Math.sin(Math.toRadians(angle * -1));
		//	double offsetZ = 0 * Math.sin(Math.toRadians(angle * -1)) + 1 * Math.cos(Math.toRadians(angle * -1));
			//double angleToHit = Math.atan2(projectileHit.getZ() - (pZ + offsetZ), projectileHit.getX() - (pX + offsetX));
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

		*/

///Hit detection
//tempEntity.tick();

//if (i % 100 == 0) {
//	tempEntity.remove();
//}

//HitResult hitResult = ProjectileUtil.getCollision(tempEntity, (ThrownItemEntity) -> ThrownItemEntity instanceof EnderPearlEntity, RayTraceContext.ShapeType.OUTLINE);
//if (hitResult.getType() != HitResult.Type.MISS) {
//renderBox(hitResult.getPos().x - 0.5 - d0, hitResult.getPos().y - 0.5 - d1, hitResult.getPos().z - 0.5 - d2, hitResult.getPos().x + 0.5 - d0, hitResult.getPos().y + 0.5 - d1, hitResult.getPos().z + 0.5 - d2);
//angleToHit = Math.acos((1 / hitResult.getPos().distanceTo(player.getPos())));
//tempEntity.remove();
//break;
//}

//Render and position update
//GL11.glVertex3d(prevX + offsetX + (accurateX-d0), entityPosition.y - accurateY + (accurateY - d1), prevZ + offsetZ + (accurateZ - d2));

//entityPosition = entityPosition.add(entityVelocity);
//entityVelocity = entityVelocity.multiply(drag);
//entityVelocity = new Vec3d(entityVelocity.x, entityVelocity.y - ngravity, entityVelocity.z);
//double newX = entityPosition.x * Math.cos(angleToHit-1.5708) - entityPosition.z * Math.sin(angleToHit-1.5708); // Rotation to point trajectory curve towards hit mark.
//double newZ = entityPosition.x * Math.sin(angleToHit-1.5708) + entityPosition.z * Math.cos(angleToHit-1.5708); //
//GL11.glVertex3d((prevX - accurateX) + offsetX + (accurateX-d0), (prevY - accurateY) + (accurateY - d1), (prevZ - accurateZ) + offsetZ + (accurateZ - d2));
//double newX = tempEntity.getX() * Math.cos(angleToHit-1.5708) - tempEntity.getZ() * Math.sin(angleToHit-1.5708); // Rotation to point trajectory curve towards hit mark.
//double newZ = tempEntity.getX()  * Math.sin(angleToHit-1.5708) + tempEntity.getZ() * Math.cos(angleToHit-1.5708); //
//GL11.glVertex3d(newX + offsetX + (accurateX-d0), entityPosition.y - accurateY + (accurateY - d1), newZ + offsetZ + (accurateZ - d2));
//GL11.glVertex3d((newX - accurateX) + offsetX + (accurateX-d0), (tempEntity.getY() - accurateY) + (accurateY - d1), (newZ - accurateZ) + offsetZ + (accurateZ - d2));

//prevX = newX;
//prevY = tempEntity.getY();
//prevZ = newZ;
//EnderPearlEntity tempEntity = new EnderPearlEntity(world, player);
//tempEntity.setPos(accurateX + entityPosition.x, accurateY + entityPosition.y, accurateZ + entityPosition.z);
//tempEntity.setPos(accurateX, accurateY + 1.5, accurateZ);
//tempEntity.setProperties(player, player.pitch, player.yaw, 0.0f, 1.5f, 0);
//tempEntity.setInvisible(true);
//tempEntity.setInvulnerable(true);

//Box boundingBox = new Box(0, 0, 0, 0, 0, 0);
//tempEntity.setBoundingBox(boundingBox);
//tempEntity.setVelocity(entityVelocity);
// Find enderpearl hit location
		/*
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
		*/
//double angle = yaw+90;
//double gravity = 0.180;
//double velocity = 1.5;
//double xdistance = 0;
//double ydistance = 1; // + xdistance * Math.tan(Math.toRadians(pitch*-1)) - Math.pow((gravity * xdistance), 2)/2*Math.pow(velocity*Math.cos(Math.toRadians(pitch*-1)), 2);
//double zdistance = 0;
//double prevY = 0 + xdistance * Math.tan(Math.toRadians(pitch*-1)) - (Math.pow((gravity * xdistance), 2))/(2*Math.pow(velocity*Math.cos(Math.toRadians(pitch*-1)), 2));

///boolean trigger = false;
//int extrasteps = 0;
//BlockPos projectileHit = null;