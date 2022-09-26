package com.wyattk.appframe.glsl.util;

public abstract class DVecN extends AbsVecN<Double> {

    public DVecN(int size, Double... values) {
        super(size, values);
    }

    @Override
    public int byteSize() {
        return Double.BYTES * this.size;
    }
}
