package com.teamlightbox.appframe.test;

import com.teamlightbox.appframe.Appframe;

public class Main {

    public static void main(String[] args){
        Appframe.Properties gameProperties = new Appframe.Properties()
                .setWindowName("Appframe Test")
                .setAllowResize(true)
                .shouldManageMeshGPU(false);
        Appframe game = new TestGame(gameProperties);
        game.run();
    }
}
