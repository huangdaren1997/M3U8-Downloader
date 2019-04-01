package com.hdr.downloader;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.hdr.ts.TsSource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * @author Huang Da Ren
 */
public class AvgleTsDownloader implements TsDownloader {


	private BlockingQueue<TsSource> queue;

	public AvgleTsDownloader(BlockingQueue<TsSource> queue) {
		this.queue = queue;
	}

	@Override
	public boolean download(TsSource ts) {

		HttpResponse response = HttpRequest.get(ts.getUrl())
				.header(ts.getHeaders())
				.execute();

		if (response.getStatus() == HttpStatus.HTTP_OK) {
			String file = ts.getSavePath() + "/" + ts.getName();
			try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(file)))) {
				bos.write(response.bodyBytes());
				bos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}

		return false;
	}

	@Override
	public void run() {
		while (!queue.isEmpty()) {
			TsSource tsSource = null;
			try {
				tsSource = queue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			assert (tsSource != null);
			if (!download(tsSource))
				queue.offer(tsSource);

		}
	}
}
