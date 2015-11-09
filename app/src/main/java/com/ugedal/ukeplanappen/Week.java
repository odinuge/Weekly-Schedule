package com.ugedal.ukeplanappen;

/**
 * Created by odin on 11/8/15.
 */
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

    public String getInfo(){return info;}


    public int getWeekNumber() {
        return weekNumber;
    }

    @Override
    public String toString(){
        return getWeekNumber()+getTitle()+getDlUrl();
    }
}