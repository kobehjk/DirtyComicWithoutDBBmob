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

package com.blackman.ehviewer.ui.scene;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blackman.ehviewer.Settings;
import com.blackman.ehviewer.client.EhCacheKeyFactory;
import com.blackman.ehviewer.client.EhUtils;
import com.blackman.ehviewer.client.data.GalleryInfo;
import com.blackman.ehviewer.ui.CommonOperations;
import com.blackman.ehviewer.ui.MainActivity;
import com.blackman.ehviewer.widget.SimpleRatingView;
import com.blackman.scene.Announcer;
import com.blackman.scene.SceneFragment;
import com.blackman.util.ApiHelper;
import com.blackman.util.DrawableManager;
import com.blackman.view.ViewTransition;
import com.blackman.widget.LoadImageView;
import com.blackman.widget.recyclerview.AutoStaggeredGridLayoutManager;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.easyrecyclerview.FastScroller;
import com.hippo.easyrecyclerview.HandlerDrawable;
import com.hippo.easyrecyclerview.MarginItemDecoration;
import com.blackman.ehviewer.R;
import com.hippo.ripple.Ripple;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.ViewUtils;

import junit.framework.Assert;


public class HistoryScene extends ToolbarScene
        implements EasyRecyclerView.OnItemClickListener,
        EasyRecyclerView.OnItemLongClickListener{

    /*---------------
     View life cycle
     ---------------*/
    @Nullable
    private EasyRecyclerView mRecyclerView;
    @Nullable
    private ViewTransition mViewTransition;
    @Nullable
    private RecyclerView.Adapter<?> mAdapter;

    @Override
    public int getNavCheckedItem() {
        return R.id.nav_history;
    }

    @Nullable
    @Override
    public View onCreateView3(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scene_history, container, false);
        View content = ViewUtils.$$(view, R.id.content);
        mRecyclerView = (EasyRecyclerView) ViewUtils.$$(content, R.id.recycler_view);
        FastScroller fastScroller = (FastScroller) ViewUtils.$$(content, R.id.fast_scroller);
        TextView tip = (TextView) ViewUtils.$$(view, R.id.tip);
        mViewTransition = new ViewTransition(content, tip);

        Context context = getContext2();
        Assert.assertNotNull(context);
        Resources resources = context.getResources();

        Drawable drawable = DrawableManager.getDrawable(context, R.drawable.big_history);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tip.setCompoundDrawables(null, drawable, null, null);

        RecyclerViewTouchActionGuardManager guardManager = new RecyclerViewTouchActionGuardManager();
        guardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        guardManager.setEnabled(true);
        RecyclerViewSwipeManager swipeManager = new RecyclerViewSwipeManager();
        mAdapter = new HistoryAdapter();
        mAdapter.setHasStableIds(true);
        mAdapter = swipeManager.createWrappedAdapter(mAdapter);
        mRecyclerView.setAdapter(mAdapter);
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        animator.setSupportsChangeAnimations(false);
        mRecyclerView.setItemAnimator(animator);
        AutoStaggeredGridLayoutManager layoutManager = new AutoStaggeredGridLayoutManager(
                0, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setColumnSize(resources.getDimensionPixelOffset(Settings.getDetailSizeResId()));
        layoutManager.setStrategy(AutoStaggeredGridLayoutManager.STRATEGY_MIN_SIZE);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setSelector(Ripple.generateRippleDrawable(context, false));
        mRecyclerView.setDrawSelectorOnTop(true);
        mRecyclerView.hasFixedSize();
        mRecyclerView.setClipToPadding(false);
        mRecyclerView.setOnItemClickListener(this);
        mRecyclerView.setOnItemLongClickListener(this);
        int interval = resources.getDimensionPixelOffset(R.dimen.gallery_list_interval);
        int paddingH = resources.getDimensionPixelOffset(R.dimen.gallery_list_margin_h);
        int paddingV = resources.getDimensionPixelOffset(R.dimen.gallery_list_margin_v);
        MarginItemDecoration decoration = new MarginItemDecoration(interval, paddingH, paddingV, paddingH, paddingV);
        mRecyclerView.addItemDecoration(decoration);
        decoration.applyPaddings(mRecyclerView);
        guardManager.attachRecyclerView(mRecyclerView);
        swipeManager.attachRecyclerView(mRecyclerView);

        fastScroller.attachToRecyclerView(mRecyclerView);
        HandlerDrawable handlerDrawable = new HandlerDrawable();
        handlerDrawable.setColor(ResourcesUtils.getAttrColor(context, R.attr.colorAccent));
        fastScroller.setHandlerDrawable(handlerDrawable);

        updateLazyList();
        updateView(false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.history);
        setNavigationIcon(R.drawable.v_arrow_left_dark_x24);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();


        if (null != mRecyclerView) {
            mRecyclerView.stopScroll();
            mRecyclerView = null;
        }

        mViewTransition = null;
        mAdapter = null;
    }

    // Remember to notify
    private void updateLazyList() {

    }

    private void updateView(boolean animation) {
        if (null == mAdapter || null == mViewTransition) {
            return;
        }

        if (mAdapter.getItemCount() == 0) {
            mViewTransition.showView(1, animation);
        } else {
            mViewTransition.showView(0, animation);
        }
    }

    @Override
    public void onNavigationClick() {
        onBackPressed();
    }

    @Override
    public int getMenuResId() {
        return R.menu.scene_history;
    }

    private void showClearAllDialog() {
        new AlertDialog.Builder(getContext2())
                .setMessage(R.string.clear_all_history)
                .setPositiveButton(R.string.clear_all, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DialogInterface.BUTTON_POSITIVE != which || null == mAdapter) {
                            return;
                        }


                        updateLazyList();
                        mAdapter.notifyDataSetChanged();
                        updateView(true);
                    }
                }).show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Skip when in choice mode
        Context context = getContext2();
        if (null == context) {
            return false;
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.action_clear_all: {
                showClearAllDialog();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {



        return true;
    }

    @Override
    public boolean onItemLongClick(EasyRecyclerView parent, View view, int position, long id) {
        final Context context = getContext2();
        final MainActivity activity = getActivity2();
        if (null == context || null == activity ) {
            return false;
        }


        return true;
    }

    private class HistoryHolder extends AbstractSwipeableItemViewHolder {

        public final LoadImageView thumb;
        public final TextView title;
        public final TextView uploader;
        public final SimpleRatingView rating;
        public final TextView category;
        public final TextView posted;
        public final TextView simpleLanguage;

        public HistoryHolder(View itemView) {
            super(itemView);

            thumb = (LoadImageView) itemView.findViewById(R.id.thumb);
            title = (TextView) itemView.findViewById(R.id.title);
            uploader = (TextView) itemView.findViewById(R.id.uploader);
            rating = (SimpleRatingView) itemView.findViewById(R.id.rating);
            category = (TextView) itemView.findViewById(R.id.category);
            posted = (TextView) itemView.findViewById(R.id.posted);
            simpleLanguage = (TextView) itemView.findViewById(R.id.simple_language);
        }

        @Override
        public View getSwipeableContainerView() {
            return itemView;
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder>
            implements SwipeableItemAdapter<HistoryHolder> {

        private final LayoutInflater mInflater;

        public HistoryAdapter() {
            mInflater = getLayoutInflater2();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new HistoryHolder(mInflater.inflate(R.layout.item_gallery_list, parent, false));
        }

        @Override
        public void onBindViewHolder(HistoryHolder holder, int position) {




        }

        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public int onGetSwipeReactionType(HistoryHolder holder, int position, int x, int y) {
            return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H;
        }

        @Override
        public void onSetSwipeBackground(HistoryHolder holder, int position, int type) {}

        @Override
        public SwipeResultAction onSwipeItem(HistoryHolder holder, int position, int result) {
            switch (result) {
                case SwipeableItemConstants.RESULT_SWIPED_LEFT:
                case SwipeableItemConstants.RESULT_SWIPED_RIGHT:
                    return new SwipeResultActionClear(position);
                case SwipeableItemConstants.RESULT_CANCELED:
                default:
                    return new SwipeResultActionDefault();
            }
        }
    }

    private class SwipeResultActionClear extends SwipeResultActionRemoveItem {

        private final int mPosition;

        protected SwipeResultActionClear(int position) {
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            updateLazyList();
            mAdapter.notifyItemRemoved(mPosition);
            updateView(true);
        }
    }

    private static class addToFavoriteListener extends EhCallback<HistoryScene, Void> {

        public addToFavoriteListener(Context context, int stageId, String sceneTag) {
            super(context, stageId, sceneTag);
        }

        @Override
        public void onSuccess(Void result) {
            showTip(R.string.add_to_favorite_success, LENGTH_SHORT);
        }

        @Override
        public void onFailure(Exception e) {
            showTip(R.string.add_to_favorite_failure, LENGTH_SHORT);
        }

        @Override
        public void onCancel() {}

        @Override
        public boolean isInstance(SceneFragment scene) {
            return scene instanceof HistoryScene;
        }
    }
}