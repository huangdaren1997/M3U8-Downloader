package com.hdr.core;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author hdr
 */
@Data
public class DownloadMessage {

	public Map<String, List<String>> requestHeader;

	public List<String> tsUrls;

	public String savePath;

	public String saveName;

}
