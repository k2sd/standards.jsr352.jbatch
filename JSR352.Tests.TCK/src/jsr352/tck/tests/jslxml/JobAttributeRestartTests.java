/*
 * Copyright 2012 International Business Machines Corp.
 * 
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package jsr352.tck.tests.jslxml;

import static jsr352.tck.utils.AssertionUtils.assertWithMessage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.batch.operations.JobOperator.BatchStatus;
import javax.batch.operations.exception.JobInstanceAlreadyCompleteException;
import javax.batch.operations.exception.JobRestartException;
import javax.batch.operations.exception.JobStartException;
import javax.batch.operations.exception.NoSuchJobException;
import javax.batch.operations.exception.NoSuchJobExecutionException;
import javax.batch.runtime.JobExecution;

import jsr352.tck.utils.IOHelper;
import jsr352.tck.utils.JobOperatorBridge;

import org.junit.Before;
import org.testng.Reporter;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class JobAttributeRestartTests {

	private JobOperatorBridge jobOp = null;
	
	private long TIMEOUT = 5000L;
	
	private static final String JOB_FILE = "job_attributes_test";
	
	/**
	 * @testName: testJobAttributeRestartableTrue
	 * @assertion: Section 5.1 job attribute restartable
	 * @test_Strategy: set restartable true should allow job to restart
	 * 
	 * @throws JobStartException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws JobRestartException 
	 * @throws NoSuchJobException 
	 * @throws NoSuchJobExecutionException 
	 * @throws JobInstanceAlreadyCompleteException 
	 */
	@Test @org.junit.Test
	public void testJobAttributeRestartableTrue() throws JobStartException, FileNotFoundException, IOException, InterruptedException, JobInstanceAlreadyCompleteException, NoSuchJobExecutionException, NoSuchJobException, JobRestartException {
		
		Properties jobParams = new Properties();
		jobParams.setProperty("restartable", "true");

		Reporter.log("starting job");
		JobExecution jobExec = jobOp.startJobWithoutWaitingForResult(JOB_FILE, jobParams);

		IOHelper.waitForBatchStatusOrTimeout(jobExec, BatchStatus.STARTED, TIMEOUT);
		Reporter.log("Job Status = " + jobExec.getBatchStatus());
		assertWithMessage("Job started", jobExec.getBatchStatus().equals(BatchStatus.STARTED));
		Reporter.log("job started");
		
		if(jobExec.getBatchStatus().equals(BatchStatus.STARTED)) {
			
			Reporter.log("stopping job");
			jobOp.stopJobAndWaitForResult(jobExec);
			
			Reporter.log("Job Status = " + jobExec.getBatchStatus());
			assertWithMessage("Job stopped", jobExec.getBatchStatus().equals(BatchStatus.STOPPED));
			Reporter.log("job stopped");
			
			if(jobExec.getBatchStatus().equals(BatchStatus.STOPPED) && jobExec.getJobParameters().getProperty("restartable").equalsIgnoreCase("true")) {

				Reporter.log("restarting job");
				JobExecution newJobExec = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId());

				Reporter.log("Job Status = " + newJobExec.getBatchStatus());
				assertWithMessage("Job completed", newJobExec.getBatchStatus().equals(BatchStatus.COMPLETED));
				Reporter.log("job completed");
			}
		}
	}
	
	/**
	 * @testName: testJobAttributeRestartableFalse
	 * @assertion: Section 5.1 job attribute restartable
	 * @test_Strategy: set restartable false should not allow job to restart
	 * 
	 * @throws JobStartException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test @org.junit.Test
	public void testJobAttributeRestartableFalse() throws JobStartException, FileNotFoundException, IOException, InterruptedException {
		
		Properties jobParams = new Properties();
		jobParams.setProperty("restartable", "false");

		Reporter.log("starting job");
		JobExecution jobExec = jobOp.startJobWithoutWaitingForResult(JOB_FILE, jobParams);

		IOHelper.waitForBatchStatusOrTimeout(jobExec, BatchStatus.STARTED, TIMEOUT);
		Reporter.log("Job Status = " + jobExec.getBatchStatus());
		assertWithMessage("Job started", jobExec.getBatchStatus().equals(BatchStatus.STARTED));
		Reporter.log("job started");
		
		if(jobExec.getBatchStatus().equals(BatchStatus.STARTED)) {
			
			Reporter.log("stopping job");
			jobOp.stopJobAndWaitForResult(jobExec);
			
			Reporter.log("Job Status = " + jobExec.getBatchStatus());
			assertWithMessage("Job stopped", jobExec.getBatchStatus().equals(BatchStatus.STOPPED));
			Reporter.log("job stopped");
			
			if(jobExec.getBatchStatus().equals(BatchStatus.STOPPED) && jobExec.getJobParameters().getProperty("restartable").equalsIgnoreCase("false")) {

				Reporter.log("restarting job, should fail because restartable is false");
				JobExecution newJobExec = null;
				try {
					newJobExec = jobOp.restartJobAndWaitForResult(jobExec.getExecutionId());
				} catch (JobInstanceAlreadyCompleteException jiace) {
					Reporter.log("JobInstanceAlreadyCompleteException = " + jiace.getLocalizedMessage());
					assertWithMessage("Job Restart = false should throw JobRestartException NOT JobInstanceAlreadyCompleteException", false);
				} catch (NoSuchJobExecutionException nsjee) {
					Reporter.log("NoSuchJobExecutionException = " + nsjee.getLocalizedMessage());
					assertWithMessage("Job Restart = false should throw JobRestartException NOT NoSuchJobExecutionException", false);
				} catch (NoSuchJobException nsje) {
					Reporter.log("NoSuchJobException = " + nsje.getLocalizedMessage());
					assertWithMessage("Job Restart = false should throw JobRestartException NOT NoSuchJobException", false);
				} catch (JobRestartException jre) {
					Reporter.log("JobRestartException = " + jre.getLocalizedMessage());
					assertWithMessage("Job Restart = false throws JobRestartException", true);
				}

				assertWithMessage("Job should fail to restart", newJobExec == null);
				Reporter.log("job should fail to restart");
			}
		}
	}

	@BeforeTest
    @Before
	public void beforeTest() throws ClassNotFoundException {
		jobOp = new JobOperatorBridge(); 
	}

	@AfterTest
	public void afterTest() {
		jobOp = null;
	}
	
}
