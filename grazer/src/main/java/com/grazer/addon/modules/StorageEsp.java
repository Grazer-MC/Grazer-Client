package com.grazer.addon.modules;

import com.grazer.addon.GrazerAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class StorageEsp extends Module {
    private record Hit(BlockPos pos, Color color) {}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTypes = settings.createGroup("Storage Types");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range").description("Horizontal and vertical scan radius in blocks.")
        .defaultValue(64).min(8).sliderRange(8, 128).build());

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers").description("Draw a line from the crosshair to each storage block.")
        .defaultValue(false).build());

    private final Setting<Boolean> chests = sgTypes.add(new BoolSetting.Builder().name("chests").defaultValue(true).build());
    private final Setting<Boolean> trapped = sgTypes.add(new BoolSetting.Builder().name("trapped-chests").defaultValue(true).build());
    private final Setting<Boolean> barrels = sgTypes.add(new BoolSetting.Builder().name("barrels").defaultValue(true).build());
    private final Setting<Boolean> ender = sgTypes.add(new BoolSetting.Builder().name("ender-chests").defaultValue(true).build());
    private final Setting<Boolean> shulkers = sgTypes.add(new BoolSetting.Builder().name("shulker-boxes").defaultValue(true).build());

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode").defaultValue(ShapeMode.Both).build());

    private final Setting<SettingColor> chestColor = color(sgRender, "chest-color", 255, 160, 0);
    private final Setting<SettingColor> trappedColor = color(sgRender, "trapped-chest-color", 255, 60, 60);
    private final Setting<SettingColor> barrelColor = color(sgRender, "barrel-color", 160, 110, 60);
    private final Setting<SettingColor> enderColor = color(sgRender, "ender-chest-color", 170, 60, 255);
    private final Setting<SettingColor> shulkerColor = color(sgRender, "shulker-color", 255, 120, 220);

    private final List<Hit> found = new ArrayList<>();
    private final List<Hit> next = new ArrayList<>();
    private final Color side = new Color();

    public StorageEsp() {
        super(GrazerAddon.CATEGORY, "storage-esp", "Shows chests, barrels, shulkers and other storage through walls.");
    }

    private static Setting<SettingColor> color(SettingGroup group, String name, int r, int g, int b) {
        return group.add(new ColorSetting.Builder().name(name).defaultValue(new SettingColor(r, g, b, 255)).build());
    }

    @Override
    public void onDeactivate() {
        found.clear();
        next.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        int r = range.get();
        BlockIterator.register(r, r, (pos, state) -> {
            Block b = state.getBlock();
            Color c = null;

            if (b == Blocks.CHEST) { if (chests.get()) c = chestColor.get(); }
            else if (b == Blocks.TRAPPED_CHEST) { if (trapped.get()) c = trappedColor.get(); }
            else if (b == Blocks.BARREL) { if (barrels.get()) c = barrelColor.get(); }
            else if (b == Blocks.ENDER_CHEST) { if (ender.get()) c = enderColor.get(); }
            else if (b instanceof ShulkerBoxBlock) { if (shulkers.get()) c = shulkerColor.get(); }

            if (c != null) next.add(new Hit(pos.toImmutable(), c));
        });
        BlockIterator.after(() -> {
            found.clear();
            found.addAll(next);
            next.clear();
        });
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (Hit hit : found) {
            Color line = hit.color();
            side.set(line.r, line.g, line.b, 35);
            event.renderer.box(hit.pos(), side, line, shapeMode.get(), 0);

            if (tracers.get()) {
                event.renderer.line(
                    RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                    hit.pos().getX() + 0.5, hit.pos().getY() + 0.5, hit.pos().getZ() + 0.5,
                    line);
            }
        }
    }

    @Override
    public String getInfoString() {
        return found.isEmpty() ? null : String.valueOf(found.size());
    }
}
