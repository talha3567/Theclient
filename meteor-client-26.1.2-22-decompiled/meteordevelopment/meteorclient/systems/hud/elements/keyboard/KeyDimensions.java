package meteordevelopment.meteorclient.systems.hud.elements.keyboard;

enum KeyDimensions {
    UNIT_1U(1.0),
    UNIT_1_25U(1.25),
    UNIT_1_5U(1.5),
    UNIT_1_75U(1.75),
    UNIT_2U(2.0),
    UNIT_2_25U(2.25),
    UNIT_2_75U(2.75),
    UNIT_6_25U(6.25);

    public final double units;
    public static final KeyDimensions STANDARD;
    public static final KeyDimensions TAB;
    public static final KeyDimensions CAPS_LOCK;
    public static final KeyDimensions ENTER_ANSI;
    public static final KeyDimensions LEFT_SHIFT_ANSI;
    public static final KeyDimensions RIGHT_SHIFT;
    public static final KeyDimensions BACKSPACE;
    public static final KeyDimensions LEFT_SHIFT_ISO;
    public static final KeyDimensions ENTER_ISO_WIDTH;
    public static final KeyDimensions ENTER_ISO_HEIGHT;
    public static final KeyDimensions CTRL;
    public static final KeyDimensions ALT;
    public static final KeyDimensions GUI;
    public static final KeyDimensions MENU;
    public static final KeyDimensions SPACEBAR;

    private KeyDimensions(double units) {
        this.units = units;
    }

    public double toPixels(double baseUnit, double gap) {
        return this.units * baseUnit + (this.units - 1.0) * gap;
    }

    public double toPixels(double baseUnit) {
        return this.units * baseUnit;
    }

    static {
        STANDARD = UNIT_1U;
        TAB = UNIT_1_5U;
        CAPS_LOCK = UNIT_1_75U;
        ENTER_ANSI = UNIT_2_25U;
        LEFT_SHIFT_ANSI = UNIT_2_25U;
        RIGHT_SHIFT = UNIT_2_75U;
        BACKSPACE = UNIT_2U;
        LEFT_SHIFT_ISO = UNIT_1_25U;
        ENTER_ISO_WIDTH = UNIT_1_25U;
        ENTER_ISO_HEIGHT = UNIT_2U;
        CTRL = UNIT_1_25U;
        ALT = UNIT_1_25U;
        GUI = UNIT_1_25U;
        MENU = UNIT_1_25U;
        SPACEBAR = UNIT_6_25U;
    }
}
