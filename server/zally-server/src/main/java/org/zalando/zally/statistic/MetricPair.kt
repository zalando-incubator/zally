package org.zalando.zally.statistic

import java.util.concurrent.atomic.AtomicLong

data class MetricPair(val metricName: MetricName, val metricValue: AtomicLong)
