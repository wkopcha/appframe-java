package com.teamlightbox.appframe.glsl.util;

public abstract class VecN extends AbsVecN<Float> {

    public VecN(int size, Float... values) {
        super(size, values);
    }

    @Override
    public int byteSize() {
        return Float.BYTES * this.size;
    }
}
