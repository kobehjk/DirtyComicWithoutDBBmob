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

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.blackman.util.DrawableManager;
import com.blackman.view.ViewTransition;
import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.blackman.ehviewer.R;
import com.hippo.yorozuya.ViewUtils;

import java.util.List;

public class FilterActivity extends ToolbarActivity {

    @Nullable
    private EasyRecyclerView mRecyclerView;
    @Nullable
    private ViewTransition mViewTransition;

    @Nullable
    private FilterList mFilterList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        setNavigationIcon(R.drawable.v_arrow_left_dark_x24);

        mFilterList = new FilterList();

        mRecyclerView = (EasyRecyclerView) ViewUtils.$$(this, R.id.recycler_view);
        TextView tip = (TextView) ViewUtils.$$(this, R.id.tip);
        mViewTransition = new ViewTransition(mRecyclerView, tip);

        Drawable drawable = DrawableManager.getDrawable(this, R.drawable.big_filter);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tip.setCompoundDrawables(null, drawable, null, null);

        mRecyclerView.setClipToPadding(false);
        mRecyclerView.setClipChildren(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.hasFixedSize();

        updateView(false);
    }

    private void updateView(boolean animation) {
        if (null == mViewTransition) {
            return;
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRecyclerView = null;
        mViewTransition = null;

        mFilterList = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_add:
                showAddFilterDialog();
                return true;
            case R.id.action_tip:
                showTipDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showTipDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.filter)
                .setMessage(R.string.filter_tip)
                .show();
    }

    private void showAddFilterDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_filter)
                .setView(R.layout.dialog_add_filter)
                .setPositiveButton(R.string.add, null)
                .show();
        AddFilterDialogHelper helper = new AddFilterDialogHelper();
        helper.setDialog(dialog);
    }



    private class AddFilterDialogHelper implements View.OnClickListener {

        @Nullable
        private AlertDialog mDialog;
        @Nullable
        private Spinner mSpinner;
        @Nullable
        private TextInputLayout mInputLayout;
        @Nullable
        private EditText mEditText;

        public void setDialog(AlertDialog dialog) {
            mDialog = dialog;
            mSpinner = (Spinner) ViewUtils.$$(dialog, R.id.spinner);
            mInputLayout = (TextInputLayout) ViewUtils.$$(dialog, R.id.text_input_layout);
            mEditText = mInputLayout.getEditText();
            View button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (null != button) {
                button.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (null == mFilterList || null == mDialog || null == mSpinner ||
                    null == mInputLayout || null == mEditText) {
                return;
            }

            String text = mEditText.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                mInputLayout.setError(getString(R.string.text_is_empty));
                return;
            } else {
                mInputLayout.setError(null);
            }
            int mode = mSpinner.getSelectedItemPosition();




            updateView(true);

            mDialog.dismiss();
            mDialog = null;
            mSpinner = null;
            mInputLayout = null;
            mEditText = null;
        }
    }

    private class FilterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView text;
        private final ImageView icon;

        public FilterHolder(View itemView) {
            super(itemView);
            text = (TextView) ViewUtils.$$(itemView, R.id.text);
            icon = (ImageView) itemView.findViewById(R.id.icon);

            if (null != icon) {
                icon.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position < 0 || null == mFilterList) {
                return;
            }

        }
    }



    private class FilterList {

        public static final int MODE_HEADER = -1;



        public FilterList() {

        }




    }
}
