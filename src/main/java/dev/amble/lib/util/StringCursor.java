package dev.amble.lib.util;

public class StringCursor {

    private final String str;
    private int cursor;
    private final int step;

    public StringCursor(String str, int start, int step) {
        this.str = str;
        this.cursor = start;
        this.step = step;
    }

    public void next() {
        this.cursor += step;
    }

    public char peek() {
        return this.str.charAt(cursor);
    }

    public char peekNext() {
        return this.str.charAt(cursor + step);
    }

    public String substring() {
        if (step > 0)
            return str.substring(this.cursor);

        if (step < 0)
            return str.substring(0, this.cursor + 1);

        return "";
    }
}
