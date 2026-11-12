package com.grazer.addon.gui;

import com.grazer.addon.modules.AnchorEsp;
import com.grazer.addon.modules.StorageEsp;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Grazer menu. Immediate-mode UI styled like the reference screenshot:
 * pink accents, circle checkboxes, value/max sliders, dropdowns, top tabs, two columns.
 * Right-click a section title to open that module's full Meteor settings.
 */
public class GrazerScreen extends Screen {
    // ---- Theme (ARGB) ----
    private static final int OVERLAY    = 0x80000000;
    private static final int WINDOW_BG  = 0xFF101014;
    private static final int BORDER     = 0xFF2C2C34;
    private static final int SECTION_BG = 0xFF17171C;
    private static final int TRACK      = 0xFF2A2A30;
    private static final int PINK       = 0xFFFFB6D9;
    private static final int PINK_DIM   = 0xFFB07890;
    private static final int TEXT       = 0xFFE8E8EC;
    private static final int TEXT_DIM   = 0xFF8A8A94;

    private static final int W = 400, H = 340, PAD = 8;
    private static final String[] TABS = {"ESP", "About"};

    private final Module anchor, storage;
    private int tab = 0;

    // input state
    private int pendingClick = -1;
    private boolean mouseDown;
    private String dragId;

    // per-frame
    private DrawContext ctx;
    private int mx, my;

    public GrazerScreen() {
        super(Text.literal("Grazer"));
        Modules m = Modules.get();
        anchor = m.get(AnchorEsp.class);
        storage = m.get(StorageEsp.class);
    }

    // ------------------------------------------------------------ frame

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ctx = context; mx = mouseX; my = mouseY;
        ctx.fill(0, 0, width, height, OVERLAY);

        int x0 = (width - W) / 2, y0 = (height - H) / 2;
        ctx.fill(x0 - 1, y0 - 1, x0 + W + 1, y0 + H + 1, BORDER);
        ctx.fill(x0, y0, x0 + W, y0 + H, WINDOW_BG);

        // Title bar
        ctx.drawTextWithShadow(textRenderer, "Grazer - <3", x0 + PAD, y0 + 7, PINK);
        ctx.fill(x0, y0 + 20, x0 + W, y0 + 21, BORDER);

        // Tabs
        int tx = x0 + PAD;
        for (int i = 0; i < TABS.length; i++) {
            int tw = textRenderer.getWidth(TABS[i]);
            boolean active = tab == i;
            if (pendingClick == 0 && hit(tx - 3, y0 + 24, tw + 6, 12)) { tab = i; pendingClick = -1; }
            ctx.drawTextWithShadow(textRenderer, TABS[i], tx, y0 + 26, active ? PINK : TEXT_DIM);
            if (active) ctx.fill(tx, y0 + 36, tx + tw, y0 + 37, PINK);
            tx += tw + 14;
        }

        if (tab == 0) espTab(x0, y0 + 42); else aboutTab(x0, y0 + 42);

        pendingClick = -1;
    }

    private void espTab(int x0, int top) {
        int colW = (W - PAD * 3) / 2;
        int secH = (height - H) / 2 + H - PAD - top;
        int lx = x0 + PAD, rx = x0 + PAD * 2 + colW;

        // Left: Anchor ESP
        int y = section(lx, top, colW, secH, "Anchor ESP", anchor);
        int ix = lx + PAD, iw = colW - PAD * 2;
        y = check(ix, y, "Enabled", anchor.isActive(), anchor::toggle);
        y = boolSetting(ix, y, anchor, "tracers", "Tracers");
        y = label(ix, y, "Range");
        y = slider(ix, y, iw, anchor);
        y = label(ix, y, "Shape Mode");
        y = dropdown(ix, y, iw, anchor);
        y = swatch(ix, y, iw, anchor, "uncharged-color", "Uncharged");
        swatch(ix, y, iw, anchor, "charged-color", "Charged");

        // Right: Storage ESP
        y = section(rx, top, colW, secH, "Storage ESP", storage);
        ix = rx + PAD;
        y = check(ix, y, "Enabled", storage.isActive(), storage::toggle);
        y = boolSetting(ix, y, storage, "tracers", "Tracers");
        y = label(ix, y, "Range");
        y = slider(ix, y, iw, storage);
        y = label(ix, y, "Shape Mode");
        y = dropdown(ix, y, iw, storage);
        y = label(ix, y + 2, "Types");
        y = boolSetting(ix, y, storage, "chests", "Chests");
        y = boolSetting(ix, y, storage, "trapped-chests", "Trapped Chests");
        y = boolSetting(ix, y, storage, "barrels", "Barrels");
        y = boolSetting(ix, y, storage, "ender-chests", "Ender Chests");
        boolSetting(ix, y, storage, "shulker-boxes", "Shulker Boxes");
    }

    private void aboutTab(int x0, int top) {
        int x = x0 + PAD;
        int y = section(x, top, W - PAD * 2, 90, "Grazer", null);
        y = label(x + PAD, y, "Anchor ESP: Respawn Anchors through walls");
        y = label(x + PAD, y, "Storage ESP: chests, barrels, shulkers");
        y = label(x + PAD, y, "Delete: open / close this menu");
        label(x + PAD, y, "Right-click a section title for all settings");
    }

    // ------------------------------------------------------------ widgets

    private boolean hit(int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int section(int x, int y, int w, int h, String title, Module m) {
        ctx.fill(x, y, x + w, y + h, BORDER);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, SECTION_BG);
        ctx.drawTextWithShadow(textRenderer, title, x + PAD, y + 7, TEXT);
        ctx.fill(x + PAD, y + 19, x + w - PAD, y + 20, BORDER);
        if (m != null && pendingClick == 1 && hit(x, y, w, 20)) {
            pendingClick = -1;
            client.setScreen(GuiThemes.get().moduleSettings(m));
        }
        return y + 27;
    }

    private int label(int x, int y, String text) {
        ctx.drawTextWithShadow(textRenderer, text, x, y, TEXT_DIM);
        return y + 12;
    }

    private void circle(int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.sqrt(r * r - dy * dy);
            ctx.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, color);
        }
    }

    private int check(int x, int y, String text, boolean on, Runnable toggle) {
        int w = 20 + textRenderer.getWidth(text);
        if (pendingClick == 0 && hit(x, y - 1, w + 4, 14)) { toggle.run(); pendingClick = -1; }
        circle(x + 5, y + 5, 5, BORDER);
        circle(x + 5, y + 5, 4, on ? PINK : TRACK);
        ctx.drawTextWithShadow(textRenderer, text, x + 16, y + 1, on ? TEXT : TEXT_DIM);
        return y + 16;
    }

    @SuppressWarnings("unchecked")
    private int boolSetting(int x, int y, Module m, String name, String text) {
        Setting<Boolean> s = (Setting<Boolean>) m.settings.get(name);
        return check(x, y, text, s.get(), () -> s.set(!s.get()));
    }

    @SuppressWarnings("unchecked")
    private int slider(int x, int y, int w, Module m) {
        Setting<Integer> s = (Setting<Integer>) m.settings.get("range");
        int min = 8, max = 128, h = 14;
        String id = m.name + ":range";

        if (pendingClick == 0 && hit(x, y, w, h)) { dragId = id; pendingClick = -1; }
        if (id.equals(dragId) && mouseDown) {
            float f = Math.max(0f, Math.min(1f, (mx - x) / (float) w));
            s.set(min + Math.round(f * (max - min)));
        }

        ctx.fill(x, y, x + w, y + h, TRACK);
        int fill = Math.round((s.get() - min) / (float) (max - min) * w);
        ctx.fill(x, y, x + fill, y + h, PINK);
        String t = s.get() + "/" + max;
        ctx.drawTextWithShadow(textRenderer, t, x + (w - textRenderer.getWidth(t)) / 2, y + 3, 0xFFFFFFFF);
        return y + h + 6;
    }

    @SuppressWarnings("unchecked")
    private int dropdown(int x, int y, int w, Module m) {
        Setting<ShapeMode> s = (Setting<ShapeMode>) m.settings.get("shape-mode");
        int h = 14;
        if (pendingClick == 0 && hit(x, y, w, h)) {
            ShapeMode[] v = ShapeMode.values();
            s.set(v[(s.get().ordinal() + 1) % v.length]);
            pendingClick = -1;
        }
        ctx.fill(x, y, x + w, y + h, hit(x, y, w, h) ? 0xFF34343C : TRACK);
        ctx.drawTextWithShadow(textRenderer, s.get().name(), x + 5, y + 3, TEXT);
        ctx.drawTextWithShadow(textRenderer, "v", x + w - 10, y + 3, TEXT_DIM);
        return y + h + 6;
    }

    @SuppressWarnings("unchecked")
    private int swatch(int x, int y, int w, Module m, String name, String text) {
        Setting<SettingColor> s = (Setting<SettingColor>) m.settings.get(name);
        SettingColor c = s.get();
        ctx.drawTextWithShadow(textRenderer, text, x, y + 1, TEXT_DIM);
        int sx = x + w - 16;
        ctx.fill(sx - 1, y - 1, sx + 17, y + 11, BORDER);
        ctx.fill(sx, y, sx + 16, y + 10, 0xFF000000 | (c.r << 16) | (c.g << 8) | c.b);
        return y + 16;
    }

    // ------------------------------------------------------------ input (1.21.9+ signatures)

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        pendingClick = click.button();
        mouseDown = click.button() == 0;
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        mouseDown = false;
        dragId = null;
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_DELETE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
