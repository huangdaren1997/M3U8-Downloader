package com.hdr;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.hdr.header.AvgleHeader;
import com.hdr.utils.CmdLineArgs;
import com.hdr.utils.TsCombiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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


	private static void downloadP(String propertiesPath) {
		initByProperties(propertiesPath);
		doDownload();

		TsCombiner.merge(tempPath, filmPath);
	}


	private static void download() {
		init();
		doDownload();

		TsCombiner.merge(tempPath, filmPath);
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


		TsCombiner.merge(tempPath, filmPath);
	}


	private static void init() {

		String m3u8Path = null;
		String videoUrl = null;
		String savePath = null;
		String saveName = null;

		// 读取默认配置
		Path defaultConfigPath = Paths.get(System.getProperty("user.dir") + File.separator + "default.properties");
		if (Files.exists(defaultConfigPath)) {
			try (BufferedReader br = Files.newBufferedReader(defaultConfigPath)) {

				Properties prop = new Properties();
				prop.load(br);
				m3u8Path = prop.getProperty("m3u8Path");
				savePath = prop.getProperty("savePath");
				videoUrl = prop.getProperty("videoUrl");
				saveName = prop.getProperty("saveName");

			} catch (IOException e) {
				log.error("文件{}不存在", defaultConfigPath.toString(), e);
			}
		}

		// 补充缺乏的信息
		try (Scanner scanner = new Scanner(System.in)) {
			if (m3u8Path == null) {
				for (; ; ) {
					log.info("请输入m3u8文件所在路径:");
					m3u8Path = scanner.nextLine();
					m3u8 = new File(m3u8Path);
					if (m3u8.exists()) break;
					log.error("文件{}不存在", m3u8Path);
				}
			}

			if (videoUrl == null) {
				log.info("请输入下载视频的URL:");
				videoUrl = scanner.nextLine();
				header = AvgleHeader.header(videoUrl);
			}


			if (savePath == null) {
				for (; ; ) {
					log.info("请输入视频下载目录:");
					savePath = scanner.nextLine();
					if (new File(savePath).exists()) break;
					log.error("目录不存在:{}", savePath);
				}
			}

			if (saveName == null) {
				log.info("请输入视频名称:");
				String name = scanner.nextLine();
				tempPath = savePath.endsWith(File.separator) ? String.format("%s.%s", savePath, name) : String.format("%s%s.%s", savePath, File.separator, name);
				filmPath = String.format("../%s.mp4", name);
			}
		}

	}

	private static void initByProperties(String propertiesPath) {
		Path path = Files.exists(Paths.get(propertiesPath)) ? Paths.get(propertiesPath)
				: Paths.get(System.getProperty("user.dir") + File.separator + "default.properties");

		try (BufferedReader br = Files.newBufferedReader(path)) {
			Properties prop = new Properties();
			prop.load(br);

			String m3u8Path = prop.getProperty("m3u8Path");
			String savePath = prop.getProperty("savePath");
			String videoUrl = prop.getProperty("videoUrl");
			String saveName = prop.getProperty("saveName");

			if (StringUtils.isBlank(videoUrl)) throw new RuntimeException("videoUrl can not be null");
			if (StringUtils.isBlank(saveName)) throw new RuntimeException("saveName can not be null");
			if (!(new File(m3u8Path).exists())) throw new RuntimeException("m3u8Path有误:" + m3u8Path);
			if (!(new File(savePath).exists())) throw new RuntimeException("savePath有误:" + savePath);


			m3u8 = new File(m3u8Path);
			header = AvgleHeader.header(videoUrl);
			tempPath = savePath.endsWith(File.separator) ? String.format("%s.%s", savePath, saveName) : String.format("%s%s.%s", savePath, File.separator, saveName);
			filmPath = String.format("..%s%s.mp4", File.separator, saveName);

		} catch (IOException e) {
			log.error("文件{}不存在", propertiesPath, e);
		}

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

		try {
			latch.await();
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
		CmdLineArgs cmdLineArgs = new CmdLineArgs();
		CmdLineParser parser = new CmdLineParser(cmdLineArgs);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			e.printStackTrace();
		}

		if (cmdLineArgs.downloadFlag) {
			String propertiesPath = cmdLineArgs.propertiesPath;
			if (StringUtils.isNotBlank(propertiesPath)) {
				downloadP(propertiesPath);
			} else {
				download();
			}
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
			File file = new File(savePath + File.separator + tsName);
			if (file.exists()) {
				log.info("{}文件已存在", file.getName());
				latch.countDown();
				log.info("剩余{}", latch.getCount());
				return;
			}
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
			} finally {
				latch.countDown();
				log.info("剩余{}", latch.getCount());
			}


		}


	}


}
