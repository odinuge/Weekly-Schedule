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

import android.content.Context;
import android.content.SharedPreferences;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleExtractor {

    /**
     * Returns an ArrayList<Schedule> that contain all the schedules for the
     * given class/name. It will try to download the content from the given
     * url, and throw an IOException if there are any problems.
     *
     * @param className  the active grade/class
     * @param sharedPref sharedPreferences instance to save cache
     * @param ctx        context for getting strings
     * @return a ArrayList containing all the schedules
     * @throws IOException if the content cannot be downloaded
     * @see Schedule
     */
    public static ArrayList<Schedule> extractSchedules(int className, SharedPreferences sharedPref, Context ctx)
            throws IOException {

        String htmlUrl = "http://aset.no/klassetrinn/" + className + "-trinn/?vis=ukeplaner";
        // Parse
        Document doc = Jsoup.connect(htmlUrl).get();
        Elements ukeplans = doc.getElementsByAttributeValue("id", "GradeTop");

        // Save cache
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("cache_" + className, ukeplans.toString());
        editor.apply();
        return getList(ukeplans.toString(), className, sharedPref, ctx);
    }

    /**
     * Returns an ArrayList<Schedule> that contain all the schedules for the
     * given class/grade. If no html content is given, the info will be
     * fetched from the cache
     *
     * @param HTML       the new HTML-content
     * @param className  the active grade/class
     * @param sharedPref sharedPreferences instance to save cache
     * @param ctx        context for getting strings
     * @return a ArrayList containing all the schedules
     * @see Schedule
     */
    public static ArrayList<Schedule> getList(String HTML, int className, SharedPreferences sharedPref, Context ctx) {
        final ArrayList<Schedule> result = new ArrayList<Schedule>();

        if (HTML == null) {
            HTML = sharedPref.getString("cache_" + className, "");
        }
        Document doc = Jsoup.parse(HTML);
        doc.setBaseUri("http://aset.no");

        Elements schedules = doc.getElementsByAttributeValue("id", "GradeTop").select("a[href]");

        Pattern pattern = Pattern.compile("[0-9]0?[ABCDabcd]");
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM yy");

        for (Element schedule : schedules) {
            int weekNumber = getWeekNumber(schedule.text());
            String title = schedule.text().replace(".pdf", "");
            title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();

            String info = "";
            if (weekNumber != Schedule.NO_WEEK_NUMBER) {

                Calendar cal = Calendar.getInstance();
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                if (weekNumber > Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) + 2) {
                    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 1);
                }
                cal.set(Calendar.WEEK_OF_YEAR, weekNumber);


                info = sdf.format(cal.getTime());
                info += " - ";
                cal.add(Calendar.DATE, 6);
                info += sdf.format(cal.getTime());

                title = ctx.getString(R.string.weekly_schedule);

                Matcher m = pattern.matcher(schedule.text());
                if (m.find()) {
                    title += (m.group(0).toUpperCase());
                }
            } else {
                info = title;
                title = ctx.getString(R.string.document);
            }
            result.add(new Schedule(weekNumber, title, schedule.attr("abs:href"), info));

        }

        return result;
    }

    /**
     * Returns an int with the week of year number, parsed from
     * the given string.
     * <p/>
     * If no number is found, it will return Schedule.NO_WEEK_NUMBER
     *
     * @param data containing the title of the entry
     * @return the week of year, or Schedule.NO_WEEK_NUMBER
     * @see Schedule
     */
    private static int getWeekNumber(String data) {
        String weekNumberString = data.replaceAll("[0-9]0?[ABCDabcd]_?[0-9]?[0-9]?", "").replaceAll("\\D+", "");
        return !weekNumberString.isEmpty() ? Integer.parseInt(weekNumberString) : Schedule.NO_WEEK_NUMBER;
    }

}
