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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.giovanniterlingen.windesheim.R;
import com.giovanniterlingen.windesheim.handlers.DownloadHandler;
import com.giovanniterlingen.windesheim.handlers.NatSchoolWebHandler;
import com.giovanniterlingen.windesheim.objects.Content;
import com.giovanniterlingen.windesheim.ui.Adapters.ContentAdapter;
import com.giovanniterlingen.windesheim.ui.ContentsActivity;

import java.util.List;

/**
 * A schedule app for students and teachers of Windesheim
 *
 * @author Giovanni Terlingen
 */
public class ContentsFragment extends Fragment {

    public static final String STUDYROUTE_ID = "STUDYROUTE_ID";
    public static final String PARENT_ID = "PARENT_ID";
    public static final String STUDYROUTE_NAME = "STUDYROUTE_NAME";
    private int studyRouteId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        final ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_contents, container, false);
        final RecyclerView recyclerView = (RecyclerView) viewGroup.findViewById(R.id.courses_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final ProgressBar progressBar = (ProgressBar) viewGroup.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        Bundle bundle = this.getArguments();
        ActionBar toolbar = ((ContentsActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            if (bundle != null && bundle.getString(STUDYROUTE_NAME) != null &&
                    bundle.getString(STUDYROUTE_NAME).length() != 0) {
                toolbar.setTitle(bundle.getString(STUDYROUTE_NAME));
            } else {
                toolbar.setTitle(getResources().getString(R.string.courses));
            }
            // ugly workaround to fix toolbar title truncation
            toolbar.setDisplayHomeAsUpEnabled(false);
            toolbar.setDisplayHomeAsUpEnabled(true);
        }
        new NatSchoolWebHandler((bundle == null ? -1 : (studyRouteId = bundle.getInt(STUDYROUTE_ID))),
                (bundle == null ? -1 : bundle.getInt(PARENT_ID, -1)), getActivity()) {
            @Override
            public void onFinished(final List<Content> courses) {
                progressBar.setVisibility(View.GONE);
                if (courses.isEmpty()) {
                    TextView emptyTextView = (TextView) viewGroup.findViewById(R.id.empty_textview);
                    emptyTextView.setVisibility(View.VISIBLE);
                    return;
                }
                recyclerView.setAdapter(new ContentAdapter(getContext(), courses) {
                    @Override
                    protected void onContentClick(Content content) {
                        if (content.url == null || content.url.length() == 0) {
                            Bundle bundle = new Bundle();
                            if (content.id == -1) {
                                bundle.putInt(STUDYROUTE_ID, content.studyRouteItemId);
                            } else {
                                bundle.putInt(STUDYROUTE_ID, studyRouteId);
                                bundle.putInt(PARENT_ID, content.id);
                            }
                            bundle.putString(STUDYROUTE_NAME, content.name);

                            ContentsFragment contentsFragment = new ContentsFragment();
                            contentsFragment.setArguments(bundle);

                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.contents_fragment, contentsFragment, "")
                                    .addToBackStack("")
                                    .commit();
                        } else {
                            if (content.type == 1) {
                                createWebview(content.url, false);
                                return;
                            }
                            if (content.type == 3) {
                                createWebview(content.url, true);
                                return;
                            }
                            if (content.type == 10) {
                                ProgressDialog mProgressDialog = new ProgressDialog(getActivity());
                                mProgressDialog.setMessage(getResources().getString(R.string.downloading));
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                mProgressDialog.setCancelable(true);
                                new DownloadHandler(getActivity(), mProgressDialog)
                                        .execute("https://elo.windesheim.nl" + content.url);
                                return;
                            }
                            View view = ((ContentsActivity) getActivity()).view;
                            if (view != null) {
                                Snackbar snackbar = Snackbar.make(view, getContext().getResources()
                                        .getString(R.string.not_supported), Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        }
                    }
                });
            }
        }.execute();
        return viewGroup;
    }

    private void createWebview(String url, boolean externalView) {
        Bundle bundle = new Bundle();
        bundle.putString(WebviewFragment.KEY_URL, "https://elo.windesheim.nl" + url);
        bundle.putBoolean(WebviewFragment.EXTERNAL, externalView);

        WebviewFragment webviewFragment = new WebviewFragment();
        webviewFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.contents_fragment, webviewFragment, "")
                .addToBackStack("")
                .commit();
    }
}