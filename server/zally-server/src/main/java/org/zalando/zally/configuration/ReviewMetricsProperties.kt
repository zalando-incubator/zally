package org.zalando.zally.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("metrics.review")
class ReviewMetricsProperties {
    var namePrefix: String = "zally_"
    var filterLabels: List<String> = emptyList()
}
