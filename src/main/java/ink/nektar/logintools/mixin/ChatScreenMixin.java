package ink.nektar.logintools.mixin;

import ink.nektar.logintools.LoginTools;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "render", at = @At("TAIL"))
    private void loginTools$maskLoginPassword(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String text = this.chatField.getText();
        if (!LoginTools.shouldMaskLoginCommand(text)) {
            return;
        }

        String maskedText = LoginTools.maskedLoginText(text);
        int x1 = this.chatField.getX() + 2;
        int y1 = this.chatField.getY() + 2;
        int x2 = this.chatField.getX() + this.chatField.getWidth() - 2;
        int y2 = this.chatField.getY() + this.chatField.getHeight() - 2;
        context.fill(x1, y1, x2, y2, 0xFF000000);

        int textY = this.chatField.getY() + (this.chatField.getHeight() - 8) / 2;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(maskedText), this.chatField.getX() + 4, textY, 0xE0E0E0);
    }
}
