package com.teamlightbox.appframe.mesh;

import com.teamlightbox.appframe.shader.Shader;
import com.teamlightbox.appframe.util.Color;

import java.util.Arrays;
import org.lwjgl.opengl.GL42;

/**
 * Class for creating meshes
 */
public class MeshBuilder {

    /**
     * DEFAULT_COLOR    the default color of the mesh if no color was specified
     * positions        the float array of position data (see Mesh.java)
     * colors           the float array of color data (see Mesh.java)
     * indices          the int array of index order (see Mesh.java)
     * useAlpha         a bool telling the builder to expect alpha information with the color input
     */
    private static Color DEFAULT_COLOR = Color.WHITE;
    private float[] positions, colors;
    private int[] indices;
    private boolean useAlpha = false;
    private Shader shader;

    public MeshBuilder(){
    }

    /**
     * Creates the mesh from given data and throws an error if some critical data is missing
     * All non-critical, unspecified data will be filled with defaults
     * @return the new mesh build with the previously given data
     */
    public Mesh build(){
        if(positions == null)
            throw new IllegalStateException("Positions must be set");
        if(indices == null)
            throw new IllegalStateException("Indices must be set");
        if(colors == null) {
            Color[] defaultColors = new Color[positions.length];
            Arrays.fill(defaultColors, DEFAULT_COLOR);
            setColors(defaultColors);
        }
        if(shader == null)
            shader = Shader.PASSTHROUGH;
        Mesh mesh = new Mesh(GL42.GL_TRIANGLES, positions, colors, indices);
        mesh.useShader(shader);
        return mesh;
    }

    /**
     * Sets the positions vector with a 1d list of 3d coordinates (see Mesh.java for detail about positions)
     * @param positions is the float array to set the positions to
     * @return self for chaining
     */
    public MeshBuilder setPositions(float[] positions) {
        this.positions = positions;
        return this;
    }

    /**
     * Sets the color vector with a 1d list of 3d colors (r, g, b) (see Mesh.java for detail about colors)
     * Can also use 4d colors (rgba) if useAlphaData is called
     * Defaults to DEFAULT_COLOR
     * @param colors is the float array to set the colors to
     * @return self for chaining
     */
    public MeshBuilder setColors(float[] colors) {
        if(useAlpha)
            this.colors = colors;
        else {
            float[] trueColors = new float[colors.length + (colors.length / 3)]; // make space for alpha
            for(int x = 0, y=0; x < colors.length; x++){
                trueColors[y] = colors[x];
                y++;
                if(x % 3 == 2) {
                    trueColors[y] = 1.0f;
                    y++;
                }
            }
            this.colors = trueColors;
        }
        return this;
    }

    /**
     * Sets the color vector based off of a given array of Colors
     * Each index in Color[] corresponds to 1 vertex at the same index
     * Defaults to DEFAULT_COLOR
     * @param colors is the Color array to process
     * @return self for chaining
     */
    public MeshBuilder setColors(Color[] colors) {
        float[] cs = new float[colors.length * 4];
        if(useAlpha) {
            for (int x = 0; x < colors.length; x++) {
                cs[4 * x] = colors[x].r;
                cs[4 * x + 1] = colors[x].g;
                cs[4 * x + 2] = colors[x].b;
                cs[4 * x + 3] = colors[x].a;
            }
        } else {
            for (int x = 0; x < colors.length; x++) {
                cs[4 * x] = colors[x].r;
                cs[4 * x + 1] = colors[x].g;
                cs[4 * x + 2] = colors[x].b;
                cs[4 * x + 3] = 1.0f;
            }
        }
        this.colors = cs;
        return this;
    }

    /**
     * Sets the entire mesh to this color
     * To call this method, you must already know how many vertices are in this mesh,
     *   so ** positions must be set ** before use, otherwise it will error
     * Defaults to DEFAULT_COLOR
     * @param r is the r value to set the mesh color to
     * @param g is the g value to set the mesh color to
     * @param b is the b value to set the mesh color to
     * @return self for chaining
     */
    public MeshBuilder setColor(float r, float g, float b) {
        if(positions == null)
            throw new IllegalStateException("Positions must be set before using this method");
        float[] color = new float[positions.length * 4];
        for(int x = 0; x < positions.length; x++){
            color[4 * x] = r;
            color[4 * x + 1] = g;
            color[4 * x + 2] = b;
            color[4 * x + 3] = 1.0f;
        }
        this.colors = color;
        return this;
    }

    /**
     * Sets the entire mesh to this color
     * To call this method, you must already know how many vertices are in this mesh,
     *   so ** positions must be set ** before use, otherwise it will error
     * Defaults to DEFAULT_COLOR
     * @param r is the r value to set the mesh color to
     * @param g is the g value to set the mesh color to
     * @param b is the b value to set the mesh color to
     * @param a is the alpha value to set the mesh to
     * @return self for chaining
     */
    public MeshBuilder setColor(float r, float g, float b, float a) {
        if(positions == null)
            throw new IllegalStateException("Positions must be set before using this method");
        float[] color = new float[positions.length * 4];
        for(int x = 0; x < positions.length; x++){
            color[4 * x] = r;
            color[4 * x + 1] = g;
            color[4 * x + 2] = b;
            color[4 * x + 3] = a;
        }
        this.colors = color;
        return this;
    }

    /**
     * Sets the entire mesh to this color
     * To call this method, you must already know how many vertices are in this mesh,
     *   so ** positions must be set ** before use, otherwise it will error
     * Defaults to DEFAULT_COLOR
     * @param color is the color to set the mesh to
     * @return self for chaining
     */
    public MeshBuilder setColor(Color color) {
        if(useAlpha)
            return setColor(color.r, color.g, color.b, color.a);
        return setColor(color.r, color.g, color.b);
    }

    /**
     * Sets the indices vector for determining vertex order when being drawn
     * @param indices is the array to set the index vector to
     * @return self for chaining
     */
    public MeshBuilder setIndices(int[] indices) {
        this.indices = indices;
        return this;
    }

    /**
     * Tells the builder to use/expect alpha color information in color inputs
     * MUST BE CALLED BEFORE SETTING COLOR DATA
     * @return self for chaining
     */
    public MeshBuilder useAlphaData() {
        this.useAlpha = true;
        return this;
    }

    /**
     * Tells the mesh to use a given shader
     * @param shader is the shader for the mesh to use
     * @return self for chaining
     */
    public MeshBuilder useShader(Shader shader) {
        this.shader = shader;
        return this;
    }


    /**
     * Allows the changing of the default color for meshes with unspecified colors
     * @param color is the color to set as the default color
     */
    public static void setDefaultColor(Color color){
        DEFAULT_COLOR = color;
    }
}
