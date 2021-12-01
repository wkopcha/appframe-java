package com.teamlightbox.appframe.glsl.util;

import java.io.Serializable;

public interface GLSLDataType<T extends Serializable> {

    T[] get();
}
