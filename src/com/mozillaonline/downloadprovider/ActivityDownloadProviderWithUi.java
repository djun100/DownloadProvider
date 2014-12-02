package com.mozillaonline.downloadprovider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Set;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mozillaonline.providers.DownloadManager;
import com.mozillaonline.providers.DownloadManager.Request;
import com.mozillaonline.providers.downloads.DownloadService;
import com.mozillaonline.providers.downloads.DownloadService.OnUiUpdateListener;
import com.mozillaonline.providers.downloads.Downloads;

/**a modify of DownloadProviderActivity.java
 * @author wangxc <br/>
 */
public class ActivityDownloadProviderWithUi extends Activity implements OnClickListener, OnUiUpdateListener{
    private static final String TAG = ActivityDownloadProviderWithUi.class.getName();

    private BroadcastReceiver mReceiver;
    // ui old declare
    /*
     * EditText mUrlInputEditText; Button mStartDownloadButton; Button
     * mShowDownloadListButton;
     */
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

    private static final String URL = "http://www.chinaums.com/static/ums2013/chinaums/app/download/qmf.apk";
    private static final String URL2 = "http://down.mumayi.com/41052/mbaidu";
    private static final String URL3 = "http://down.apk.hiapk.com/down?aid=1832508&em=13";
    // func declare
    private Cursor mSizeSortedCursor;
    private Cursor mDateSortedCursor;
    private MyContentObserver mContentObserver = new MyContentObserver();
    private MyDataSetObserver mDataSetObserver = new MyDataSetObserver();

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
    private int mPositionCursor;//当前游标位置
    private int mLocalUriColumnId;
    private int status;
    private DateFormat mDateFormat;
    private DateFormat mTimeFormat;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_dialog);

        mDownloadManager = new DownloadManager(getContentResolver(), getPackageName());
        buildComponents();
        startDownloadService();

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        DownloadManager.Query baseQuery = new DownloadManager.Query().setOnlyIncludeVisibleInDownloadsUi(true);
        mDateSortedCursor = mDownloadManager.query(baseQuery);
        mSizeSortedCursor = mDownloadManager.query(baseQuery.orderBy(DownloadManager.COLUMN_TOTAL_SIZE_BYTES,
                DownloadManager.Query.ORDER_DESCENDING));

        if (haveCursors()) {
            startManagingCursor(mSizeSortedCursor);

            mIdColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID);
            mTitleColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE);
            mStatusColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS);
            mReasonColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON);
            mTotalBytesColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            mCurrentBytesColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            mMediaTypeColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE);
            mDateColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP);
            mUriColumnId=mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_URI);
            mLocalUriColumnId = mSizeSortedCursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI);
            
            mDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
            mTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        }
        
        DownloadService.setListener(this);
        
        if(!hasDownloadRecord(mSizeSortedCursor,URL3)){
            startDownload(URL3);
           }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (haveCursors()) {
            registerContentObservers();
/*            mSizeSortedCursor.registerContentObserver(mContentObserver);
            mSizeSortedCursor.registerDataSetObserver(mDataSetObserver);*/
            refresh();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (haveCursors()) {
            getContentResolver().unregisterContentObserver(mContentObserver);  
/*            mSizeSortedCursor.unregisterContentObserver(mContentObserver);
            mSizeSortedCursor.unregisterDataSetObserver(mDataSetObserver);*/
            
        }
        //不然会出现unregister错误
        finish();
    }

    /**
     * Requery the database and update the UI.<br>
     * Register an observer that is called when changes happen to the content backing this cursor. <br>
     * Typically the data set won't change until requery() is called.
     */
    private void refresh() {
        mSizeSortedCursor.requery();
    }

    private boolean haveCursors() {
        return mSizeSortedCursor != null;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        //to do error
        if (status == DownloadManager.STATUS_RUNNING) {
            mDownloadManager.pauseDownload(mDownloadId);
        }
        super.onDestroy();
    }

    private void buildComponents() {
        // old
        /*
         * mUrlInputEditText = (EditText) findViewById(R.id.url_input_edittext);
         * mStartDownloadButton = (Button)
         * findViewById(R.id.start_download_button); mShowDownloadListButton =
         * (Button) findViewById(R.id.show_download_list_button);
         * 
         * mStartDownloadButton.setOnClickListener(this);
         * mShowDownloadListButton.setOnClickListener(this);
         * 
         * mUrlInputEditText.setText(URL3);
         */
        // new
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

//                    startDownload(URL3);                    

                break;
            default:
                break;
        }
    }

    private void startDownload(String url) {
        Uri srcUri = Uri.parse(url);
        DownloadManager.Request request = new Request(srcUri);
        //
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/");
        //
        request.setDescription("Just for test");
        mDownloadId=mDownloadManager.enqueue(request);
        Log.e(TAG, "mDownloadId:"+mDownloadId);
    }

    private void doInvalidateUi() {
        
        String title = null;
        long totalBytes = 0;
        long currentBytes = 0;
        if(mSizeSortedCursor.moveToPosition(mPositionCursor)){          
            Log.e(TAG, "数据行数：" + mSizeSortedCursor.getCount() 
                    + "\n" + "游标位置：" + mSizeSortedCursor.getPosition()
                    + "\n" + "数据列数：" + mSizeSortedCursor.getColumnCount() );
            
            title = mSizeSortedCursor.getString(mTitleColumnId);
            totalBytes = mSizeSortedCursor.getLong(mTotalBytesColumnId);
            currentBytes = mSizeSortedCursor.getLong(mCurrentBytesColumnId);
            status = mSizeSortedCursor.getInt(mStatusColumnId);
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
        if (status == DownloadManager.STATUS_FAILED || status == DownloadManager.STATUS_SUCCESSFUL) {
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
        final int pencent = (int) (currentBytes * 100 / totalBytes);

        received_progress_number.setText(getSizeText(currentBytes) + "/" + getSizeText(totalBytes));
        received_progress_percent.setText(pencent + "%");

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
            Log.e(TAG, "ContentObserver onChange()");
        }
    }

    /**
     * DataSetObserver每调用两次，ContentObserver调用一次
     * 
     * @author wangxc <br/>
     */
    private class MyDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            Log.e(TAG, "DataSetObserver------------- onChanged()");
            // invalidate ui
            doInvalidateUi();
        }
    }

    /**
     * traversal bundle with string values.
     * 
     * @param bundle
     * @return
     */
    public static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder("");
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            sb.append(key).append(":").append(bundle.get(key)).append(";");
        }
        return sb.toString();
    }
    
    /**是否下载过，包括下载完和未下载完
     * @param cursor
     * @return
     */
    public boolean hasDownloadRecord(Cursor cursor,String url){
        for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
            String uri=cursor.getString(mUriColumnId);
            Log.e(TAG, "hasDownloaded compare:"+uri);
            status = mSizeSortedCursor.getInt(mStatusColumnId);
            if(uri.equals(url)){
                mDownloadId=cursor.getLong(mIdColumnId);
                mPositionCursor=cursor.getPosition();
                Log.e(TAG, "匹配到下载id："+mDownloadId);
                Log.e(TAG, "mStatusColumnId:"+status);
                if (status==DownloadManager.STATUS_PAUSED) {
                    mDownloadManager.resumeDownload(mDownloadId);
                }else if(status==DownloadManager.STATUS_SUCCESSFUL){
                    //open
//                    openCurrentDownload(mSizeSortedCursor);
                    //delete
                    deleteDownload(mDownloadId);
                    return false;
                }else if(status==DownloadManager.STATUS_FAILED){
                    mDownloadManager.restartDownload(mDownloadId);
                }else if(status==DownloadManager.STATUS_PENDING){
                    mDownloadManager.restartDownload(mDownloadId);
                }else if(status==DownloadManager.STATUS_RUNNING){
                    return true;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUiUpdate() {
        refresh();
    }
    /**
     * Send an Intent to open the download currently pointed to by the given
     * cursor.
     */
    private void openCurrentDownload(Cursor cursor) {
        Uri localUri = Uri.parse(cursor.getString(mLocalUriColumnId));
        try {
            getContentResolver().openFileDescriptor(localUri, "r").close();
        } catch (FileNotFoundException exc) {
            Log.d(TAG, "Failed to open download " + cursor.getLong(mIdColumnId), exc);
            Toast.makeText(this,getString(R.string.dialog_file_missing_body),Toast.LENGTH_SHORT).show();
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
        // 通过调用getUriFor 方法获得 system表里的"飞行模式"所在行的Uri  
        Uri uri = Downloads.ALL_DOWNLOADS_CONTENT_URI;  
        // 注册内容观察者  
        // 第二个参数false 为精确匹配  
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
    }
    
    /**
     * Move {@link #mDateSortedCursor} to the download with the given ID.
     * mDateSortedCursor游标移动到指定downloadId处
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
}