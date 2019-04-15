package com.hdr.downloader;

import com.hdr.ts.Ts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
			executor.submit(new Downloader(tsQueue));
		}
		executor.shutdown();
		while (!executor.isTerminated()) {

		}


	}


}
