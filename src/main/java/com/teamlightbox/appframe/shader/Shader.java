package com.teamlightbox.appframe.shader;

import com.teamlightbox.appframe.ShaderProgram;
import com.teamlightbox.appframe.util.IUsesNativeMemory;

import java.util.LinkedList;
import java.util.List;

/**
 * Class that stores the shader program and list of attributes used in the shader program
 * Exists to separate some more high-level/abstract ideas from the OpenGL code itself
 */
public class Shader implements IUsesNativeMemory {

    /**
     * shadeProgram     the shader program itself
     * attributes       the list of attributes used in the shader program
     */
    private final ShaderProgram shaderProgram;
    private final LinkedList<ShaderAttribute> attributes = new LinkedList<>();

    /**
     * Creates the shader from a couple of given shader files
     * @param vertexPath is the path to the vertex shader code
     * @param fragmentPath is the path to the fragment shader code
     * @throws Exception when something goes wrong making the shader program
     */
    public Shader(String vertexPath, String fragmentPath) throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(vertexPath);
        shaderProgram.createFragmentShader(fragmentPath);
        shaderProgram.link();
    }

    /**
     * Creates the shader from a couple of given shader files
     * @param vertexPath is the path to the vertex shader code
     * @param fragmentPath is the path to the fragment shader code
     * @param attributes is the attributes the shader uses (comma separated), order matters
     * @throws Exception when something goes wrong making the shader program
     */
    public Shader(String vertexPath, String fragmentPath, ShaderAttribute... attributes) throws Exception {
        this(vertexPath, fragmentPath);
        setAttributes(attributes);
    }

    /**
     * Sets the used attributes to the list of given attributes
     * @param attributes is the attributes the shader uses (comma separated), order matters
     */
    public void setAttributes(ShaderAttribute... attributes) {
        this.attributes.clear();
        this.attributes.addAll(List.of(attributes));
    }

    /**
     * @return the list of used attributes
     */
    public LinkedList<ShaderAttribute> getAttributes(){
        return attributes;
    }

    /**
     * @param attributes is the attributes to look for, comma separated
     * @return true if the shader has all the given shader attributes
     */
    public boolean hasAttributes(ShaderAttribute... attributes) {
        for(ShaderAttribute sa: attributes)
            if(!hasAttribute(sa))
                return false;
        return true;
    }

    /**
     * @param attribute is the attribute to search for
     * @return true if the shader has the given attribute
     */
    public boolean hasAttribute(ShaderAttribute attribute) {
        return attributes.contains(attribute);
    }

    /**
     * Binds the shader program in the shader
     */
    public void bind() {
        shaderProgram.bind();
    }

    /**
     * Unbinds the shader program in the shader
     */
    public void unbind() {
        shaderProgram.unbind();
    }

    /**
     * Cleans up the shader
     */
    @Override
    public void cleanup() {
        shaderProgram.cleanup();
    }
}
