/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blackman.ehviewer.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.blackman.app.ListCheckBoxDialogBuilder;
import com.blackman.ehviewer.EhApplication;
import com.blackman.ehviewer.R;
import com.blackman.ehviewer.Settings;
import com.blackman.ehviewer.UrlOpener;
import com.blackman.ehviewer.client.EhClient;
import com.blackman.ehviewer.client.EhRequest;
import com.blackman.ehviewer.client.data.GalleryInfo;
import com.blackman.ehviewer.ui.scene.BaseScene;
import com.blackman.text.Html;
import com.hippo.unifile.UniFile;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class CommonOperations {

    private static final String TAG = CommonOperations.class.getSimpleName();

    private static boolean UPDATING;

    public static void checkUpdate(Activity activity, boolean feedback) {
        if (!UPDATING) {
            UPDATING = true;
            new UpdateTask(activity, feedback).execute();
        }
    }

    private static final class UpdateTask extends AsyncTask<Void, Void, JSONObject> {

        private final Activity mActivity;
        private final OkHttpClient mHttpClient;
        private final boolean mFeedback;

        public UpdateTask(Activity activity, boolean feedback) {
            mActivity = activity;
            mHttpClient = EhApplication.getOkHttpClient(activity);
            mFeedback = feedback;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                String url;
                if (Settings.getBetaUpdateChannel()) {
                    url = "http://www.ehviewer.com/update_beta.json";
                } else {
                    url = "http://www.ehviewer.com/update.json";
                }
                Log.d(TAG, url);
                Request request = new Request.Builder().url(url).build();
                Response response = mHttpClient.newCall(request).execute();
                return new JSONObject(response.body().string());
            } catch (IOException e) {
                return null;
            } catch (JSONException e) {
                return null;
            }
        }

        private void showUpToDateDialog() {
            if (!mFeedback) {
                return;
            }

            new AlertDialog.Builder(mActivity)
                    .setMessage(R.string.update_to_date)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }

        private void showUpdateDialog(String versionName, String size, CharSequence info, final String url) {
            new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.update)
                    .setMessage(mActivity.getString(R.string.update_plain, versionName, size, info))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UrlOpener.openUrl(mActivity, url, false);
                        }
                    }).show();
        }


        private void handleResult(JSONObject jo) {
            if (null == jo || mActivity.isFinishing()) {
                return;
            }

            String versionName;
            String size;
            CharSequence info;
            String url;

            try {
                PackageManager pm = mActivity.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
                int currentVersionCode = pi.versionCode;
                int newVersionCode = jo.getInt("version_code");
                if (currentVersionCode >= newVersionCode) {
                    // Update to date
                    showUpToDateDialog();
                    return;
                }

                versionName = jo.getString("version_name");
                size = FileUtils.humanReadableByteCount(jo.getLong("size"), false);
                info = Html.fromHtml(jo.getString("info"));
                url = jo.getString("url");
            } catch (PackageManager.NameNotFoundException | JSONException e) {
                return;
            }

            showUpdateDialog(versionName, size, info, url);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                handleResult(jsonObject);
            } finally {
                UPDATING = false;
            }
        }
    }

    private static void doAddToFavorites(Activity activity, GalleryInfo galleryInfo,
            int slot, EhClient.Callback<Void> listener) {
        if (slot == -1) {
            listener.onSuccess(null);
        } else if (slot >= 0 && slot <= 9) {
            EhClient client = EhApplication.getEhClient(activity);
            EhRequest request = new EhRequest();
            request.setMethod(EhClient.METHOD_ADD_FAVORITES);
            request.setArgs(galleryInfo.gid, galleryInfo.token, slot, "");
            request.setCallback(listener);
            client.execute(request);
        } else {
            listener.onFailure(new Exception()); // TODO Add text
        }
    }

    public static void addToFavorites(final Activity activity, final GalleryInfo galleryInfo,
            final EhClient.Callback<Void> listener) {
        int slot = Settings.getDefaultFavSlot();
        if (slot >= -1 && slot <= 9) {
            doAddToFavorites(activity, galleryInfo, slot, listener);
        } else {
            String[] items = new String[11];
            items[0] = activity.getString(R.string.local_favorites);
            String[] favCat = Settings.getFavCat();
            System.arraycopy(favCat, 0, items, 1, 10);
            new ListCheckBoxDialogBuilder(activity, items,
                    new ListCheckBoxDialogBuilder.OnItemClickListener() {
                        @Override
                        public void onItemClick(ListCheckBoxDialogBuilder builder, AlertDialog dialog, int position) {
                            int slot = position - 1;
                            doAddToFavorites(activity, galleryInfo, slot, listener);
                            if (builder.isChecked()) {
                                Settings.putDefaultFavSlot(slot);
                            } else {
                                Settings.putDefaultFavSlot(Settings.INVALID_DEFAULT_FAV_SLOT);
                            }
                        }
                    }, activity.getString(R.string.remember_favorite_collection), false)
                    .setTitle(R.string.add_favorites_dialog_title)
                    .show();
        }
    }

    public static void removeFromFavorites(Activity activity, GalleryInfo galleryInfo,
            final EhClient.Callback<Void> listener) {
        EhClient client = EhApplication.getEhClient(activity);
        EhRequest request = new EhRequest();
        request.setMethod(EhClient.METHOD_ADD_FAVORITES);
        request.setArgs(galleryInfo.gid, galleryInfo.token, -1, "");
        request.setCallback(listener);
        client.execute(request);
    }

    // TODO Add context if activity and context are different style
    public static void startDownload(final MainActivity activity, final GalleryInfo galleryInfo, boolean forceDefault) {

    }

    public static void ensureNoMediaFile(UniFile file) {
        if (null == file) {
            return;
        }

        UniFile noMedia = file.createFile(".nomedia");
        if (null == noMedia) {
            return;
        }

        InputStream is = null;
        try {
            is = noMedia.openInputStream();
        } catch (IOException e) {
            // Ignore
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static void removeNoMediaFile(UniFile file) {
        if (null == file) {
            return;
        }

        UniFile noMedia = file.subFile(".nomedia");
        if (null != noMedia && noMedia.isFile()) {
            noMedia.delete();
        }
    }
}
