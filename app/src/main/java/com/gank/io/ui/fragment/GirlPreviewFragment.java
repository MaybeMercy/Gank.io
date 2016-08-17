package com.gank.io.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.logging.FLog;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.gank.io.R;
import com.gank.io.model.ContentItem;
import com.gank.io.presenter.GirlPreviewPresenter;
import com.gank.io.ui.view.IFragmentView;
import com.gank.io.util.CommonUtils;
import com.gank.io.util.FragmentUtils;
import com.gank.io.util.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by lucifer on 16-1-4.
 */
public class GirlPreviewFragment extends Fragment implements IFragmentView{

    private static final int SAVE_PIC_ID = 1;
    private static final String LOG_TAG = GirlPreviewFragment.class.getSimpleName();

    private SimpleDraweeView meizhiimg;

    private GirlPreviewPresenter mPrensenter;
    private String mUrl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUrl = bundle.getString(ContentItem.URL);
        }
        View root = inflater.inflate(R.layout.meizhi_preview, container, false);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        meizhiimg = (SimpleDraweeView) root.findViewById(R.id.meizhi_preview_img);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(mControllerListener)
                .setUri(Uri.parse(mUrl))
                .build();
        meizhiimg.setController(controller);
        initPresenter();
        return root;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.clear();
        menu.add(0, SAVE_PIC_ID, 0, "保存图片到本地");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == SAVE_PIC_ID) {
            mPrensenter.saveImg(mUrl, new GirlPreviewPresenter.SaveImgCallback() {
                @Override
                public void onSuccess(String path) {
                    Toast.makeText(getContext(), R.string.save_image_success, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(String errorMsg) {
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPrensenter.unSubscribe();
    }

    @Override
    public void onBackPressed() {
        FragmentUtils.popBackStack(getActivity());
    }


    @Override
    public void initPresenter() {
        mPrensenter = new GirlPreviewPresenter(getActivity(), this);
    }

    @Override
    public void fillData(List data) {

    }

    /**
     *  监听 drawee 图片加载
     */
    private ControllerListener mControllerListener = new BaseControllerListener<ImageInfo>() {

        @Override
        /**
         * 图片下载成功时
         */
        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
            if (imageInfo == null) {
                return;
            }
            // 图片加载成功之后可以使用 ContextMenu
            registerForContextMenu(meizhiimg);
            QualityInfo qualityInfo = imageInfo.getQualityInfo();
            FLog.i(LOG_TAG, "Final image received! Size %d x %d Quality level %d, good enough: %s, full quality: %s",
                    imageInfo.getWidth(),
                    imageInfo.getHeight(),
                    qualityInfo.getQuality(),
                    qualityInfo.isOfGoodEnoughQuality(),
                    qualityInfo.isOfFullQuality());
            Logger.i(String.format("Final image received! Size %d x %d Quality level %d, good enough: %s, full quality: %s",
                    imageInfo.getWidth(),
                    imageInfo.getHeight(),
                    qualityInfo.getQuality(),
                    qualityInfo.isOfGoodEnoughQuality(),
                    qualityInfo.isOfFullQuality()));
        }

        @Override
        public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
            super.onIntermediateImageSet(id, imageInfo);
        }

        @Override
        /**
         * 图片下载失败时
         */
        public void onFailure(String id, Throwable throwable) {
            super.onFailure(id, throwable);
            Toast.makeText(getContext(), "貌似出了点问题..", Toast.LENGTH_SHORT).show();
        }
    };
}