package com.hdr.ts;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author Huang Da Ren
 */
@Data
public class Ts {
	private String url;
	private Map<String, List<String>> headers;
	private String savePath;
}
