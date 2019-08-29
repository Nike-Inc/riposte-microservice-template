import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder

private String getEnvironmentString() {
    def environment = System.getProperty("@environment")
    if (environment != null) {
        return environment
    }

    return System.getProperty("archaius.deployment.environment")
}

private boolean isLocalEnvironment() {
    // This logback config file is for unit testing - the answer is always true
    return true
}

private boolean shouldOutputToConsole() {
    // This logback config file is for unit testing - the answer is always true
    return true
}

private boolean shouldOutputToLogFile() {
    // This logback config file is for unit testing - the answer is always false
    return false
}

addInfo("Processing logback.groovy, environment: " + getEnvironmentString() + "...")
println("Processing logback.groovy, environment: " + getEnvironmentString() + "...")

def encoderPattern = "traceId=%X{traceId:-NO_TRACE_RUNNING} %date{\"yyyy-MM-dd'T'HH:mm:ss,SSSXXX\"} [%thread] |-%-5level %logger{36} - %msg%n"
def defaultAsyncQueueSize = 16000

def Appender consoleAppender = null
def allAsyncAppendersArray = []

addInfo("******Outputting to console: " + shouldOutputToConsole())
println("******Outputting to console: " + shouldOutputToConsole())

if (shouldOutputToConsole()) {
    appender("ConsoleAppender", ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = encoderPattern
        }

        consoleAppender = component
    }

    appender("AsyncConsoleAppender", AsyncAppender) {
        queueSize = defaultAsyncQueueSize
        component.addAppender(consoleAppender)
    }

    allAsyncAppendersArray.add("AsyncConsoleAppender")
}

logger("org.apache.http", INFO, allAsyncAppendersArray, false)
logger("io.restassured", INFO, allAsyncAppendersArray, false)
logger("com.ning.http.client", INFO, allAsyncAppendersArray, false)

logger("com.nike.wingtips.Tracer", INFO, allAsyncAppendersArray, false)
logger("com.nike.wingtips.http.HttpRequestTracingUtils", INFO, allAsyncAppendersArray, false)
logger("VALID_WINGTIPS_SPANS", INFO, allAsyncAppendersArray, false)
logger("INVALID_WINGTIPS_SPANS", INFO, allAsyncAppendersArray, false)

logger("com.codahale.metrics.JmxReporter", INFO, allAsyncAppendersArray, false)

logger("org.reflections.Reflections", INFO, allAsyncAppendersArray, false)

// Root logger.
root(DEBUG, allAsyncAppendersArray)

addInfo("...logback.groovy processing finished.")
println("...logback.groovy processing finished.")