package com.teamlightbox.appframe.test;

import com.teamlightbox.appframe.Appframe;
import com.teamlightbox.appframe.mesh.Mesh;
import com.teamlightbox.appframe.mesh.MeshBuilder;
import com.teamlightbox.appframe.util.Color;
import org.lwjgl.glfw.GLFW;

public class TestGame extends Appframe {

    public TestGame(Properties properties){
        super(properties);
    }

    boolean color = false, prevSpaceState = false;

    Mesh rect, triangle;

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
                        0.5f, 0f, 0f,
                        0f, 0.5f, 0f,
                        0f, 0f, 0.5f,
                        0f, 0.5f, 0.5f
                })
                .build();
        rect.gpuLoad();
        addMeshToRenderQueue(rect);

        triangle = new MeshBuilder()
                .useAlphaData()
                .setPositions(new float[]{
                        0.7f, 0.2f, 1f,
                        0, 0f, 0f,
                        1f, -0.2f, 1f
                })
                .setIndices(new int[]{0,1,2})
                .setColor(new Color(1, 0, 1, 0.5f))
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
                if (!color) {
                    triangle.gpuFree();
                    removeMeshFromRenderQueue(triangle);
                }
                else {
                    triangle.gpuLoad();
                    addMeshToRenderQueue(triangle);
                }
                color = !color;
                prevSpaceState = !prevSpaceState;
            }
        } else
            prevSpaceState = false;
    }
}
