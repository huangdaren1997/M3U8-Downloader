package com.hdr.ts;

import java.io.File;
import java.util.concurrent.BlockingQueue;

/**
 * 用来产生Ts对象的抽象方法
 *
 * @author Huang Da Ren
 */
public abstract class AbstractTsFactory {

	public abstract BlockingQueue<Ts> produce(File m3u8);


}
