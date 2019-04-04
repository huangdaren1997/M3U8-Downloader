package com.hdr.downloader;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.hdr.ts.TsSource;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Huang Da Ren
 */
public class TsDownloader implements Runnable {

	private BlockingQueue<TsSource> queue;
	private static final Logger logger = Logger.getLogger(TsDownloader.class.getName());

	public TsDownloader(BlockingQueue<TsSource> queue) {
		this.queue = queue;
	}


	public void download(TsSource ts) {
		HttpResponse response = HttpRequest.get(ts.getUrl())
				.header(ts.getHeaders())
				.execute();

		if (response.getStatus() == HttpStatus.HTTP_OK) {
			try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(ts.getSavePath()))) {
				bos.write(response.bodyBytes());
				bos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.log(Level.WARNING, "failure to download {0}", ts.getSavePath().getName());
	}

	@Override
	public void run() {
		while (true) {
			try {
				TsSource tsSource = queue.poll(10, TimeUnit.SECONDS);
				if (tsSource == null) break;
				download(tsSource);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
