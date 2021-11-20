package com.teamlightbox.appframe.util;

import com.teamlightbox.appframe.mesh.Mesh;

/**
 * Simple class that has some translation tools (Temp)
 */
public class Tools {

    public static float[] getMeshColorArray(Mesh mesh, Color color) {
        int vertexCount = mesh.getVertexCount();
        float[] colors = new float[vertexCount * 4];
        for(int x = 0; x < vertexCount; x++) {
            colors[4 * x] = color.r;
            colors[4 * x + 1] = color.g;
            colors[4 * x + 2] = color.b;
            colors[4 * x + 3] = color.a;
        }
        return colors;
    }
}
