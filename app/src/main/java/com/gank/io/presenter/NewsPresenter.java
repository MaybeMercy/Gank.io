package com.gank.io.presenter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.gank.io.model.ContentItem;
import com.gank.io.ui.view.IBaseView;
import com.gank.io.ui.view.IFragmentView;
import com.gank.io.util.GetRss;
import com.gank.io.util.Logger;
import com.gank.io.util.ParseRss;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Lucifer on 2016/7/16.
 */
public class NewsPresenter extends BasePresenter {

    private static final String TAG = NewsPresenter.class.getSimpleName();
    private boolean isLoading = false;

    public NewsPresenter(Activity activity, IBaseView view) {
        super(activity, view);
    }

    /**
     * load the daily news
     * @param date
     */
    public void loadNews(final String date) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isLoading = true;
                String results = GetRss.getRssContent(date);
                if (TextUtils.isEmpty(results)) {
                    Logger.i(TAG, "getRssContent but no response.");
                    isLoading = false;
                    return;
                }
                ArrayList<ContentItem> mContents  = ParseRss.parseDailyContent(results);
                if (mContents == null || mContents.isEmpty()) {
                    Logger.i(TAG, "parseDailyContent but no result.");
                    isLoading = false;
                    return;
                }
                if (mView instanceof IFragmentView) {
                    ((IFragmentView) mView).fillData(mContents);
                }
                isLoading = false;
            }
        }).start();

    }

    public boolean isLoading() {
        return isLoading;
    }
}
