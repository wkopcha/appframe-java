package com.wyattk.appframe.mesh;

import com.wyattk.appframe.util.Color;

import java.util.ArrayList;
import java.util.LinkedList;

public class MeshTools {

    public static float[] calculateNormals(float[] positions, int[] indices) {
        ArrayList<LinkedList<float[]>> adjacentNormals = new ArrayList<>(indices.length);

        //give each vertex a list float (normal) vectors
        for(int x = 0; x < positions.length / 3; x++)
            adjacentNormals.add(new LinkedList<>());

        //for each set of 3 indices listed (1 triangle),
        //calculate the face normal and add it to the lists of normals for each vertex involved
        for(int x = 0; x < indices.length; x+=3) {
            float[] normal = new float[3];
            int x1 = indices[x]*3, y1 = indices[x]*3+1, z1 = indices[x]*3+2,
                    x2 = indices[x+1]*3, y2 = indices[x+1]*3+1, z2 = indices[x+1]*3+2,
                    x3 = indices[x+2]*3, y3 = indices[x+2]*3+1, z3 = indices[x+2]*3+2;
            normal[0] =
                    (positions[y1]-positions[y2])*(positions[z2]-positions[z3]) -
                            (positions[z1]-positions[z2])*(positions[y2]-positions[y3]);
            normal[1] =
                    (positions[x1]-positions[x2])*(positions[z2]-positions[z3]) -
                            (positions[z1]-positions[z2])*(positions[x2]-positions[x3]);
            normal[2] =
                    (positions[x1]-positions[x2])*(positions[y2]-positions[y3]) -
                            (positions[y1]-positions[y2])*(positions[x2]-positions[x3]);
            adjacentNormals.get(indices[x]).add(normal);
            adjacentNormals.get(indices[x+1]).add(normal);
            adjacentNormals.get(indices[x+2]).add(normal);
        }

        float[] normals = new float[positions.length]; //3d -> 3d
        for(int a = 0; a < adjacentNormals.size(); a++) {
            float x = 0, y = 0, z = 0;
            for(float[] vecs: adjacentNormals.get(a)) {
                x += vecs[0];
                y += vecs[1];
                z += vecs[2];
            }
            float len = (float) Math.sqrt(x*x+y*y+z*z);
            normals[3*a] = x / len;
            normals[3*a + 1] = y / len;
            normals[3*a + 2] = z / len;
        }

        return normals;
    }

    public static float[] getMeshColorArray(int vertexCount, Color color) {
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
