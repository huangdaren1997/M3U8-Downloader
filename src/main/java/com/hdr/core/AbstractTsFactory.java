package com.hdr.core;

import java.util.List;
import java.util.Map;

/**
 * @author hdr
 */
public abstract class AbstractTsFactory {

	public static Map<String, List<String>> header;

	/**
	 * 生成TsUrlList
	 * @return TsUrlList
	 */
	public abstract List<String> generateTsList();

}
