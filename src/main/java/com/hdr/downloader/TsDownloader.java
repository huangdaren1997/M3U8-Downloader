package com.hdr.downloader;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.hdr.ts.Ts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * @author Huang Da Ren
 */
@Log
@Getter
@Setter
@NoArgsConstructor
public class TsDownloader {


	private BlockingQueue<Ts> tsQueue;

	private AtomicInteger failureTime = new AtomicInteger(0);

	public TsDownloader(BlockingQueue<Ts> tsQueue) {
		this.tsQueue = tsQueue;
	}

	public void download() {

		int nThreads = 10;
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		for (int i = 0; i < nThreads; i++) {
			executor.submit(new Downloader());
		}
		executor.shutdown();
		while (!executor.isTerminated()) {

		}

	}


	private class Downloader implements Runnable {

		@Override
		public void run() {

			while (true) {
				try {
					Ts ts = tsQueue.poll(1, TimeUnit.MINUTES);
					if (ts == null) {
						log.log(Level.WARNING, "thread {0} ts == null", Thread.currentThread().getName());
						break;
					}
					HttpResponse response = HttpRequest.get(ts.getUrl())
							.header(ts.getHeaders())
							.execute();

					if (response.getStatus() == HttpStatus.HTTP_OK) {
						try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(ts.getSavePath()))) {
							bos.write(response.bodyBytes());
							bos.flush();
							log.log(Level.INFO, "{0}下载成功", ts.getSavePath());
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						log.log(Level.INFO, "{0}下载失败", ts.getSavePath());
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}


	}

}
