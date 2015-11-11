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

package com.ugedal.ukeplanappen;

public class Week {

    private String title;
    private int weekNumber;
    private String dlUrl;
    private String info;


    public Week() {
        super();
    }

    public Week(int weekNumber, String title, String dlUrl, String info) {
        super();
        this.weekNumber = weekNumber;
        this.title = title;
        this.dlUrl=dlUrl;
        this.info = info;

    }
    public String getTitle() {
        return title;
    }

    public String getDlUrl() {
        return dlUrl;
    }

    public String getInfo(){
		return info;
	}

    public int getWeekNumber() {
        return weekNumber;
    }

    @Override
    public String toString(){
        return getWeekNumber()+getTitle()+getDlUrl();
    }
}
