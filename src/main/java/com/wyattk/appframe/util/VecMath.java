package com.wyattk.appframe.util;

public class VecMath {

    public static float[] vecMul(float[] u, float[] v) {
        if(u.length != v.length)
            throw new IllegalArgumentException("Cannot component multiply vectors of different lengths, u:"+u.length+" v:"+v.length);
        float[] vec = new float[u.length];
        for(int x = 0; x < vec.length; x++)
            vec[x] = u[x]*v[x];
        return vec;
    }

    public static float[] vecAdd(float[] u, float[] v) {
        if(u.length != v.length)
            throw new IllegalArgumentException("Cannot component add vectors of different lengths, u:"+u.length+" v:"+v.length);
        float[] vec = new float[u.length];
        for(int x = 0; x < vec.length; x++)
            vec[x] = u[x]+v[x];
        return vec;
    }

    public static float[] vecScale(float[] u, float scalar) {
        float[] vec = new float[u.length];
        for(int x = 0; x < vec.length; x++)
            vec[x] = u[x]*scalar;
        return vec;
    }

    public static float vecDot(float[] u, float[] v) {
        if(u.length != v.length)
            throw new IllegalArgumentException("Cannot dot multiply vectors of different lengths, u:"+u.length+" v:"+v.length);
        float out = 0;
        for(int x = 0; x < u.length; x++)
            out += u[x]*v[x];
        return out;
    }

    public static float[] vec3Cross(float[] u, float[] v) {
        if(u.length != 3 || v.length != 3)
            throw new IllegalArgumentException("Cannot cross multiply vectors not size 3, u:"+u.length+" v:"+v.length);
        return new float[] {
                u[1]*v[2] - u[2]*v[1],
                u[0]*v[2] - u[2]*v[0],
                u[0]*v[1] - u[1]*v[0]
        };
    }

    public static float vecMag(float[] u) {
        float len = 0;
        for (float f : u) len += f * f;
        return (float) Math.sqrt(len);
    }

    public static float[] normal(float[] u) {
        return vecScale(u, vecMag(u));
    }



    public static float[] matMul(float[] a, float[] b, int size) {
        if(a.length != size*size)
            throw new IllegalArgumentException("Unexpected matrix size! Array a length: "+a.length+", Expected: "+size*size);
        if(b.length != size*size)
            throw new IllegalArgumentException("Unexpected matrix size! Array b length: "+b.length+", Expected: "+size*size);
        if(a.length != b.length)
            throw new IllegalArgumentException("Cannot multiply matrices of different sizes, a: "+a.length+" , b: "+b.length);
        float[] mat = new float[size*size];
        float val;
        for(int outR = 0; outR < size; outR++) {
            for(int outC = 0; outC < size; outC++) {
                val = 0;
                for(int x = 0; x < size; x++)
                    val += a[matOffset(outR, x, size)]*b[matOffset(outC, x, size)];
                mat[matOffset(outR, outC, size)] = val;
            }
        }
        return mat;
    }

    public static float[] matVecMul(float[] mat, float[] vec) {
        if(mat.length != vec.length*vec.length)
            throw new IllegalArgumentException("Unexpected matrix size! Array length: "+mat.length+", Expected: "+vec.length*vec.length);
        float[] out = new float[vec.length];
        float val;
        for(int outR = 0; outR < vec.length; outR++) {
            val = 0;
            for(int x = 0; x < vec.length; x++)
                val += mat[matOffset(outR, x, vec.length)]*vec[x];
            out[outR] = val;
        }
        return out;
    }

    public static int matOffset(int row, int col, int colCount) {
        return row * colCount + col;
    }



    public static float[] IdentityMat(int size) {
        float[] mat = new float[size*size];
        for(int r = 0; r < size; r++)
            for(int c = 0; c < size; c++)
                mat[matOffset(r, c, size)] = (r == c ? 1 : 0);
        return mat;
    }
}
