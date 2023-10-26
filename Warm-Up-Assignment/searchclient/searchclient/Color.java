package searchclient;

import java.util.Locale;

public enum Color
{
    Blue(0),
    Red(1),
    Cyan(2),
    Purple(3),
    Green(4),
    Orange(5),
    Pink(6),
    Grey(7),
    Lightblue(8),
    Brown(9);

    private final int value;
    private Color(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Color fromString(String s)
    {
        switch (s.toLowerCase(Locale.ROOT))
        {
            case "blue":
                return Blue;
            case "red":
                return Red;
            case "cyan":
                return Cyan;
            case "purple":
                return Purple;
            case "green":
                return Green;
            case "orange":
                return Orange;
            case "pink":
                return Pink;
            case "grey":
                return Grey;
            case "lightblue":
                return Lightblue;
            case "brown":
                return Brown;
            default:
                return null;
        }
    }
}
