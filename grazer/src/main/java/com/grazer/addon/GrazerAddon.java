package com.grazer.addon;

import com.grazer.addon.gui.MenuKeyHandler;
import com.grazer.addon.modules.AnchorEsp;
import com.grazer.addon.modules.StorageEsp;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class GrazerAddon extends MeteorAddon {
    public static final Category CATEGORY = new Category("Grazer");

    @Override
    public void onInitialize() {
        Modules.get().add(new AnchorEsp());
        Modules.get().add(new StorageEsp());

        // Delete key opens / closes the Grazer menu.
        MeteorClient.EVENT_BUS.subscribe(new MenuKeyHandler());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.grazer.addon";
    }
}
