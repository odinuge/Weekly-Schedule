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
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
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

        Pattern weekNumberPattern = Pattern.compile("[^_0-9]([0-9]{1,2})");
        Pattern classNamePattern = Pattern.compile("(^|[^0-9])(10|[1-9])\\s?([a-eA-E])");

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM yy");

        for (Element schedule : schedules) {
            String htmlText = schedule.text();
            String downloadUrl = schedule.attr("abs:href");
            String title = htmlText
                    .replace(".pdf", "")
                    .replace(".docx", "");
            title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();


            Matcher m = classNamePattern.matcher(htmlText);
            String classTitle = ctx.getString(R.string.weekly_schedule);
            if (m.find()) {
                title += (m.group(2) + m.group(3).toUpperCase());
                htmlText = htmlText.replaceAll(m.group(0), "");
            }

            List<Integer> weekNumberList = getWeekNumber(htmlText, weekNumberPattern);



            String info = "";
            String weekNumberText = "";
            if (weekNumberList.isEmpty()) {
                info = title;
                title = ctx.getString(R.string.document);
            } else {
                title = classTitle;
                int firstWeek = weekNumberList.get(0);
                int lastWeek = weekNumberList.get(weekNumberList.size() - 1);


                Calendar firstWeekCal = Calendar.getInstance(new Locale("no"));
                firstWeekCal.setFirstDayOfWeek(Calendar.MONDAY);
                firstWeekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                if (firstWeek > Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) + 2) {
                    firstWeekCal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 1);
                }
                firstWeekCal.set(Calendar.WEEK_OF_YEAR, firstWeek);

                Calendar lastWeekCal = Calendar.getInstance(new Locale("no"));
                lastWeekCal.setFirstDayOfWeek(Calendar.MONDAY);
                lastWeekCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                if (lastWeek > Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) + 2) {
                    lastWeekCal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 1);
                }
                lastWeekCal.set(Calendar.WEEK_OF_YEAR, lastWeek);
                info = String.format("%s - %s", sdf.format(firstWeekCal.getTime()), sdf.format(lastWeekCal.getTime()));

                weekNumberText = TextUtils.join("\n", weekNumberList);
                final long calendarWeek = 34;
            }

            result.add(new Schedule(weekNumberText, title, downloadUrl, info));

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
    private static List<Integer> getWeekNumber(String data, Pattern weekNumberPattern) {
        Matcher m = weekNumberPattern.matcher(data);
        List<Integer> weekNumberList = new ArrayList<Integer>();
        while (m.find()) {
            int newNum = Integer.parseInt(m.group(1));
            if (newNum <= 53 && newNum >= 1)
                weekNumberList.add(newNum);
        }

        return weekNumberList;

    }

}
