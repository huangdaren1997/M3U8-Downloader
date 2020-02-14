package com.hdr.header;

import cn.hutool.http.Header;

import java.util.*;

/**
 * @author Huang Da Ren
 */
public class AvgleHeader {

	public static Map<String, List<String>> header(String reffer) {
		String origin = "https://avgle.com";
		String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36";

		Map<String, List<String>> headers = new HashMap<>(100);
		headers.put(Header.ORIGIN.toString(), Collections.singletonList(origin));
		headers.put(Header.USER_AGENT.toString(), Collections.singletonList(userAgent));
		headers.put(Header.REFERER.toString(), Collections.singletonList(reffer));
		return headers;
	}
}
