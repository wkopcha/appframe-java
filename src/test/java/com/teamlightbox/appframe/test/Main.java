package com.teamlightbox.appframe.test;

import com.teamlightbox.appframe.Appframe;
import com.teamlightbox.appframe.glsl.vec.Vec3;
import com.teamlightbox.appframe.util.Logger;

import java.util.Arrays;

public class Main {

    public static void main(String[] args){
        Logger.enable();
        Logger.withColor();
        Logger.verbose();
        Appframe.Properties gameProperties = new Appframe.Properties()
                .setWindowName("Appframe Test")
                .setAllowResize(true)
                .shouldManageMeshGPU(false);
        Game game = new Game();
        Appframe appframe = new Appframe(gameProperties, game::init, game::tick, game::end);
        appframe.begin();
    }
}
