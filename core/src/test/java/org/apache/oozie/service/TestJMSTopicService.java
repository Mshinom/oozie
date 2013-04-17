/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oozie.service;

import org.apache.oozie.BundleActionBean;
import org.apache.oozie.BundleJobBean;
import org.apache.oozie.CoordinatorActionBean;
import org.apache.oozie.CoordinatorJobBean;
import org.apache.oozie.WorkflowActionBean;
import org.apache.oozie.WorkflowJobBean;
import org.apache.oozie.client.CoordinatorAction;
import org.apache.oozie.client.CoordinatorJob;
import org.apache.oozie.client.Job;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.service.Services;
import org.apache.oozie.test.XDataTestCase;
import org.apache.oozie.workflow.WorkflowInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJMSTopicService extends XDataTestCase {

    private Services services;

    @Before
    protected void setUp() throws Exception {
        super.setUp();
        services = setupServicesForTopic();
        services.init();
    }

    @After
    protected void tearDown() throws Exception {
        services.destroy();
        super.tearDown();
    }

    private Services setupServicesForTopic() throws ServiceException {
        Services services = new Services();
        services.getConf().set(Services.CONF_SERVICE_EXT_CLASSES, JMSTopicService.class.getName());
        return services;
    }

    @Test
    public void testTopicAsUser() {
        try {
            JMSTopicService jmsTopicService = Services.get().get(JMSTopicService.class);
            WorkflowJobBean wfj = addRecordToWfJobTable(WorkflowJob.Status.SUCCEEDED, WorkflowInstance.Status.SUCCEEDED);
            assertEquals(wfj.getUser(), jmsTopicService.getTopic(wfj.getId()));
            WorkflowActionBean wab = addRecordToWfActionTable(wfj.getId(), "1", WorkflowAction.Status.RUNNING);
            assertEquals(wfj.getUser(), jmsTopicService.getTopic(wab.getId()));
            CoordinatorJobBean cjb = addRecordToCoordJobTable(CoordinatorJob.Status.SUCCEEDED, true, true);
            assertEquals(wfj.getUser(), jmsTopicService.getTopic(cjb.getId()));
            CoordinatorActionBean cab = addRecordToCoordActionTable(cjb.getId(), 1, CoordinatorAction.Status.SUCCEEDED,
                    "coord-action-for-action-input-check.xml", 0);
            assertEquals(wfj.getUser(), jmsTopicService.getTopic(cab.getId()));
            BundleJobBean bjb = addRecordToBundleJobTable(Job.Status.RUNNING, true);
            assertEquals(wfj.getUser(), jmsTopicService.getTopic(bjb.getId()));
            BundleActionBean bab = addRecordToBundleActionTable(bjb.getId(), "1", 1, Job.Status.RUNNING);
            assertEquals(wfj.getUser(), jmsTopicService.getTopic(bab.getBundleActionId()));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testTopicAsJobId() {
        try {
            services.destroy();
            services = setupServicesForTopic();
            services.getConf().set(JMSTopicService.TOPIC_NAME, "default=" + JMSTopicService.TopicType.JOBID.getValue());
            services.init();
            JMSTopicService jmsTopicService = Services.get().get(JMSTopicService.class);
            WorkflowJobBean wfj = addRecordToWfJobTable(WorkflowJob.Status.SUCCEEDED, WorkflowInstance.Status.SUCCEEDED);
            assertEquals(wfj.getId(), jmsTopicService.getTopic(wfj.getId()));
            WorkflowActionBean wab = addRecordToWfActionTable(wfj.getId(), "1", WorkflowAction.Status.RUNNING);
            assertEquals(wfj.getId(), jmsTopicService.getTopic(wab.getId()));
            CoordinatorJobBean cjb = addRecordToCoordJobTable(CoordinatorJob.Status.SUCCEEDED, true, true);
            assertEquals(cjb.getId(), jmsTopicService.getTopic(cjb.getId()));
            CoordinatorActionBean cab = addRecordToCoordActionTable(cjb.getId(), 1, CoordinatorAction.Status.SUCCEEDED,
                    "coord-action-for-action-input-check.xml", 0);
            assertEquals(cjb.getId(), jmsTopicService.getTopic(cab.getId()));
            BundleJobBean bjb = addRecordToBundleJobTable(Job.Status.RUNNING, true);
            assertEquals(bjb.getId(), jmsTopicService.getTopic(bjb.getId()));
            BundleActionBean bab = addRecordToBundleActionTable(bjb.getId(), "1", 1, Job.Status.RUNNING);
            assertEquals(bjb.getId(), jmsTopicService.getTopic(bab.getBundleActionId()));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testTopicAsFixedString() {
        try {
            services.destroy();
            services = setupServicesForTopic();
            services.getConf().set(
                    JMSTopicService.TOPIC_NAME,
                    JMSTopicService.JobType.WORKFLOW.getValue() + " =workflow,"
                            + JMSTopicService.JobType.COORDINATOR.getValue() + "=coord,"
                            + JMSTopicService.JobType.BUNDLE.getValue() + "=bundle");
            services.init();
            JMSTopicService jmsTopicService = Services.get().get(JMSTopicService.class);
            WorkflowJobBean wfj = addRecordToWfJobTable(WorkflowJob.Status.SUCCEEDED, WorkflowInstance.Status.SUCCEEDED);
            assertEquals("workflow", jmsTopicService.getTopic(wfj.getId()));
            WorkflowActionBean wab = addRecordToWfActionTable(wfj.getId(), "1", WorkflowAction.Status.RUNNING);
            assertEquals("workflow", jmsTopicService.getTopic(wab.getId()));
            CoordinatorJobBean cjb = addRecordToCoordJobTable(CoordinatorJob.Status.SUCCEEDED, true, true);
            assertEquals("coord", jmsTopicService.getTopic(cjb.getId()));
            CoordinatorActionBean cab = addRecordToCoordActionTable(cjb.getId(), 1, CoordinatorAction.Status.SUCCEEDED,
                    "coord-action-for-action-input-check.xml", 0);
            assertEquals("coord", jmsTopicService.getTopic(cab.getId()));
            BundleJobBean bjb = addRecordToBundleJobTable(Job.Status.RUNNING, true);
            assertEquals("bundle", jmsTopicService.getTopic(bjb.getId()));
            BundleActionBean bab = addRecordToBundleActionTable(bjb.getId(), "1", 1, Job.Status.RUNNING);
            assertEquals("bundle", jmsTopicService.getTopic(bab.getBundleActionId()));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testMixedTopic1() {
        try {
            services.destroy();
            services = setupServicesForTopic();
            services.getConf().set(
                    JMSTopicService.TOPIC_NAME,
                    JMSTopicService.JobType.WORKFLOW.getValue() + " = workflow,"
                            + JMSTopicService.JobType.COORDINATOR.getValue() + "=coord, default = "
                            + JMSTopicService.TopicType.JOBID.getValue());
            services.init();
            JMSTopicService jmsTopicService = Services.get().get(JMSTopicService.class);
            WorkflowJobBean wfj = addRecordToWfJobTable(WorkflowJob.Status.SUCCEEDED, WorkflowInstance.Status.SUCCEEDED);
            assertEquals("workflow", jmsTopicService.getTopic(wfj.getId()));
            WorkflowActionBean wab = addRecordToWfActionTable(wfj.getId(), "1", WorkflowAction.Status.RUNNING);
            assertEquals("workflow", jmsTopicService.getTopic(wab.getId()));
            CoordinatorJobBean cjb = addRecordToCoordJobTable(CoordinatorJob.Status.SUCCEEDED, true, true);
            assertEquals("coord", jmsTopicService.getTopic(cjb.getId()));
            CoordinatorActionBean cab = addRecordToCoordActionTable(cjb.getId(), 1, CoordinatorAction.Status.SUCCEEDED,
                    "coord-action-for-action-input-check.xml", 0);
            assertEquals("coord", jmsTopicService.getTopic(cab.getId()));
            BundleJobBean bjb = addRecordToBundleJobTable(Job.Status.RUNNING, true);
            assertEquals(bjb.getId(), jmsTopicService.getTopic(bjb.getId()));
            BundleActionBean bab = addRecordToBundleActionTable(bjb.getId(), "1", 1, Job.Status.RUNNING);
            assertEquals(bjb.getId(), jmsTopicService.getTopic(bab.getBundleActionId()));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testMixedTopic2() {
        try {
            services.destroy();
            services = setupServicesForTopic();
            services.getConf().set(
                    JMSTopicService.TOPIC_NAME,
                    JMSTopicService.JobType.WORKFLOW.getValue() + " = workflow,"
                            + JMSTopicService.JobType.COORDINATOR.getValue() + "=coord");
            services.init();
            JMSTopicService jmsTopicService = Services.get().get(JMSTopicService.class);
            WorkflowJobBean wfj = addRecordToWfJobTable(WorkflowJob.Status.SUCCEEDED, WorkflowInstance.Status.SUCCEEDED);
            assertEquals("workflow", jmsTopicService.getTopic(wfj.getId()));
            WorkflowActionBean wab = addRecordToWfActionTable(wfj.getId(), "1", WorkflowAction.Status.RUNNING);
            assertEquals("workflow", jmsTopicService.getTopic(wab.getId()));
            CoordinatorJobBean cjb = addRecordToCoordJobTable(CoordinatorJob.Status.SUCCEEDED, true, true);
            assertEquals("coord", jmsTopicService.getTopic(cjb.getId()));
            CoordinatorActionBean cab = addRecordToCoordActionTable(cjb.getId(), 1, CoordinatorAction.Status.SUCCEEDED,
                    "coord-action-for-action-input-check.xml", 0);
            assertEquals("coord", jmsTopicService.getTopic(cab.getId()));
            BundleJobBean bjb = addRecordToBundleJobTable(Job.Status.RUNNING, true);
            // As no default is specified, user will be considered as topic
            assertEquals(bjb.getUser(), jmsTopicService.getTopic(bjb.getId()));
            BundleActionBean bab = addRecordToBundleActionTable(bjb.getId(), "1", 1, Job.Status.RUNNING);
            assertEquals(bjb.getUser(), jmsTopicService.getTopic(bab.getBundleActionId()));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Test
    public void testIncorrectConfigurationJobType() {
        try {
            services.destroy();
            services = setupServicesForTopic();
            services.getConf().set(JMSTopicService.TOPIC_NAME,
                    "InvalidJobType" + " = workflow," + JMSTopicService.JobType.COORDINATOR.getValue() + "=coord");
            services.init();
            fail("Expected Service Exception");
        }
        catch (ServiceException se) {
            assertTrue(se.getMessage().contains("Incorrect job type"));
        }
    }

    @Test
    public void testIncorrectConfigurationDefault() {
        try {
            services.destroy();
            services = setupServicesForTopic();
            services.getConf().set(JMSTopicService.TOPIC_NAME, "default=" + "invalidvalue");
            services.init();
            fail("Expected Service Exception");
        }
        catch (ServiceException se) {
            assertTrue(se.getMessage().contains("not allowed in default"));
        }
    }

}
