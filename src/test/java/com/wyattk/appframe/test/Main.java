package com.wyattk.appframe.test;

import com.wyattk.appframe.Appframe;
import com.wyattk.appframe.util.Logger;

public class Main {

    public static void main(String[] args){
        Logger.enable();
        Logger.withColor();
        Logger.verbose();
        Appframe.Properties gameProperties = new Appframe.Properties()
                .setWindowName("Appframe Test")
                .setAllowResize(true)
                .shouldManageMeshGPU(false)
                .setDebug(true);
        Game game = new Game();
        Appframe appframe = new Appframe(gameProperties, game::init, game::tick, game::end);
        appframe.begin();
    }
}
