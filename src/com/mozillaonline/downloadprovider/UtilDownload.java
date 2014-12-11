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
import com.mozillaonline.providers.downloads.DownloadService;
import com.mozillaonline.providers.downloads.Downloads;

/**
 * a modify of DownloadProviderActivity.java
 * 
 * @author wangxc <br/>
 */
public class UtilDownload extends Activity implements OnClickListener{
    private static final String TAG = UtilDownload.class.getName();
    public static final String PATHDIR = "pathDir";
    public static final String URL = "url";
    public static final String NAME = "name";

    private BroadcastReceiver mReceiver;
    // ui declare
    public ImageView itemsIcon;
    public TextView itemsTitle;
    public TextView itemsDesc;
    public Button operateBtn;
    public ProgressBar progressBar;
    public TextView received_progress_percent;
    public TextView received_progress_number;
    public RelativeLayout received_progressBar;

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
    private int mDateColumnId;
    private int mIdColumnId;
    private int mUriColumnId;
    private long mDownloadId;
    private int mPositionCursor;// 当前游标位置
    private int mLocalUriColumnId;
    private int status;

    private String mPathLocal;
    private String pathDir;
    private String name;
    private String url;
    
    private MyFileObserver myFileObserver;
    private String mTitle = null ;
    private long mTotalBytes = 0 ;
    private File myFile;
    /** 已跳转过安装界面*/
    private boolean flagOpenedInstall=false;
    private boolean flagCanStartFileObserver=false; 
    
    public UtilDownload() {
        mDownloadManager = new DownloadManager(getContentResolver(), getPackageName());
        startDownloadService();
        registerContentObservers();
    }
    public void download(){
        DownloadManager.Query baseQuery = new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true);
        mSizeSortedCursor = mDownloadManager.query(baseQuery.orderBy(DownloadManager.COLUMN_TOTAL_SIZE_BYTES,
                DownloadManager.Query.ORDER_DESCENDING));

        if (haveCursors()) {
            startManagingCursor(mSizeSortedCursor);

            mIdColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
            mTitleColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE);
            mStatusColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
            mReasonColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
            mTotalBytesColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            mCurrentBytesColumnId = mSizeSortedCursor
                    .getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            mMediaTypeColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE);
            mDateColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);
            mUriColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_URI);
            mLocalUriColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI);

        }

        if (!hasDownloadRecord(mSizeSortedCursor, url)) {
            startDownload(url);
        }//此时mDownloadId确定
//        DownloadThread.setOnDownLoadInfoReport(this);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_dialog);
        getData();
        mDownloadManager = new DownloadManager(getContentResolver(), getPackageName());
        buildComponents();
        startDownloadService();
        registerContentObservers();
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        DownloadManager.Query baseQuery = new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true);
        mSizeSortedCursor = mDownloadManager.query(baseQuery.orderBy(DownloadManager.COLUMN_TOTAL_SIZE_BYTES,
                DownloadManager.Query.ORDER_DESCENDING));

        if (haveCursors()) {
            startManagingCursor(mSizeSortedCursor);

            mIdColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
            mTitleColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE);
            mStatusColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
            mReasonColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
            mTotalBytesColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            mCurrentBytesColumnId = mSizeSortedCursor
                    .getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            mMediaTypeColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE);
            mDateColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);
            mUriColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_URI);
            mLocalUriColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI);

        }

        if (!hasDownloadRecord(mSizeSortedCursor, url)) {
            startDownload(url);
        }//此时mDownloadId确定
//        DownloadThread.setOnDownLoadInfoReport(this);
    }

    private void getData() {
        Intent intent = getIntent();
        pathDir = intent.getStringExtra(PATHDIR);
        name = intent.getStringExtra(NAME);
        url = intent.getStringExtra(URL);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (haveCursors()) {
            refresh();

        }
    }

    /**
     * Requery the database and update the UI.<br>
     * Register an observer that is called when changes happen to the content
     * backing this cursor. <br>
     * Typically the data set won't change until requery() is called.
     */
    private void refresh() {
        if(flagCanStartFileObserver){
            startFileObserver();
            return;
        }else{
            mSizeSortedCursor.requery();
            doInvalidateUi();            
        }
    }

    private boolean haveCursors() {
        return mSizeSortedCursor != null;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        getContentResolver().unregisterContentObserver(mContentObserver);
        if(myFileObserver!=null) myFileObserver.stopWatching();
        super.onDestroy();
    }

    private void buildComponents() {
        itemsIcon = (ImageView) findViewById(R.id.itemsIcon);
        itemsTitle = (TextView) findViewById(R.id.itemsTitle);
        itemsDesc = (TextView) findViewById(R.id.itemsDesc);
        operateBtn = (Button) findViewById(R.id.operateBtn);
        progressBar = (ProgressBar) findViewById(R.id.received_progress);
        received_progress_percent = (TextView) findViewById(R.id.received_progress_percent);
        received_progress_number = (TextView) findViewById(R.id.received_progress_number);
        received_progressBar = (RelativeLayout) findViewById(R.id.received_progressBar);
    }

    private void startDownloadService() {
        Intent intent = new Intent();
        intent.setClass(this, DownloadService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.start_download_button:

                // startDownload(URL3);

                break;
            default:
                break;
        }
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
        if(mSizeSortedCursor.getString(mLocalUriColumnId) != null&&totalBytes!=0){
            mPathLocal=getPathLocal(mSizeSortedCursor.getString(mLocalUriColumnId));
            mTotalBytes=totalBytes;
            flagCanStartFileObserver=true;
        }
        if (title.length() == 0) {
            title = getString(R.string.missing_title);
        }
        itemsTitle.setText(title);
        int progress = getProgressValue(totalBytes, currentBytes);

        boolean indeterminate = status == DownloadManager.STATUS_PENDING;
        progressBar.setIndeterminate(indeterminate);
        if (!indeterminate) {
            progressBar.setProgress(progress);
        }
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            progressBar.setVisibility(View.GONE);

        } else if (status == DownloadManager.STATUS_FAILED) {
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "下载失败，请退出重试", Toast.LENGTH_LONG).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
        final int pencent = (int) (currentBytes * 100 / totalBytes);

        received_progress_number.setText(getSizeText(currentBytes) + "/" + getSizeText(totalBytes));
        received_progress_percent.setText(pencent + "%");
        if (status == DownloadManager.STATUS_SUCCESSFUL) {

            openCurrentDownload(mSizeSortedCursor);
            finish();
        }
    }

    private String getSizeText(long totalBytes) {
        String sizeText = "";
        if (totalBytes >= 0) {
            sizeText = Formatter.formatFileSize(this, totalBytes);
        }
        return sizeText;
    }

    public int getProgressValue(long totalBytes, long currentBytes) {
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
            //notificate invalidate ui
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
                    mPathLocal =getPathLocal(mPathLocal);
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
                    //上次退出时保存链接有效性为未确定状态
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
        if(flagOpenedInstall) return;
        else flagOpenedInstall=true;
        Log.d("开始安装");
        Uri localUri = Uri.parse(cursor.getString(mLocalUriColumnId));
        try {
            getContentResolver().openFileDescriptor(localUri, "r").close();
        } catch (FileNotFoundException exc) {
            Log.d(TAG, "Failed to open download " + cursor.getLong(mIdColumnId) + "  " + exc);
            Toast.makeText(this, getString(R.string.dialog_file_missing_body), Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException exc) {
            // close() failed, not a problem
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(localUri, cursor.getString(mMediaTypeColumnId));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.download_no_application_title, Toast.LENGTH_LONG).show();
        }
    }

    private void registerContentObservers() {
        Uri uri = Downloads.ALL_DOWNLOADS_CONTENT_URI;
        // 注册内容观察者
        Log.e(TAG, "注册内容观察者");
        getContentResolver().registerContentObserver(uri, true, mContentObserver);
        
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e("KeyEvent.KEYCODE_BACK status:"+status);
//            if (status == DownloadManager.STATUS_RUNNING||status==DownloadManager.STATUS_PENDING) {
                Log.e("excute pauseDownload");
                mDownloadManager.pauseDownload(mDownloadId);
//            }
        }
        return super.onKeyDown(keyCode, event);
    }
    private class MyFileObserver extends FileObserver{
        private String myPath;
        public MyFileObserver(String path, int mask) {
            super(path, mask);
            myPath=path;
        }

        @Override
        public void onEvent(int event, String path) {
            switch(event){

                case FileObserver.MODIFY:
//                    Log.e("FileObserver path:"+path+" was modified,myPath:"+myPath);
                    runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            doInvalidateUiV2();
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
        if(mTitle==null){
            mTitle=new File( getPathLocal(mPathLocal)).getName();
        }
        if(myFile==null) {
            myFile=new File(getPathLocal(mPathLocal));
        }
        currentBytes=myFile.length();
        
        itemsTitle.setText(mTitle);
        progressBar.setIndeterminate(false);
        int progress = getProgressValue(mTotalBytes, currentBytes);
        progressBar.setProgress(progress);
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            
        } else if (status == DownloadManager.STATUS_FAILED) {
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "下载失败，请退出重试", Toast.LENGTH_LONG).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
        int pencent = 0;
        if(mTotalBytes!=0){
            pencent = (int) (currentBytes * 100 / mTotalBytes);            
        }

        received_progress_number.setText(getSizeText(currentBytes) + "/" + getSizeText(mTotalBytes));
        received_progress_percent.setText(pencent + "%");
        if (currentBytes==mTotalBytes) {
            Log.d("开始安装");
            openCurrentDownload(mSizeSortedCursor);
            finish();
        }
    }
    /**有时候pathLocal为file:///开头，有时候为/sdcard...
     * @param path
     * @return
     */
    String getPathLocal(String path){
        return path.startsWith("file:")?path.substring(7).replace("//", "/"):path.replace("//", "/");
    }
    
    private void startFileObserver() {
        if(mPathLocal!=null&&myFileObserver==null) {
            Log.e("mPathLocal:"+mPathLocal+" start fileobserver");
            myFileObserver= new MyFileObserver(mPathLocal, FileObserver.MODIFY);
            myFileObserver.startWatching();
        }
    }
}