package com.teamlightbox.appframe.mesh;

import com.teamlightbox.appframe.util.IUsesNativeMemory;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL42.*;

/**
 * Class for making the creation of shaped in OpenGl easier
 */
public class Mesh implements IUsesNativeMemory {

    /**
     * vaoId        the id of the vertex array object for this mesh's vbo s
     * posVboId     the id of the vertex buffer object storing position data
     * colorVboId   the id of the vertex buffer object storing color data
     * idxVboId     the id of the vertex buffer object storing index order data
     * vertexCount  the number of vertices in the mesh
     * drawMode     the OpenGL draw mode to draw the mesh with
     */
    private final int vaoId, posVboId, colorVboId, idxVboId, vertexCount, drawMode;

    /**
     * Creates a mesh and does the OpenGL setup for getting mesh data to the GPU
     * @param drawMode the OpenGL draw mode to draw the mesh with
     * @param positions is an array of floats following the pattern {x0, y0, z0, x1, y1, z1, ... xn, yn, zn}
     * @param colors is an array of floats following the pattern {r0, b0, g0, a0, r1, b1, g1, a1, ... rn, gn, bn, an}
     * @param indices is an array of (int) indices to use, in order, to draw the mesh. {1, 2} would correspond to positions[0:2], positions[3:5]
     */
    Mesh(int drawMode, float[] positions, float[] colors, int[] indices){
        this.drawMode = drawMode;
        FloatBuffer posBuffer = null;
        FloatBuffer colorBuffer = null;
        IntBuffer idxBuffer = null;
        try {
            vertexCount = indices.length;

            // fill buffers with appropriate data
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();

            colorBuffer = MemoryUtil.memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();

            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();

            // create & bind the vertex array object for filling
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // create position vertex buffer object, enable vertex attrib pointer 0 to position data for shaders
            posVboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // create color vertex buffer object, enable vertex attrib pointer 1 to color data for shaders
            colorVboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);

            // create index order vertex buffer object
            idxVboId = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);

            // unbind buffers and arrays
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {
            // cleanup the stacks we made, we no longer need the data in our RAM; it's in the GPU by now
            if(posBuffer != null)
                MemoryUtil.memFree(posBuffer);
            if(colorBuffer != null)
                MemoryUtil.memFree(colorBuffer);
            if(idxBuffer != null)
                MemoryUtil.memFree(idxBuffer);
        }
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
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        // delete vbo
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(colorVboId);
        glDeleteBuffers(idxVboId);

        // delete vao
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
