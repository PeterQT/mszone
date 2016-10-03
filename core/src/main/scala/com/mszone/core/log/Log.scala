package com.mszone.core.log

import org.apache.logging.log4j.LogManager

trait Log {
  protected val logger: Logger = new Logger(LogManager.getLogger(getClass.getName))
}

