/*
 * Copyright 2010-2012 Xebia b.v.
 * Copyright 2010-2012 Xebium contributers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.xebia.incubator.xebium;

import com.thoughtworks.selenium.CommandProcessor;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.xebia.incubator.xebium.FitNesseUtil.asFile;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * Deals with the matters of capturing a screenshot and saving those to file.
 */
class ScreenCapture {

	enum ScreenshotPolicy {
		NONE,
		ASSERTION,
		FAILURE,
		STEP
	}


	private static final Logger LOG = LoggerFactory.getLogger(ScreenCapture.class);

	private static final String PATH_SEP = System.getProperty("file.separator");

	private static final int INITIAL_STEP_NUMBER = 1;

	// ensure step indexes are maintained among ScreenCapture instances
	private static Map<String, Integer> stepNumbers = new HashMap<String, Integer>();

	// screenshotBaseDir is guaranteed to have a trailing path separtor.
	private String screenshotBaseDir = "FitNesseRoot/files/testResults/screenshots/".replace("/", PATH_SEP);

	private CommandProcessor commandProcessor;

	private ScreenshotPolicy screenshotPolicy = ScreenshotPolicy.ASSERTION;


	void setCommandProcessor(CommandProcessor commandProcessor) {
		this.commandProcessor = commandProcessor;
	}

	void setScreenshotBaseDir(String screenshotBaseDir) {
		this.screenshotBaseDir = asFile(screenshotBaseDir).getAbsolutePath();
		if (!this.screenshotBaseDir.endsWith(PATH_SEP)) {
			this.screenshotBaseDir += PATH_SEP;
		}
	}

	void setScreenshotPolicy(String policy) throws IOException {
		if ("none".equals(policy) || "nothing".equals(policy)) {
			screenshotPolicy = ScreenshotPolicy.NONE;
		} else if ("failure".equals(policy) || "error".equals(policy)) {
			screenshotPolicy = ScreenshotPolicy.FAILURE;
		} else if ("step".equals(policy) || "every step".equals(policy)) {
			screenshotPolicy = ScreenshotPolicy.STEP;
		} else if ("assertion".equals(policy) || "every assertion".equals(policy)) {
			screenshotPolicy = ScreenshotPolicy.ASSERTION;
		}
		LOG.info("Screenshot policy set to " + screenshotPolicy);
	}

	/**
	 * Is a screenshot desired, based on the command and the test result.
	 */
	public boolean requireScreenshot(final ExtendedSeleniumCommand command,
			boolean result) {
		return
			(!command.isAssertCommand()
				&& !command.isVerifyCommand()
				&& !command.isWaitForCommand()
				&& screenshotPolicy == ScreenshotPolicy.STEP)
			|| (!result
				&& (screenshotPolicy == ScreenshotPolicy.FAILURE
					|| (command.isAssertCommand() && screenshotPolicy == ScreenshotPolicy.ASSERTION)));
	}


	void captureScreenshot(String methodName, String[] values) {
		int stepNumber = nextStepNumber();
		final File file = createFile(screenshotBaseDir + String.format("%04d-%s.png", stepNumber, trim(methodName)));
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
	 * <p>Provide an easy to use index.html file for viewing the screenshots.
	 * </p>
	 * <p>The base directory is expected to exist at this point.
	 * </p>
	 *
	 * @throws IOException
	 */
	private void updateIndexFile(int stepNumber, File file, String methodName,
			String[] values) throws IOException {
		File indexFile = initializeIndexIfNeeded();

		BufferedWriter w = new BufferedWriter(new FileWriter(indexFile, stepNumber > INITIAL_STEP_NUMBER));
		try {
			String title = "| " + methodName + " | " + (values.length > 0 ? values[0] : "") + " | " + (values.length > 1 ? values[1] : "") + " |";
			w.write("<h2>" + stepNumber + ". " + title + "</h2>\n");
			w.write("<img src='" + file.getName() +"' alt='" + title + "'/>\n");
		} finally {
			try {
				w.close();
			} catch (IOException e) {
				LOG.error("Unable to close screenshot file " + file.getPath(), e);
			}
		}
	}

	private File initializeIndexIfNeeded() throws IOException {
		File indexFile = createFile(screenshotBaseDir + "index.html");
		if (!indexFile.exists()) {
			BufferedWriter w = new BufferedWriter(new FileWriter(indexFile, false));
			try {
				w.write("<h1>Xebium screenshot overview</h1>\n");
			} finally {
				w.close();
			}
		}
		return indexFile;
	}

	private File createFile(String filename) {
		File file = new File(filename);
		File parent = file.getParentFile();

		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}

		return file;
	}

	static void writeToFile(final File file, final String output) throws IOException {
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
