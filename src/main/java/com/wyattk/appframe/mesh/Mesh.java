package com.wyattk.appframe.mesh;

import com.wyattk.appframe.shader.Shader;
import com.wyattk.appframe.shader.ShaderAttribute;
import com.wyattk.appframe.util.IUsesNativeMemory;
import com.wyattk.appframe.util.Logger;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL45.*;
import static com.wyattk.appframe.util.VecMath.*;

/**
 * Class for making the creation of shaped in OpenGl easier
 */
public class Mesh implements IUsesNativeMemory, AutoCloseable {

    /**
     * vertexCount      the number of vertices in the mesh
     * drawMode         the OpenGL draw mode to draw the mesh with
     * vaoId            the id of the vertex array object for this mesh's VBOs
     * idxVboId         the id of the vertex buffer object storing index order data
     * staticVboId      the id of the vertex buffer object containing all static mesh data
     * staticShaderA... a list of shader attributes that should be accessing static data
     * staticMeshData   an already-condensed list of data for the static vbo to use (saves time)
     * dynamicVboIds    a map of shader attributes depending on dynamic data to the vbo for said attribute
     * positions        the positions of each vertex in the mesh as a flat array
     * colors           the color of each vertex in the mesh as a flat array
     * indices          the order in which the vertices are drawn
     * isOnGpu          describes if the mesh data is on the gpu or not
     * blendColors      describes if the mesh should alpha blend (takes away performance)
     * positionValue... describes if this mesh's position values should change
     * colorValuesCh... describes if this mesh's color values should change
     * shader           the shader that the mesh currently uses
     */
    private final int  vertexCount, drawMode;
    private int vaoId, idxVboId, staticVboId = -1;
    private final LinkedList<ShaderAttribute> staticShaderAttributes = new LinkedList<>();
    private float[] staticMeshData;
    private final HashMap<ShaderAttribute, Integer> dynamicVboIds = new HashMap<>();
    private float[] vertexPositions, colors, normals;
    private final HashMap<ShaderAttribute, Integer> attributeIdx = new HashMap<>();
    private final int[] indices;
    private boolean isOnGpu = false, blendColors = false;
    private final boolean positionValuesChange, colorValuesChange;
    private Shader shader;
    private float[] transformationMatrix = IdentityMat(4);

    /**
     * Creates a mesh and does the OpenGL setup for getting mesh data to the GPU
     * @param drawMode the OpenGL draw mode to draw the mesh with
     * @param vertexPositions is an array of floats following the pattern {x0, y0, z0, x1, y1, z1, ... xn, yn, zn}
     * @param colors is an array of floats following the pattern {r0, b0, g0, a0, r1, b1, g1, a1, ... rn, gn, bn, an}
     * @param indices is an array of (int) indices to use, in order, to draw the mesh. {1, 2} would correspond to positions[0:2], positions[3:5]
     * @param positionValuesChange tells if the position values of this mesh can change
     * @param colorValuesChange tells if the color values of this mesh can change
     */
    Mesh(
            int drawMode,
            float[] vertexPositions,
            float[] colors,
            float[] normals,
            int[] indices,
            boolean positionValuesChange,
            boolean colorValuesChange
    ){
        this.drawMode = drawMode;
        vertexCount = indices.length;
        this.vertexPositions = vertexPositions;
        this.colors = colors;
        this.normals = normals;
        this.indices = indices;
        this.positionValuesChange = positionValuesChange;
        this.colorValuesChange = colorValuesChange;
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
        Logger.verb("Cleaning " + this + "...");
        if(isOnGpu)
            gpuFree();
        dynamicVboIds.clear();
    }

    public void close() {
        cleanup();
    }

    public String toString() {
        return getClass().getName() + " " + hashCode() + " with " + vertexCount + " vertices";
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

        if(staticVboId != -1) {
            glDeleteBuffers(staticVboId);
            staticVboId = -1;
        }

        for(int dynamicVboId: dynamicVboIds.values())
            glDeleteBuffers(dynamicVboId);
        dynamicVboIds.replaceAll((d, v) -> null);
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

        LinkedList<FloatBuffer> dynamicVboBuffers = new LinkedList<>();
        FloatBuffer staticVboBuffer = null;
        IntBuffer idxBuffer = null;
        try {

            // fill index buffer
            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();

            // create & bind the vertex array object for filling
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            //int attrIdx = 0;
            // do we have static data? if so, run this if
            if(staticMeshData.length > 0) {
                // create and fill static data buffer
                staticVboBuffer = MemoryUtil.memAllocFloat(staticMeshData.length);
                staticVboBuffer.put(staticMeshData).flip();

                // get vbo ptr
                int vboId = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, staticVboBuffer, GL_STATIC_DRAW);

                long prevPtr = 0;

                // compute the vertex attribute pointers and their pointer offsets
                // since all the data is packed together without indication in changes
                for(ShaderAttribute sa: staticShaderAttributes) {
                    //glVertexAttribPointer(attrIdx, sa.getSize(), GL_FLOAT, false, 0, prevPtr);
                    glVertexAttribPointer(attributeIdx.get(sa), sa.getSize(), GL_FLOAT, false, 0, prevPtr);
                    prevPtr = prevPtr + (long) Float.BYTES * sa.getData(this).length;
                    //attrIdx++;
                }

                // save vbo ptr
                staticVboId = vboId;
            }

            for(ShaderAttribute dsa: dynamicVboIds.keySet()) {
                float[] data = dsa.getData(this);
                FloatBuffer temp = MemoryUtil.memAllocFloat(data.length);
                temp.put(data).flip();
                dynamicVboBuffers.add(temp);
                int vboId = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferData(GL_ARRAY_BUFFER, temp, GL_DYNAMIC_DRAW);
                //glVertexAttribPointer(attrIdx, dsa.getSize(), GL_FLOAT, false, 0, 0);
                glVertexAttribPointer(attributeIdx.get(dsa), dsa.getSize(), GL_FLOAT, false, 0, 0);
                //attrIdx++;
                dynamicVboIds.put(dsa, vboId);
            }

            //for(int x = 0; x < attrIdx; x++)
            for(int x: attributeIdx.values())
                glEnableVertexAttribArray(x);

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
            dynamicVboBuffers.forEach((FloatBuffer fb) -> {
                if(fb != null)
                    MemoryUtil.memFree(fb);
            });

            if(staticVboBuffer != null)
                MemoryUtil.memFree(staticVboBuffer);

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
        staticShaderAttributes.clear();
        int attrIdx = 0;
        if(positionValuesChange || colorValuesChange) {
            for (ShaderAttribute s : shader.getAttributes()) {
                attributeIdx.put(s, attrIdx);
                attrIdx++;
                if (positionValuesChange && s.dependantOnPositionData()) {
                    dynamicVboIds.put(s, null);
                    continue;
                }
                if (colorValuesChange && s.dependantOnColorData()) {
                    dynamicVboIds.put(s, null);
                    continue;
                }
                staticShaderAttributes.add(s);
            }
        } else {
            staticShaderAttributes.addAll(shader.getAttributes());
            for(int x = 0; x < shader.getAttributes().size(); x++)
                attributeIdx.put(shader.getAttributes().get(x), x);
        }

        if(staticShaderAttributes.size() <= 0) {
            staticMeshData = new float[0];
            return;
        }

        // if there is static attributes, pre-load them into an array
        int staticDataLength = 0;
        LinkedList<float[]> staticDataList = new LinkedList<>();

        // get static data and save to an array of static data
        for(ShaderAttribute sa: staticShaderAttributes)
            staticDataList.add(sa.getData(this));

        // compute final length of the packed array
        for(float[] data: staticDataList)
            staticDataLength += data.length;

        //create and fill the packed array
        staticMeshData = new float[staticDataLength];
        int staticMeshPos = 0;
        for(float[] data: staticDataList)
            for(int x = 0; x < data.length; x++, staticMeshPos++)
                staticMeshData[staticMeshPos] = data[x];
    }

    /**
     * Changes the position data if allowed
     * @param newVertexPositionData is the new position data to replace the old
     *                        Cannot have a different number of vertices, use a different mesh for that
     *                        Vertex order is preserved
     */
    public void changePositionData(float[] newVertexPositionData, float[] newNormalData) {
        if(!positionValuesChange)
            throw new IllegalArgumentException("Cannot change position data for a non-dynamic position mesh");
        if(newVertexPositionData.length != vertexPositions.length)
            throw new IllegalArgumentException("Cannot change length of position data");
        if(newNormalData.length != normals.length)
            throw new IllegalArgumentException("Cannot change length of normal data");

        vertexPositions = newVertexPositionData;
        normals = newNormalData;
        //update GPU data
        if(isOnGpu)
            for(ShaderAttribute dsa: dynamicVboIds.keySet()) {
                if(!dsa.dependantOnPositionData())
                    continue;
                float[] data = dsa.getData(this);
                FloatBuffer floatBuffer = MemoryUtil.memAllocFloat(data.length);
                floatBuffer.put(data).flip();
                glBindVertexArray(vaoId);
                glNamedBufferSubData(dynamicVboIds.get(dsa), 0, floatBuffer);
                glBindVertexArray(0);
                MemoryUtil.memFree(floatBuffer);
            }
    }

    /**
     * Changes the color data if allowed
     * @param newColorData is the new color data to replace the old
     *                     Cannot have a different number of vertices, use a different mesh for that
     *                     Vertex order is preserved
     */
    public void changeColorData(float[] newColorData) {
        if(!colorValuesChange)
            throw new IllegalArgumentException("Cannot change color data for a non-dynamic color mesh");
        if(newColorData.length != colors.length)
            throw new IllegalArgumentException("Cannot change length of color data");

        colors = newColorData;
        //update GPU data
        if(isOnGpu)
            for(ShaderAttribute dsa: dynamicVboIds.keySet()) {
                if(!dsa.dependantOnColorData())
                    continue;
                float[] data = dsa.getData(this);
                FloatBuffer floatBuffer = MemoryUtil.memAllocFloat(data.length);
                floatBuffer.put(data).flip();
                glBindVertexArray(vaoId);
                glNamedBufferSubData(dynamicVboIds.get(dsa), 0, floatBuffer);
                glBindVertexArray(0);
                MemoryUtil.memFree(floatBuffer);
            }
    }

    /**
     * A method for setting the transformation matrix of the mesh
     * For multi-matrix operations (such as rotation around some point), operations /should/ be done in a shader or extended class
     * Some simple operations are built in, but any further complicated transformations must be implemented using
     *      getTransformationMatrix in combination with this method
     * @param matrix is the matrix to set the transformation matrix to
     */
    public void setTransformationMatrix(float[] matrix) {
        if(matrix.length != transformationMatrix.length)
            throw new IllegalArgumentException("Cannot set the transformation matrix to a non-4x4 matrix, expected: 16, given: "+matrix.length);
        this.transformationMatrix = matrix;
    }

    public float[] getTransformationMatrix() {
        return this.transformationMatrix;
    }

    /**
     * Shortcut function to change the position of the entire mesh
     * @param vec3position is the position to move the mesh to
     */
    public void translateBy(float[] vec3position) {
        for(int x = 0; x < 3; x++)
            this.transformationMatrix[matOffset(3, x,4)] += vec3position[x];
    }

    /**
     * Shortcut function to change the position of the entire mesh
     * @param vec3position is the position to move the mesh to
     */
    public void translateTo(float[] vec3position) {
        for(int x = 0; x < 3; x++)
            this.transformationMatrix[matOffset(3, x,4)] = vec3position[x];
    }

    /**
     * Shortcut function to (uniformly) change the size of the entire mesh
     * @param scalingFactor is how much the mesh should be scaled to
     */
    public void scaleBy(float scalingFactor) {
        for(int x = 0; x < 3; x++)
            this.transformationMatrix[matOffset(x, x,4)] *= scalingFactor;
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
    public float[] getVertexPositions() {
        return vertexPositions;
    }

    /**
     * @return the color of each vertex as a flat array
     */
    public float[] getColors() {
        return colors;
    }

    public float[] getNormals() {
        return normals;
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

    public boolean isMeshBlended() {
        return blendColors;
    }

    public void setBlend(boolean blend) {
        this.blendColors = blend;
    }
}
