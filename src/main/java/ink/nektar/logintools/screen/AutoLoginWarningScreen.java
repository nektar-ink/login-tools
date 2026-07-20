package ink.nektar.logintools.screen;

import ink.nektar.logintools.config.LoginToolsConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class AutoLoginWarningScreen extends Screen {
    private static final int TIMER_SECONDS = 5;

    private final Screen parent;
    private final LoginToolsConfig workingCopy;
    private int ticksRemaining = TIMER_SECONDS * 20;
    private ButtonWidget confirmButton;

    public AutoLoginWarningScreen(Screen parent, LoginToolsConfig workingCopy) {
        super(Text.literal("Auto Login Settings"));
        this.parent = parent;
        this.workingCopy = workingCopy;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int panelWidth = 286;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("No, I prefer be safe."), button -> this.close())
            .dimensions(centerX - 116, this.height - 52, 232, 20).build());

        this.confirmButton = this.addDrawableChild(ButtonWidget.builder(this.confirmText(), button -> this.client.setScreen(new AutoLoginSettingsScreen(this.parent, this.workingCopy)))
            .dimensions(centerX - 116, this.height - 28, 232, 20).build());
        this.confirmButton.active = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ticksRemaining > 0) {
            this.ticksRemaining--;
            this.confirmButton.setMessage(this.confirmText());
            if (this.ticksRemaining == 0) {
                this.confirmButton.active = true;
                this.confirmButton.setMessage(this.confirmText());
            }
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int panelX = this.width / 2 - 146;
        int panelY = 42;
        int panelWidth = 292;
        int panelHeight = 122;
        int textX = panelX + 14;
        this.drawPanel(context, panelX, panelY, panelWidth, panelHeight, 0xD0140C0C, 0xFFFF5555);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Visible warning"), textX, 54, 0xFFFF8080);
        this.drawWrappedText(context, Text.literal("If someone else brought you to this screen, they may be trying to steal your saved passwords."), textX, 72, 264, 0xFFF0D0D0);
        this.drawWrappedText(context, Text.literal("Continue only if you understand that passwords are stored locally and can be used automatically."), textX, 114, 264, 0xFFFFFFFF);
    }

    private void drawPanel(DrawContext context, int x, int y, int width, int height, int fillColor, int borderColor) {
        context.fill(x, y, x + width, y + height, fillColor);
        context.fill(x, y, x + width, y + 1, borderColor);
        context.fill(x, y + height - 1, x + width, y + height, borderColor);
        context.fill(x, y, x + 1, y + height, borderColor);
        context.fill(x + width - 1, y, x + width, y + height, borderColor);
    }

    private void drawWrappedText(DrawContext context, Text text, int x, int y, int width, int color) {
        int currentY = y;
        for (OrderedText line : this.textRenderer.wrapLines(text, width)) {
            context.drawTextWithShadow(this.textRenderer, line, x, currentY, color);
            currentY += this.textRenderer.fontHeight + 2;
        }
    }

    private Text confirmText() {
        if (this.ticksRemaining <= 0) {
            return Text.literal("I know what I'm doing!");
        }

        int seconds = (this.ticksRemaining + 19) / 20;
        return Text.literal("I know what I'm doing! (" + seconds + "s)");
    }
}
