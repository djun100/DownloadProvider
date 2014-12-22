package com.mozillaonline.downloadprovider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.k.File.UtilFile;
import com.k.application.Log;
import com.mozillaonline.providers.DownloadManager;
import com.mozillaonline.providers.DownloadManager.Request;
import com.mozillaonline.providers.downloads.Constants;
import com.mozillaonline.providers.downloads.DownloadService;
import com.mozillaonline.providers.downloads.Downloads;

/**
 * a modify of DownloadProviderActivity.java
 * 一个UtilDownload代表一个文件下载实例
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        getContentResolver().unregisterContentObserver(mContentObserver);
        if (myFileObserver != null)
            myFileObserver.stopWatching();
        super.onDestroy();
    }
 * @author wangxc <br/>
 */
public class UtilDownload {
    private static final String TAG = UtilDownload.class.getName();
    public static final String PATHDIR = "pathDir";
    public static final String URL = "url";
    public static final String NAME = "name";

    DownloadManager mDownloadManager;

    // func declare
    private Cursor mSizeSortedCursor;
    private MyContentObserver mContentObserver = new MyContentObserver();

    private int mTitleColumnId;
    private int mStatusColumnId;
    private int mReasonColumnId;
    private int mTotalBytesColumnId;
    private int mCurrentBytesColumnId;
    private int mMediaTypeColumnId;
    private int mIdColumnId;
    private int mUriColumnId;
    private long mDownloadId;
    private int mPositionCursor;// 当前游标位置
    private int mLocalUriColumnId;
    private int status;

    private String mPathLocal;
    private String pathDir;

    private MyFileObserver myFileObserver;
    private long mTotalBytes = 0;
    private File myFile;
    private Activity mContext;
    private IReportDownloadProgress iCallBack;
    /** 已跳转过安装界面 */
    private boolean flagOpenedInstall = false;
    private boolean flagCanStartFileObserver = false;

    public UtilDownload(Activity context) {
        mContext = context;
        mDownloadManager = new DownloadManager(mContext.getContentResolver(), mContext.getPackageName());
        startDownloadService();
        registerContentObservers();
    }

    /**
     * @param iCallBack
     * @param url
     * @param name is useless
     * @param pathDir eg: Download 则保存在sdCard的Download文件夹下
     */
    public void download(IReportDownloadProgress iCallBack, String url, String name, String pathDir) {
        this.iCallBack = iCallBack;
        this.pathDir = pathDir;
        DownloadManager.Query baseQuery = new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true);
        mSizeSortedCursor = mDownloadManager.query(baseQuery.orderBy(DownloadManager.COLUMN_TOTAL_SIZE_BYTES,
                DownloadManager.Query.ORDER_DESCENDING));

        if (haveCursors()) {
            mContext.startManagingCursor(mSizeSortedCursor);
            mIdColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
            mTitleColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE);
            mStatusColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
            mReasonColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
            mTotalBytesColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            mCurrentBytesColumnId = mSizeSortedCursor
                    .getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            mMediaTypeColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE);
            mUriColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_URI);
            mLocalUriColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI);

        }

        if (!hasDownloadRecord(mSizeSortedCursor, url)) {
            startDownload(url);
        }
    }

    /**
     * Requery the database and update the UI.<br>
     * Register an observer that is called when changes happen to the content
     * backing this cursor. <br>
     * Typically the data set won't change until requery() is called.
     */
    private void refresh() {
        if (flagCanStartFileObserver) {
            startFileObserver();
            return;
        } else {
            mSizeSortedCursor.requery();
            doInvalidateUi();
        }
    }

    private boolean haveCursors() {
        return mSizeSortedCursor != null;
    }

    private void startDownloadService() {
        Intent intent = new Intent();
        intent.setClass(mContext, DownloadService.class);
        mContext.startService(intent);
    }

    private void startDownload(String url) {
        Uri srcUri = Uri.parse(url);
        DownloadManager.Request request = new Request(srcUri);

        UtilFile.makeDirsInExternalStorageDirectory(UtilFile.addStartPathSeparator(pathDir));
        request.setDestinationInExternalPublicDir(pathDir, "/");
        // UtilFile.makeDirsInExternalStorageDirectory("/Download");
        // request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"/");
        //
        request.setDescription("request.setDescription");
        mDownloadId = mDownloadManager.enqueue(request);
        Log.e(TAG, "mDownloadId:" + mDownloadId);
        mSizeSortedCursor.requery();
        moveToDownload(mDownloadId);
        mPathLocal = mSizeSortedCursor.getString(mLocalUriColumnId);
        Log.d("mPathLocal:" + mPathLocal);
    }

    /**
     * 更新下载UI
     */
    private void doInvalidateUi() {
        String title = null;
        long totalBytes = 0;
        long currentBytes = 0;
        if (mSizeSortedCursor.moveToPosition(mPositionCursor)) {
            Log.e(TAG, "数据行数：" + mSizeSortedCursor.getCount() + "\n" + "游标位置：" + mSizeSortedCursor.getPosition() + "\n"
                    + "status：" + status + "\n" + "数据列数：" + mSizeSortedCursor.getColumnCount());

            title = mSizeSortedCursor.getString(mTitleColumnId);
            totalBytes = mSizeSortedCursor.getLong(mTotalBytesColumnId);
            currentBytes = mSizeSortedCursor.getLong(mCurrentBytesColumnId);
            status = mSizeSortedCursor.getInt(mStatusColumnId);
        }
        if (mSizeSortedCursor.getString(mLocalUriColumnId) != null && totalBytes != 0) {
            mPathLocal = getPathLocal(mSizeSortedCursor.getString(mLocalUriColumnId));
            mTotalBytes = totalBytes;
            flagCanStartFileObserver = true;
        }
        if (status == DownloadManager.STATUS_FAILED) {
            Toast.makeText(mContext, "下载失败，请退出重试", Toast.LENGTH_LONG).show();
        } 
        //初次下载该文件，传入null,0,0
        iCallBack.onUpdate(mPathLocal, totalBytes, currentBytes);
        if (status == DownloadManager.STATUS_SUCCESSFUL) {

            openCurrentDownload(mSizeSortedCursor);
        }
    }

    public static String getSizeText(Context context,long totalBytes) {
        String sizeText = "";
        if (totalBytes >= 0) {
            sizeText = Formatter.formatFileSize(context, totalBytes);
        }
        return sizeText;
    }

    public static int getProgressValue(long totalBytes, long currentBytes) {
        if (totalBytes == -1) {
            return 0;
        }
        return (int) (currentBytes * 100 / totalBytes);
    }

    private class MyContentObserver extends ContentObserver {
        public MyContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.e(TAG, "trigger ContentObserver onChange()");
            // notificate invalidate ui
            refresh();
        }
    }

    /**
     * 是否下载过，包括下载完和未下载完
     * 
     * @param cursor
     * @return
     */
    public boolean hasDownloadRecord(Cursor cursor, String url) {
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uri = cursor.getString(mUriColumnId);
            Log.e(TAG, "hasDownloaded compare:" + uri);
            status = mSizeSortedCursor.getInt(mStatusColumnId);
            if (uri.equals(url)) {
                mDownloadId = cursor.getLong(mIdColumnId);
                mPositionCursor = cursor.getPosition();
                mPathLocal = cursor.getString(mLocalUriColumnId);
                Log.e(TAG, "匹配到下载id：" + mDownloadId);
                Log.e(TAG, "mStatusColumnId:" + status);
                Log.d("mPathLocal:" + mPathLocal);
                if (status == DownloadManager.STATUS_PAUSED) {
                    mDownloadManager.resumeDownload(mDownloadId);
                } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    Log.d("删除downloadid row  和文件");
                    deleteDownload(mDownloadId);
                    mPathLocal = getPathLocal(mPathLocal);
                    Log.d("文件名：" + mPathLocal);
                    File file = new File(mPathLocal);
                    if (file.exists()) {
                        Log.d("apk存在，执行删除");
                        boolean b = file.delete();
                        if (b) {
                            Log.d("删除成功");
                        }
                    }
                    // openCurrentDownload(mSizeSortedCursor);
                    return false;
                } else if (status == DownloadManager.STATUS_FAILED) {
                    Log.d(TAG, "hasDownloadRecord() DownloadManager.STATUS_FAILED");
                    mDownloadManager.restartDownload(mDownloadId);
                } else if (status == DownloadManager.STATUS_PENDING) {
                    Log.d(TAG, "hasDownloadRecord() DownloadManager.STATUS_PENDING");
                    // 上次退出时保存链接有效性为未确定状态
                    mDownloadManager.resumeDownload(mDownloadId);
                } else if (status == DownloadManager.STATUS_RUNNING) {
                    // 状态为什么会是running？？？
                    Log.d(TAG, "hasDownloadRecord() DownloadManager.STATUS_RUNNING,return true");
                    Log.d(TAG, "hasDownloadRecord() 状态错误，可能因为异常退出造成，尝试继续下载。。");
                    // mDownloadManager.fixDataBaseState(mDownloadId);
                    mDownloadManager.resumeDownload(mDownloadId);
                    return true;
                } else {
                    Log.d("hasDownloadRecord() 未知状态");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Send an Intent to open the download currently pointed to by the given
     * cursor.
     */
    private void openCurrentDownload(Cursor cursor) {
        if (flagOpenedInstall)
            return;
        else
            flagOpenedInstall = true;
        Log.d("开始安装");
        Uri localUri = Uri.parse(cursor.getString(mLocalUriColumnId));
        try {
            mContext.getContentResolver().openFileDescriptor(localUri, "r").close();
        } catch (FileNotFoundException exc) {
            Log.d(TAG, "Failed to open download " + cursor.getLong(mIdColumnId) + "  " + exc);
            Toast.makeText(mContext, mContext.getString(R.string.dialog_file_missing_body), Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException exc) {
            // close() failed, not a problem
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(localUri, cursor.getString(mMediaTypeColumnId));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(mContext, R.string.download_no_application_title, Toast.LENGTH_LONG).show();
        }
    }

    private void registerContentObservers() {
        Uri uri = Downloads.ALL_DOWNLOADS_CONTENT_URI;
        // 注册内容观察者
        Log.e(TAG, "注册内容观察者");
        mContext.getContentResolver().registerContentObserver(uri, true, mContentObserver);

    }

    /**
     * Delete a download from the Download Manager.
     */
    private void deleteDownload(long downloadId) {
        if (moveToDownload(downloadId)) {
            int status = mSizeSortedCursor.getInt(mStatusColumnId);
            boolean isComplete = status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED;
            String localUri = mSizeSortedCursor.getString(mLocalUriColumnId);
            if (isComplete && localUri != null) {
                String path = Uri.parse(localUri).getPath();
                if (path.startsWith(Environment.getExternalStorageDirectory().getPath())) {
                    mDownloadManager.markRowDeleted(downloadId);
                    return;
                }
            }
        }
        mDownloadManager.remove(downloadId);
        mSizeSortedCursor.requery();
    }

    /**
     * Move {@link #mDateSortedCursor} to the download with the given ID.
     * mDateSortedCursor游标移动到指定downloadId处
     * 
     * @return true if the specified download ID was found; false otherwise
     */
    private boolean moveToDownload(long downloadId) {
        for (mSizeSortedCursor.moveToFirst(); !mSizeSortedCursor.isAfterLast(); mSizeSortedCursor.moveToNext()) {
            if (mSizeSortedCursor.getLong(mIdColumnId) == downloadId) {
                return true;
            }
        }
        return false;
    }

    private class MyFileObserver extends FileObserver {
        private String myPath;
        private long timeLastReport=0;
        public MyFileObserver(String path, int mask) {
            super(path, mask);
            myPath = path;
        }
        public MyFileObserver(String mPathLocal) {
            super(mPathLocal);
            myPath = mPathLocal;
        }
        @Override
        public void onEvent(int event, String path) {
            switch (event) {
                //确保文件完全下载完发出更新UI通知，否则可能出现界面更新至9x%停止的状态
                case FileObserver.CLOSE_WRITE:
                    Log.w("关闭文件写入");
                case FileObserver.MODIFY:
                    mContext.runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            long now = System.currentTimeMillis();
                            if (now - timeLastReport > 100) {
                                doInvalidateUiV2();
                                timeLastReport = now;
                            }
                        }
                    });
                    break;
            }
        }

    }

    /**
     * 更新下载UI
     */

    private void doInvalidateUiV2() {
        long currentBytes = 0;
        if (myFile == null) {
            myFile = new File(getPathLocal(mPathLocal));
        }
        currentBytes = myFile.length();
        if (status == DownloadManager.STATUS_FAILED) {
            Toast.makeText(mContext, "下载失败，请重试", Toast.LENGTH_LONG).show();
        }
        iCallBack.onUpdate(mPathLocal, mTotalBytes, currentBytes);
    }

    /**
     * 有时候pathLocal为file:///开头，有时候为/sdcard...
     * 
     * @param path
     * @return
     */
    public static String getPathLocal(String path) {
        return path.startsWith("file:") ? path.substring(7).replace("//", "/") : path.replace("//", "/");
    }

    private void startFileObserver() {
        if (mPathLocal != null && myFileObserver == null) {
            Log.e("mPathLocal:" + mPathLocal + " start fileobserver");
            myFileObserver = new MyFileObserver(mPathLocal);
            myFileObserver.startWatching();
        }
    }
    /**
     * must call on destroy()
     */
    public void unRegister() {
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
        if (myFileObserver != null)
            myFileObserver.stopWatching();
    }
    /**
     * 暂停该UtilDownload对应的文件下载实例
     */
    public void pauseDownload(){
        Log.e("excute pauseDownload");
        mDownloadManager.pauseDownload(mDownloadId);
    }
    /**
     * 安装
     */
    public void openDownload(){
        Log.e("open Download");
        openCurrentDownload(mSizeSortedCursor);
    }
    public interface IReportDownloadProgress {
        public void onUpdate(String pathFile, long byteTotal, long byteCurrent);
    }
}