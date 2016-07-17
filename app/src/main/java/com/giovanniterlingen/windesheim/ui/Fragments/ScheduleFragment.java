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
package com.giovanniterlingen.windesheim.ui.Fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.giovanniterlingen.windesheim.ApplicationLoader;
import com.giovanniterlingen.windesheim.R;
import com.giovanniterlingen.windesheim.handlers.ScheduleHandler;
import com.giovanniterlingen.windesheim.ui.Adapters.ScheduleAdapter;
import com.giovanniterlingen.windesheim.ui.ScheduleActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A schedule app for students and teachers of Windesheim
 *
 * @author Giovanni Terlingen
 */
public class ScheduleFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static String componentId;
    private static int type;
    private Date date;
    private ScheduleAdapter adapter;
    private DateFormat simpleDateFormat;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyTextView;
    private ProgressBar spinner;
    private RecyclerView recyclerView;

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        componentId = getArguments().getString("componentId");
        type = getArguments().getInt("type");
        date = (Date) getArguments().getSerializable("date");
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            if (recyclerView != null && recyclerView.getAdapter() == null) {
                new ScheduleFetcher(false, true, false).execute();
            } else {
                new ScheduleFetcher(false, false, false).execute();
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int month = calendar.get(Calendar.MONTH);
            String monthString = null;
            switch (month) {
                case 0:
                    monthString = getResources().getString(R.string.january);
                    break;
                case 1:
                    monthString = getResources().getString(R.string.february);
                    break;
                case 2:
                    monthString = getResources().getString(R.string.march);
                    break;
                case 3:
                    monthString = getResources().getString(R.string.april);
                    break;
                case 4:
                    monthString = getResources().getString(R.string.may);
                    break;
                case 5:
                    monthString = getResources().getString(R.string.june);
                    break;
                case 6:
                    monthString = getResources().getString(R.string.july);
                    break;
                case 7:
                    monthString = getResources().getString(R.string.august);
                    break;
                case 8:
                    monthString = getResources().getString(R.string.september);
                    break;
                case 9:
                    monthString = getResources().getString(R.string.october);
                    break;
                case 10:
                    monthString = getResources().getString(R.string.november);
                    break;
                case 11:
                    monthString = getResources().getString(R.string.december);
            }
            ActionBar toolbar = ((ScheduleActivity) getActivity()).getSupportActionBar();
            if (toolbar != null) {
                toolbar.setTitle(simpleDateFormat.format(date) + " " + monthString);
                // ugly workaround to fix toolbar title truncation
                toolbar.setDisplayHomeAsUpEnabled(false);
                toolbar.setDisplayHomeAsUpEnabled(true);
            }
            if (!ApplicationLoader.scheduleDatabase.containsWeek(date) &&
                    !ApplicationLoader.scheduleDatabase.isFetched(date)) {
                new ScheduleFetcher(true, true, false).execute();
            } else {
                new ScheduleFetcher(false, false, false).execute();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_schedule, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) viewGroup.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimaryText, R.color.colorPrimary);
        emptyTextView = (TextView) viewGroup.findViewById(R.id.schedule_not_found);
        spinner = (ProgressBar) viewGroup.findViewById(R.id.progress_bar);
        recyclerView = (RecyclerView) viewGroup.findViewById(R.id.schedule_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Cursor scheduleDay = ApplicationLoader.scheduleDatabase.getLessons(
                simpleDateFormat.format(date), componentId);
        if (scheduleDay != null && scheduleDay.getCount() > 0) {
            adapter = new ScheduleAdapter(getActivity(), scheduleDay, simpleDateFormat.format(date),
                    componentId, date);
            recyclerView.setAdapter(adapter);
        }
        if (recyclerView != null && recyclerView.getAdapter() != null) {
            emptyTextView.setVisibility(View.GONE);
        }
        return viewGroup;
    }

    private void alertConnectionProblem() {
        if (!getUserVisibleHint()) {
            return;
        }
        ApplicationLoader.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.alert_connection_title))
                        .setMessage(getResources().getString(R.string.alert_connection_description))
                        .setPositiveButton(getResources().getString(R.string.connect),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        new ScheduleFetcher(true, false, true).execute();
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }).show();
            }
        });
    }

    @Override
    public void onRefresh() {
        new ScheduleFetcher(true, false, true).execute();
    }

    /**
     * Workaround, this method is called from another class
     */
    public void updateLayout() {
        if (emptyTextView != null) {
            if (adapter == null || adapter.getItemCount() == 0) {
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                emptyTextView.setVisibility(View.GONE);
            }
        }
    }

    public class ScheduleFetcher extends AsyncTask<Void, Void, Void> {

        private final boolean fetchData;
        private final boolean showSpinner;
        private final boolean showSwipeRefresh;
        private Cursor scheduleDay;

        public ScheduleFetcher(boolean fetchData, boolean showSpinner, boolean showSwipeRefresh) {
            this.fetchData = fetchData;
            this.showSpinner = showSpinner;
            this.showSwipeRefresh = showSwipeRefresh;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (adapter == null || adapter.getItemCount() == 0) {
                if (showSpinner && spinner != null && emptyTextView != null) {
                    emptyTextView.setVisibility(View.GONE);
                    spinner.setVisibility(View.VISIBLE);
                }
            }
            if (showSwipeRefresh && swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }

        @Override
        protected Void doInBackground(Void... param) {
            if (fetchData) {
                try {
                    ScheduleHandler.saveSchedule(ScheduleHandler.getScheduleFromServer(componentId, date, type), date, componentId);
                } catch (Exception e) {
                    alertConnectionProblem();
                }
            }
            scheduleDay = ApplicationLoader.scheduleDatabase.getLessons(simpleDateFormat.format(date), componentId);
            return null;
        }


        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            if (adapter == null) {
                adapter = new ScheduleAdapter(getActivity(), scheduleDay,
                        simpleDateFormat.format(date), componentId, date);
                if (recyclerView != null) {
                    recyclerView.setAdapter(adapter);
                }
            } else {
                adapter.changeCursor(scheduleDay);
            }
            if (adapter.getItemCount() == 0) {
                if (emptyTextView != null) {
                    emptyTextView.setVisibility(View.VISIBLE);
                }
            } else {
                if (emptyTextView != null) {
                    emptyTextView.setVisibility(View.GONE);
                }
            }
            if (showSpinner && spinner != null) {
                spinner.setVisibility(View.GONE);
            }
            if (showSwipeRefresh && swipeRefreshLayout != null
                    && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}