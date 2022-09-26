package com.wyattk.appframe.glsl.util;

import java.io.Serializable;

abstract class AbsVecN<T extends Serializable> implements GLSLDataType<T> {

    protected T[] vec;
    protected int size;

    @SafeVarargs
    AbsVecN(int size, T... values) {
        if(size > 4 || size < 2)
            throw new IllegalArgumentException("Cannot create vector of size outside bounds [1,4]");
        if(values.length != size)
            throw new IllegalArgumentException("Array size should match variable size: Expected " + size + " got " + values.length);
        this.size = size;
        this.vec = (T[]) new Serializable[size];
        set(values);
    }

    @SafeVarargs
    public final void set(T... values) {
        System.arraycopy(values, 0, this.vec, 0, size);
    }

    public void set(char index, T value) {
        if(index == 'x' || index == 'r') {
            this.vec[0] = value;
            return;
        }
        if(size > 1 && (index == 'y' || index == 'g')) {
            this.vec[1] = value;
            return;
        }
        if(size > 2 && (index == 'z' || index == 'b')) {
            this.vec[2] = value;
            return;
        }
        if(size > 3 && (index == 'w' || index == 'a')) {
            this.vec[3] = value;
            return;
        }
        throw new IllegalArgumentException("Cannot access position outside of vector: " + index + " with size " + size);
    }

    public T get(char index) {
        if(index == 'x' || index == 'r')
            return this.vec[0];
        if(size > 1 && (index == 'y' || index == 'g'))
            return this.vec[1];
        if(size > 2 && (index == 'z' || index == 'b'))
            return this.vec[2];
        if(size > 3 && (index == 'w' || index == 'a'))
            return this.vec[3];
        throw new IllegalArgumentException("Cannot access position outside of vector");
    }

    public T[] get() {
        return vec;
    }

    public abstract int byteSize();
}
