package com.wyattk.appframe.glsl.util;

public abstract class BVecN extends AbsVecN<Boolean> {

    public BVecN(int size, Boolean... values) {
        super(size, values);
    }

    @Override
    public int byteSize() {
        return Integer.BYTES * this.size;
    }
}
