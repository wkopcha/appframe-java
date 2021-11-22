package com.teamlightbox.appframe.test;

import com.teamlightbox.appframe.Appframe;
import com.teamlightbox.appframe.mesh.Mesh;
import com.teamlightbox.appframe.mesh.MeshBuilder;
import com.teamlightbox.appframe.util.Color;

import com.teamlightbox.appframe.util.Tools;
import org.lwjgl.glfw.GLFW;

public class Game {

    private boolean color = false, prevSpaceState = false, blend = true, prevBState = false, loaded = true, prevNState = false;

    private Mesh rect, triangle;
    private final Color triColorA = new Color(1, 0, 1, 0.5f);
    private final Color triColorB = new Color(0, 1, 1, 0.2f);

    public void init(Appframe appframe) {
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
                .build();
        rect.gpuLoad();
        appframe.addMeshToRenderQueue(rect);

        triangle = new MeshBuilder()
                .setPositions(new float[]{
                        0.7f, 0.2f, 1f,
                        0, 0f, 0f,
                        1f, -0.2f, 1f
                })
                .setIndices(new int[]{0,1,2})
                .setColors(new Color[]{
                        triColorA,
                        triColorA,
                        triColorB
                })
                .dynamicColors()
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
                    triangle.changeColorData(Tools.getMeshColorArray(triangle, triColorB));
                }
                else {
                    triangle.changeColorData( Tools.getMeshColorArray(triangle, triColorA));
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
}
