package com.hdr.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * ts文件合并然后转换成mp4格式的视频
 *
 * @author Huang Da Ren
 */
@Slf4j
public class TsCombiner {

	/**
	 * 合并ts文件，然后转换成MP4格式的视频，合并后删除ts文件所在目录
	 *
	 * @param tsFileLocation ts文件所在路径
	 * @param filmName       最终生成的视频的名字
	 */
	public static void merge(String tsFileLocation, String filmName) {
		File tsDir = new File(tsFileLocation);
		File[] tsFiles = listFile(tsDir);
		if (tsFiles.length == 0) return;

		String os = System.getProperty("os.name");
		List<String> cms = os.toLowerCase().startsWith("win") ? createWindowsCommand(tsFiles, filmName) : createLinuxCommand(tsFiles, filmName);

		try {
			executeCommand(cms, tsFileLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Scanner scanner = new Scanner(System.in);
		log.info("是否删除临时文件? y/n");
		boolean del = scanner.nextLine().contains("y");
		if (del){
			Arrays.stream(tsFiles).forEach(File::delete);
			tsDir.delete();
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

	private static List<String> createWindowsCommand(File[] tsFiles, String filmName) {
		ArrayList<String> cms = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		sb.append("ffmpeg.exe -i concat:");
		sb.append("'");
		Arrays.stream(tsFiles).forEach(file -> sb.append(file.getName()).append("|"));
		sb.append("' ");
		sb.append("-c copy ");
		sb.append(filmName);
		cms.add(sb.toString());
		cms.forEach(System.out::println);
		return cms;
	}

	private static List<String> createLinuxCommand(File[] tsFiles, String filmName) {
		//  /bin/bash -c "ffmpeg -i concat:"0.ts|1.ts|2.ts| -c copy fileName""
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
		cms.forEach(System.out::println);
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

}
