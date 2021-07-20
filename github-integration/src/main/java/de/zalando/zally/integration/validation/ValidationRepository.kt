package de.zalando.zally.integration.validation

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ValidationRepository : JpaRepository<PullRequestValidation, Long>
