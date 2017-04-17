/*
 * Copyright (C) 2017  Taylor Jackle Spriggs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.arfilters.filter;

import com.arfilters.shader.Shader;
import com.arfilters.shader.data.Matrix3x3Data;

class ColorblindFilter extends ColorMapFilter {

    private static class Matrix {
        static final int size = 3;
        private class Augmented {
            Augmented() {
                buf = new double[size][size*2];
                for(int i = 0; i < size; ++i) {
                    for(int j = 0; j < size; ++j) {
                        buf[i][j] = values[i][j];
                        buf[i][j+size] = i==j ? 1. : 0.;
                    }
                }
            }
            int nextNonZero(int n) {
                int i = n;
                while(buf[i][n] == 0.) ++i;
                return i;
            }
            void rowSwap(int a, int b) {
                if(a != b) {
                    for(int j = a; j < size*2; ++j) {
                        double tmp = buf[a][j];
                        buf[a][j] = buf[b][j];
                        buf[b][j] = tmp;
                    }
                }
            }
            void rowMult(int i, double v) {
                for(int j = i; j < size*2; ++j)
                    buf[i][j] *= v;
            }
            void rowSub(int l, int c, int a, int b) {
                double v = buf[a][c];
                for(int j = l; j < size*2; ++j)
                    buf[a][j] -= v*buf[b][j];
            }
            void rrDown() {
                for(int n = 0; n < size; ++n) {
                    int i = nextNonZero(n);
                    rowSwap(n, i);
                    rowMult(n, 1./buf[n][n]);
                    for(i = n+1; i < size; ++i)
                        rowSub(n, n, i, n);
                }
            }
            void rrUp() {
                for(int n = size - 1; n > 0; --n)
                    for(int i = n-1; i >= 0; --i)
                        rowSub(size, n, i, n);
            }
            double[][] result() {
                double[][] r = new double[size][size];
                for(int i = 0; i < size; ++i)
                    for(int j = 0; j < size; ++j)
                        r[i][j] = buf[i][j+size];
                return r;
            }
            double[][] buf;
        }
        Matrix(double[][] v) {
            values = v;
        }
        Matrix inverse() {
            Augmented aug = new Augmented();
            aug.rrDown();
            aug.rrUp();
            return new Matrix(aug.result());
        }
        Matrix mult(Matrix o) {
            double[][] r = new double[size][size];
            for(int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    r[i][j] = 0.;
                    for(int k = 0; k < size; ++k) {
                        r[i][j] += values[i][k]*o.values[k][j];
                    }
                }
            }
            return new Matrix(r);
        }
        Matrix add(Matrix o) {
            double[][] r = new double[size][size];
            for(int i = 0; i < size; ++i)
                for (int j = 0; j < size; ++j)
                    r[i][j] = values[i][j]+o.values[i][j];
            return new Matrix(r);
        }
        Matrix sub(Matrix o) {
            double[][] r = new double[size][size];
            for(int i = 0; i < size; ++i)
                for (int j = 0; j < size; ++j)
                    r[i][j] = values[i][j]-o.values[i][j];
            return new Matrix(r);
        }
        float[] getValues() {
            float[] vals = new float[size*size];
            int k = 0;
            for(int i = 0; i < size; ++i)
            for(int j = 0; j < size; ++j)
                    vals[k++] = (float) values[i][j];
            return vals;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            for(int i = 0; i < size; ++i) {
                sb.append('{');
                for(int j = 0; j < size; ++j) {
                    sb.append(values[i][j]);
                    if(j+1 < size)
                        sb.append(',');
                    else
                        sb.append('}');
                }
                if(i+1 < size)
                    sb.append(',');
                else
                    sb.append('}');
            }
            return sb.toString();
        }
        final double[][] values;
    }

    private static Matrix getLMSDeficitMatrix(FilterType ft) {
        switch(ft) {
            case DEUTERANOPIA:
                return new Matrix(new double[][] {
                        {1,0,0},{0.494207,0,1.24827},{0,0,1}
                });
            case PROTANOPIA:
                return new Matrix(new double[][] {
                        {0,2.02344,-2.52581},{0,1,0},{0,0,1}
                });
            case TRITANOPIA:
                return new Matrix(new double[][] {
                        {1,0,0},{0,1,0},{-0.395913,0.801109,0}
                });
        }
        return null;
    }

    private static final Matrix rgb2lms = new Matrix(new double[][] {
        {17.8824,43.5161,4.11935},{3.45565,27.1554,3.86714},{0.0299566,0.184309,1.46709}
    });
    private static final Matrix lms2rgb = rgb2lms.inverse();
    private static final Matrix err2mod = new Matrix(new double[][] {
            {0,0,0},{0.7,1,0},{0.7,0,1}
    });
    private static final Matrix identity = new Matrix(new double[][] {
            {1,0,0},{0,1,0},{0,0,1}
    });

    @Override
    public void prepareView() {
        updateColorMap(colorMap);
    }

    ColorblindFilter(Shader sh,
                     Matrix3x3Data vertMatrix,
                     VertexMatrixUpdater vmi,
                     Matrix3x3Data colorMapMat,
                     FilterType ft) {
        super(sh, vertMatrix, vmi, colorMapMat, ft.getName());
        Matrix map = err2mod;
        Matrix chain = err2mod;
        chain = chain.mult(lms2rgb);
        chain = chain.mult(getLMSDeficitMatrix(ft));
        chain = chain.mult(rgb2lms);
        map = map.sub(chain);
        map = map.add(identity);
        colorMap = map.getValues();
    }

    protected float[] colorMap;
}
