package yal.testmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import target.Main;

@Mixin(Main.class)
public class TestMixin {
	@Inject(at = @At("HEAD"), method = "main")
	private static void mainInjector(CallbackInfo info) {
		System.out.println("Hello Mixin");
	}
}
