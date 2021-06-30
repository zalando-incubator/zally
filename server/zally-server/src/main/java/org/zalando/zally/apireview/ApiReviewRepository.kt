package org.zalando.zally.apireview

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.zalando.zally.statistic.ReviewStatistics
import java.time.LocalDate
import java.util.UUID

interface ApiReviewRepository : CrudRepository<ApiReview, Long> {

    @Query(
        """
        SELECT new org.zalando.zally.statistic.ReviewStatistics(
	        COUNT(r) AS totalReviews,
	        COUNT(DISTINCT r.name) AS totalReviewDeduplicated,
	        COALESCE(SUM(CASE WHEN r.isSuccessfulProcessed = 'True' THEN 1 ELSE 0 END),0) AS successfulReviews,
	        COALESCE(SUM(r.numberOfEndpoints),0) AS numberOfEndpoints,
	        COALESCE(SUM(r.mustViolations),0) AS mustViolations,
	        COALESCE(SUM(r.shouldViolations),0) AS shouldViolations,
	        COALESCE(SUM(r.mayViolations),0) AS mayViolations,
	        COALESCE(SUM(r.hintViolations),0) AS hintViolations)
        FROM org.zalando.zally.apireview.ApiReview r
        WHERE day >= :from AND day <= :to AND user_agent LIKE :userAgent
    """
    )
    fun getReviewStatistics(
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
        @Param("userAgent") userAgent: String = "%"
    ): ReviewStatistics

    @Query(
        """
        SELECT r
        FROM org.zalando.zally.apireview.ApiReview r
        WHERE (r.name, r.created) IN (
            SELECT a.name, MAX(a.created) 
            FROM org.zalando.zally.apireview.ApiReview a
            LEFT OUTER JOIN a.customLabels l
            WHERE l IN (:labelFilter)
            GROUP BY a.name, l
        )
    """
    )
    fun findLatestApiReviews(@Param("labelFilter") labelFilter: List<String>): List<ApiReview>

    @Query(
        """
        SELECT r
        FROM org.zalando.zally.apireview.ApiReview r
        WHERE (r.name, r.created) IN (
            SELECT a.name, MAX(a.created) 
            FROM org.zalando.zally.apireview.ApiReview a
            LEFT OUTER JOIN a.customLabels l
            GROUP BY a.name, l
        )
    """
    )
    fun findLatestApiReviews(): List<ApiReview>

    /**
     * Find ApiReview instance by it's externalId UUID.
     *
     * @return the found ApiReview or null.
     */
    fun findByExternalId(externalId: UUID): ApiReview?
}
