package com.mozillaonline.downloadprovider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.mozillaonline.providers.DownloadManager;
import com.mozillaonline.providers.DownloadManager.Request;
import com.mozillaonline.providers.downloads.DownloadService;
import com.mozillaonline.providers.downloads.ui.ActivityDownloadList;
import com.mozillaonline.providers.downloads.ui.DownloadList;

/**
 * @author wangxc <br/>
 */
public class DownloadProviderActivity extends Activity implements OnClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = DownloadProviderActivity.class.getName();

    private BroadcastReceiver mReceiver;

    EditText mUrlInputEditText;
    Button mStartDownloadButton;
    DownloadManager mDownloadManager;
    Button mShowDownloadListButton;
    Button btnDownloadSingleFile;
    private static final String URL = "http://www.chinaums.com/static/ums2013/chinaums/app/download/qmf.apk";
    private static final String URL2 = "http://down.mumayi.com/41052/mbaidu";
    private static final String URL3 = "http://down.apk.hiapk.com/down?aid=1832508&em=13";
    private static final String URL4 = "http://lx.cdn.baidupcs.com/file/cc5e9e4b55d0ced03119c2fb5a94163c?bkt=p2-nb-511&xcode=f8c6f56e9fe995e9d3faa705d1104851c1bc17c4ed6d2fe7f77424e07ee197d9&fid=3825345855-250528-1096701338319316&time=1416380638&sign=FDTAXERLB-DCb740ccc5511e5e8fedcff06b081203-EzaHg8kft2ImTvDTPnEW9Pp83Gc%3D&to=cb&fm=Nin,B,U,t&sta_dx=466&sta_cs=27&sta_ft=zip&sta_ct=4&newver=1&newfm=1&flow_ver=3&sl=81723486&expires=8h&rt=sh&r=337222293&mlogid=2342353874&vuk=3825345855&vbdid=1289766231&fin=I9220-D-a.zip&fn=I9220-D-a.zip";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_main);

        mDownloadManager = new DownloadManager(getContentResolver(), getPackageName());
        buildComponents();
        startDownloadService();

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                showDownloadList();
            }
        };

        registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void buildComponents() {
        mUrlInputEditText = (EditText) findViewById(R.id.url_input_edittext);
        mStartDownloadButton = (Button) findViewById(R.id.start_download_button);
        mShowDownloadListButton = (Button) findViewById(R.id.show_download_list_button);
        btnDownloadSingleFile = (Button) findViewById(R.id.btnDownloadSingleFile);

        mStartDownloadButton.setOnClickListener(this);
        mShowDownloadListButton.setOnClickListener(this);
        btnDownloadSingleFile.setOnClickListener(this);

        mUrlInputEditText.setText(URL4);
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
                startDownload(URL4);
                break;
            case R.id.show_download_list_button:
                showDownloadList();
                break;
            case R.id.btnDownloadSingleFile:
                Intent intent = new Intent();
                intent.setClass(this, ActivityUpdateApp.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void showDownloadList() {
        Intent intent = new Intent();
        intent.setClass(this, ActivityDownloadList.class);
        startActivity(intent);
    }

    private void startDownload(String url) {
        Uri srcUri = Uri.parse(url);
        DownloadManager.Request request = new Request(srcUri);
        //
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/");
        //
        request.setDescription("Just for test");
        mDownloadManager.enqueue(request);
    }
}