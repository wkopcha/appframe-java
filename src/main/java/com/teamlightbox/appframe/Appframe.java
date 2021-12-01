package com.teamlightbox.appframe;

import com.teamlightbox.appframe.mesh.Mesh;
import com.teamlightbox.appframe.shader.PassThroughShader;
import com.teamlightbox.appframe.shader.Shader;
import com.teamlightbox.appframe.util.Color;
import com.teamlightbox.appframe.util.Logger;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.HashMap;
import java.util.LinkedList;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Main class the begins and manages most of the OpenGL nastiness
 * Should be extended by the application class and instantiated to be used
 */
public class Appframe {

    /**
     * windowHandle     The identifier for OpenGL to find the window
     * properties       The properties of the AppFrame
     * appInitFunc      The function that runs when the AppFrame is begun
     * loopTickFunc     A function that runs every tick, meant to update logic
     */
    private long windowHandle;
    private final Properties properties;
    private final appFunction appInitFunc, loopTickFunc, finalFunc;

    /**
     * renderQueue      Map of shaders to linked list of meshes using that shader
     */
    HashMap<Shader, LinkedList<Mesh>> renderQueue = new HashMap<>();

    /**
     * Creates the appframe. Must be called by subclasses
     * @param properties are the properties of the appframe (see Appframe.Properties class)
     * @param initFunc is the function that should run before the loop but after the window initialization
     *                 Used to set default state of the Appframe
     * @param tickFunc is a function that runs every tick, meant for updating application logic
     * @param finalFunc is a function run at the end of the window's life
     */
    public Appframe(Properties properties, appFunction initFunc, appFunction tickFunc, appFunction finalFunc) {
        this.properties = properties;
        this.appInitFunc = initFunc;
        this.loopTickFunc = tickFunc;
        this.finalFunc = finalFunc;
    }

    /**
     * Runs the app
     */
    public void begin() {
        try {
            init();
            appInitFunc.call(this);
            loop();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }

        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
    }

    /**
     * Initializes the OpenGL stuff
     */
    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        Logger.log("Initializing...");

        // initialize GLFW
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // get some basic GLFW initializations
        setGLFWHints();

        createWindow();

        // !! sets the OpenGL context to the window
        glfwMakeContextCurrent(windowHandle);

        //Enable vsync
        if (properties.vsyncEnable)
            glfwSwapInterval(1);

        glfwShowWindow(windowHandle);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the default color for a blank window
        glClearColor(properties.clearColor.r, properties.clearColor.g, properties.clearColor.b, 0f);

        // when alpha blending is enabled, use the function dest.a = 1 - src.a
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Enable gl depth testing on a scale of [0,1] where 0 is closer
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LESS);
        glDepthRange(0.0f, 1.0f);

        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);

    }

    /**
     * Method that sets the GLFW hints for the window
     * Protected so that implementations can override this method to set their own GLFWHints
     * if they do not like the ones listed here
     */
    protected void setGLFWHints() {
        // stuff of nightmares
        // https://www.glfw.org/docs/3.3/window_guide.html#window_hints
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        if (properties.allowResize)
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        else
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will not be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // I think these 2 hints specify that the window
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2); // must be compatible with OpenGL 3.2
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Tells OpenGL to use the core profile
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // Allows newer versions of OpenGL to work with this
    }

    /**
     * Creates the glfw window
     */
    private void createWindow() {
        // Creates the window for OpenGL to use, no default monitor/sharing
        windowHandle = glfwCreateWindow(properties.initWidth, properties.initHeight, properties.windowName, NULL, NULL);
        if (windowHandle == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // I think this is setting up where the window will appear
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int* pWidth
            IntBuffer pHeight = stack.mallocInt(1); // int* pHeight

            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidMode == null)
                throw new RuntimeException("Failed to get Video Mode from Primary Monitor");

            glfwSetWindowPos(
                    windowHandle,
                    (vidMode.width() - pWidth.get(0)) / 2,
                    (vidMode.height() - pHeight.get(0)) / 2
            );
        } // stack frame is popped automatically apparently
    }

    /**
     * Contains the program loop
     */
    private void loop() {
        // Init variables for timing & debug
        double lastTime = glfwGetTime();
        double timer = lastTime;
        double timeToNextUpdate = 0, curTime;
        int framesSinceUpdate = 0, updatesSinceUpdate = 0;

        // while window is open
        while (!glfwWindowShouldClose(windowHandle)) {
            // save time, calculate time until next update, update last time
            curTime = glfwGetTime();
            timeToNextUpdate += (curTime - lastTime) / properties.fpsLimit;
            lastTime = curTime;

            // while the time until next update is
            while (timeToNextUpdate >= 1) {
                //tickLogic();
                loopTickFunc.call(this);
                updatesSinceUpdate++;
                timeToNextUpdate--;

                //Poll for window events. The key callback will only be invoked during this call
                glfwPollEvents();
            }

            render();
            framesSinceUpdate++;

            // this is for outputting fps every 1.0 seconds
            if (glfwGetTime() - timer > 1.0) {
                timer++;
                Logger.log("FPS: " + framesSinceUpdate + " Updates: " + updatesSinceUpdate);
                updatesSinceUpdate = 0;
                framesSinceUpdate = 0;
            }
        }
    }

    /**
     * Final method to call before end of use
     * Cleans up all resources (this has access to) that needs cleaning up
     */
    private void cleanup() {
        Logger.log("Cleaning up...");

        finalFunc.call(this);

        if(PassThroughShader.get() != null)
            PassThroughShader.get().cleanup();

        for(Shader s: renderQueue.keySet()){
            renderQueue.get(s).forEach(Mesh::cleanup);
            s.cleanup();
        }

        Logger.log("Done Cleaning");
    }

    /**
     * Sets the default color for the cleared screen
     * @param color is the color the cleared screen will be set to
     */
    public void setClearColor(Color color){
        glClearColor(color.r, color.g, color.b, color.a);
    }

    /**
     * Clears the screen and renders all meshes in meshesToRender
     */
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Clear framebuffer

        /* @Todo Properly Handle resizing the screen with different modes
         *
         * Adapt this code to work:
         * if (window.isResized()) {
         *     glViewport(0, 0, window.getWidth(), window.getHeight());
         *     window.setResized(false);
         * }
         *
         * Allow for different resize handling modes:
         *  - Stretch
         *  - Add black bars
         *  - Expand area
         */
        for(Shader s: renderQueue.keySet()) {
            s.bind();

            renderQueue.get(s).forEach((Mesh mesh) -> {
                if(!mesh.isOnGpu())
                    throw new IllegalStateException("Cannot render mesh that is not on GPU");

                glBindVertexArray(mesh.getVaoId());

                if(mesh.isMeshBlended())
                    glEnable(GL_BLEND);

                glDrawElements(mesh.getDrawMode(), mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

                if(mesh.isMeshBlended())
                    glDisable(GL_BLEND);

                glBindVertexArray(0);
            });

            s.unbind();
        }

        glfwSwapBuffers(windowHandle); // swap color buffers
    }

    /**
     * Closes the window (& subsequently OpenGL application
     */
    public void close() {
        glfwSetWindowShouldClose(windowHandle, true);
    }

    /**
     * @param keyCode is the GLFW key code being tested
     * @return true if the key from keyCode is pressed, false if not
     */
    public boolean keyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    /**
     * Adds a mesh to render
     * @param mesh is the mesh being added to render
     */
    public void addMeshToRenderQueue(Mesh mesh) {
        Shader s = mesh.getShader();
        if(properties.meshManage)
            mesh.gpuLoad();
        if(!renderQueue.containsKey(s))
            renderQueue.put(s, new LinkedList<>());
        renderQueue.get(s).add(mesh);
    }

    /**
     * Removes (and potentially cleans-up) a mesh from the rendering list
     * @param mesh is the mesh to remove from the rendering list
     */
    public void removeMeshFromRenderQueue(Mesh mesh) {
        Shader s = mesh.getShader();
        if(properties.meshManage)
            mesh.gpuFree();
        if(renderQueue.containsKey(s))
            renderQueue.get(s).remove(mesh);
    }

    /**
     * Properties class for Appframe creation
     */
    public static class Properties {

        /**
         * clearColor   The color that the windows refreshes as
         * vsyncEnable  Enable vsync?
         * allowResize  Allow the manual resizing of the window?
         * meshManage   Tells the app if it should automatically load and unload meshes from the GPU or not
         *              true means less chance of a memory leak, but false means potentially better performance
         * initWidth    The initial width of the window
         * windowName   The title of the window
         * fpsLimit     Maximum times per second the window's logic will update. Measured in seconds/frame
         */
        private Color clearColor = Color.BLACK;
        private boolean vsyncEnable = true, allowResize = false, meshManage = true;
        private int initWidth = 1600, initHeight = 900;
        private String windowName = "Appframe Application";
        private double fpsLimit = 1d / 60d; // Default 60fps

        public Properties() {
        }

        public Properties setClearColor(Color clearColor) {
            this.clearColor = clearColor;
            return this;
        }

        public Properties setVsyncEnable(boolean vsyncEnable) {
            this.vsyncEnable = vsyncEnable;
            return this;
        }

        public Properties setAllowResize(boolean allowResize) {
            this.allowResize = allowResize;
            return this;
        }

        public Properties setInitWidth(int width) {
            this.initWidth = width;
            return this;
        }

        public Properties setInitHeight(int height) {
            this.initHeight = height;
            return this;
        }

        public Properties setWindowName(String name) {
            this.windowName = name;
            return this;
        }

        public Properties setFPSLimit(double secondsPerFrame) {
            this.fpsLimit = secondsPerFrame;
            return this;
        }

        public Properties shouldManageMeshGPU(boolean management) {
            this.meshManage = management;
            return this;
        }
    }

    public interface appFunction {
        void call(Appframe appframe);
    }
}
