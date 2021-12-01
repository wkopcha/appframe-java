package com.teamlightbox.appframe.glsl.mat;

import com.teamlightbox.appframe.glsl.util.GLSLDataType;

public class MatNxM implements GLSLDataType<Float> {

    protected Float[] mat;
    private final int n, m;

    public MatNxM(int n, int m, Float... values) {
        if(values.length != n * m)
            throw new IllegalArgumentException("Values must be the size of the matrix: expected " + m*n + " got " + values.length);
        this.n = n;
        this.m = m;
        set(values);
    }

    public void set(Float... values) {
        if(values.length != n * m)
            throw new IllegalArgumentException("Values must be the size of the matrix: expected " + m*n + " got " + values.length);
        System.arraycopy(values, 0, this.mat, 0, n * m);
    }

    public void set(int column, Float... values) {
        if(column < 0 || column > n)
            throw new IllegalArgumentException("Column must fall on [0," + n + ") got " + column);
        if(values.length != m)
            throw new IllegalArgumentException("Values must be the size of the column: expected " + m + " got " + values.length);
        System.arraycopy(values, 0, this.mat, column * n, m);
    }

    public void set(int column, int row, Float value) {
        if(column < 0 || column > n)
            throw new IllegalArgumentException("Column must fall on [0," + n + ") got " + column);
        if(row < 0 || row > m)
            throw new IllegalArgumentException("Row must fall on [0," + m + ") got " + column);
        this.mat[column * n + row] = value;
    }

    public Float[] get(){
        return mat;
    }

    public Float get(int column, int row) {
        return mat[column*n + row];
    }
}
