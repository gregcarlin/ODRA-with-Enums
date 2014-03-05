package odra.system.log;

import odra.AssemblyInfo;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public final class UniversalLogger {

	private final Logger logger;

	private final AssemblyInfo assemblyInfo;

	private final Class originClass;

	private <Type extends AssemblyInfo> UniversalLogger(Class<Type> assemblyInfoClass, Class originClass) {
		this.logger = Logger.getLogger(originClass);
		this.assemblyInfo = AssemblyInfo.genericGetInstance(assemblyInfoClass);
		this.originClass = originClass;
	}

	public static <Type extends AssemblyInfo> UniversalLogger getInstance(Class<Type> assemblyInfoClass,
				Class originClass) {
		return new UniversalLogger(assemblyInfoClass, originClass);
	}

	private void logStackTrace(Severity level, Throwable ex) {
		StackTraceElement[] elements = ex.getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			this.logger.log(level.loggingLevel, elements[i]);
		}
	}

	private void log(Severity level, String key, String message, Throwable ex) {
		if (this.isEffectiveLevelLessOrEqual(level)) {
			this.logger.log(level.loggingLevel, getLoggerMessage(key, message), ex);
			this.logStackTrace(level, ex);
		}
	}

	private void log(Severity level, String key, String message) {
		if (this.isEffectiveLevelLessOrEqual(level)) {
			this.logger.log(level.loggingLevel, getLoggerMessage(key, message));
		}
	}

	private void log(Severity level, String message) {
		if (this.isEffectiveLevelLessOrEqual(level)) {
			this.logger.log(level.loggingLevel, message);
		}
	}

	private void log(Severity level, String message, Throwable ex) {
		if (this.isEffectiveLevelLessOrEqual(level)) {
			this.logger.log(level.loggingLevel, message, ex);
			this.logStackTrace(level, ex);
		}
	}

	private void log(Severity level, Object message, Throwable ex) {
		if (this.isEffectiveLevelLessOrEqual(level)) {
			this.logger.log(level.loggingLevel, message, ex);
			this.logStackTrace(level, ex);
		}
	}

	public void fatal(String key, String message, Throwable ex) {
		this.log(Severity.FATAL, key, message, ex);
	}

	public void fatal(String key, String message) {
		this.log(Severity.ERROR, key, message);
	}

	public void fatal(Object object, Throwable ex) {
		this.log(Severity.ERROR, object, ex);
	}

	public void fatal(String message, Throwable ex) {
		this.log(Severity.ERROR, message, ex);
	}

	public void error(String key, String message, Throwable ex) {
		this.log(Severity.ERROR, key, message, ex);
	}

	public void error(String key, String message) {
		this.log(Severity.ERROR, key, message);
	}

	public void error(Object object, Throwable ex) {
		this.log(Severity.ERROR, object, ex);
	}

	public void error(String message) {
		this.log(Severity.ERROR, message);
	}

	public void error(String message, Throwable ex) {
		this.log(Severity.ERROR, message, ex);
	}

	public void warn(String key, String message, Throwable ex) {
		this.log(Severity.WARN, key, message, ex);
	}

	public void warn(String key, String message) {
		this.log(Severity.WARN, key, message);
	}

	public void warn(String message) {
		this.log(Severity.WARN, message);
	}

	public void info(String key, String message, Throwable ex) {
		this.log(Severity.INFO, key, message, ex);
	}

	public void info(String key, String message) {
		this.log(Severity.INFO, key, message);
	}

	public void info(Object object) {
		this.info(object.toString());
	}

	public void info(String message) {
		this.log(Severity.INFO, message);
	}

	public void debug(String key, String message, Throwable ex) {
		this.log(Severity.DEBUG, key, message, ex);
	}

	public void debug(String key, String message) {
		this.log(Severity.DEBUG, key, message);
	}

	public void debug(String message) {
		this.log(Severity.DEBUG, message);
	}

	public void debug(Object object) {
		this.debug(object.toString());
	}

	public void trace(String key, String message, Throwable ex) {
		this.log(Severity.DEBUG, key, message, ex);
	}

	public void trace(String key, String message) {
		this.log(Severity.DEBUG, key, message);
	}

	public void trace(String message) {
		this.log(Severity.DEBUG, message);
	}

	private String getLoggerMessage(String key, String message) {
		return this.assemblyInfo.getLocalizedMessage(this.originClass, key, message);
	}

	private boolean isEffectiveLevelLessOrEqual(Severity level) {
		Level loggingLevel = level.loggingLevel;
		Level effectiveLevel = this.logger.getEffectiveLevel();
		return loggingLevel.isGreaterOrEqual(effectiveLevel);
	}

	public static enum Severity {

		OFF(0, Level.OFF), FATAL(1, Level.FATAL), ERROR(2, Level.ERROR), WARN(3, Level.WARN), INFO(4, Level.INFO), DEBUG(
					5, Level.DEBUG), TRACE(6, Level.TRACE);

		private final int severityValue;

		private final Level loggingLevel;

		private Severity(int severityValue, Level loggingLevel) {
			this.severityValue = severityValue;
			this.loggingLevel = loggingLevel;
		}

		public static Severity[] getSeverityHierarchy() {
			return values();
		}

		public boolean isAtLeastEquallySevere(Severity otherLevel) {
			return this.compareTo(otherLevel) <= 0;
		}

		public int getSeverityValue() {
			return this.severityValue;
		}
	}
}