package com.hdr;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author Huang Da Ren
 */
@Slf4j
public class Main {


	@Test
	public void testLog() {
		log.info("hello,{}","hdr");
	}
}
