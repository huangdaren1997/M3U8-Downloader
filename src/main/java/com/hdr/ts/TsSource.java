package com.hdr.ts;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Huang Da Ren
 */
@Getter
@Setter
public class TsSource {

	private File savePath;
	private String url;
	private Map<String, List<String>> headers;
	private int failureTime = 0;

}
