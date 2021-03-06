modify form https://github.com/yxl/DownloadProvider<br>
可以识别服务器转发的下载链接<br>
Android平台面向开发者提供了DownloadManager这个服务（service），可以用来完成下载，同时异步地得到下载进度的实时更新提示。
原生的浏览器，Android Market以及GMail等客户端都使用了该接口。
该接口也部分的提供了断点续传功能：如果在下载过程中遇到网络错误，如信号中断等，DownloadManager会在网络恢复时尝试断点续传继续下载该文件，
但不支持由用户发起的暂停然后断点续传。<br>
android的下载并不提倡使用多线程。主要是因为手机一般不会下载多么大的文件，而多线程本身的线程开销加上使用数据库或额外的记录文件产生的IO开销也不小，使用多线程的意义并不是很大。<br>
已发现的问题：用fileobserver观察文件大小来更新界面UI会出现严重的跳帧现象，跳帧几十到一百多不等，已限制最快更新为100ms。
downloadThread中http的412错误可以尝试进行重试，待修改。
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
