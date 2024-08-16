package com.feihu.cp.file;

import android.content.Context;
import android.os.Bundle;

import androidx.loader.content.CursorLoader;


/**
 * Created by newbiechen on 2018/1/14.
 */

public class LoaderCreator {
    public static final int ALL_BOOK_FILE = 1;

    public static CursorLoader create(Context context, int id, Bundle bundle) {
        LocalFileLoader loader = null;
        switch (id) {
            case ALL_BOOK_FILE:
                String keyWords = bundle.getString("keyWords");
                loader = new LocalFileLoader(context, keyWords);
                break;
            default:
                loader = null;
                break;
        }
        if (loader != null) {
            return loader;
        }

        throw new IllegalArgumentException("The id of Loader is invalid!");
    }
}
