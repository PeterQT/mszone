package com.mszone.core.log

import org.apache.logging.log4j.{Logger => Log4jLogger}

class Logger private[mszone] (logger: Log4jLogger) {

  def fatal(message: => Any): Unit = if (logger.isFatalEnabled) logger.fatal(message)

  def fatal(message: => Any, t: Throwable): Unit = if (logger.isFatalEnabled) logger.fatal(message, t)

  def error(message: => Any): Unit = if (logger.isErrorEnabled) logger.error(message)

  def error(message: => Any, t: Throwable): Unit = if (logger.isErrorEnabled) logger.error(message, t)

  def warn(message: => Any): Unit = if (logger.isWarnEnabled) logger.warn(message)

  def info(message: => Any): Unit = if (logger.isInfoEnabled) logger.info(message)

  def debug(message: => Any): Unit = if (logger.isDebugEnabled) logger.debug(message)

  def trace(message: => Any): Unit = if (logger.isTraceEnabled) logger.trace(message)
}