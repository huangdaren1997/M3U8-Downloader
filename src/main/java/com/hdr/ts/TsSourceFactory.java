package com.hdr.ts;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Huang Da Ren
 */
@Getter
@Setter
public class TsSourceFactory {

	private Map<String, List<String>> headers;
	private File m3u8;
	private String savePath;

	public TsSourceFactory() {

	}

	public TsSourceFactory(File m3u8, String savePath, Map<String, List<String>> headers) {
		this.m3u8 = m3u8;
		this.savePath = savePath;
		this.headers = headers;
	}

	public BlockingQueue<TsSource> produce() {
		BlockingQueue<TsSource> queue = new LinkedBlockingQueue<>();

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(m3u8), StandardCharsets.UTF_8))) {
			String line;
			for (int i = 0; (line = br.readLine()) != null; i++) {
				if (line.contains("https")) {
					TsSource tsSource = new TsSource();
					tsSource.setUrl(line);
					tsSource.setHeaders(headers);
					File savePath = new File(this.savePath + File.pathSeparator + i + ".ts");
					tsSource.setSavePath(savePath);
					queue.add(tsSource);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return queue;
	}
}
