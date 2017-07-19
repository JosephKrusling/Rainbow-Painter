/**
 * Created by josep on 7/7/2017.
 */
public class Color {

    public byte r, g, b;

    public Color(byte r, byte g, byte b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int brightness() {
        return r + g + b;
    }

    public int humanBrightness() {
        return (int) (0.299*r + 0.587*g + 0.114*b);
    }

    public float difference(Color otherColor) {
        return Math.abs(otherColor.r - r) + Math.abs(otherColor.g - g) + Math.abs(otherColor.b - b);
    }
}
