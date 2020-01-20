package com.example.picpractice;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.DateUtils;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    private static int diffNum = 0;
    private GridImageAdapter mAdapter;
    private List<LocalMedia> selectList = new ArrayList<>();
    private int chooseMode = PictureMimeType.ofAll();
    private int themeId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        themeId = R.style.picture_default_style;
        RecyclerView recyclerView = findViewById(R.id.recycler);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(MainActivity.this, 4, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        mAdapter = new GridImageAdapter(MainActivity.this, onAddPicClickListener);
        mAdapter.setList(selectList);
        mAdapter.setSelectMax(5);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new GridImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (selectList.size() > 0) {
                    LocalMedia media = selectList.get(position);
                    String pictureType = media.getPictureType();
                    int mediaType = PictureMimeType.pictureToVideo(pictureType);
                    switch (mediaType) {
                        case 1:
                            // 预览图片 可自定长按保存路径
                            //PictureSelector.create(MainActivity.this).themeStyle(themeId).externalPicturePreview(position, "/custom_file", selectList);
                            PictureSelector.create(MainActivity.this).themeStyle(themeId).openExternalPreview(position, selectList);
                            break;
                        case 2:
                            // 预览视频
                            PictureSelector.create(MainActivity.this).externalPictureVideo(media.getPath());
                            break;
                        case 3:
                            // 预览音频
                            PictureSelector.create(MainActivity.this).externalPictureAudio(media.getPath());
                            break;
                    }
                }
            }
        });
        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        RxPermissions permissions = new RxPermissions(this);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    PictureFileUtils.deleteCacheDirFile(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.picture_jurisdiction), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private GridImageAdapter.onAddPicClickListener onAddPicClickListener = new GridImageAdapter.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            // 进入相册 以下是例子：不需要的api可以不写
            PictureSelector.create(MainActivity.this)
                    .openGallery(chooseMode)// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                    .theme(themeId)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                    .maxSelectNum(2)// 最大图片选择数量
                    .minSelectNum(2)// 最小选择数量
                    .imageSpanCount(4)// 每行显示个数
                    .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选, PictureConfig.SINGLE
                    .previewImage(true)// 是否可预览图片
                    .previewVideo(false)// 是否可预览视频
                    .enablePreviewAudio(true) // 是否可播放音频
                    .isCamera(true)// 是否显示拍照按钮
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                    //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                    .enableCrop(false)// 是否裁剪
                    .compress(false)// 是否压缩
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    //.compressSavePath(getPath())//压缩图片保存地址
                    //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                    .glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                    .isGif(true)// 是否显示gif图片
                    .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                    .circleDimmedLayer(false)// 是否圆形裁剪
                    .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                    .openClickSound(false)// 是否开启点击声音
                    .selectionMedia(selectList)// 是否传入已选图片
                    .withAspectRatio(3, 2)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
//                    .isDragFrame(false)// 是否可拖动裁剪框(固定)
//                    .videoMaxSecond(15)
//                    .videoMinSecond(10)
                    //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                    //.cropCompressQuality(90)// 裁剪压缩质量 默认100
                    .minimumCompressSize(100)// 小于100kb的图片不压缩
                    //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                    //.rotateEnabled(true) // 裁剪是否可旋转图片
                    //.scaleEnabled(true)// 裁剪是否可放大缩小图片
                    //.videoQuality()// 视频录制质量 0 or 1
                    //.videoSecond()//显示多少秒以内的视频or音频也可适用
                    //.recordVideoSecond()//录制视频秒数 默认60s
                    .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data);

                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                    for (LocalMedia media : selectList) {
                        Log.i("图片-----》", media.getPath());

                    }
                    mAdapter.setList(selectList);
                    mAdapter.notifyDataSetChanged();

                    try {
                        Bitmap bitmap8 = ThumbnailUtils.extractThumbnail(MediaStore.Images.Media.getBitmap(this.getContentResolver(), getImageStreamFromExternal(selectList.get(0).getPath())), 8, 8);
                        Bitmap bitmap9 = ThumbnailUtils.extractThumbnail(MediaStore.Images.Media.getBitmap(this.getContentResolver(), getImageStreamFromExternal(selectList.get(1).getPath())), 8, 8);
                        diff(imgUtils.binaryString2hexString(imgUtils.getBinary(imgUtils.convertGreyImg(bitmap8), imgUtils.getAvg(bitmap8))), imgUtils.binaryString2hexString(imgUtils.getBinary(imgUtils.convertGreyImg(bitmap9), imgUtils.getAvg(bitmap9))));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    /**
     * 自定义压缩存储地址
     *
     * @return
     */
    private String getPath() {
        String path = Environment.getExternalStorageDirectory() + "/Luban/image/";
        File file = new File(path);
        if (file.mkdirs()) {
            return path;
        }
        return path;
    }

    private void diff(String s1, String s2) {
        char[] s1s = s1.toCharArray();
        char[] s2s = s2.toCharArray();

        for (int i = 0; i < s1s.length; i++) {
            if (s1s[i] != s2s[i]) {
                diffNum++;
            }
        }
        System.out.println("diffNum=" + diffNum);
        if (diffNum <= 5) {
            Toast.makeText(MainActivity.this, "所选照片及其相似", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "所选照片差别较大", Toast.LENGTH_SHORT).show();

        }
        diffNum=0;
    }

    public static Uri getImageStreamFromExternal(String imageName) {


        File picPath = new File(imageName);
        Uri uri = null;
        if (picPath.exists()) {
            uri = Uri.fromFile(picPath);
        }

        return uri;
    }


    public static class GridImageAdapter extends
            RecyclerView.Adapter<GridImageAdapter.ViewHolder> {
        public static final int TYPE_CAMERA = 1;
        public static final int TYPE_PICTURE = 2;
        private LayoutInflater mInflater;
        private List<LocalMedia> list = new ArrayList<>();
        private int selectMax = 9;
        private Context context;
        /**
         * 点击添加图片跳转
         */
        private onAddPicClickListener mOnAddPicClickListener;

        public interface onAddPicClickListener {
            void onAddPicClick();
        }

        public GridImageAdapter(Context context, onAddPicClickListener mOnAddPicClickListener) {
            this.context = context;
            mInflater = LayoutInflater.from(context);
            this.mOnAddPicClickListener = mOnAddPicClickListener;
        }

        public void setSelectMax(int selectMax) {
            this.selectMax = selectMax;
        }

        public void setList(List<LocalMedia> list) {
            this.list = list;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView mImg;
            LinearLayout ll_del;
            TextView tv_duration;

            public ViewHolder(View view) {
                super(view);
                mImg = view.findViewById(R.id.fiv);
                ll_del = view.findViewById(R.id.ll_del);
                tv_duration = view.findViewById(R.id.tv_duration);
            }
        }

        @Override
        public int getItemCount() {
            if (list.size() < selectMax) {
                return list.size() + 1;
            } else {
                return list.size();
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isShowAddItem(position)) {
                return TYPE_CAMERA;
            } else {
                return TYPE_PICTURE;
            }
        }

        /**
         * 创建ViewHolder
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = mInflater.inflate(R.layout.gv_filter_image,
                    viewGroup, false);
            final ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        private boolean isShowAddItem(int position) {
            int size = list.size() == 0 ? 0 : list.size();
            return position == size;
        }

        /**
         * 设置值
         */
        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            //少于8张，显示继续添加的图标
            if (getItemViewType(position) == TYPE_CAMERA) {
                viewHolder.mImg.setImageResource(R.mipmap.addimg_1x);
                viewHolder.mImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnAddPicClickListener.onAddPicClick();
                    }
                });
                viewHolder.ll_del.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.ll_del.setVisibility(View.VISIBLE);
                viewHolder.ll_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int index = viewHolder.getAdapterPosition();
                        // 这里有时会返回-1造成数据下标越界,具体可参考getAdapterPosition()源码，
                        // 通过源码分析应该是bindViewHolder()暂未绘制完成导致，知道原因的也可联系我~感谢
                        if (index != RecyclerView.NO_POSITION) {
                            list.remove(index);
                            notifyItemRemoved(index);
                            notifyItemRangeChanged(index, list.size());

                        }
                    }
                });
                LocalMedia media = list.get(position);
                int mimeType = media.getMimeType();
                String path = "";
                if (media.isCut() && !media.isCompressed()) {
                    // 裁剪过
                    path = media.getCutPath();
                } else if (media.isCompressed() || (media.isCut() && media.isCompressed())) {
                    // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                    path = media.getCompressPath();
                } else {
                    // 原图
                    path = media.getPath();
                }
                // 图片
                if (media.isCompressed()) {
                    Log.i("compress image result:", new File(media.getCompressPath()).length() / 1024 + "k");
                    Log.i("压缩地址::", media.getCompressPath());
                }

                Log.i("原图地址::", media.getPath());
                int pictureType = PictureMimeType.isPictureType(media.getPictureType());
                if (media.isCut()) {
                    Log.i("裁剪地址::", media.getCutPath());
                }
                long duration = media.getDuration();
                viewHolder.tv_duration.setVisibility(pictureType == PictureConfig.TYPE_VIDEO
                        ? View.VISIBLE : View.GONE);
                if (mimeType == PictureMimeType.ofAudio()) {
                    viewHolder.tv_duration.setVisibility(View.VISIBLE);
                    Drawable drawable = ContextCompat.getDrawable(context, R.drawable.picture_audio);
                    StringUtils.modifyTextViewDrawable(viewHolder.tv_duration, drawable, 0);
                } else {
                    Drawable drawable = ContextCompat.getDrawable(context, R.drawable.video_icon);
                    StringUtils.modifyTextViewDrawable(viewHolder.tv_duration, drawable, 0);
                }
                viewHolder.tv_duration.setText(DateUtils.timeParse(duration));
                if (mimeType == PictureMimeType.ofAudio()) {
                    viewHolder.mImg.setImageResource(R.drawable.audio_placeholder);
                } else {
                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .placeholder(R.color.color_f6)
                            .diskCacheStrategy(DiskCacheStrategy.ALL);
                    Glide.with(viewHolder.itemView.getContext())
                            .load(path)
                            .apply(options)
                            .into(viewHolder.mImg);
                }
                //itemView 的点击事件
                if (mItemClickListener != null) {
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int adapterPosition = viewHolder.getAdapterPosition();
                            mItemClickListener.onItemClick(adapterPosition, v);
                        }
                    });
                }
            }
        }

        protected OnItemClickListener mItemClickListener;

        public interface OnItemClickListener {
            void onItemClick(int position, View v);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.mItemClickListener = listener;
        }
    }

}
