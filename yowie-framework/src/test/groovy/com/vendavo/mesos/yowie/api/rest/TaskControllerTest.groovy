package com.vendavo.mesos.yowie.api.rest

import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.mesos.YowieFramework
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.notNullValue
import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner)
@SpringApplicationConfiguration(classes = RestTestContext)
@WebAppConfiguration
public class TaskControllerTest {

    public static final String REQUEST_MINIMAL = """
{
    "name": "micro-benchmark-3215",
    "cpus": 4,
    "mem": 12288,
    "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
    }
}
"""
    public static final String REQUEST_FULL = """
{
    "name": "micro-benchmark-3215",
    "cpus": 4,
    "mem": 12288,
    "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2",
        "network": "BRIDGE"
    },
    "env": {
        "BUILD_NUMBER": "3215",
        "ES_URL": "http://es-benchmarks.dc.vendavo.com",
        "BUILD_TYPE": "jenkins"
    },
    "constraints": [
        { "name": "slaveName", "value": "slave-1" }
    ]
}
"""
    private MediaType contentType = MediaType.APPLICATION_JSON

    @Autowired
    private WebApplicationContext webApplicationContext

    @Autowired
    YowieFramework framework

    @Autowired
    TaskController controller

    MockMvc mockMvc

    @Before
    public void setUp() throws Exception {

        framework = mock(YowieFramework)
        controller.framework = framework
        
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    public void "should return 201 for minimal task request"() throws Exception {

        mockMvc.perform(post('/tasks')
                .contentType(contentType)
                .accept(contentType)
                .content(REQUEST_MINIMAL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.name', equalTo('micro-benchmark-3215')))
                .andExpect(jsonPath('$.id', notNullValue()))

        verify(framework, times(1)).createTask((Task) any(Task))
    }

    @Test
    public void "should return 201 for full task request"() throws Exception {

        mockMvc.perform(post('/tasks')
                .contentType(contentType)
                .accept(contentType)
                .content(REQUEST_FULL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.name', equalTo('micro-benchmark-3215')))
                .andExpect(jsonPath('$.id', notNullValue()))

        verify(framework, times(1)).createTask((Task) any(Task))
    }

    @Test
    public void "should return finished task contexts"() throws Exception {

        when(framework.getFinishedTaskContexts()).thenReturn([new TaskContext(new Task())])
        
        mockMvc.perform(get('/tasks/status/finished')
                .contentType(contentType)
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$', hasSize(1)))
    }

    @Test
    public void "should return validation errors as no values are provided"() throws Exception {

        mockMvc.perform(post('/tasks')
                .contentType(contentType)
                .accept(contentType)
                .content('{}'))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.errors', hasSize(4)))
    }

    @Test
    public void "should return validation errors as container is empty"() throws Exception {

        mockMvc.perform(post('/tasks')
                .contentType(contentType)
                .accept(contentType)
                .content("""
{
    "name": "micro-benchmark-3215",
    "cpus": 4,
    "mem": 12288,
    "container": {
        
    }
}
"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.errors', hasSize(1)))
                .andExpect(jsonPath('$.errors[0].code', equalTo('NotEmpty')))
    }

    @Test
    public void "should return validation errors as dependency is not valid"() throws Exception {

        mockMvc.perform(post('/tasks')
                .contentType(contentType)
                .accept(contentType)
                .content("""
{
    "name": "micro-benchmark-3215",
    "cpus": 4,
    "mem": 12288,
    "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
    },
    "dependsOn": {
    }
}
"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.errors', hasSize(2)))
    }

    @Test
    public void "should return validation errors as constraints is not valid"() throws Exception {

        mockMvc.perform(post('/tasks')
                .contentType(contentType)
                .accept(contentType)
                .content("""
{
    "name": "micro-benchmark-3215",
    "cpus": 4,
    "mem": 12288,
    "container": {
        "image": "dockerregistry.dc.vendavo.com:5000/eps-benchmarks-runner:v2"
    },
    "constraints": [
        { "name": "", "value": "" }
    ]
}
"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.errors', hasSize(2)))
    }

    @Test
    public void "should return generic error when unexpected exception is thrown"() throws Exception {

        when(framework.createTask((Task)any())).thenThrow(new RuntimeException())
        
        mockMvc.perform(post('/tasks')
                .contentType(contentType)
                .accept(contentType)
                .content(REQUEST_MINIMAL))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath('$.id', notNullValue()))
                .andExpect(jsonPath('$.message', notNullValue()))
    }
}
