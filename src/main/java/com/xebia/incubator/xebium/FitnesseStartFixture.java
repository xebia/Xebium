package com.xebia.incubator.xebium;

import fitnesseMain.*;

/**
 * Fixture to start FitNesse itself - mainly for our own integration tests
  */
public class FitnesseStartFixture {
  public void startFitnesseOnPort(Integer port) throws Exception {
    new FitNesseMain().launchFitNesse(new Arguments("-p", port.toString()));
  }
}
