package com.wyattk.appframe.glsl.mat;

public class Mat4 extends MatNxM {

    public static final Mat4 IDENTITY = new Mat4(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
    );

    public Mat4(Float... values) {
        super(4, 4, values);
    }
}
