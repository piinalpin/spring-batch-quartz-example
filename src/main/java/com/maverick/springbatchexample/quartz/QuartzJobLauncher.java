package com.maverick.springbatchexample.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class QuartzJobLauncher extends QuartzJobBean {

    private static final Logger LOG = LoggerFactory.getLogger(QuartzJobLauncher.class);

    private String jobName;
    private JobLauncher jobLauncher;
    private JobLocator jobLocator;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    public void setJobLauncher(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    public JobLocator getJobLocator() {
        return jobLocator;
    }

    public void setJobLocator(JobLocator jobLocator) {
        this.jobLocator = jobLocator;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            Job job = jobLocator.getJob(jobName);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time",System.currentTimeMillis())
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            LOG.info("Job was completed successfully.", job.getName(), jobExecution.getId());
        } catch (JobParametersInvalidException | NoSuchJobException | JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobRestartException ex) {
            LOG.error("Failed execute job !!!");
            LOG.error(ex.getMessage());
        }
    }

}
