package com.cloudmonitor.ai.scheduler;

import com.cloudmonitor.ai.service.DesignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to purge old design records.
 *
 * Configure via:
 * - app.scheduler.purge.enabled: Enable/disable the scheduler
 * - app.scheduler.purge.retention-days: Number of days to retain designs
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PurgeOldDesignsScheduler {

    private final DesignService designService;

    @Value("${app.scheduler.purge.enabled:false}")
    private boolean enabled;

    @Value("${app.scheduler.purge.retention-days:30}")
    private int retentionDays;

    /**
     * Run daily at 2 AM to purge old designs.
     */
    @Scheduled(cron = "${app.scheduler.purge.cron:0 0 2 * * ?}")
    public void purgeOldDesigns() {
        if (!enabled) {
            log.debug("Purge scheduler is disabled");
            return;
        }

        log.info("Starting scheduled purge of designs older than {} days", retentionDays);

        try {
            int deleted = designService.purgeOldDesigns(retentionDays);
            log.info("Scheduled purge completed. Deleted {} designs", deleted);
        } catch (Exception e) {
            log.error("Error during scheduled purge", e);
        }
    }
}
