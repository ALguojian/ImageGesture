package com.alguojian.imagegesture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alguojian.imagegesture.util.NoDoubleClickUtils;
import com.alguojian.imagegesture.view.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.guojian.aldialog.dialog.LoadingDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ImageGesture extends AppCompatActivity {

    private static final String KEY_TAG = "ImageGesture.this";

    private List<String> list = new ArrayList<>();

    private static final String KEY_LIST = "LIST";
    private static final String KEY_INDEX = "INDEX";

    /**
     * 表示当前页面的索引
     */
    private int selectedIndex = 0;


    /**
     * 用于保存每个图片对象
     */
    private TreeMap<Integer, Bitmap> bitmaps = new TreeMap<>();

    /**
     * 现在是第几张
     */
    private TextView indicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        setContentView(R.layout.activity_image_gesture);
        ViewPager viewPager = findViewById(R.id.viewPager);

        list = getIntent().getStringArrayListExtra(KEY_LIST);

        indicator = findViewById(R.id.indicator);

        indicator.setText(getIntent().getIntExtra(KEY_INDEX, 0) + 1 + "/" + list.size());

        TextView down = findViewById(R.id.down);

        down.setOnClickListener(v -> {

            if (NoDoubleClickUtils.isDoubleClick(ImageGesture.this)) {

                downloadImage();
            }
        });

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {

                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                PhotoView photoView = new PhotoView(ImageGesture.this);

                photoView.setClickable(true);
                photoView.setOnClickListener(v -> finish());

                photoView.enable();
                photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);


                loadImage(position, photoView, list.get(position));

                container.addView(photoView);

                return photoView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                indicator.setText(position + 1 + "/" + list.size());

                selectedIndex = position;
                System.out.println("-------------" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setCurrentItem(getIntent().getIntExtra(KEY_INDEX, 0));
    }


    /**
     * 下载图片,之前先要判断是否含有本地读取权限（权限判断不适配所有）
     */
    public void downloadImage() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //2、申请权限: 参数二：权限的数组；参数三：请求码
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
            }
        } else {

            if (bitmaps.isEmpty())
                return;

            saveImageToGallery(bitmaps.get(selectedIndex));
        }
    }

    /**
     * 本地保存图片bitmap对象，通知相册
     *
     * @param downloadImgBitmap
     */
    private void saveImageToGallery(Bitmap downloadImgBitmap) {


        Toast.makeText(this, "正在下载中...", Toast.LENGTH_SHORT).show();

        final String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
                ? Environment.getExternalStorageDirectory().getAbsolutePath()
                : "/xiaodian/cache";//保存到SD卡

        // 首先保存图片
        File appDir = new File(SAVE_PIC_PATH + "/images/");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        long nowSystemTime = System.currentTimeMillis();
        String fileName = nowSystemTime + ".png";
        File file = new File(appDir, fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            downloadImgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendBroadcast(
                new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));

        Toast.makeText(this, "已保存到本地相册", Toast.LENGTH_SHORT).show();

    }


    /**
     * 加载图片资源
     *
     * @param position
     * @param photoView
     * @param string
     */
    private void loadImage(final int position, PhotoView photoView, String string) {

        final LoadingDialog loadingDialog = new LoadingDialog(ImageGesture.this, 2);

        loadingDialog.show();


        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        RequestListener<Drawable> bitmapRequestListener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                loadingDialog.dismiss();
                Toast.makeText(ImageGesture.this,"加载失败",Toast.LENGTH_SHORT).show();

                if (null != e)
                    Log.d(KEY_TAG, e.getMessage());
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                Log.d(KEY_TAG, "加载成功");

                loadingDialog.dismiss();

                if (null != resource) {
                    bitmaps.put(position, ((BitmapDrawable) resource).getBitmap());
                }
                return false;
            }
        };

        Glide.with(this)
                .load(string)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(bitmapRequestListener)
                .apply(requestOptions)
                .into(photoView);

    }

    /**
     * 传递过来数组
     *
     * @param context 上下文对象
     * @param positon 索引
     * @param list    数组
     */
    public static void setDate(Context context, int positon, @NonNull List<String> list) {
        Intent intent = new Intent(context, ImageGesture.class);
        // 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
        intent.putStringArrayListExtra(KEY_LIST, (ArrayList<String>) list);
        intent.putExtra(KEY_INDEX, positon);
        context.startActivity(intent);
    }
}
