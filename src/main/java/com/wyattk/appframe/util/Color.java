package com.wyattk.appframe.util;

/**
 * Class for holding color data as floats
 */
public class Color {

    public static final Color BLACK = new Color(0f,0f,0f);
    public static final Color WHITE = new Color(1f,1f,1f);
    public static final Color RED = new Color(1f,0f,0f);
    public static final Color ORANGE = new Color(1f,0.5f,0f);
    public static final Color YELLOW = new Color(1f,1f,0f);
    public static final Color GREEN = new Color(0f,1f,0f);
    public static final Color CYAN = new Color(0f,1f,1f);
    public static final Color BLUE = new Color(0f,0f,1f);
    public static final Color PURPLE = new Color(0.5f,0f,1f);
    public static final Color MAGENTA = new Color(1f,0f,1f);

    public float r, g, b, a;

    /**
     * Builds a color defined by 4 floats range [0, 1]
     * @param r is the red value
     * @param g is the green value
     * @param b is the blue value
     * @param a is the alpha value
     */
    public Color(float r, float g, float b, float a){
        if(r > 1 || r < 0)
            throw new IllegalArgumentException("r be outside the bounds [0, 1]");
        if(g > 1 || g < 0)
            throw new IllegalArgumentException("g be outside the bounds [0, 1]");
        if(b > 1 || b < 0)
            throw new IllegalArgumentException("b be outside the bounds [0, 1]");
        if(a > 1 || a < 0)
            throw new IllegalArgumentException("a be outside the bounds [0, 1]");
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /**
     * Builds a color defined by 4 floats range [0, 1], sets alpha to 1
     * @param r is the red value
     * @param g is the green value
     * @param b is the blue value
     */
    public Color(float r, float g, float b){
        this(r, g, b, 1.0f);
    }

    /**
     * Builds a color defined by 4 ints range [0, 255]
     * @param r is the red value
     * @param g is the green value
     * @param b is the blue value
     * @param a is the alpha value
     */
    public Color(int r, int g, int b, int a){
        if(r > 255 || r < 0)
            throw new IllegalArgumentException("r be outside the bounds [0, 255]");
        if(g > 255 || g < 0)
            throw new IllegalArgumentException("g be outside the bounds [0, 255]");
        if(b > 255 || b < 0)
            throw new IllegalArgumentException("b be outside the bounds [0, 255]");
        if(a > 255 || a < 0)
            throw new IllegalArgumentException("a be outside the bounds [0, 255]");
        this.r = r/255f;
        this.g = g/255f;
        this.b = b/255f;
        this.a = a/255f;
    }

    /**
     * Builds a color defined by 4 ints range [0, 255], sets alpha to 255
     * @param r is the red value
     * @param g is the green value
     * @param b is the blue value
     */
    public Color(int r, int g, int b){
        this(r, g, b, 255);
    }
}
