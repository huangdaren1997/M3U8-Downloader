package com.hdr.ts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author Huang Da Ren
 */
@Getter
@Setter
public class Ts {
	private String url;
	private Map<String, List<String>> headers;
	private String savePath;

	@Override
	public String toString() {
		return "Ts{" +
				"url='" + url + '\'' +
				", savePath='" + savePath + '\'' +
				'}';
	}
}
