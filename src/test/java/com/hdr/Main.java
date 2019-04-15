package com.hdr;

import com.hdr.avgle.AvgleHeader;
import com.hdr.avgle.AvgleTsFactory;
import com.hdr.ts.AbstractTsFactory;
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
	public void download() {
		long start = System.currentTimeMillis();

		File m3u8 = new File("/home/hdr/video.m3u8");
		String savePath = "/home/hdr/Videos/绝顶";
		String referer = "https://avgle.com/video/136340/%E7%B5%B6%E9%A0%82%C3%974%E6%9C%AC%E7%95%AA-%E4%B8%89%E4%B8%8A%E6%82%A0%E4%BA%9C-tek-072";
		String fimName = "三上.mp4";
		Map<String, List<String>> headers = AvgleHeader.header(referer);

		AbstractTsFactory tsFactory = new AvgleTsFactory(savePath, headers);

		DownloadExecutor downloadExecutor = new DownloadExecutor(m3u8, savePath, fimName, tsFactory);
		downloadExecutor.execute();

		long end = System.currentTimeMillis();
		float costTime = ((float) end - start) / 1000 / 60;
		log.log(Level.INFO, "{0}min", costTime);
	}
}
