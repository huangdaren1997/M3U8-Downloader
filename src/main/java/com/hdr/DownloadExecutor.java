package com.hdr;

import com.hdr.downloader.TsDownloader;
import com.hdr.downloader.TsFusion;
import com.hdr.ts.AbstractTsFactory;
import com.hdr.ts.Ts;
import lombok.extern.java.Log;

import java.io.File;
import java.util.concurrent.BlockingQueue;

/**
 * @author Huang Da Ren
 */
@Log
public class DownloadExecutor {


	private AbstractTsFactory tsSourceFactory;
	private File m3u8 = new File("/home/hdr/video.m3u8");
	private String savePath;
	private String filmName;

	public DownloadExecutor(String savePath,
	                        String filmName,
	                        AbstractTsFactory tsSourceFactory) {
		checkDir(savePath);
		this.savePath = savePath;
		this.filmName = filmName;
		this.tsSourceFactory = tsSourceFactory;
	}

	public DownloadExecutor(File m3u8,
	                        String savePath,
	                        String filmName,
	                        AbstractTsFactory tsSourceFactory) {
		checkDir(savePath);
		this.m3u8 = m3u8;
		this.savePath = savePath;
		this.filmName = filmName;
		this.tsSourceFactory = tsSourceFactory;
	}


	public void execute() {
		BlockingQueue<Ts> tsQueue = tsSourceFactory.produce(m3u8);
		tsQueue.forEach(System.out::println);
		TsDownloader tsDownloader = new TsDownloader(tsQueue);
		tsDownloader.download();
		TsFusion.merge(savePath, filmName);

	}


	private void checkDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			if (!file.mkdir())
				throw new IllegalArgumentException("there is something wrong with the save path " + savePath);
		}

	}



}
