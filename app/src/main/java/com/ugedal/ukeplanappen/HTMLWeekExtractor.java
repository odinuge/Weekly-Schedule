package com.ugedal.ukeplanappen;

/**
 * Created by odin on 11/8/15.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLWeekExtractor {
    FragmentActivity ctx;

    public HTMLWeekExtractor(FragmentActivity ctx){
        this.ctx = ctx;
    }

    public ArrayList<Week> extractWeeks(String url, int className)
            throws IOException {

        Document doc = Jsoup.connect(url).get();
        Elements ukeplans = doc.getElementsByAttributeValue("id", "GradeTop");

        SharedPreferences sharedPref = ctx.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("cache_"+className, ukeplans.toString());
        editor.commit();
        return getList(ukeplans.toString(),className);
    }
    public ArrayList<Week> getList(String HTML, int className){
        final ArrayList<Week> result = new ArrayList<Week>();

        if(HTML == null){
            SharedPreferences sharedPref = ctx.getPreferences(Context.MODE_PRIVATE);
            HTML = sharedPref.getString("cache_"+className,"");
        }
        Document doc = Jsoup.parse(HTML);
        doc.setBaseUri("http://aset.no");
        Elements ukeplans = doc.getElementsByAttributeValue("id", "GradeTop");
        Elements links = ukeplans.select("a[href]");

        Pattern p = Pattern.compile("[0-9]0?[ABCDabcd]");
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM yy");


        for (Element link : links) {

            int weekNumber = getWeekNumber(link.text());
            String title = link.text().replace(".pdf", "");

            String info = new String();
            if(weekNumber>0){

                Calendar cal = Calendar.getInstance();
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                if(weekNumber > Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)+2){
                    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR)-1 );
                }

                cal.set(Calendar.WEEK_OF_YEAR, weekNumber);


                info = sdf.format(cal.getTime());
                info += " - ";
                cal.add(Calendar.DATE,6);
                info += sdf.format(cal.getTime());
                title = "Ukeplan "+className+".Trinn";

                Matcher m = p.matcher(link.text());
                if (m.find()) {
                    title = "Ukeplan " + (m.group(0).toUpperCase());
                }
            }
            result.add(new Week(weekNumber ,title,link.attr("abs:href"),info));

        }

        return result;
    }
    private static int getWeekNumber(String data){
        String weekNumberString = data.replaceAll("[0-9]0?[ABCDabcd]_?[0-9]?[0-9]?", "").replaceAll("\\D+", "");
        int weekNumber = weekNumberString != null && !weekNumberString.isEmpty()? Integer.parseInt(weekNumberString):-1;
        return weekNumber;
    }

}