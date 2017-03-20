/**
 * Copyright (c) 2016 Giovanni Terlingen
 * <p/>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 **/
package com.giovanniterlingen.windesheim.handlers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.giovanniterlingen.windesheim.ApplicationLoader;

import java.util.Calendar;

/**
 * A schedule app for students and teachers of Windesheim
 *
 * @author Giovanni Terlingen
 */
public class DailyScheduleHandler extends Thread {

    private volatile boolean running = true;

    @Override
    public void run() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(ApplicationLoader.applicationContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        while (isRunning()) {
            if (sharedPreferences.getLong("checkTime", 0) == 0 ||
                    !DateUtils.isToday(sharedPreferences.getLong("checkTime", 0))) {
                try {
                    Calendar calendar = Calendar.getInstance();
                    ScheduleHandler.getAndSaveAllSchedules(calendar.getTime());
                    calendar.add(Calendar.DATE, 7);
                    ScheduleHandler.getAndSaveAllSchedules(calendar.getTime());
                    editor.putLong("checkTime", System.currentTimeMillis());
                    editor.apply();
                } catch (Exception e) {
                    stopRunning();
                }
            }
            try {
                sleep(60000);
            } catch (InterruptedException e) {
                //
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void stopRunning() {
        running = false;
    }
}
