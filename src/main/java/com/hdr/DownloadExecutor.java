package com.hdr;

import cn.hutool.http.Header;
import com.hdr.common.Headers;
import com.hdr.downloader.AvgleTsDownloader;
import com.hdr.ts.TsSource;
import com.hdr.ts.TsSourceFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Huang Da Ren
 */
public class DownloadExecutor {

	public int total;

	private BlockingQueue<TsSource> queue;
	private Class<? extends TsSourceFactory> tsSourceFactory;
	private File m3u8;
	private Map<String, List<String>> headers;
	private String savePath;
	private String filmName;

	public DownloadExecutor(File m3u8, Map<String, List<String>> headers, String savePath,
	                        String filmName, Class<? extends TsSourceFactory> tsSourceFactory) {
		this.m3u8 = m3u8;
		this.headers = headers;
		this.savePath = savePath;
		this.filmName = filmName;
		this.tsSourceFactory = tsSourceFactory;

		checkDir(savePath);
	}


	public void execute() {
		prepare();
		download();
		merge();
	}


	public void prepare() {

		try {
			TsSourceFactory factory = this.tsSourceFactory.newInstance();
			factory.setM3u8(m3u8);
			factory.setSavePath(savePath);
			factory.setHeaders(headers);
			queue = factory.produce();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	private void checkDir(String path) {
		File file = new File(path);

		if (!file.exists()) {
			if (!file.mkdir())
				throw new IllegalArgumentException("there is something wrong with the save path " + savePath);
		}

	}


	public void download() {
		total = queue.size();

		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 10; i++) {
			executor.submit(new AvgleTsDownloader(queue));
		}

		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(1000);

				System.out.println("正在下载..." + ((float) queue.size() / total) * 100 + "%");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void merge() {
		File[] tsFiles = listFile(new File(savePath));
		assert tsFiles != null;
		List<String> cms = createCommands(tsFiles);
		try {
			executeCommand(cms);
		} catch (IOException e) {
			e.printStackTrace();
		}
		cleanUp(tsFiles);
	}

	private File[] listFile(File directory) {
		assert (directory != null);
		if (directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles();
			assert (files != null);
			Arrays.sort(files, (x, y) -> {
				String xStr = x.getName().split("\\.")[0];
				String yStr = y.getName().split("\\.")[0];
				Integer xNum = Integer.valueOf(xStr);
				Integer yNum = Integer.valueOf(yStr);
				return xNum.compareTo(yNum);
			});
			return files;
		} else {
			throw new IllegalArgumentException("there is something wrong with the file :" + directory.getName());
		}
	}

	public List<String> createCommands(File[] tsFiles) {
		ArrayList<String> cms = new ArrayList<>();
		cms.add("/bin/bash");
		cms.add("-c");

		StringBuilder sb = new StringBuilder();
		sb.append("ffmpeg -i concat:");
		sb.append("'");
		for (File f : tsFiles) {
			sb.append(f.getName()).append("|");
		}
		sb.append("' ");
		sb.append("-c copy ");
		sb.append(filmName);
		cms.add(sb.toString());
		//  /bin/bash -c "ffmpeg -i concat:"0.ts|1.ts|2.ts| -c copy fileName""
		return cms;
	}


	public void executeCommand(List<String> cms) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(cms);
		processBuilder.directory(new File(savePath));
		Process process = processBuilder.start();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		}
	}

	public void cleanUp(File[] files) {
		for (File f : files) {
			f.delete();
		}
	}


	public static void main(String[] args) {
		File m3u8 = new File("/home/hdr/video.m3u8");
		String savePath = "/home/hdr/Videos/hello";
		String referer = "https://avgle.com/video/Ts8PrPxkoPP/%E9%9F%93%E5%9C%8B%E6%BC%94%E8%97%9D%E5%9C%88%E8%B3%A3%E6%B7%AB%E5%81%B7%E6%8B%8D%E6%82%B2%E6%85%98%E4%BA%8B%E4%BB%B6-south-korean-prostitution-vol-19";
		String fimName = "test.mp4";

		Map<String, List<String>> headers = Headers.avgleHeader();
		headers.put(Header.REFERER.toString(), Arrays.asList(referer));

		DownloadExecutor downloadExecutor = new DownloadExecutor(m3u8, headers, savePath, fimName, TsSourceFactory.class);
		downloadExecutor.execute();
	}

}
