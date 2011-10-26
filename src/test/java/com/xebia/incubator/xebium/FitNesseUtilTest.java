package com.xebia.incubator.xebium;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FitNesseUtilTest {

	@Test
	public void buildString() {
		assertEquals("Suite,Test,Normal", FitNesseUtil.stringArrayToString(new String[] { "Suite", "Test", "Normal" }));
	}

	@Test
	public void buildStringWithEscapedChars() {
		assertEquals("Suite,T\\,est,N\\\\ormal", FitNesseUtil.stringArrayToString(new String[] { "Suite", "T,est", "N\\ormal" }));
	}

}
