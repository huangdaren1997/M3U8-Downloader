package com.hdr;

import com.hdr.avgle.AvgleHeader;
import com.hdr.avgle.AvgleTsFactory;
import com.hdr.downloader.TsDownloader;
import com.hdr.downloader.TsFusion;
import com.hdr.ts.AbstractTsFactory;
import com.hdr.ts.Ts;
import lombok.extern.java.Log;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

/**
 * @author Huang Da Ren
 */
@Log
public class DownloadExecutor {

	/**
	 * 用来存储需要下载的ts文件的队列
	 */

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


	public static void main(String[] args) {
		long start = System.currentTimeMillis();

		File m3u8 = new File("/home/hdr/video.m3u8");
		String savePath = "/home/hdr/Videos/绝顶";
		String referer = "https://avgle.com/video/136340/%E7%B5%B6%E9%A0%82%C3%974%E6%9C%AC%E7%95%AA-%E4%B8%89%E4%B8%8A%E6%82%A0%E4%BA%9C-tek-072";
		String fimName = "三上.mp4";
		Map<String, List<String>> headers = AvgleHeader.header(referer);

		AbstractTsFactory tsFactory = new AvgleTsFactory(savePath, headers);

		DownloadExecutor downloadExecutor = new DownloadExecutor(m3u8, savePath, fimName, tsFactory);
		downloadExecutor.execute();

		long end = System.currentTimeMillis();
		float costTime = ((float) end - start) / 1000 / 60;
		log.log(Level.INFO, "{0}min", costTime);

	}

}
