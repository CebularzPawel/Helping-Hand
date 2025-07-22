package net.cebularz.helpinghand.utils;

public class GuiUtility
{
    public static String formatTicksToTime(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
