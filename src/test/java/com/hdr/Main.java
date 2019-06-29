package com.hdr;

import com.hdr.header.AvgleHeader;
import com.hdr.ts.GeneralTsFactory;
import com.hdr.ts.TsFactory;
import lombok.extern.java.Log;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Huang Da Ren
 */
@Log
public class Main {


	@Test
	public void avgleDownloader() {
		long start = System.currentTimeMillis();

		File m3u8 = new File("/home/hdr/video.m3u8");
		String savePath = "/home/hdr/Videos/av";
		String referer = "https://avgle.com/video/vBZSZSd6KS1/%E9%AB%98%E6%B8%85%E4%B8%AD%E6%96%87%E5%AD%97%E5%B9%95-venu-442-%E4%BA%BA%E5%A6%BB%E6%A4%8E%E5%90%8D%E7%94%B1%E5%A5%88%E5%92%8C%E5%84%BF%E5%AD%90%E4%B8%8D%E8%83%BD%E5%91%8A%E8%AF%89%E4%B8%88%E5%A4%AB%E7%9A%84%E7%A7%98%E5%AF%86";
		String fimName = "椎名由奈的禁忌之恋.mp4";

		TsFactory tsFactory = new GeneralTsFactory(savePath, AvgleHeader.header(referer));

		new DownloadExecutor(m3u8, savePath, fimName, tsFactory).execute();

		long end = System.currentTimeMillis();
		log.log(Level.INFO, "{0}s", (end - start) / 1000);
	}

	@Test
	public void javdoveDownloader() {
		long start = System.currentTimeMillis();

		File m3u8 = new File("/home/hdr/video.m3u8");
		String savePath = "/home/hdr/Videos/自拍";
		String fimName = "长相很漂亮美女.mp4";

		TsFactory tsFactory = new GeneralTsFactory(savePath, null);

		new DownloadExecutor(m3u8, savePath, fimName, tsFactory).execute();

		long end = System.currentTimeMillis();
		log.log(Level.INFO, "{0}s", (end - start) / 1000);
	}
}
