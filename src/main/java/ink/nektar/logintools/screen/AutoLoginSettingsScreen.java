package ink.nektar.logintools.screen;

import ink.nektar.logintools.config.LoginToolsConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class AutoLoginSettingsScreen extends Screen {
    private final Screen parent;
    private final LoginToolsConfig workingCopy;
    private final List<RowWidgets> rowWidgets = new ArrayList<>();
    private TextFieldWidget globalPasswordField;

    public AutoLoginSettingsScreen(Screen parent, LoginToolsConfig workingCopy) {
        super(Text.literal("Auto Login Settings"));
        this.parent = parent;
        this.workingCopy = workingCopy;
    }

    @Override
    protected void init() {
        this.rowWidgets.clear();
        int centerX = this.width / 2;
        int top = 118;

        this.addDrawableChild(ButtonWidget.builder(this.scopeText(), button -> {
            this.workingCopy.setPasswordScope(this.workingCopy.passwordScope().next());
            this.client.setScreen(new AutoLoginSettingsScreen(this.parent, this.workingCopy));
        }).dimensions(centerX - 110, top, 220, 20).build());

        if (this.workingCopy.passwordScope() == LoginToolsConfig.PasswordScope.GLOBAL) {
            this.globalPasswordField = this.addDrawableChild(new TextFieldWidget(this.textRenderer, centerX - 110, 162, 220, 20, Text.literal("Password for all servers")));
            this.globalPasswordField.setMaxLength(256);
            this.globalPasswordField.setText(this.workingCopy.globalPassword());
            this.globalPasswordField.setChangedListener(this.workingCopy::setGlobalPassword);
        } else {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Add server"), button -> {
                this.workingCopy.serverPasswords().add(new LoginToolsConfig.ServerPasswordEntry("", ""));
                this.client.setScreen(new AutoLoginSettingsScreen(this.parent, this.workingCopy));
            }).dimensions(centerX - 110, 154, 220, 20).build());

            int y = 206;
            for (int i = 0; i < this.workingCopy.serverPasswords().size(); i++) {
                LoginToolsConfig.ServerPasswordEntry entry = this.workingCopy.serverPasswords().get(i);
                TextFieldWidget addressField = this.addDrawableChild(new TextFieldWidget(this.textRenderer, centerX - 110, y, 106, 20, Text.literal("address")));
                addressField.setMaxLength(255);
                addressField.setText(entry.address());

                TextFieldWidget passwordField = this.addDrawableChild(new TextFieldWidget(this.textRenderer, centerX + 4, y, 76, 20, Text.literal("password")));
                passwordField.setMaxLength(255);
                passwordField.setText(entry.password());

                int index = i;
                addressField.setChangedListener(value -> this.updateEntry(index, value, passwordField.getText()));
                passwordField.setChangedListener(value -> this.updateEntry(index, addressField.getText(), value));

                ButtonWidget deleteButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), button -> {
                    this.workingCopy.serverPasswords().remove(index);
                    this.client.setScreen(new AutoLoginSettingsScreen(this.parent, this.workingCopy));
                }).dimensions(centerX + 88, y, 60, 20).build());

                this.rowWidgets.add(new RowWidgets(addressField, passwordField, deleteButton));
                y += 24;
            }
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> this.close())
            .dimensions(centerX - 100, this.height - 28, 200, 20).build());
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
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, 20, 0xFFFFFF);
        this.drawPanel(context, panelX, 42, 276, 58, 0xD0140C0C, 0xFFFF5555);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Password safety warning"), panelX + 12, 52, 0xFFFF8080);
        this.drawWrappedText(context, Text.literal("Saved passwords stay visible in the local config file. Anyone with access to this computer can read them."), panelX + 12, 68, 252, 0xFFFFFFFF);

        this.drawPanel(context, panelX, 108, 276, this.workingCopy.passwordScope() == LoginToolsConfig.PasswordScope.GLOBAL ? 86 : 64, 0xB0101010, 0xFF3A3A3A);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Storage mode"), panelX + 12, 118, 0xFFFFFF);
        this.drawWrappedText(context, Text.literal("Choose one password for every server or save individual passwords for selected addresses."), panelX + 12, 134, 252, 0xA8A8A8);

        if (this.workingCopy.passwordScope() == LoginToolsConfig.PasswordScope.GLOBAL) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("Password for all servers"), centerX - 110, 150, 0xFFFFFF);
        } else if (this.rowWidgets.isEmpty()) {
            this.drawWrappedText(context, Text.literal("No saved servers yet. Add a server address and its password below."), centerX - 110, 186, 220, 0xA0A0A0);
        } else {
            context.drawTextWithShadow(this.textRenderer, Text.literal("Saved server passwords"), centerX - 110, 186, 0xFFFFFF);
        }
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

    private Text scopeText() {
        return Text.literal("Global/Certain : " + this.workingCopy.passwordScope().displayText());
    }

    private void updateEntry(int index, String address, String password) {
        if (index >= 0 && index < this.workingCopy.serverPasswords().size()) {
            this.workingCopy.serverPasswords().set(index, new LoginToolsConfig.ServerPasswordEntry(address, password));
        }
    }

    private record RowWidgets(TextFieldWidget addressField, TextFieldWidget passwordField, ButtonWidget deleteButton) {
    }
}
