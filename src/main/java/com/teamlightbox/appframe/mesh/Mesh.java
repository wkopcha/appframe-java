package com.teamlightbox.appframe.mesh;

import com.teamlightbox.appframe.shader.Shader;
import com.teamlightbox.appframe.shader.ShaderAttribute;
import com.teamlightbox.appframe.util.IUsesNativeMemory;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL42.*;

/**
 * Class for making the creation of shaped in OpenGl easier
 */
public class Mesh implements IUsesNativeMemory {

    /**
     * vertexCount  the number of vertices in the mesh
     * drawMode     the OpenGL draw mode to draw the mesh with
     * vaoId        the id of the vertex array object for this mesh's VBOs
     * idxVboId     the id of the vertex buffer object storing index order data
     * vboIds       the list of vbo ids used - exact contents linked to what shader is used
     * positions    the positions of each vertex in the mesh as a flat array
     * colors       the color of each vertex in the mesh as a flat array
     * indices      the order in which the vertices are drawn
     * isOnGpu      describes if the mesh data is on the gpu or not
     * shader       the shader that the mesh currently uses
     */
    private final int  vertexCount, drawMode;
    private int vaoId, idxVboId;
    private final LinkedList<Integer> vboIds = new LinkedList<>();
    private final float[] positions, colors;
    private final int[] indices;
    private boolean isOnGpu = false;
    private Shader shader;

    /**
     * Creates a mesh and does the OpenGL setup for getting mesh data to the GPU
     * @param drawMode the OpenGL draw mode to draw the mesh with
     * @param positions is an array of floats following the pattern {x0, y0, z0, x1, y1, z1, ... xn, yn, zn}
     * @param colors is an array of floats following the pattern {r0, b0, g0, a0, r1, b1, g1, a1, ... rn, gn, bn, an}
     * @param indices is an array of (int) indices to use, in order, to draw the mesh. {1, 2} would correspond to positions[0:2], positions[3:5]
     */
    Mesh(int drawMode, float[] positions, float[] colors, int[] indices){
        this.drawMode = drawMode;
        vertexCount = indices.length;
        this.positions = positions;
        this.colors = colors;
        this.indices = indices;
    }

    /**
     * @return the id of the vertex array object containing this mesh's vbo s
     */
    public int getVaoId(){
        return vaoId;
    }

    /**
     * @return the number of vertices in the mash
     */
    public int getVertexCount(){
        return vertexCount;
    }

    /**
     * @return the OpenGL draw mode to draw the mesh with
     */
    public int getDrawMode() {
        return drawMode;
    }

    /**
     * Cleans up resources on the GPU that the mesh uses
     */
    public void cleanup(){
        if(isOnGpu)
            gpuFree();
    }

    /**
     * Removes the mesh data from the gpu
     * Cannot remove from the gpu if the data is not there
     */
    public void gpuFree(){
        if(!isOnGpu)
            throw new IllegalStateException("Cannot free space if mesh is not on GPU!");

        glBindVertexArray(vaoId);

        //glDisableVertexAttribArray(0);
        //glDisableVertexAttribArray(1);

        // delete vbo
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for(int vboId: vboIds)
            glDeleteBuffers(vboId);
        glDeleteBuffers(idxVboId);

        // delete vao
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);

        isOnGpu = false;
    }

    /**
     * Puts the mesh data on the gpu to render
     * Cannot load to the gpu if the data is already there
     */
    public void gpuLoad(){
        if(isOnGpu)
            throw new IllegalStateException("Cannot load to GPU if already loaded!");

        LinkedList<FloatBuffer> vboBuffers = new LinkedList<>();
        IntBuffer idxBuffer = null;
        try {
            // fill buffers (1 per shader-given attribute) with data as provided by the shader attribute
            LinkedList<ShaderAttribute> attributes = shader.getAttributes();
            for(ShaderAttribute sa: attributes) {
                float[] data = sa.getData(this);
                FloatBuffer temp = MemoryUtil.memAllocFloat(data.length);
                temp.put(data).flip();
                vboBuffers.add(temp);
            }

            // fill index buffer
            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();

            // create & bind the vertex array object for filling
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // creates the vertex buffer objects for each attribute given by the shader
            for(int x = 0; x < attributes.size(); x++) {
                int vboId = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, vboBuffers.get(x), GL_STATIC_DRAW);
                //glEnableVertexAttribArray(x);
                glVertexAttribPointer(x, attributes.get(x).getSize(), GL_FLOAT, false, 0, 0);
                vboIds.add(vboId);
            }

            // create index order vertex buffer object
            idxVboId = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);

            // unbind buffers and arrays
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

            isOnGpu = true;
        } finally {
            // cleanup the stacks we made, we no longer need the data in our RAM; it's in the GPU by now
            for(FloatBuffer buffer: vboBuffers)
                if(buffer != null)
                    MemoryUtil.memFree(buffer);

            if(idxBuffer != null)
                MemoryUtil.memFree(idxBuffer);
        }
    }

    /**
     * Tells the mesh to use the given shader
     * Cannot be changed when on the gpu
     * @param shader is the new shader to make the mesh use
     */
    public void useShader(Shader shader) {
        if(isOnGpu)
            throw new IllegalStateException("Cannot set mesh shader while mesh data is on the GPU!");
        this.shader = shader;
    }

    /**
     * @return the shader the mesh is using
     */
    public Shader getShader() {
        return shader;
    }

    /**
     * @return the positions of each vertex as a flat array
     */
    public float[] getPositions() {
        return positions;
    }

    /**
     * @return the color of each vertex as a flat array
     */
    public float[] getColors() {
        return colors;
    }

    /**
     * @return the vertex order
     */
    public int[] getIndices() {
        return indices;
    }

    /**
     * @return whether the mesh data is on the gpu or not
     */
    public boolean isOnGpu(){
        return isOnGpu;
    }
}
