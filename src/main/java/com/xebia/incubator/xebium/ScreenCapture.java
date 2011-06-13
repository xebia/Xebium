package com.xebia.incubator.xebium;

import static com.xebia.incubator.xebium.FitNesseUtil.asFile;
import static org.apache.commons.lang.StringUtils.trim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.selenium.CommandProcessor;

/**
 * Deals with the matters of capturing a screenshot and saving those to file.
 */
class ScreenCapture {

	private static final Logger LOG = LoggerFactory.getLogger(ScreenCapture.class);

	private static final String PATH_SEP = System.getProperty("file.separator");
	
	private static final int INITIAL_STEP_NUMBER = 1;

	// screenshotBaseDir is guaranteed to have a trailing path separtor.
	private String screenshotBaseDir = "FitNesseRoot/files/testResults/screenshots/".replace("/", PATH_SEP);
	
	private static Map<String, Integer> stepNumbers = new HashMap<String, Integer>();

	private CommandProcessor commandProcessor;

	
	ScreenCapture(CommandProcessor commandProcessor) {
		this.commandProcessor = commandProcessor;
	}
	
	void setScreenshotBaseDir(String screenshotBaseDir) {
		this.screenshotBaseDir = screenshotBaseDir;
		if (!this.screenshotBaseDir.endsWith(PATH_SEP)) {
			this.screenshotBaseDir += PATH_SEP;
		}
	}
	
	void captureScreenshot(String methodName, String[] values) {
		int stepNumber = nextStepNumber();
		final File file = asFile(screenshotBaseDir + String.format("%04d-%s.png", stepNumber, trim(methodName)));
		LOG.info("Storing screenshot in " + file.getAbsolutePath());

		try {
			String output = executeCommand("captureScreenshotToString", new String[] { });
	
			writeToFile(file, output);
			
			updateIndexFile(stepNumber, file, methodName, values);
		} catch (Exception e) {
			LOG.warn("Unable to finish screenshot capturing: " + e.getMessage());
		}
	}
	
	/**
	 * @return next step number dependent on screenshot base dir.
	 */
	private int nextStepNumber() {
		synchronized (stepNumbers) {
			Integer i = stepNumbers.get(screenshotBaseDir);
			if (i == null) {
				i = INITIAL_STEP_NUMBER;
			}
			stepNumbers.put(screenshotBaseDir, i + 1);
			return i;
		}
	}

	private String executeCommand(String methodName, final String[] values) {
			return commandProcessor.doCommand(methodName, values);
	}

	/**
	 * Provide an easy to use index.html file for viewing the screenshots.
	 * 
	 * @param stepNumber
	 * @param file
	 * @param methodName
	 * @param values
	 */
	private void updateIndexFile(int stepNumber, File file, String methodName,
			String[] values) {
		final File parent = new File(file.getParent() + PATH_SEP + "index.html");

		if (stepNumber == INITIAL_STEP_NUMBER) {
			// TODO: start new index.html file
		} // TODO: else: append screenshot to existing index.html file
	}

	static void writeToFile(final File file, final String output) throws IOException {
		final File parent = file.getParentFile();
		
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		
		FileOutputStream w = new FileOutputStream(file);
		try {
			w.write(Base64.decodeBase64(output));
		} finally {
			try {
				w.close();
			} catch (IOException e) {
				LOG.error("Unable to close screenshot file " + file.getPath(), e);
			}
		}
	}

}
