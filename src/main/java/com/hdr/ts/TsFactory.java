package com.hdr.ts;

import java.io.File;
import java.util.concurrent.BlockingQueue;

/**
 * @author hdr
 */
public interface TsFactory {

	BlockingQueue<Ts> produce(File m3u8);
}
