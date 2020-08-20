package net.Segovo.TrajectoryFabric;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import com.mojang.brigadier.arguments.BoolArgumentType;
import java.awt.*;
import java.io.File;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static io.github.cottonmc.clientcommands.ArgumentBuilders.argument;
import static io.github.cottonmc.clientcommands.ArgumentBuilders.literal;

public class TrajectoryCommands implements ClientCommandPlugin {

    FileConfig config = getConfigReference();

    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> dispatcher) {
        dispatcher.register(literal("trajectory")
                .then(literal("lineColor")
                        .then(argument("red", IntegerArgumentType.integer(0, 255))
                                .then(argument("green", IntegerArgumentType.integer(0, 255))
                                        .then(argument("blue", IntegerArgumentType.integer(0, 255))
                                                .then(argument("alpha", IntegerArgumentType.integer(0, 100)).executes(context -> {
                                                            config.set("lineColorR", getInteger(context, "red"));
                                                            config.set("lineColorG", getInteger(context, "green"));
                                                            config.set("lineColorB", getInteger(context, "blue"));
                                                            config.set("lineColorA", getInteger(context, "alpha"));
                                                            config.save();
                                                            TrajectoryFabric.remoteLoadConfig();
                                                            sendPrivateMessage(new TranslatableText("lineColor.set", describeColor(getInteger(context, "red"), getInteger(context, "green"), getInteger(context, "blue"))));
                                                            return 1;
                                                        })

                                                )))))
                .then(literal("arrowTrajectory")
                        .then(literal("true").executes(context -> {
                            config.set("arrowTrajectory", true);
                            config.save();
                            TrajectoryFabric.remoteLoadConfig();
                            sendPrivateMessage(new TranslatableText("arrowTrajectory.true"));
                            return 1;
                        }))
                        .then(literal("false").executes(context -> {
                            config.set("arrowTrajectory", false);
                            config.save();
                            TrajectoryFabric.remoteLoadConfig();
                            sendPrivateMessage(new TranslatableText("arrowTrajectory.false"));
                            return 1;
                        }))
                )
                .then(literal("componentVisibility")
                        .then(literal("line")
                                .then(literal("true").executes(context -> {
                                    config.set("lineVisibility", true);
                                    config.save();
                                    TrajectoryFabric.remoteLoadConfig();
                                    sendPrivateMessage(new TranslatableText("lineVisibility.true"));
                                    return 1;
                                }))
                                .then(literal("false").executes(context -> {
                                    config.set("lineVisibility", false);
                                    config.save();
                                    TrajectoryFabric.remoteLoadConfig();
                                    sendPrivateMessage(new TranslatableText("lineVisibility.false"));
                                    return 1;
                                }))
                        )
                        .then(literal("box")
                                .then(literal("true").executes(context -> {
                                    config.set("boxVisibility", true);
                                    config.save();
                                    TrajectoryFabric.remoteLoadConfig();
                                    sendPrivateMessage(new TranslatableText("boxVisibility.true"));
                                    return 1;
                                }))
                                .then(literal("false").executes(context -> {
                                    config.set("boxVisibility", false);
                                    config.save();
                                    TrajectoryFabric.remoteLoadConfig();
                                    sendPrivateMessage(new TranslatableText("boxVisibility.false"));
                                    return 1;
                                }))
                        )
                        .then(literal("approxBox")
                                .then(literal("true").executes(context -> {
                                    config.set("approxBoxVisibility", true);
                                    config.save();
                                    TrajectoryFabric.remoteLoadConfig();
                                    sendPrivateMessage(new TranslatableText("approxBoxVisibility.true"));
                                    return 1;
                                }))
                                .then(literal("false").executes(context -> {
                                    config.set("approxBoxVisibility", false);
                                    config.save();
                                    TrajectoryFabric.remoteLoadConfig();
                                    sendPrivateMessage(new TranslatableText("approxBoxVisibility.false"));
                                    return 1;
                                }))
                        )
                )
        );
    }

    // https://github.com/AMereBagatelle/AFKPeace/blob/1.16.x/src/main/java/amerebagatelle/github/io/afkpeace/commands/ConfigCommand.java
    public void sendPrivateMessage(Text message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.inGameHud.addChatMessage(MessageType.SYSTEM, message, mc.player.getUuid());
    }

    public FileConfig getConfigReference() {
        return TrajectoryFabric.getConfigRef();
    }

    public String describeColor(int r, int g, int b) {
        double lastSmallestDistance = Double.MAX_VALUE;
        String returnColor = "null";
        for (int i = 0, color_i = 0; color_i < colorNames.length; i=i+3, color_i++) {
            //float rgbDistance = Math.abs(r - colorRGBs[i]) +
                       // Math.abs(g - colorRGBs[i+1]) +
                        //Math.abs(b - colorRGBs[i+2]);
            double rgbDistance = Math.pow((r-colorRGBs[i]), 2)
                    + Math.pow((g-colorRGBs[i+1]), 2)
                    + Math.pow((b-colorRGBs[i+2]), 2);
            if (rgbDistance < lastSmallestDistance) {
                lastSmallestDistance = rgbDistance;
                returnColor = colorNames[color_i];
              }
        }
        return returnColor;
    }

    String[] colorNames = {
            "maroon",
            "dark red",
            "brown",
            "firebrick",
            "crimson",
            "red",
            "tomato",
            "coral reaf",
            "indian red",
            "light coral",
            "dark salmon",
            "salmon fish",
            "light salmon",
            "orange red",
            "dark orange",
            "orange",
            "gold bar",
            "dark golden rod",
            "golden rod",
            "pale golden rod",
            "dark khaki pants",
            "khaki",
            "olive",
            "mellow yellow",
            "mellow yellow green",
            "dark olive green",
            "olive drab",
            "lawn green",
            "chart reuse",
            "green yellow",
            "dark green",
            "green",
            "forest green",
            "lime",
            "lime green",
            "light green",
            "pale green",
            "dark sea green",
            "medium spring green",
            "spring green",
            "sea green",
            "medium aqua marine",
            "medium sea green",
            "light sea green",
            "dark slate gray",
            "teal",
            "dark cyan cup",
            "ocean",
            "cyan cup",
            "light cyan",
            "dark turquoise",
            "turquoise",
            "medium turquoise",
            "pale turquoise",
            "aqua marine",
            "powder blue",
            "cadet blue",
            "steel blue",
            "corn flower blue",
            "deep sky blue",
            "dodger blue",
            "calm sky color",
            "sky blue",
            "light sky blue",
            "midnight blue",
            "navy",
            "dark blue",
            "medium blue",
            "blue",
            "royal blue",
            "blue violet",
            "indigo",
            "dark slate blue",
            "slate blue",
            "medium slate blue",
            "medium purple plum",
            "dark magenta",
            "dark violet",
            "dark orchid",
            "medium orchid",
            "fresh purple plum",
            "thistle",
            "plum",
            "violet",
            "magenta",
            "orchid",
            "medium violet red",
            "pale violet red",
            "deep pink",
            "hot pink",
            "light pink",
            "pink",
            "antique white",
            "beige.",
            "bisque",
            "blanched almond",
            "wheat",
            "corn silk",
            "lemon chiffon",
            "light golden rod yellow",
            "light yellow",
            "saddle brown",
            "sienna",
            "chocolate bar",
            "peru",
            "sandy brown",
            "burly wood",
            "boring tan",
            "rosy brown",
            "moccasin",
            "navajo white",
            "peach puff",
            "misty rose",
            "lavender blush",
            "linen",
            "old lace",
            "papaya whip",
            "sea shell",
            "mint cream",
            "slate gray",
            "light slate gray",
            "light steel blue",
            "lavender",
            "floral white",
            "alice blue",
            "ghost white",
            "honeydew, yuck",
            "ivory",
            "azure",
            "snow",
            "black",
            "dim gray",
            "gray",
            "dark gray",
            "silver from the mines",
            "light gray",
            "gainsboro",
            "white smoke",
            "white"
    };

  int[] colorRGBs = {
        128,0,0,
        139,0,0,
        165,42,42,
        178,34,34,
        220,20,60,
        255,0,0,
        255,99,71,
        255,127,80,
        205,92,92,
        240,128,128,
        233,150,122,
        250,128,114,
        255,160,122,
        255,69,0,
        255,140,0,
        255,165,0,
        255,215,0,
        184,134,11,
        218,165,32,
        238,232,170,
        189,183,107,
        240,230,140,
        128,128,0,
        255,255,0,
        154,205,50,
        85,107,47,
        107,142,35,
        124,252,0,
        127,255,0,
        173,255,47,
        0,100,0,
        0,128,0,
        34,139,34,
        0,255,0,
        50,205,50,
        144,238,144,
        152,251,152,
        143,188,143,
        0,250,154,
        0,255,127,
        46,139,87,
        102,205,170,
        60,179,113,
        32,178,170,
        47,79,79,
        0,128,128,
        0,139,139,
        0,255,255,
        0,255,255,
        224,255,255,
        0,206,209,
        64,224,208,
        72,209,204,
        175,238,238,
        127,255,212,
        176,224,230,
        95,158,160,
        70,130,180,
        100,149,237,
        0,191,255,
        30,144,255,
        173,216,230,
        135,206,235,
        135,206,250,
        25,25,112,
        0,0,128,
        0,0,139,
        0,0,205,
        0,0,255,
        65,105,225,
        138,43,226,
        75,0,130,
        72,61,139,
        106,90,205,
        123,104,238,
        147,112,219,
        139,0,139,
        148,0,211,
        153,50,204,
        186,85,211,
        128,0,128,
        216,191,216,
        221,160,221,
        238,130,238,
        255,0,255,
        218,112,214,
        199,21,133,
        219,112,147,
        255,20,147,
        255,105,180,
        255,182,193,
        255,192,203,
        250,235,215,
        245,245,220,
        255,228,196,
        255,235,205,
        245,222,179,
        255,248,220,
        255,250,205,
        250,250,210,
        255,255,224,
        139,69,19,
        160,82,45,
        210,105,30,
        205,133,63,
        244,164,96,
        222,184,135,
        210,180,140,
        188,143,143,
        255,228,181,
        255,222,173,
        255,218,185,
        255,228,225,
        255,240,245,
        250,240,230,
        253,245,230,
        255,239,213,
        255,245,238,
        245,255,250,
        112,128,144,
        119,136,153,
        176,196,222,
        230,230,250,
        255,250,240,
        240,248,255,
        248,248,255,
        240,255,240,
        255,255,240,
        240,255,255,
        255,250,250,
        0,0,0,
        105,105,105,
        128,128,128,
        169,169,169,
        192,192,192,
        211,211,211,
        220,220,220,
        245,245,245,
        255,255,255
  };


}
