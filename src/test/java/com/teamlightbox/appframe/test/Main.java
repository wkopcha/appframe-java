package com.teamlightbox.appframe.test;

import com.teamlightbox.appframe.Appframe;

public class Main {

    public static void main(String[] args){
        Appframe.Properties gameProperties = new Appframe.Properties()
                .setWindowName("Appframe Test")
                .setFPSLimit(1/30.0);
        Appframe game = new Appframe(gameProperties, () -> {});
    }
}
