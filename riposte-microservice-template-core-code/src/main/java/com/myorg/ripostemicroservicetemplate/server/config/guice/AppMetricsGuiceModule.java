package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.riposte.metrics.codahale.CodahaleMetricsCollector;
import com.nike.riposte.metrics.codahale.CodahaleMetricsEngine;
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener;
import com.nike.riposte.metrics.codahale.ReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultGraphiteReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultJMXReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultSLF4jReporterFactory;
import com.nike.riposte.server.config.AppInfo;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * A Guice module that initializes the various components of the Riposte metrics-gathering system. The main component
 * that the application will want to inject and interact with is the {@link CodahaleMetricsCollector} - the rest of
 * this class is internal plumbing.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AppMetricsGuiceModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Provides
    @Singleton
    public CodahaleMetricsCollector codahaleMetricsCollector(@Nullable List<ReporterFactory> reporters) {
        if (reporters == null) {
            return null;
        }

        return new CodahaleMetricsCollector();
    }

    @Provides
    @Singleton
    public List<ReporterFactory> metricsReporters(
        @Named("metrics.slf4j.reporting.enabled") boolean slf4jReportingEnabled,
        @Named("metrics.jmx.reporting.enabled") boolean jmxReportingEnabled,
        @Named("metrics.graphite.url") String graphiteUrl,
        @Named("metrics.graphite.port") int graphitePort,
        @Named("metrics.graphite.reporting.enabled") boolean graphiteEnabled,
        @Named("appInfoFuture") CompletableFuture<AppInfo> appInfoFuture
    ) {
        List<ReporterFactory> reporters = new ArrayList<>();

        if (slf4jReportingEnabled) {
            reporters.add(new DefaultSLF4jReporterFactory());
        }

        if (jmxReportingEnabled) {
            reporters.add(new DefaultJMXReporterFactory());
        }

        if (graphiteEnabled) {
            AppInfo appInfo = appInfoFuture.join();
            String graphitePrefix = appInfo.appId() + "." + appInfo.dataCenter() + "." + appInfo.environment()
                                    + "." + appInfo.instanceId();
            reporters.add(new DefaultGraphiteReporterFactory(graphitePrefix, graphiteUrl, graphitePort));
        }

        if (reporters.isEmpty()) {
            logger.info("No metrics reporters enabled - disabling metrics entirely.");
            return null;
        }

        String metricReporterTypes = reporters.stream()
                                              .map(rf -> rf.getClass().getSimpleName())
                                              .collect(Collectors.joining(",", "[", "]"));
        logger.info("Metrics reporters enabled. metric_reporter_types={}", metricReporterTypes);

        return reporters;
    }

    @Provides
    @Singleton
    public CodahaleMetricsEngine codahaleMetricsEngine(@Nullable CodahaleMetricsCollector cmc,
                                                       @Nullable List<ReporterFactory> reporters,
                                                       @Named("metrics.reportJvmMetrics") boolean reportJvmMetrics) {
        if (cmc == null) {
            return null;
        }

        if (reporters == null) {
            reporters = Collections.emptyList();
        }

        CodahaleMetricsEngine engine = new CodahaleMetricsEngine(cmc, reporters);
        if (reportJvmMetrics) {
            engine.reportJvmMetrics();
        }
        engine.start();
        return engine;
    }

    @Provides
    @Singleton
    public CodahaleMetricsListener metricsListener(@Nullable CodahaleMetricsCollector metricsCollector,
                                                   @Nullable CodahaleMetricsEngine engine) {
        if (metricsCollector == null) {
            return null;
        }

        // We don't actually need the CodahaleMetricsEngine, but we ask for it here to guarantee that it is created and
        //      started.

        return new CodahaleMetricsListener(metricsCollector);
    }
}
