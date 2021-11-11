package com.teamlightbox.appframe;

import com.teamlightbox.appframe.util.IUsesNativeMemory;

import static org.lwjgl.opengl.GL42.*;

/**
 * Class for creating and storing a shader program
 */
public class ShaderProgram implements IUsesNativeMemory {

    /**
     * programId            the id of the shader program
     * vertexShaderId       the id of the vertex shader
     * fragmentShaderId     the id of the fragment shader
     */
    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    /**
     * Creates a shader program
     * @throws Exception if it fails to create a shader
     */
    public ShaderProgram() throws Exception {
        programId = glCreateProgram();
        if(programId == GL_FALSE)
            throw new Exception("Could not create Shader");
    }

    /**
     * Creates & compiles the vertex shader
     * @param shaderCode is the code of the vertex shader as a string
     * @throws Exception if there's an error creating the shader
     */
    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    /**
     * Creates & compiles the fragment shader
     * @param shaderCode is the code of the fragment shader as a string
     * @throws Exception if there's an error creating the shader
     */
    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    /**
     * Creates & compiles a shader of the shader type from the given shader code
     * @param shaderCode is the code to compile for the shader
     * @param shaderType is the type of shader being compiled
     * @return the id of the compiled shader
     * @throws Exception if there is an error creating the shader
     */
    public int createShader(String shaderCode, int shaderType) throws Exception {
        // creates the shader with the given shader type
        int shaderId = glCreateShader(shaderType);
        if(shaderId == GL_FALSE)
            throw new Exception("Error creating shader type: " + shaderType);

        // compiles the shader code for the shader type
        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE)
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));

        // attach shader to the program
        glAttachShader(programId, shaderId);

        return shaderId;
    }

    /**
     * Links the shaders to the shader program, then detach the shaders after successful linking
     * @throws Exception if there is a problem linking the shaders to the program
     */
    public void link() throws Exception {
        // link shaders
        glLinkProgram(programId);
        if(glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE)
            throw new Exception("Error linking shader code: " + glGetProgramInfoLog(programId, 1024));

        // if the vertex shader exists, detach the shader
        if(vertexShaderId != GL_FALSE)
            glDetachShader(programId, vertexShaderId);

        // if the fragment shader exists, detach the shader
        if(fragmentShaderId != GL_FALSE)
            glDetachShader(programId, fragmentShaderId);

        // check if everything's good
        glValidateProgram(programId);
        if(glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE)
            System.err.println("Warning validating shader code: " + glGetProgramInfoLog(programId, 1024));
    }

    /**
     * Binds this shader program
     */
    public void bind(){
        glUseProgram(programId);
    }

    /**
     * Unbinds this shader program
     */
    public void unbind(){
        glUseProgram(0);
    }

    /**
     * Cleanup the shader by deleting all existing shaders in the program
     */
    public void cleanup(){
        unbind();
        if(programId != 0)
            glDeleteProgram(programId);

        if(vertexShaderId != 0)
            glDeleteShader(vertexShaderId);

        if(fragmentShaderId != 0)
            glDeleteShader(fragmentShaderId);
    }
}
