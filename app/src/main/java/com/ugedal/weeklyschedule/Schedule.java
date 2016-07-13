/*
 * Copyright (c) 2015 Odin Ugedal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ugedal.weeklyschedule;

import android.widget.Toast;

public class Schedule {

    public static final int NO_WEEK_NUMBER = -0xff;
    private String title;
    private String weekNumber;
    private String dlUrl;
    private String info;
    private String fileName;


    public Schedule() {
        super();
    }

    public Schedule(String weekNumber, String title, String dlUrl, String info) {
        super();
        this.weekNumber = weekNumber;
        this.title = title;
        this.dlUrl = dlUrl;
        this.info = info;

    }

    public String getTitle() {
        return title;
    }

    public String getDlUrl() {
        return dlUrl;
    }

    public String getInfo() {
        return info;
    }

    public String getWeekNumber() {
        return weekNumber;
    }

    public String getFileName (){
        String filename = String.valueOf(this.dlUrl.hashCode());
        int i = dlUrl.lastIndexOf('.');
        if (i > 0) {
            filename += "." + dlUrl.substring(i+1);
        }

        return filename;
    }

    @Override
    public String toString() {
        return getWeekNumber() + getTitle() + getDlUrl();
    }
}
