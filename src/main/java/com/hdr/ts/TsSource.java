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
public class TsSource {
	private String savePath;
	private String name;
	private String url;
	private Map<String, List<String>> headers;
}
