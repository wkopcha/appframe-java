package com.teamlightbox.appframe.shader;

import com.teamlightbox.appframe.util.FileRead;
import com.teamlightbox.appframe.util.Logger;

/**
 * Basic (built-in) shader that just outputs a vertex's given position and color, nothing fancy
 */
public class PassThroughShader {

    private static Shader SHADER = null;

    private static void initPassThrough() {
        if(SHADER != null) {
            Logger.warn("Cannot re-initialize Pass Through Shader Singleton, please use get()");
            return;
        }

        Shader temp;
        try {
            temp = new Shader(
                    FileRead.readResource("./shaders/vertex.vert"),
                    FileRead.readResource("./shaders/fragment.frag"),
                    ShaderAttribute.POSITION, ShaderAttribute.COLOR
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not create passthrough shader!");
        }
        SHADER = temp;
    }

    public static Shader get() {
        if(SHADER == null)
            initPassThrough();
        return SHADER;
    }
}
