package com.wyattk.appframe.glsl.mat;

public class Mat2 extends MatNxM {

    public static final Mat2 IDENTITY = new Mat2(
            1f, 0f,
            0f, 1f
    );

    public Mat2(Float... values) {
        super(2, 2, values);
    }
}
