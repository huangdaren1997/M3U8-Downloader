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
		String savePath = "/home/hdr/Videos/佐佐木明希";
		String referer = "https://avgle.com/video/CUGlwl1Rxll/121914-760-%E6%85%9F%E5%93%AD%E3%81%AE%E5%A5%B3%E6%95%99%E5%B8%AB-%E5%BE%8C%E7%B7%A8-%E3%81%A0%E3%82%89%E3%81%97%E3%81%AA%E3%81%84%E7%94%9F%E3%81%8D%E7%89%A9%E3%81%AB%E5%A0%95%E3%81%A1%E3%81%9F%E5%A5%B3-%E5%A4%A7%E6%A1%A5%E6%9C%AA%E4%B9%85";
		String fimName = "慟哭女教師下.mp4";
		Map<String, List<String>> headers = AvgleHeader.header(referer);

		AbstractTsFactory tsFactory = new AvgleTsFactory(savePath, headers);

		DownloadExecutor downloadExecutor = new DownloadExecutor(m3u8, savePath, fimName, tsFactory);
		downloadExecutor.execute();

		long end = System.currentTimeMillis();
		float costTime = ((float) end - start) / 1000 / 60;
		log.log(Level.INFO, "{0}min", costTime);
	}
}
