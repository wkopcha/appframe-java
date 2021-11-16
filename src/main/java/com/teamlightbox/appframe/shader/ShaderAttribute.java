package com.teamlightbox.appframe.shader;

import com.teamlightbox.appframe.mesh.Mesh;

/**
 * Describes the behavior of a specific shader attribute
 */
public class ShaderAttribute {

    /**
     * POSITION         3d position of the vertex (x, y, z)
     * COLOR            4d color of the vertex (r, g, b, a)
     * VERTEX_NORMAL    3d direction of the vertex normal (x, y, z)
     * TEX_COORD_1      2d coordinate of a texture (x, y)
     * TEX_COORD_2      2d coordinate of a texture (x,y)
     */
    public static final ShaderAttribute POSITION = new ShaderAttribute(3, Mesh::getPositions);
    public static final ShaderAttribute COLOR = new ShaderAttribute(4, Mesh::getColors);
    public static final ShaderAttribute VERTEX_NORMAL = new ShaderAttribute(3, (Mesh mesh) -> {
        float[] normal = new float[mesh.getPositions().length];
        float[] positions = mesh.getPositions();
        int[] idxs = mesh.getIndices();


        return normal;
    });
    //public static final ShaderAttribute TEX_COORD_1 = new ShaderAttribute(2, (Mesh mesh) -> new float[]{0, 0});
    //public static final ShaderAttribute TEX_COORD_2 = new ShaderAttribute(2, (Mesh mesh) -> new float[]{0, 0});


    /**
     * size     the size of the attribute/the number of floats per index of the mesh
     * func     a function returning the data to use to fill the attribute
     *              The length of the returned array *should* be (attribute size) * (index count)
     *              Aka there are (attribute size) values per index, so for all the indices to
     *              have the attribute you need (attribute size) * (index count) floats of data
     */
    private final int size;
    private final AttributeFunction func;

    /**
     * Creates a shader attribute
     * @param size is the number of floats per index for this attribute
     * @param evaluationFunction is the function used to get the values to pass to the GPU.
     *                           The length of the returned array *should* be (attribute size) * (index count)
     *                           Aka there are (attribute size) values per index, so for all the indices to
     *                           have the attribute you need (attribute size) * (index count) floats of data
     */
    public ShaderAttribute(int size, AttributeFunction evaluationFunction) {
        this.size = size;
        func = evaluationFunction;
    }

    /**
     * @param mesh is the mesh to extract data from
     * @return the data for the attribute
     */
    public float[] getData(Mesh mesh) {
        return func.getAttributeData(mesh);
    }

    /**
     * @return the size of the attribute/the number of floats per index of the mesh
     */
    public int getSize() {
        return size;
    }


    /**
     * Lambda for getting a float[] from a mesh
     */
    public interface AttributeFunction {
        float[] getAttributeData(Mesh mesh);
    }
}
