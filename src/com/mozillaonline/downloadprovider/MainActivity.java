package com.mozillaonline.downloadprovider;

import com.mozillaonline.downloadprovider.UtilDownload.IReportDownloadProgress;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**fix pauseDownload when weakup is counting down
 * 报超时异常的时候立马重试，weakup time始终设为0
 * @author wangxc <br/>
 */
public class MainActivity extends Activity implements OnClickListener{
    
    private static final String URL = "http://www.chinaums.com/static/ums2013/chinaums/app/download/qmf.apk";
    private static final String URL2 = "http://down.mumayi.com/41052/mbaidu";
    private static final String URL3 = "http://down.apk.hiapk.com/down?aid=1832508&em=13";
    private static final String pathDir="DownloadProvider";
//    private static final String pathDir="Download";
    private Button button1;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        find();
    }

    private void find() {
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==button1){
            Intent intent=new Intent();
            intent.putExtra(ActivityUpdateApp.PATHDIR,pathDir);
            intent.putExtra("name","");
            intent.putExtra(ActivityUpdateApp.URL,URL2);
            intent.setClass(getApplicationContext(), ActivityUpdateApp.class);
            startActivity(intent);            
        }else if(v==button2){
            Intent intent=new Intent();
            intent.putExtra(ActivityUpdateApp.PATHDIR,pathDir);
            intent.putExtra("name","");
            intent.putExtra(ActivityUpdateApp.URL,URL2);
            intent.setClass(getApplicationContext(), ActivityUtilDownloadDemo.class);
            startActivity(intent); 
        }

    }

}
