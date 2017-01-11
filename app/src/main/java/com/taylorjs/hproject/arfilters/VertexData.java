/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taylorjs.hproject.arfilters;

/**
 * Contains vertex, normal and color data.
 */
public final class VertexData {

  public static final int FACE_COORD_DIMENSION = 2;
  public static final float[] FACE_COORDS = new float[] {
          // Front direct
          -1.0f, -1.0f,
          -1.0f, 1.0f,
          1.0f, -1.0f,
          1.0f, 1.0f,
  };

  public static final int FACE_TEX_COORD_DIMENSION = 3;
  public static final float[] FACE_TEX_COORDS = new float[] {
          // Front direct
          0.0f, 1.0f, 1.0f,
          0.0f, 0.0f, 1.0f,
          1.0f, 1.0f, 1.0f,
          1.0f, 0.0f, 1.0f,
  };

  public static final int FACE_NUMBER_VERTICES = FACE_COORDS.length/FACE_COORD_DIMENSION;
}
