package com.hdr;

import com.hdr.download.TsDownloader;
import com.hdr.download.TsSynthesizer;
import com.hdr.ts.Ts;
import com.hdr.ts.TsFactory;
import lombok.extern.java.Log;

import java.io.File;
import java.util.concurrent.BlockingQueue;

/**
 * @author Huang Da Ren
 */
@Log
public class DownloadExecutor {


	private TsFactory tsFactory;
	private File m3u8;
	private String savePath;
	private String filmName;


	public DownloadExecutor(File m3u8,
	                        String savePath,
	                        String filmName,
	                        TsFactory tsFactory) {
		checkDir(savePath);
		this.m3u8 = m3u8;
		this.savePath = savePath;
		this.filmName = filmName;
		this.tsFactory = tsFactory;
	}


	public void execute() {
		BlockingQueue<Ts> tsQueue = tsFactory.produce(m3u8);
		TsDownloader tsDownloader = new TsDownloader(tsQueue);
		tsDownloader.download();
		TsSynthesizer.merge(savePath, filmName);
	}


	private void checkDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			if (!file.mkdir()) {
				throw new IllegalArgumentException("there is something wrong with the save path " + savePath);
			}
		}

	}



}
