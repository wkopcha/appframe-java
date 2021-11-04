package com.teamlightbox.appframe.util;

public class Color {

    public static final Color BLACK = new Color(0f,0f,0f);
    public static final Color RED = new Color(1f,0f,0f);
    public static final Color ORANGE = new Color(1f,0.5f,0f);
    public static final Color YELLOW = new Color(1f,1f,0f);
    public static final Color GREEN = new Color(0f,1f,0f);
    public static final Color CYAN = new Color(0f,1f,1f);
    public static final Color BLUE = new Color(0f,0f,1f);
    public static final Color PURPLE = new Color(0.5f,0f,1f);
    public static final Color MAGENTA = new Color(1f,0f,1f);

    public float r, g, b, a;

    public Color(float r, float g, float b, float a){
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(float r, float g, float b){
        this(r, g, b, 1.0f);
    }

    public Color(int r, int g, int b, int a){
        this(r/255f, g/255f, b/255f, a/255f);
    }

    public Color(int r, int g, int b){
        this(r, g, b, 255);
    }
}
