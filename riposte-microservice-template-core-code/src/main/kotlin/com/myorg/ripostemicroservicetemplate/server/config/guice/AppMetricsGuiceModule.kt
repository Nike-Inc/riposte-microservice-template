package com.myorg.ripostemicroservicetemplate.server.config.guice

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.nike.riposte.metrics.codahale.CodahaleMetricsCollector
import com.nike.riposte.metrics.codahale.CodahaleMetricsEngine
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener
import com.nike.riposte.metrics.codahale.ReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultGraphiteReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultJMXReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultSLF4jReporterFactory
import com.nike.riposte.server.config.AppInfo
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.util.concurrent.CompletableFuture
import javax.annotation.Nullable
import javax.inject.Named
import javax.inject.Singleton

/**
 * A Guice module that initializes the various components of the Riposte metrics-gathering system. The main component
 * that the application will want to inject and interact with is the [CodahaleMetricsCollector] - the rest of
 * this class is internal plumbing.
 */
@Suppress("unused")
class AppMetricsGuiceModule : AbstractModule() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Provides
    @Singleton
    @JvmSuppressWildcards
    fun codahaleMetricsCollector(@Nullable reporters: List<ReporterFactory>?): CodahaleMetricsCollector? {
        return if (reporters == null) null else CodahaleMetricsCollector()
    }

    @Provides
    @Singleton
    fun metricsReporters(
        @Named("metrics.slf4j.reporting.enabled") slf4jReportingEnabled: Boolean,
        @Named("metrics.jmx.reporting.enabled") jmxReportingEnabled: Boolean,
        @Named("metrics.graphite.url") graphiteUrl: String,
        @Named("metrics.graphite.port") graphitePort: Int,
        @Named("metrics.graphite.reporting.enabled") graphiteEnabled: Boolean,
        @Named("appInfoFuture") appInfoFuture: CompletableFuture<AppInfo>
    ): List<ReporterFactory>? {
        val reporters = ArrayList<ReporterFactory>()

        if (slf4jReportingEnabled)
            reporters.add(DefaultSLF4jReporterFactory())

        if (jmxReportingEnabled)
            reporters.add(DefaultJMXReporterFactory())

        if (graphiteEnabled) {
            val appInfo = appInfoFuture.join()
            val graphitePrefix = appInfo.appId() + "." + appInfo.dataCenter() + "." + appInfo.environment() +
                "." + appInfo.instanceId()
            reporters.add(DefaultGraphiteReporterFactory(graphitePrefix, graphiteUrl, graphitePort))
        }

        if (reporters.isEmpty()) {
            logger.info("No metrics reporters enabled - disabling metrics entirely.")
            return null
        }

        val metricReporterTypes = reporters.joinToString(",", "[", "]") { rf -> rf.javaClass.simpleName }
        logger.info("Metrics reporters enabled. metric_reporter_types={}", metricReporterTypes)

        return reporters
    }

    @Provides
    @Singleton
    @JvmSuppressWildcards
    fun codahaleMetricsEngine(
        @Nullable cmc: CodahaleMetricsCollector?,
        @Nullable reporters: List<ReporterFactory>?,
        @Named("metrics.reportJvmMetrics") reportJvmMetrics: Boolean
    ): CodahaleMetricsEngine? {
        if (cmc == null)
            return null

        val engine = CodahaleMetricsEngine(cmc, reporters ?: emptyList())
        if (reportJvmMetrics) {
            engine.reportJvmMetrics()
        }
        engine.start()
        return engine
    }

    @Provides
    @Singleton
    fun metricsListener(
        @Nullable metricsCollector: CodahaleMetricsCollector?,
        // We don't actually need the CodahaleMetricsEngine, but we ask for it here to guarantee that it is created
        //      and started.
        @Suppress("UNUSED_PARAMETER")
        @Nullable engine: CodahaleMetricsEngine?
    ): CodahaleMetricsListener? {
        return if (metricsCollector == null) null else CodahaleMetricsListener(metricsCollector)
    }
}
