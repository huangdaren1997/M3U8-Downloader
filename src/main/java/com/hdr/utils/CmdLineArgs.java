package com.hdr.utils;

import org.kohsuke.args4j.Argument;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hdr
 */
public class CmdLineArgs {

	@Option(name = "-d",usage = "download mode")
	public boolean downloadFlag;

	@Option(name="-p",usage = "use properties file")
	public String propertiesPath;

	@Argument
	private List<String> arguments = new ArrayList<>();


	public static void main(String[] args) {
		CmdLineArgs cmdLineArgs = new CmdLineArgs();
		CmdLineParser parser = new CmdLineParser(cmdLineArgs);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			e.printStackTrace();
		}

		System.out.println(cmdLineArgs.propertiesPath);

	}

}
