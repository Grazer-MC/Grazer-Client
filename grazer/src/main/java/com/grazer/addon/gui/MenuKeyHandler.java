package com.grazer.addon.gui;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/** Opens the Grazer menu when Delete is pressed in-game. Polls the key so it does not depend on Meteor's KeyEvent API. */
public class MenuKeyHandler {
    private boolean wasDown;

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null) return;

        boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_DELETE) == GLFW.GLFW_PRESS;
        if (down && !wasDown && mc.currentScreen == null && mc.world != null) {
            mc.setScreen(new GrazerScreen());
        }
        wasDown = down;
    }
}
