package ink.nektar.logintools.screen;

import ink.nektar.logintools.LoginTools;
import ink.nektar.logintools.config.LoginToolsConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class LoginToolsConfigScreen extends Screen {
    private final Screen parent;
    private final LoginToolsConfig workingCopy;

    public LoginToolsConfigScreen(Screen parent, LoginToolsConfig workingCopy) {
        super(Text.literal("Login Tools"));
        this.parent = parent;
        this.workingCopy = workingCopy;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int rowWidth = 260;
        int settingsWidth = 34;
        int x = centerX - rowWidth / 2;

        this.addDrawableChild(ButtonWidget.builder(this.autoLoginText(), button -> {
            this.workingCopy.setAutoLoginMode(this.workingCopy.autoLoginMode().next());
            button.setMessage(this.autoLoginText());
        }).dimensions(x, 106, rowWidth - settingsWidth - 6, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("..."), button -> this.client.setScreen(new AutoLoginWarningScreen(this, this.workingCopy)))
            .dimensions(x + rowWidth - settingsWidth, 106, settingsWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(this.hidePasswordText(), button -> {
            this.workingCopy.setHidePassword(!this.workingCopy.hidePassword());
            button.setMessage(this.hidePasswordText());
        }).dimensions(x, 162, rowWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> this.saveAndClose())
            .dimensions(centerX - 102, this.height - 28, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> this.close())
            .dimensions(centerX + 2, this.height - 28, 100, 20).build());
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
        int centerX = this.width / 2;
        int panelX = centerX - 138;
        this.drawPanel(context, panelX, 48, 276, 154, 0xB0101010, 0xFF3A3A3A);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 20, 0xFFFFFF);
        this.drawWrappedText(context, Text.literal("Manages to protect your passwords."), panelX + 12, 64, 252, 0xB8B8B8);

        context.drawTextWithShadow(this.textRenderer, Text.literal("Auto Login"), panelX + 12, 92, 0xFFFFFF);
        this.drawWrappedText(context, Text.literal("Choose whether saved passwords should be sent automatically, confirmed in-world, or never used."), panelX + 12, 132, 214, 0x9F9F9F);

        context.drawTextWithShadow(this.textRenderer, Text.literal("Hide Password"), panelX + 12, 148, 0xFFFFFF);
        this.drawWrappedText(context, Text.literal("Masks the visible /login command while you type so the password does not appear on screen."), panelX + 12, 188, 252, 0x9F9F9F);
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

    private Text autoLoginText() {
        return Text.literal("Auto Login : " + this.workingCopy.autoLoginMode().displayText());
    }

    private Text hidePasswordText() {
        return Text.literal("Hide Password : " + (this.workingCopy.hidePassword() ? "Yes" : "No"));
    }

    private void saveAndClose() {
        LoginTools.setConfig(this.workingCopy.copy());
        LoginTools.saveConfig();
        this.close();
    }
}
