modify form https://github.com/yxl/DownloadProvider<br>
可以识别服务器转发的下载链接<br>
Android平台面向开发者提供了DownloadManager这个服务（service），可以用来完成下载，同时异步地得到下载进度的实时更新提示。
原生的浏览器，Android Market以及GMail等客户端都使用了该接口。
该接口也部分的提供了断点续传功能：如果在下载过程中遇到网络错误，如信号中断等，DownloadManager会在网络恢复时尝试断点续传继续下载该文件，
但不支持由用户发起的暂停然后断点续传。<br>
使用方法：
```java
		...
        download = new UtilDownload(this);
        download.download(iCallBack, url, "", pathDir);
		...
		
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
	
	...
	
	    // callback update ui
    IReportDownloadProgress iCallBack = new IReportDownloadProgress() {

        @Override
        public void onUpdate(String pathFile, long byteTotal, long byteCurrent) {
	...
```
This project ports the DownloadProvider of Android 2.3.7. It supports Android 2.2 and above. 

Remarks
com.mozillaonline.providers.downloads.Downloads.AUTHORITY defines the authority of the DownloadProvider. 
Change the authority both in the code and the AndroidManifest.xml file to avoid conflict with other applicaitons.

License
Apache License, Version 2.0 
