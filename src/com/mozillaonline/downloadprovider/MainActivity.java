package com.mozillaonline.downloadprovider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{
    
    private static final String URL = "http://www.chinaums.com/static/ums2013/chinaums/app/download/qmf.apk";
    private static final String URL2 = "http://down.mumayi.com/41052/mbaidu";
    private static final String URL3 = "http://down.apk.hiapk.com/down?aid=1832508&em=13";
    private static final String pathDir="Download";
    private TextView textView1;
    private Button button1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        find();
    }

    private void find() {
        textView1 = (TextView) findViewById(R.id.textView1);
        button1 = (Button) findViewById(R.id.button1);
        
        button1.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        intent.putExtra(ActivityUpdateApp.PATHDIR,pathDir);
        intent.putExtra("name","");
        intent.putExtra(ActivityUpdateApp.URL,URL3);
        intent.setClass(getApplicationContext(), ActivityUpdateApp.class);
        startActivity(intent);
    }
}
