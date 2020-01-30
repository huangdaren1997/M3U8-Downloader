package com.hdr.download;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * ts文件合并然后转换成mp4格式的视频
 *
 * @author Huang Da Ren
 */
@Slf4j
public class TsSynthesizer {

	/**
	 * 合并ts文件，然后转换成MP4格式的视频
	 *
	 * @param tsFileLocation ts文件所在路径
	 * @param filmName       最终生成的视频的名字
	 */
	public static void merge(String tsFileLocation, String filmName) {
		File tsDir = new File(tsFileLocation);
		File[] tsFiles = listFile(tsDir);
		assert tsFiles != null;
		List<String> cms = createCommands(tsFiles, filmName);
		cms.forEach(System.out::println);
		try {
			executeCommand(cms, tsFileLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File[] listFile(File directory) {
		assert (directory != null);
		if (directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles((dir, name) -> name.contains(".ts"));
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

	/**
	 * 创建合并ts文件的ffmpeg命令
	 *
	 * @param tsFiles  ts文件
	 * @param filmName 最终合成的视频名称
	 * @return
	 */
	private static List<String> createCommands(File[] tsFiles, String filmName) {
		ArrayList<String> cms = new ArrayList<>();
		cms.add("ubuntu1804.exe");
		cms.add("run");

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


	private static void executeCommand(List<String> cms, String executeLocation) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(cms);
		processBuilder.directory(new File(executeLocation));
		Process process = processBuilder.start();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		}
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void cleanUp(File[] files) {
		for (File f : files) {
			f.delete();
		}
	}

	public static void cleanUp(String tsFileLocation) {
			File file = new File(tsFileLocation);
			FileUtil.del(file);
	}
}
