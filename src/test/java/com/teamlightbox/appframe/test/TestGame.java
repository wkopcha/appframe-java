package com.teamlightbox.appframe.test;

import com.teamlightbox.appframe.Appframe;
import com.teamlightbox.appframe.mesh.Mesh;
import com.teamlightbox.appframe.mesh.MeshBuilder;
import com.teamlightbox.appframe.util.Color;
import com.teamlightbox.appframe.util.Tools;
import org.lwjgl.glfw.GLFW;

public class TestGame extends Appframe {

    public TestGame(Properties properties){
        super(properties);
    }

    boolean color = false, prevSpaceState = false, blend = true, prevBState = false, loaded = true, prevNState = false;

    Mesh rect, triangle;
    Color triColorA = new Color(1, 0, 1, 0.5f);
    Color triColorB = new Color(0, 1, 1, 0.2f);

    @Override
    protected void appInit() {

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
        addMeshToRenderQueue(rect);

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
        addMeshToRenderQueue(triangle);
    }

    @Override
    protected void tickLogic() {
        if(keyPressed(GLFW.GLFW_KEY_ESCAPE))
            close();
        if(keyPressed(GLFW.GLFW_KEY_SPACE)) {
            if(!prevSpaceState) {
                if (loaded) {
                    triangle.gpuFree();
                    removeMeshFromRenderQueue(triangle);
                }
                else {
                    triangle.gpuLoad();
                    addMeshToRenderQueue(triangle);
                }
                loaded = !loaded;
                prevSpaceState = !prevSpaceState;
            }
        } else
            prevSpaceState = false;

        if(keyPressed(GLFW.GLFW_KEY_N)) {
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

        if(keyPressed(GLFW.GLFW_KEY_B)) {
            if(!prevBState) {
                triangle.setBlend(blend);
                blend = !blend;
                prevBState = !prevBState;
            }
        } else
            prevBState = false;
    }

}
