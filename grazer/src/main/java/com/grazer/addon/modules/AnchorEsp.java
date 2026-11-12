package com.grazer.addon.modules;

import com.grazer.addon.GrazerAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class AnchorEsp extends Module {
    private record Hit(BlockPos pos, int charges) {}

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Horizontal and vertical scan radius in blocks.")
        .defaultValue(64).min(8).sliderRange(8, 128)
        .build());

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draw a line from the crosshair to each anchor.")
        .defaultValue(true)
        .build());

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .defaultValue(ShapeMode.Both)
        .build());

    private final Setting<SettingColor> emptyColor = sgRender.add(new ColorSetting.Builder()
        .name("uncharged-color")
        .description("Color of anchors with 0 charges.")
        .defaultValue(new SettingColor(255, 70, 70, 255))
        .build());

    private final Setting<SettingColor> chargedColor = sgRender.add(new ColorSetting.Builder()
        .name("charged-color")
        .description("Color of anchors with at least 1 charge.")
        .defaultValue(new SettingColor(180, 90, 255, 255))
        .build());

    private final List<Hit> found = new ArrayList<>();
    private final List<Hit> next = new ArrayList<>();
    private final Color side = new Color();

    public AnchorEsp() {
        super(GrazerAddon.CATEGORY, "anchor-esp", "Shows Respawn Anchors through walls.");
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
            if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
                next.add(new Hit(pos.toImmutable(), state.get(RespawnAnchorBlock.CHARGES)));
            }
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
            Color line = hit.charges() > 0 ? chargedColor.get() : emptyColor.get();
            side.set(line.r, line.g, line.b, 40);
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
