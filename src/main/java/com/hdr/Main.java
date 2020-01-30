package com.hdr;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.hdr.download.TsSynthesizer;
import com.hdr.header.AvgleHeader;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author hdr
 */
@Slf4j
public class Main {


	private static int total;
	private static CountDownLatch latch;

	private static File m3u8;
	private static Map<String, List<String>> header;
	private static String tempPath;
	private static String filmPath;

	private static void prepare() {
		m3u8 = new File("C:\\Users\\hdr\\Desktop\\list-WANZ-105.txt");
		String referer = "https://avgle.com/video/Rq1wDw4KKwU/%E6%97%A0%E7%A0%81%E6%B5%81%E5%87%BA-fc2ppv-1204330-%E6%A4%8E%E5%90%8D%E7%94%B1%E5%A5%88%E6%97%A0%E7%A0%81%E6%B5%81%E5%87%BA";
		header = AvgleHeader.header(referer);

		String name = "FC2-PPV-1204330";
		String basePath = "D:\\迅雷下载\\";
		tempPath = basePath.endsWith(File.separator) ? String.format("%s.tmp_%s", basePath, name) : String.format("%s%s.%s", basePath, File.separator, name);
		filmPath = String.format("../%s.mp4", name);

	}

	private static List<String> parseM3U8File(){
		try (BufferedReader br = Files.newBufferedReader(Paths.get(m3u8.getPath()))) {
			List<String> tsList = br.lines().filter(line -> line.contains("https")).collect(Collectors.toList());
			total = tsList.size();
			latch = new CountDownLatch(total);
			return tsList;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("解析m3u8文件失败");
		}
	}

	private static void download(){
		List<String> tsList = parseM3U8File();

		File saveDir = new File(tempPath);
		if (!saveDir.exists()) saveDir.mkdirs();

		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int i = 0; i < tsList.size(); i++) {
			String tsUrl = tsList.get(i);
			String tsName = i + ".ts";
			executor.execute(new Downloader(tsUrl, tempPath, tsName, header));
		}

		executor.shutdown();
		try {
			latch.await();
			if (!executor.awaitTermination(1,TimeUnit.SECONDS)){
				executor.shutdownNow();
				if (!executor.awaitTermination(1,TimeUnit.SECONDS)) log.error("executor did not terminate");
			}
		}catch (InterruptedException e) {
			e.printStackTrace();
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		prepare();
		download();

		TsSynthesizer.merge(tempPath, filmPath);
		TsSynthesizer.cleanUp(tempPath);
		long end = System.currentTimeMillis();
		log.info("下载完成:耗时{}s", (end - start) / 1000);

	}

	private static class Downloader implements Runnable {

		private String tsUrl;
		private String savePath;
		private String tsName;
		private Map<String, List<String>> header;

		public Downloader(String tsUrl, String savePath, String tsName, Map<String, List<String>> header) {
			this.tsUrl = tsUrl;
			this.savePath = savePath;
			this.tsName = tsName;
			this.header = header;
		}

		@Override
		public void run() {
			HttpResponse response = HttpRequest.get(tsUrl).header(header).execute();
			if (response.getStatus() == HttpStatus.HTTP_OK) {
				try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(savePath + File.separator + tsName))) {
					bos.write(response.bodyBytes());
					bos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				log.warn("{}下载失败", tsName);
			}
			latch.countDown();
			log.info("剩余{}",latch.getCount());
		}


	}


}
