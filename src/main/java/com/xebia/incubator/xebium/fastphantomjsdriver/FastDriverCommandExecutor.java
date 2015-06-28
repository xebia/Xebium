package com.xebia.incubator.xebium.fastphantomjsdriver;

import com.google.common.base.Throwables;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.remote.service.DriverService;

import java.io.IOException;
import java.net.*;

/**
 * Variation on the official DriverCommandExecutor+HttpCommandExecutor.
 *
 * Sleeps 100ms before retrying a failed connection instead of 2 seconds.
 *
 * Useful because PhantomJS regularly drops the connection without a response.
 *
 * See also:
 * http://code.google.com/p/selenium/issues/detail?id=6803&thanks=6803&ts=1389020345
 * https://github.com/ariya/phantomjs/issues/11875
 */
public class FastDriverCommandExecutor extends FastHttpCommandExecutor {

	private final DriverService service;

	public FastDriverCommandExecutor(DriverService service) {
		super(service.getUrl());
		this.service = service;
	}

	/**
	 * Sends the {@code command} to the driver server for execution. The server will be started
	 * if requesting a new session. Likewise, if terminating a session, the server will be shutdown
	 * once a response is received.
	 *
	 * @param command The command to execute.
	 * @return The command response.
	 * @throws java.io.IOException If an I/O error occurs while sending the command.
	 */
	@Override
	public Response execute(Command command) throws IOException {
		if (DriverCommand.NEW_SESSION.equals(command.getName())) {
			service.start();
		}

		try {
			return super.execute(command);
		} catch (Throwable t) {
			Throwable rootCause = Throwables.getRootCause(t);
			if (rootCause instanceof ConnectException &&
					"Connection refused".equals(rootCause.getMessage()) &&
					!service.isRunning()) {
				throw new WebDriverException("The driver server has unexpectedly died!", t);
			}
			Throwables.propagateIfPossible(t);
			throw new WebDriverException(t);
		} finally {
			if (DriverCommand.QUIT.equals(command.getName())) {
				service.stop();
			}
		}
	}
}
