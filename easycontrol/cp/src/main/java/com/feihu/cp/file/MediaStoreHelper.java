package com.feihu.cp.file;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;


/**
 * Created by newbiechen on 2018/1/14.
 * 获取媒体库的数据。
 */

public class MediaStoreHelper {

    /**
     * 获取媒体库中所有的书籍文件
     * <p>
     * 暂时只支持 TXT
     *
     * @param activity
     * @param resultCallback
     */
    public static void getAllBookFile(FragmentActivity activity, String keyWords, MediaResultCallback resultCallback) {
        // 将文件的获取处理交给 LoaderManager。
        Bundle bundle = new Bundle();
        bundle.putString("keyWords", keyWords);
        LoaderManager.getInstance(activity)
                .restartLoader(LoaderCreator.ALL_BOOK_FILE, bundle, new MediaLoaderCallbacks(activity, resultCallback));
    }

    public interface MediaResultCallback {
        void onResultCallback(List<File> files);
    }

    /**
     * Loader 回调处理
     */
    static class MediaLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        protected WeakReference<Context> mContext;
        protected MediaResultCallback mResultCallback;

        public MediaLoaderCallbacks(Context context, MediaResultCallback resultCallback) {
            mContext = new WeakReference<>(context);
            mResultCallback = resultCallback;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return LoaderCreator.create(mContext.get(), id, args);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            LocalFileLoader localFileLoader = (LocalFileLoader) loader;
            localFileLoader.parseData(data, mResultCallback);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }
}
