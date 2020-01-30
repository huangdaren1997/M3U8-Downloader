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
import java.util.Scanner;
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


	private static CountDownLatch latch;

	private static File m3u8;
	private static Map<String, List<String>> header;
	private static String tempPath;
	private static String filmPath;


	private static void download() {
		init();
		doDownload();

		TsSynthesizer.merge(tempPath, filmPath);
		TsSynthesizer.cleanUp(tempPath);
	}

	private static void merge() {
		Scanner scanner = new Scanner(System.in);
		String basePath;
		for (; ; ) {
			log.info("请输入视频下载目录:");
			basePath = scanner.nextLine();
			if (new File(basePath).exists()) break;
			log.error("目录不存在:{}", basePath);
		}

		log.info("请输入视频名称:");
		String name = scanner.nextLine();
		tempPath = basePath.endsWith(File.separator) ? String.format("%s.%s", basePath, name) : String.format("%s%s.%s", basePath, File.separator, name);
		filmPath = String.format("../%s.mp4", name);


		TsSynthesizer.merge(tempPath, filmPath);
		TsSynthesizer.cleanUp(tempPath);
	}



	private static void init() {
		Scanner scanner = new Scanner(System.in);
		for (; ; ) {
			log.info("请输入m3u8文件所在路径:");
			String m3u8Path = scanner.nextLine();
			m3u8 = new File(m3u8Path);
			if (m3u8.exists()) break;
			log.error("文件{}不存在", m3u8Path);
		}

		log.info("请输入下载视频的URL:");
		String url = scanner.nextLine();
		header = AvgleHeader.header(url);


		String basePath;
		for (; ; ) {
			log.info("请输入视频下载目录:");
			basePath = scanner.nextLine();
			if (new File(basePath).exists()) break;
			log.error("目录不存在:{}", basePath);
		}

		log.info("请输入视频名称:");
		String name = scanner.nextLine();
		tempPath = basePath.endsWith(File.separator) ? String.format("%s.%s", basePath, name) : String.format("%s%s.%s", basePath, File.separator, name);
		filmPath = String.format("../%s.mp4", name);

	}

	private static void doDownload() {
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

	private static List<String> parseM3U8File() {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(m3u8.getPath()))) {
			List<String> tsList = br.lines().filter(line -> line.contains("https")).collect(Collectors.toList());
			latch = new CountDownLatch(tsList.size());
			return tsList;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("解析m3u8文件失败");
		}
	}


	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		if (args.length == 0) throw new IllegalArgumentException("参数不能为空");
		if (args[0].equals("download")) {
			download();
		} else if (args[0].equals("merge")) {
			merge();
		} else if (args[0].equals("clean")) {

		} else {
			log.warn("unknown option {}", args[0]);
		}


		long end = System.currentTimeMillis();
		log.info("操作完成:耗时{}s", (end - start) / 1000);

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
			try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(savePath + File.separator + tsName))){
				HttpResponse response = HttpRequest.get(tsUrl).header(header).timeout(10*1000).execute();
				if (response.getStatus() == HttpStatus.HTTP_OK) {
						bos.write(response.bodyBytes());
						bos.flush();
				} else {
					log.warn("{}下载失败:{}", tsName,response.getStatus());
				}
			} catch (Exception e) {
				log.warn("{}下载失败:", tsName, e);
			}finally {
				latch.countDown();
				log.info("剩余{}", latch.getCount());
			}


		}


	}


}
