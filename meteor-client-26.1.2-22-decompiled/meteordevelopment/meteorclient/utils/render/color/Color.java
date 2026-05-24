package meteordevelopment.meteorclient.utils.render.color;

import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Color
implements ICopyable<Color>,
ISerializable<Color> {
    public static final Color CLEAR = new Color(0, 0, 0, 0);
    public static final Color WHITE = new Color(java.awt.Color.WHITE);
    public static final Color LIGHT_GRAY = new Color(java.awt.Color.LIGHT_GRAY);
    public static final Color GRAY = new Color(java.awt.Color.GRAY);
    public static final Color DARK_GRAY = new Color(java.awt.Color.DARK_GRAY);
    public static final Color BLACK = new Color(java.awt.Color.BLACK);
    public static final Color RED = new Color(java.awt.Color.RED);
    public static final Color PINK = new Color(java.awt.Color.PINK);
    public static final Color ORANGE = new Color(java.awt.Color.ORANGE);
    public static final Color YELLOW = new Color(java.awt.Color.YELLOW);
    public static final Color GREEN = new Color(java.awt.Color.GREEN);
    public static final Color MAGENTA = new Color(java.awt.Color.MAGENTA);
    public static final Color CYAN = new Color(java.awt.Color.CYAN);
    public static final Color BLUE = new Color(java.awt.Color.BLUE);
    public int r;
    public int g;
    public int b;
    public int a;

    public Color() {
        this(255, 255, 255, 255);
    }

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
        this.validate();
    }

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.validate();
    }

    public Color(float r, float g, float b, float a) {
        this.r = (int)(r * 255.0f);
        this.g = (int)(g * 255.0f);
        this.b = (int)(b * 255.0f);
        this.a = (int)(a * 255.0f);
        this.validate();
    }

    public Color(int packed) {
        this.r = Color.toRGBAR(packed);
        this.g = Color.toRGBAG(packed);
        this.b = Color.toRGBAB(packed);
        this.a = Color.toRGBAA(packed);
    }

    public Color(Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    public Color(java.awt.Color color) {
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.a = color.getAlpha();
    }

    public Color(ChatFormatting formatting) {
        if (formatting.isColor()) {
            this.r = Color.toRGBAR(formatting.getColor());
            this.g = Color.toRGBAG(formatting.getColor());
            this.b = Color.toRGBAB(formatting.getColor());
            this.a = Color.toRGBAA(formatting.getColor());
        } else {
            this.r = 255;
            this.g = 255;
            this.b = 255;
            this.a = 255;
        }
    }

    public Color(TextColor textColor) {
        this.r = Color.toRGBAR(textColor.getValue());
        this.g = Color.toRGBAG(textColor.getValue());
        this.b = Color.toRGBAB(textColor.getValue());
        this.a = Color.toRGBAA(textColor.getValue());
    }

    public Color(Style style) {
        TextColor textColor = style.getColor();
        if (textColor == null) {
            this.r = 255;
            this.g = 255;
            this.b = 255;
            this.a = 255;
        } else {
            this.r = Color.toRGBAR(textColor.getValue());
            this.g = Color.toRGBAG(textColor.getValue());
            this.b = Color.toRGBAB(textColor.getValue());
            this.a = Color.toRGBAA(textColor.getValue());
        }
    }

    public static int fromRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + b + (a << 24);
    }

    public static int toRGBAR(int color) {
        return color >> 16 & 0xFF;
    }

    public static int toRGBAG(int color) {
        return color >> 8 & 0xFF;
    }

    public static int toRGBAB(int color) {
        return color & 0xFF;
    }

    public static int toRGBAA(int color) {
        return color >> 24 & 0xFF;
    }

    public static Color fromHsv(double h, double s, double v) {
        double g;
        double r;
        if (s <= 0.0) {
            double r2 = v;
            double g2 = v;
            double b = v;
            return new Color((int)(r2 * 255.0), (int)(g2 * 255.0), (int)(b * 255.0), 255);
        }
        double hh = h;
        if (hh >= 360.0) {
            hh = 0.0;
        }
        int i = (int)(hh /= 60.0);
        double ff = hh - (double)i;
        double p = v * (1.0 - s);
        double q = v * (1.0 - s * ff);
        double t = v * (1.0 - s * (1.0 - ff));
        return new Color((int)(r * 255.0), (int)(g * 255.0), (int)((switch (i) {
            case 0 -> {
                r = v;
                g = t;
                yield p;
            }
            case 1 -> {
                r = q;
                g = v;
                yield p;
            }
            case 2 -> {
                r = p;
                g = v;
                yield t;
            }
            case 3 -> {
                r = p;
                g = q;
                yield v;
            }
            case 4 -> {
                r = t;
                g = p;
                yield v;
            }
            default -> {
                r = v;
                g = p;
                yield q;
            }
        }) * 255.0), 255);
    }

    public Color set(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.validate();
        return this;
    }

    public Color r(int r) {
        this.r = r;
        this.validate();
        return this;
    }

    public Color g(int g) {
        this.g = g;
        this.validate();
        return this;
    }

    public Color b(int b) {
        this.b = b;
        this.validate();
        return this;
    }

    public Color a(int a) {
        this.a = a;
        this.validate();
        return this;
    }

    @Override
    public Color set(Color value) {
        this.r = value.r;
        this.g = value.g;
        this.b = value.b;
        this.a = value.a;
        this.validate();
        return this;
    }

    public boolean parse(String text) {
        String[] split = text.split(",");
        if (split.length != 3 && split.length != 4) {
            return false;
        }
        try {
            int r = Integer.parseInt(split[0]);
            int g = Integer.parseInt(split[1]);
            int b = Integer.parseInt(split[2]);
            int a = split.length == 4 ? Integer.parseInt(split[3]) : this.a;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            return true;
        }
        catch (NumberFormatException numberFormatException) {
            return false;
        }
    }

    @Override
    public Color copy() {
        return new Color(this.r, this.g, this.b, this.a);
    }

    public SettingColor toSetting() {
        return new SettingColor(this.r, this.g, this.b, this.a);
    }

    public TextColor toTextColor() {
        return TextColor.fromRgb((int)this.getPacked());
    }

    public Style toStyle() {
        return Style.EMPTY.withColor(this.toTextColor());
    }

    public Style styleWith(Style style) {
        return style.withColor(this.toTextColor());
    }

    public void validate() {
        if (this.r < 0) {
            this.r = 0;
        } else if (this.r > 255) {
            this.r = 255;
        }
        if (this.g < 0) {
            this.g = 0;
        } else if (this.g > 255) {
            this.g = 255;
        }
        if (this.b < 0) {
            this.b = 0;
        } else if (this.b > 255) {
            this.b = 255;
        }
        if (this.a < 0) {
            this.a = 0;
        } else if (this.a > 255) {
            this.a = 255;
        }
    }

    public Vec3 getVec3d() {
        return new Vec3((double)this.r / 255.0, (double)this.g / 255.0, (double)this.b / 255.0);
    }

    public Vector3f getVec3f() {
        return new Vector3f((float)this.r / 255.0f, (float)this.g / 255.0f, (float)this.b / 255.0f);
    }

    public Vector4f getVec4f() {
        return new Vector4f((float)this.r / 255.0f, (float)this.g / 255.0f, (float)this.b / 255.0f, (float)this.a / 255.0f);
    }

    public int getPacked() {
        return Color.fromRGBA(this.r, this.g, this.b, this.a);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("r", this.r);
        tag.putInt("g", this.g);
        tag.putInt("b", this.b);
        tag.putInt("a", this.a);
        return tag;
    }

    @Override
    public Color fromTag(CompoundTag tag) {
        this.r = tag.getIntOr("r", 0);
        this.g = tag.getIntOr("g", 0);
        this.b = tag.getIntOr("b", 0);
        this.a = tag.getIntOr("a", 0);
        this.validate();
        return this;
    }

    public String toString() {
        return this.r + " " + this.g + " " + this.b + " " + this.a;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Color color = (Color)o;
        return this.r == color.r && this.g == color.g && this.b == color.b && this.a == color.a;
    }

    public int hashCode() {
        int result = this.r;
        result = 31 * result + this.g;
        result = 31 * result + this.b;
        result = 31 * result + this.a;
        return result;
    }
}
