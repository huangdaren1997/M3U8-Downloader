package com.hdr.avgle;

import cn.hutool.http.Header;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Huang Da Ren
 */
public class AvgleHeader {
	private static final String AVGLE_ORIGIN = "https://avgle.com";
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36";

	public static Map<String, List<String>> header(String reffer) {
		Map<String, List<String>> headers = new HashMap<String, List<String>>(100);
		headers.put(Header.ORIGIN.toString(), Arrays.asList(AVGLE_ORIGIN));
		headers.put(Header.USER_AGENT.toString(), Arrays.asList(USER_AGENT));
		headers.put(Header.REFERER.toString(), Arrays.asList(reffer));
		return headers;
	}
}
