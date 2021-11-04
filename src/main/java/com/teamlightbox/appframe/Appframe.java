package com.teamlightbox.appframe;

import com.teamlightbox.appframe.util.Color;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import com.teamlightbox.appframe.util.BaseFunction;

//Resources used:
//https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter02/chapter2.html
//https://stackoverflow.com/questions/20390028/c-using-glfwgettime-for-a-fixed-time-step
public class Appframe {

    private long windowHandle;
    private final Properties properties;

    public Appframe(BaseFunction runLoopContents){
        this(new Properties(), runLoopContents);
    }

    public Appframe(Properties properties, BaseFunction runLoopContents){
        this.properties = properties;
        init();
        loop(runLoopContents);

        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
    }

    private void init(){
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        if(properties.allowResize)
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        else
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

        windowHandle = glfwCreateWindow(properties.initWidth, properties.initHeight, properties.windowName, NULL, NULL);
        if(windowHandle == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        //Close on escape
        glfwSetKeyCallback(windowHandle, (windowHandle, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(windowHandle, true);
        });

        try(MemoryStack stack = stackPush()){
            IntBuffer pWidth = stack.mallocInt(1); // int* pWidth
            IntBuffer pHeight = stack.mallocInt(1); // int* pHeight

            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if(vidMode == null)
                throw new RuntimeException("Failed to get Video Mode from Primary Monitor");

            glfwSetWindowPos(
                    windowHandle,
                    (vidMode.width() - pWidth.get(0)) / 2,
                    (vidMode.height() - pHeight.get(0)) / 2
            );
        } // stack frame is popped automatically apparently

        glfwMakeContextCurrent(windowHandle);

        //Enable vsync
        if(properties.vsyncEnable)
            glfwSwapInterval(1);

        glfwShowWindow(windowHandle);
    }

    private void loop(BaseFunction applicationTickLogic){
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glClearColor(properties.clearColor.r, properties.clearColor.g, properties.clearColor.b, 0f);

        double lastTime = glfwGetTime();
        double timer = lastTime;
        double timeToNextUpdate = 0, curTime = 0;
        int frames = 0, updates = 0;
        while(!glfwWindowShouldClose(windowHandle)) {
            curTime = glfwGetTime();
            timeToNextUpdate += (curTime - lastTime) / properties.fpsLimit;
            lastTime = curTime;

            // while the time until next update is
            while(timeToNextUpdate >= 1) {
                applicationTickLogic.call();
                updates++;
                timeToNextUpdate--;
            }

            render();
            frames++;

            if(glfwGetTime() - timer > 1.0){
                timer++;
                System.out.println("FPS: "+frames+" Updates: "+updates);
                updates = 0;
                frames = 0;
            }
        }
    }

    private void render(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); //Clear framebuffer
        glfwSwapBuffers(windowHandle); // swap color buffers

        //Poll for window events. The key callback will only be invoked during this call
        glfwPollEvents();
    }

    public static class Properties{

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
        private double fpsLimit = 1d/60d; // Default 60fps

        public Properties(){}
        public Properties setClearColor(Color clearColor){
            this.clearColor = clearColor;
            return this;
        }
        public Properties setVsyncEnable(boolean vsyncEnable){
            this.vsyncEnable = vsyncEnable;
            return this;
        }
        public Properties setAllowResize(boolean allowResize){
            this.allowResize = allowResize;
            return this;
        }
        public Properties setInitWidth(int width){
            this.initWidth = width;
            return this;
        }
        public Properties setInitHeight(int height){
            this.initHeight = height;
            return this;
        }
        public Properties setWindowName(String name){
            this.windowName = name;
            return this;
        }
        public Properties setFPSLimit(double secondsPerFrame){
            this.fpsLimit = secondsPerFrame;
            return this;
        }
    }
}
