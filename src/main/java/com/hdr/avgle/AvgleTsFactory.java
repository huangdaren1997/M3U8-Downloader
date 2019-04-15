package com.hdr.avgle;

import com.hdr.ts.AbstractTsFactory;
import com.hdr.ts.Ts;
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
public class AvgleTsFactory extends AbstractTsFactory {

	private Map<String, List<String>> tsHeaders;
	private String tsSavePath;
	private final static String SUFFIX = ".ts";

	public AvgleTsFactory(String tsSavePath, Map<String, List<String>> tsHeaders) {
		this.tsSavePath = tsSavePath;
		this.tsHeaders = tsHeaders;
	}


	@Override
	public BlockingQueue<Ts> produce(File m3u8) {
		BlockingQueue<Ts> queue = new LinkedBlockingQueue<>();

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(m3u8), StandardCharsets.UTF_8))) {
			String line;
			for (int i = 0; (line = br.readLine()) != null; i++) {
				if (line.contains("https")) {
					Ts ts = new Ts();
					ts.setUrl(line);
					ts.setHeaders(tsHeaders);
					ts.setSavePath(this.tsSavePath + "/" + i + SUFFIX);
					queue.add(ts);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return queue;
	}


}
