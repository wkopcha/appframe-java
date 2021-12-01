package com.teamlightbox.appframe.test;

import com.teamlightbox.appframe.Appframe;
import com.teamlightbox.appframe.mesh.Mesh;
import com.teamlightbox.appframe.mesh.MeshBuilder;
import com.teamlightbox.appframe.mesh.MeshTools;
import com.teamlightbox.appframe.shader.PassThroughShader;
import com.teamlightbox.appframe.shader.Shader;
import com.teamlightbox.appframe.shader.ShaderAttribute;
import com.teamlightbox.appframe.util.Color;

import com.teamlightbox.appframe.util.FileRead;
import com.teamlightbox.appframe.util.Tools;
import org.lwjgl.glfw.GLFW;

public class Game {

    private boolean color = false, prevSpaceState = false, blend = true, prevBState = false, loaded = true, prevNState = false;

    private Mesh rect, triangle;
    private final Color triColorA = new Color(1, 0, 1, 0.5f);
    private final Color triColorB = new Color(0, 1, 1, 0.2f);
    private final int[] triIdx = new int[]{0,1,2};
    private final float[] triPosA = new float[]{
            0.7f, 0.2f, 1f,
            0, 0f, 0f,
            1f, -0.2f, 1f
    };
    private final float[] triPosB = new float[]{
            0.7f, 0.2f, 0f,
            0, 0f, 0.5f,
            1f, -0.2f, 1f
    };
    private Shader normalShader;

    public void init(Appframe appframe) {
        try {
            normalShader = new Shader(
                    FileRead.readResource("vshader.vert"),
                    FileRead.readResource("fshader.frag"),
                    ShaderAttribute.POSITION, ShaderAttribute.COLOR, ShaderAttribute.VERTEX_NORMAL
            );
        } catch (Exception e) {
            normalShader = PassThroughShader.get();
        }

        rect = new MeshBuilder()
                .setPositions(new float[]{
                        -0.5f,  0.5f, 0.5f,
                        -0.5f, -0.5f, 0.5f,
                        0.5f,  0.5f, 0.5f,
                        0.5f, -0.5f, 0.5f,
                })
                .setIndices(new int[]{
                        0, 1, 2, 2, 1, 3
                })
                .setColors(new float[]{
                        0.5f, 0f, 0f, 1f,
                        0f, 0.5f, 0f, 1f,
                        0f, 0f, 0.5f, 1f,
                        0f, 0.5f, 0.5f, 1f
                })
                .useShader(normalShader)
                .build();
        rect.gpuLoad();
        appframe.addMeshToRenderQueue(rect);

        triangle = new MeshBuilder()
                .setPositions(triPosA)
                .setIndices(triIdx)
                .setColors(new Color[]{
                        triColorA,
                        triColorA,
                        triColorB
                })
                //.dynamicColors()
                .dynamicPositions()
                .useShader(normalShader)
                .build();
        triangle.gpuLoad();
        appframe.addMeshToRenderQueue(triangle);
    }

    public void tick(Appframe appframe) {
        if(appframe.keyPressed(GLFW.GLFW_KEY_ESCAPE))
            appframe.close();
        if(appframe.keyPressed(GLFW.GLFW_KEY_SPACE)) {
            if(!prevSpaceState) {
                if (loaded) {
                    triangle.gpuFree();
                    appframe.removeMeshFromRenderQueue(triangle);
                }
                else {
                    triangle.gpuLoad();
                    appframe.addMeshToRenderQueue(triangle);
                }
                loaded = !loaded;
                prevSpaceState = !prevSpaceState;
            }
        } else
            prevSpaceState = false;

        if(appframe.keyPressed(GLFW.GLFW_KEY_N)) {
            if(!prevNState) {
                if (!color) {
                    triangle.changePositionData(triPosA, MeshTools.calculateNormals(triPosA, triIdx));
                    //triangle.changeColorData(Tools.getMeshColorArray(triangle, triColorB));
                }
                else {
                    triangle.changePositionData(triPosB, MeshTools.calculateNormals(triPosB, triIdx));
                    //triangle.changeColorData( Tools.getMeshColorArray(triangle, triColorA));
                }
                color = !color;
                prevNState = !prevNState;
            }
        } else
            prevNState = false;

        if(appframe.keyPressed(GLFW.GLFW_KEY_B)) {
            if(!prevBState) {
                triangle.setBlend(blend);
                blend = !blend;
                prevBState = !prevBState;
            }
        } else
            prevBState = false;
    }

    public void end(Appframe appframe) {
        rect.cleanup();
        triangle.cleanup();
        normalShader.cleanup();
    }
}
