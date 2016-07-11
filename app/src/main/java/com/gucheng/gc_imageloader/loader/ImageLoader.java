package com.gucheng.gc_imageloader.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 图片缓存
 * Created by gc on 2016/7/9.
 */
public class ImageLoader {
    public static ImageLoader mInstance;
    /**
     * 图片缓存核心对象
     */
    private LruCache<String,Bitmap> mLruCache;

    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    private static int DEFAULT_THREAD_COUNT = 1;
    /**
     * 队列调度方式
     */
    private Type mType = Type.FIFO;

    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTaskQueue;

    /**
     * 轮询线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHanler;

    /**
     * UI线程中的Handler
     */
    private Handler mUiHandler;

    /**
     * 信号量来控制  并发类 同步线程顺序
     */
    private Semaphore mSemaphorePoolThreadHanler = new Semaphore(0);

    private Semaphore mSemaphoreThreadPool;


    public enum Type{
        FIFO , LIFO/* Last in first out*/
    }



    private ImageLoader(int threadCount , Type type) {
        init(threadCount,type);
    }

    public static ImageLoader getInstance(int threadCount , Type type){
        if (mInstance == null){
            synchronized (ImageLoader.class){
                if (mInstance == null){
                    mInstance = new ImageLoader(threadCount,type);
                }
            }
        }
       return mInstance;
    }

    public static ImageLoader getInstance(){
        if (mInstance == null){
            synchronized (ImageLoader.class){
                if (mInstance == null){
                    mInstance = new ImageLoader(DEFAULT_THREAD_COUNT,Type.LIFO);
                }
            }
        }
        return mInstance;
    }


    private void init(int threadCount , Type type) {
        //后台轮询线程
        mPoolThread = new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHanler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        //线程池取一个任务执行
                        mThreadPool.execute(getTask());
                        /**
                         * 执行设定个数个任务，阻塞,
                         * 每完成一个任务 释放一个信号量
                         * 然后再执行下一个任务
                         */
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //释放一个信号量
                mSemaphorePoolThreadHanler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        /**
         * 应用最大内存
         */
        long maxMemory = Runtime.getRuntime().maxMemory();
        int cacheMemory = (int) (maxMemory / 8);
        mLruCache = new LruCache<String, Bitmap>(cacheMemory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //每一行占用字节数 *　高度
                return value.getRowBytes() * value.getHeight();
            }
        };

        //创建线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<Runnable>();
        mType = type;

        /**
         * 并发执行
         */
        mSemaphoreThreadPool = new Semaphore(threadCount);
    }

    /**
     * 从任务队列取一个方法
     * @return
     */
    private Runnable getTask() {
        if (mType == Type.FIFO){
            return mTaskQueue.removeFirst();
        }else if (mType == Type.LIFO){
            return mTaskQueue.removeLast();
        }

        return null;
    }


    /**
     * 根据path展示图片
     * @param path
     * @param imageView
     */
    public void loadImage(final String path , final ImageView imageView){
        imageView.setTag(path);

        if (mUiHandler == null){
            mUiHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //获取图片，显示
                    ImageBeanHolder holder = (ImageBeanHolder) msg.obj;
                    ImageView imageView = holder.imageView;
                    Bitmap bitmap = holder.bitmap;
                    String path = holder.path;
                    //将path与getTag存储路径比较
                    if (imageView.getTag().toString().equals(path)){
                        imageView.setImageBitmap(bitmap);
                    }
                }
            };
        }
        //先从内存获取
        Bitmap bitmap = getBitmapFromCache(path);

        if (bitmap != null){
            refreshView(bitmap,path,imageView);
        }else {
            addTask(new Runnable(){
                @Override
                public void run() {
                    //加载图片  压缩图片
                    /**
                     * 1，获取图片需要显示大小
                     */
                    ImageSize imageSize = getImageViewSize(imageView);
                    /**
                     * 2，压缩图片
                     */
                    Bitmap bm = decodeSampledBitmapFromPath(path,imageSize.width,imageSize.height);
                    /**
                     * 图片加入缓存
                     */
                    addBitmapToLruCache(path,bm);

                    refreshView(bm,path,imageView);

                    /**
                     * 释放一个信号量，执行下一个任务
                     */
                    mSemaphoreThreadPool.release();
                }
            });
        }


    }

    /**
     * 图片加入缓存
     * @param path
     * @param bm
     */
    private void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromCache(path) == null){
            if (bm != null){
                mLruCache.put(path,bm);
            }
        }
    }

    /**
     * 根据图片需要显示的宽高压缩图片
     * @param path
     * @param width
     * @param height
     * @return
     */
    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        //获取图片宽高，不加载到内存
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);

        options.inSampleSize = calculateInSampleSize(options,width,height);

        //使用得到的inSampleSize再次解析
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }


    /**
     * 根据需求宽高 和 图片实际宽高  计算simpleSize
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;

        if (width > reqWidth || height > reqHeight){
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);
            /**
             * 为了节约内存，取较大值，如果为了不拉伸，需要取较小值
             */
            inSampleSize = Math.max(widthRadio,heightRadio);
        }
        return inSampleSize;
    }


    /**
     * 根据ImageView获取适当的压缩宽和高
     * @param imageView
     * @return
     */
    private ImageSize getImageViewSize(ImageView imageView) {
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

        ImageSize imageSize = new ImageSize();
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

        int width = imageView.getWidth();//获取imageView实际宽度

        if (width <= 0){
            width = lp.width;//获取layout声明宽度
        }

        if (width <= 0){
//            width = imageView.getMaxWidth();//检查最大值
            width = getImageViewFieldValue(imageView , "mMaxWidth");
        }

        if (width <= 0){
            width = displayMetrics.widthPixels;
        }


        int height = imageView.getHeight();//获取imageView实际宽度

        if (height <= 0){
            height = lp.height;//获取layout声明宽度
        }

        if (height <= 0){
//            height = imageView.getMaxHeight();//检查最大值
            height = getImageViewFieldValue(imageView , "mMaxHeight");
        }

        if (height <= 0){
            height = displayMetrics.heightPixels;
        }

        imageSize.width = width;
        imageSize.height = height;

        return imageSize;
    }

    /**
     * 通过反射 适应低版本sdk
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object,String fieldName){
        int value = 0;

        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE){
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return value;
    }

    private synchronized void addTask(Runnable runnable) {
        mTaskQueue.add(runnable);

       // if (mPoolThreadHanler != null) wait() 多线程并发
        try {
            if (mPoolThreadHanler == null)
                mSemaphorePoolThreadHanler.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mPoolThreadHanler.sendEmptyMessage(0x110);
    }

    /**
     * 刷新UI
     * @param bitmap
     */
    private void refreshView(Bitmap bitmap,String path,ImageView imageView) {
        Message message = Message.obtain();
        ImageBeanHolder holder = new  ImageBeanHolder();
        holder.bitmap = bitmap;
        holder.path = path;
        holder.imageView = imageView;
        message.obj = holder;
        mUiHandler.sendMessage(message);
    }

    /**
     * get the bitmap from cacheMemory
     * @param path
     * @return
     */
    private Bitmap getBitmapFromCache(String path) {
        Bitmap bitmap = mLruCache.get(path);
        return bitmap;
    }

    private class ImageBeanHolder{
        Bitmap bitmap;
        String path;
        ImageView imageView;
    }
    private class ImageSize {
        int width;
        int height;
    }
}
