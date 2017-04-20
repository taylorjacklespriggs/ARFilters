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

package com.arfilters;

import android.util.Log;

public class RuntimeStatistics {
    private static final String TAG = RuntimeStatistics.class.getName();

    public RuntimeStatistics(int nFrames) {
        maximumFrames = nFrames;
        reset();
    }
    private void reset() {
        sum = 0;
        squaredSum = 0;
        minimumTime = 0;
        maximumTime = 0;
        startTime = null;
        numberOfFrames = 0;
    }
    public void logAndReset() {
        double avg = sum/numberOfFrames, stdDev = squaredSum/numberOfFrames;
        stdDev -= avg*avg;
        stdDev = Math.sqrt(stdDev);
        Log.i(TAG, String.format("min max avg stdev : %.4f %.4f %.4f %.4f",
                minimumTime, maximumTime, avg, stdDev));
        reset();
    }
    public void onNewFrame() {
        startTime = System.nanoTime();
    }
    public void onFinishedFrame() {
        if(startTime != null) {
            startTime = System.nanoTime() - startTime;
            double ms = startTime / 1000000.;
            if (sum == 0) {
                minimumTime = maximumTime = ms;
            } else {
                if (ms < minimumTime)
                    minimumTime = ms;
                if (ms > maximumTime)
                    maximumTime = ms;
            }
            sum += ms;
            squaredSum += ms * ms;
            ++numberOfFrames;
            if (numberOfFrames >= maximumFrames)
                logAndReset();
        }
    }
    private Long startTime;
    private double sum, squaredSum, minimumTime, maximumTime;
    private final int maximumFrames;
    private int numberOfFrames;
}
