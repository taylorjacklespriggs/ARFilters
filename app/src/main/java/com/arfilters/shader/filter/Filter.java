package com.arfilters.shader.filter;

import com.arfilters.shader.Viewinfo;
import com.google.vr.sdk.base.Eye;

/**
 * Created by taylor on 1/23/17.
 */

public interface Filter {

    void draw(Eye eye, Viewinfo vi);

}
