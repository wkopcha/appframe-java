package com.teamlightbox.appframe;

import com.teamlightbox.appframe.mesh.Mesh;
import com.teamlightbox.appframe.util.Color;
import com.teamlightbox.appframe.util.FileRead;
import com.teamlightbox.appframe.util.IUsesNativeMemory;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
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
public abstract class Appframe {

    /**
     * windowHandle     The identifier for OpenGL to find the window
     * properties       The properties of the AppFrame
     */
    private long windowHandle;
    private final Properties properties;

    /**
     * shaderProgram    To be removed and wrapped into some sort of mesh or meta-mesh class,
     *      we'll see; currently handles the shaders used
     * meshesToRender   Linked List of meshes that are rendered each frame
     */
    ShaderProgram shaderProgram;
    LinkedList<Mesh> meshesToRender = new LinkedList<>();

    /**
     * Creates the appframe. Must be called by subclasses
     * @param properties are the properties of the appframe (see Appframe.Properties class)
     */
    public Appframe(Properties properties) {
        this.properties = properties;

        try {
            init();
            appInit();
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
     * @throws Exception if something goes wrong somewhere, which is very possible
     */
    private void init() throws Exception {
        GLFWErrorCallback.createPrint(System.err).set();

        System.out.println("Initializing...");

        // initialize GLFW
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // get some basic GLFW initializations
        // @Todo read up on what these window hints do
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        if (properties.allowResize)
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        else
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will not be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4); // I think this is version = OpenGL4.2
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3); // I think this is min version = OpenGL3.0
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // ????
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // I think this allows newer versions of OpenGL to work with this

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
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glDepthFunc(GL_LESS);
        glDepthRange(0.0f, 1.0f);


        // Creating a default shader program
        // @todo Move shader creation elsewhere
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(FileRead.readResource("./shaders/vertex.vert"));
        shaderProgram.createFragmentShader(FileRead.readResource("./shaders/fragment.frag"));
        shaderProgram.link();

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
                tickLogic();
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
                System.out.println("FPS: " + framesSinceUpdate + " Updates: " + updatesSinceUpdate);
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
        System.out.println("Cleaning up...");

        meshesToRender.forEach(Mesh::cleanup);

        if(shaderProgram != null)
            shaderProgram.cleanup();
    }

    /**
     * Sets the default color for the cleared screen
     * @param color is the color the cleared screen will be set to
     */
    public void setClearColor(Color color){
        glClearColor(color.r, color.g, color.b, color.a);
    }

    /**
     * Method that updates application logic every tick
     */
    protected abstract void tickLogic();

    /**
     * Method that runs on initialization of the app (after initialization of OpenGL & GLFW)
     */
    protected abstract void appInit();

    /**
     * Clears the screen and renders all meshes in meshesToRender
     */
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Clear framebuffer


        shaderProgram.bind();

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

        meshesToRender.forEach((Mesh mesh) -> {
            // draw
            glBindVertexArray(mesh.getVaoId()); // use mesh's vertex array object
            glEnableVertexAttribArray(0); // enable position
            glEnableVertexAttribArray(1); // enable color
            glDrawElements(mesh.getDrawMode(), mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

            // restore
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glBindVertexArray(0);
        });

        shaderProgram.unbind();

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
    public void addMeshToRendering(Mesh mesh) {
        meshesToRender.add(mesh);
    }

    /**
     * Removes (and potentially cleans-up) a mesh from the rendering list
     * @param mesh is the mesh to remove from the rendering list
     * @param finishedWithMesh tells if the object will not be used again (true) or if it may be used again (false)
     */
    public void removeMeshFromRendering(Mesh mesh, boolean finishedWithMesh) {
        meshesToRender.remove(mesh);
        if(finishedWithMesh)
            mesh.cleanup();
    }

    /**
     * Properties class for Appframe creation
     */
    public static class Properties {

        /**
         * clearColor   The color that the windows refreshes as
         * vsyncEnable  Enable vsync?
         * allowResize  Allow the manual resizing of the window?
         * initWidth    The initial width of the window
         * windowName   The title of the window
         * fpsLimit     Maximum times per second the window's logic will update. Measured in seconds/frame
         */
        private Color clearColor = Color.BLACK;
        private boolean vsyncEnable = true, allowResize = false;
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
    }
}
