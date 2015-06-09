package com.vendavo.mesos.yowie.api.rest

import com.vendavo.mesos.yowie.api.domain.Group
import com.vendavo.mesos.yowie.api.domain.GroupContext
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.mesos.YowieFramework
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.time.LocalDateTime

import static org.hamcrest.Matchers.hasToString
import static org.hamcrest.Matchers.notNullValue
import static org.mockito.Matchers.any
import static org.mockito.Mockito.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = RestTestContext)
@WebAppConfiguration
public class GroupControllerTest {

    public static final String REQUEST_MINIMAL = """
{
  "name": "micro-benchmarks",
  "tasks": [
    {
      "name": "micro-benchmark-3215",
      "cpus": 4,
      "mem": 12288,
      "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
      }
    }
  ]
}
"""

    public static final String REQUEST_TERMINATE_TASK_BAD = """
{
  "name": "micro-benchmarks",
  "terminateTask": {
        "name": "NOT_EXISTING_TASK",
        "status": "FINISHED"
  },
  "tasks": [
    {
      "name": "micro-benchmark-3215",
      "cpus": 4,
      "mem": 12288,
      "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
      }
    }
  ]
}
"""
    public static final String REQUEST_DEPENDS_CORRECT = """
{
  "name": "micro-benchmarks",
  "tasks": [
    {
      "name": "micro-benchmark-3215",
      "cpus": 4,
      "mem": 12288,
      "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
      }
    },
    {
      "name": "micro-benchmark-3216",
      "cpus": 4,
      "mem": 12288,
      "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
      },
      "dependsOn": {  
        "name": "micro-benchmark-3215",
        "status": "FINISHED"
      }
    },
    {
      "name": "micro-benchmark-3217",
      "cpus": 4,
      "mem": 12288,
      "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
      },
      "dependsOn": {
        "name": "micro-benchmark-3216",
        "status": "CUSTOM_STATUS"
      }
    }
  ]
}
"""
    public static final String REQUEST_BAD_DEPENDENCY = """{
  "name": "micro-benchmarks",
  "tasks": [
    {
      "name": "micro-benchmark-3215",
      "cpus": 4,
      "mem": 12288,
      "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
      }
    },
    {
      "name": "micro-benchmark-3216",
      "cpus": 4,
      "mem": 12288,
      "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
      },
      "dependsOn": {  
        "name": "micro-benchmark-321-not-existing-task",
        "status": "FINISHED"
      }
    },
    {
      "name": "micro-benchmark-3217",
      "cpus": 4,
      "mem": 12288,
      "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
      },
      "dependsOn": {
        "name": "micro-benchmark-3216",
        "status": "CUSTOM_STATUS"
      }
    }
  ]
}"""

    private MediaType contentType = MediaType.APPLICATION_JSON

    @Autowired
    private WebApplicationContext webApplicationContext

    @Autowired
    YowieFramework framework

    @Autowired
    GroupController controller

    MockMvc mockMvc

    @Before
    public void setUp() throws Exception {

        framework = mock(YowieFramework)
        controller.framework = framework

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    public void "should return 201 for minimal group request"() throws Exception {

        GroupContext context = new GroupContext(new Group(tasks: [new Task()]))
        context.taskContexts[0].startTime = LocalDateTime.now()

        when(framework.createTask((Group) any(Group))).thenReturn(context)

        mockMvc.perform(post('/groups')
                .contentType(contentType)
                .accept(contentType)
                .content(REQUEST_MINIMAL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.current', notNullValue()))

        verify(framework, times(1)).createTask((Group) any(Group))
    }

    @Test
    public void "should return 201 for group request w/ dependencies"() throws Exception {

        when(framework.createTask((Group) any(Group))).thenReturn(new GroupContext(new Group(tasks: [new Task()])))

        mockMvc.perform(post('/groups')
                .contentType(contentType)
                .accept(contentType)
                .content(REQUEST_DEPENDS_CORRECT))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.current', notNullValue()))

        verify(framework, times(1)).createTask((Group) any(Group))
    }

    @Test
    public void "should return 400 for group request w/ wrong dependencies"() throws Exception {

        mockMvc.perform(post('/groups')
                .contentType(contentType)
                .accept(contentType)
                .content(REQUEST_BAD_DEPENDENCY))
                .andExpect(status().isBadRequest())
    }

    @Test
    public void "should return 400 for group request w/ wrong terminate task"() throws Exception {

        mockMvc.perform(post('/groups')
                .contentType(contentType)
                .accept(contentType)
                .content(REQUEST_TERMINATE_TASK_BAD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.errors[0].message', hasToString('Termination task is not valid')))
    }
}
