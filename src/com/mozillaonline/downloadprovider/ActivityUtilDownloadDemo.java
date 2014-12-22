package com.mozillaonline.downloadprovider;

import java.io.File;

import com.k.application.Log;
import com.mozillaonline.downloadprovider.UtilDownload.IReportDownloadProgress;
import com.mozillaonline.providers.DownloadManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityUtilDownloadDemo extends Activity {
    private TextView itemsTitle;
    private Button operateBtn;
    private ProgressBar receivedProgress;
    private TextView itemsDesc;
    private TextView progressPercent;
    private TextView progressNumber;
    private ImageView itemsIcon;

    private String url;
    private String pathDir;

    private String mTitle;
    private String mPathFile = "";
    UtilDownload download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_util_download_demo);
        getData();
        find();
        /********************************************************/
        download = new UtilDownload(this);
        download.download(iCallBack, url, "", pathDir);
        /********************************************************/
    }

    @Override
    protected void onDestroy() {
        /********************************************************/
        download.unRegister();
        /********************************************************/
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        /********************************************************/
        download.pauseDownload();
        /********************************************************/
        super.onPause();
    }

    // callback update ui
    IReportDownloadProgress iCallBack = new IReportDownloadProgress() {

        @Override
        public void onUpdate(String pathFile, long byteTotal, long byteCurrent) {
            if (pathFile == null)
                return;
            if (!pathFile.equals(mPathFile)) {
                mPathFile = pathFile;
                mTitle = new File(UtilDownload.getPathLocal(pathFile)).getName();
            }
            itemsTitle.setText(mTitle);
            int progress = UtilDownload.getProgressValue(byteTotal, byteCurrent);

            receivedProgress.setProgress(progress);
            final int pencent = (int) (byteCurrent * 100 / byteTotal);

            progressNumber.setText(UtilDownload.getSizeText(getApplicationContext(), byteCurrent) + "/"
                    + UtilDownload.getSizeText(getApplicationContext(), byteTotal));
            progressPercent.setText(pencent + "%");
            /********************************************************/
            if (byteCurrent == byteTotal) {
                Log.d("开始安装");
                download.openDownload();
            }
            /********************************************************/
        }

    };

    private void getData() {
        url = getIntent().getStringExtra(ActivityUpdateApp.URL);
        pathDir = getIntent().getStringExtra(ActivityUpdateApp.PATHDIR);
    }

    private void find() {
        itemsTitle = (TextView) findViewById(R.id.itemsTitle);
        operateBtn = (Button) findViewById(R.id.operateBtn);
        receivedProgress = (ProgressBar) findViewById(R.id.received_progress);
        itemsDesc = (TextView) findViewById(R.id.itemsDesc);
        progressPercent = (TextView) findViewById(R.id.received_progress_percent);
        progressNumber = (TextView) findViewById(R.id.received_progress_number);
        itemsIcon = (ImageView) findViewById(R.id.itemsIcon);

    }
}
