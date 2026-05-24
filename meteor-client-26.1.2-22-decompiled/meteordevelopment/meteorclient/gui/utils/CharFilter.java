package meteordevelopment.meteorclient.gui.utils;

public interface CharFilter {
    public boolean filter(String var1, char var2);

    default public boolean filter(String text, int i) {
        return this.filter(text, (char)i);
    }
}
