package com.wyattk.appframe.glsl.mat;

public class Mat3 extends MatNxM {

    public static final Mat3 IDENTITY = new Mat3(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
    );

    public Mat3(Float... values) {
        super(3, 3, values);
    }
}
