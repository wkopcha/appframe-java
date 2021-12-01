package com.teamlightbox.appframe.glsl.util;

public abstract class IVecN extends AbsVecN<Integer> {

    public IVecN(int size, Integer... values) {
        super(size, values);
    }

    @Override
    public int byteSize() {
        return Integer.BYTES * this.size;
    }
}