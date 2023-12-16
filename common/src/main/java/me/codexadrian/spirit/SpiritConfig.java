package me.codexadrian.spirit;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.jetbrains.annotations.Contract;

public class SpiritConfig {

    @Contract(pure = true)
    @ExpectPlatform
    public static boolean isCollectFromCorrupt() {
        throw new AssertionError();
    }

    @Contract(pure = true)
    @ExpectPlatform
    public static String getInitialTierName() {
        throw new AssertionError();
    }

    @Contract(pure = true)
    @ExpectPlatform
    public static int getCrudeSoulCrystalCap() {
        throw new AssertionError();
    }
    @Contract(pure = true)
    @ExpectPlatform
    public static int getSoulPedestalRadius() {
        throw new AssertionError();
    }

    @Contract(pure = true)
    @ExpectPlatform
    public static boolean showChippedError() {
        throw new AssertionError();
    }
}
