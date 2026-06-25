package id.kepalakubik.minecraftbluearchivehalo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class ScreenCompat {
    // Inner class for lazy-loading (Thread-Safe & JIT Optimized)
    private static class LazyHolder {
        static final Supplier<Screen> SCREEN_SUPPLIER = buildSupplier();
    }

    private static Supplier<Screen> buildSupplier() {
        Minecraft mc = Minecraft.getInstance();
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // Try 26.2+ API: gui.screen()
        try {
            Method m = mc.gui.getClass().getMethod("screen");
            MethodHandle handle = lookup.unreflect(m);
            Object gui = mc.gui; // Safely cached because its execution is deferred by LazyHolder
            return () -> {
                try {
                    return (Screen) handle.invoke(gui);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
        } catch (NoSuchMethodException | IllegalAccessException ignored) {}

        // Fallback to 26.1 API: Minecraft.screen field
        try {
            Field f = Minecraft.class.getField("screen");
            MethodHandle handle = lookup.unreflectGetter(f);
            return () -> {
                try {
                    return (Screen) handle.invoke(mc);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            };
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Cannot resolve screen accessor on this version", e);
        }
    }

    public static Screen getCurrentScreen() {
        // The LazyHolder is loaded by the JVM the first time this function is called.
        // When this is called (inside the game loop), Minecraft.getInstance() is guaranteed to be ready.
        return LazyHolder.SCREEN_SUPPLIER.get();
    }
}