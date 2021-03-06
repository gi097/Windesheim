/**
 * Copyright (c) 2019 Giovanni Terlingen
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
package com.giovanniterlingen.windesheim;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.giovanniterlingen.windesheim.controllers.DatabaseController;
import com.giovanniterlingen.windesheim.controllers.WindesheimAPIController;

/**
 * A schedule app for students and teachers of Windesheim
 *
 * @author Giovanni Terlingen
 */
public class FetchService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters job) {
        if (DatabaseController.getInstance().hasSchedules()) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        SharedPreferences preferences = PreferenceManager
                                .getDefaultSharedPreferences(ApplicationLoader.applicationContext);
                        boolean notify = preferences
                                .getBoolean(Constants.PREFS_SCHEDULE_CHANGE_NOTIFICATION, true);
                        WindesheimAPIController.getAndSaveLessons(notify);
                        jobFinished(job, false);
                    } catch (Exception e) {
                        jobFinished(job, true);
                    }
                }
            }.start();
            return true;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }
}