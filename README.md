Xebium [![Build Status](https://travis-ci.org/xebia/Xebium.png?branch=master)](https://travis-ci.org/xebia/Xebium)
======
[Xebium](http://xebia.github.com/Xebium/) combines the powers of FitNesse and Selenium. Visit the [xebium home page](http://xebia.github.com/Xebium/) for more details and examples.

Maintainance mode
-----------------

While Xebium is no longer actively maintained by its original authors,
we do strive to respond to issues and PR's as time allows.

A version that depends on newer versions of FitNesse and Selenium is
in the works at https://github.com/xebia/Xebium/pull/148, but (ironically)
we feel our testsuite is insufficient to release it without further testing.
Help is welcome.

Features
--------

* Full Selenium-IDE - FitNesse roundtrip with your web tests
* Create data-driven tests.
* Tests are executed using the modern WebDriver interfaces and Selenium Server.
* Tests are run from FitNesse using the SLIM engine.

Getting Started
---------------

Execute the following command:

	$ mvn -Pfitnesse test

and open a browser, pointing at http://localhost:8000.

Click the Xebium link in order to get to the Xebium section and read on in the Getting Started page.

Have fun!

<hr/>
PS. For those of you who import Xebium as an Eclipse project, run `mvn eclipse:eclipse` to get your classpath setup right.
