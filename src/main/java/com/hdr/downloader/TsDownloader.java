package com.hdr.downloader;

import com.hdr.ts.TsSource;

/**
 * @author Huang Da Ren
 */
public interface TsDownloader extends Runnable {

	public boolean download(TsSource ts);

}
