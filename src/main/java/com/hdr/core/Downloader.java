package com.hdr.core;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hdr
 */
@Slf4j
public class Downloader {

	public static ExecutorService executor = Executors.newFixedThreadPool(10);

	private  void download(DownloadMessage message) {

		Map<String, List<String>> header = message.getRequestHeader();

		String tempPath = message.getSavePath();

		List<String> tsUrls = message.getTsUrls();
		AtomicInteger atomicInteger = new AtomicInteger(tsUrls.size());
		for (int i = 0; i < tsUrls.size(); i++) {
			String tsUrl = tsUrls.get(i);
			String tsName = i + ".ts";
			executor.execute(new DownloadWorker(tsUrl, tempPath, tsName, header, atomicInteger));
		}

		try {
			executor.shutdown();
			if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
				executor.shutdownNow();
				if (!executor.awaitTermination(1, TimeUnit.SECONDS)) log.error("executor did not terminate");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}



	private static class DownloadWorker implements Runnable {

		private String tsUrl;
		private String tempPath;
		private String tsName;
		private Map<String, List<String>> header;
		private AtomicInteger counter;

		public DownloadWorker(String tsUrl, String tempPath, String tsName, Map<String, List<String>> header, AtomicInteger counter) {
			this.tsUrl = tsUrl;
			this.tempPath = tempPath;
			this.tsName = tsName;
			this.header = header;
			this.counter = counter;
		}


		private void doDownload(){
			File file = new File(tempPath + File.separator + tsName);

			if (file.exists()) log.info("{}文件已存在", file.getName());
			else {
				try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
					HttpResponse response = HttpRequest.get(tsUrl).header(header).timeout(10 * 1000).execute();
					if (response.getStatus() == HttpStatus.HTTP_OK) {
						bos.write(response.bodyBytes());
						bos.flush();
					} else {
						log.warn("{}下载失败:{}", tsName, response.getStatus());
					}
				} catch (Exception e) {
					log.warn("{}下载失败:", tsName, e);
				}
			}

			log.info("剩余{}", counter.decrementAndGet());
		}


		@Override
		public void run() {
			doDownload();
		}


	}

}
