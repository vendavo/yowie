package com.vendavo.mesos.yowie.mesos.translator.impl

import com.vendavo.mesos.yowie.api.domain.ResourceType
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskBuilder
import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import com.vendavo.mesos.yowie.mesos.TaskDescription
import com.vendavo.mesos.yowie.mesos.builder.ResourceOfferBuilder
import com.vendavo.mesos.yowie.mesos.translator.NetworkTranslator
import org.apache.mesos.Protos
import spock.lang.Specification

/**
 * Created by vtajzich
 */
class MesosTaskTranslatorSpec extends Specification {

    NetworkTranslator networkTranslator
    MesosTaskTranslator translator

    def setup() {

        networkTranslator = Mock(NetworkTranslator)

        translator = new MesosTaskTranslator(networkTranslator: networkTranslator, externalUrl: 'http://yowie')
    }

    def "should add port mapping resource"() {

        given:

        ResourceOfferBuilder offerBuilder = new ResourceOfferBuilder()
        offerBuilder.withCpus(10).withMem(1024).withHDD(256000).withPorts(1025..32000)

        TaskBuilder builder = new TaskBuilder()
        builder.withEnvVariables('env_1': 'val_1').withPort(8080, 9090)

        Task task = builder.build()
        ResourceOffer resourceOffer = offerBuilder.build()

        TaskDescription description = new TaskDescription(task, resourceOffer)

        when:

        Protos.TaskInfo info = translator.translate(description)
        Protos.Resource resource = info.resourcesList.find { it.name == ResourceType.PORTS.value }

        then:

        1 * networkTranslator.translate(_) >> Protos.ContainerInfo.DockerInfo.Network.BRIDGE

        info
        resource
        
        resource.ranges.rangeCount == 1
        resource.ranges.rangeList[0].begin == 9090
        resource.ranges.rangeList[0].end == 9090
    }

    def "should add multiple port mapping resource"() {

        given:

        ResourceOfferBuilder offerBuilder = new ResourceOfferBuilder()
        offerBuilder.withCpus(10).withMem(1024).withHDD(256000).withPorts(1025..32000)

        TaskBuilder builder = new TaskBuilder()
        builder.withEnvVariables('env_1': 'val_1').withPort(8080, 9090).withPort(8081, 9091).withPort(5050, 5050)

        Task task = builder.build()
        ResourceOffer resourceOffer = offerBuilder.build()

        TaskDescription description = new TaskDescription(task, resourceOffer)

        when:

        Protos.TaskInfo info = translator.translate(description)
        Protos.Resource resource = info.resourcesList.find { it.name == ResourceType.PORTS.value }

        then:

        1 * networkTranslator.translate(_) >> Protos.ContainerInfo.DockerInfo.Network.BRIDGE

        info
        resource

        resource.ranges.rangeCount == 3
        resource.ranges.rangeList[0].begin == 5050
        resource.ranges.rangeList[0].end == 5050
        resource.ranges.rangeList[1].begin == 9090
        resource.ranges.rangeList[1].end == 9090
        resource.ranges.rangeList[2].begin == 9091
        resource.ranges.rangeList[2].end == 9091
    }

    def "should translate env variables correctly and add yowie variables"() {

        given:

        Task task = new TaskBuilder().withEnvVariables('variable_one': 'variable_value').build()

        when:

        Protos.Environment environment = translator.translateEnvVariables(task).build()

        then:

        environment.getVariablesCount() == 4
        environment.getVariablesList().find { it.name == 'YOWIE_TASK_ID' }.value != null
        environment.getVariablesList().find { it.name == 'YOWIE_URL' }.value == 'http://yowie'
        environment.getVariablesList().find { it.name == 'YOWIE_STATUS_UPDATE_URL' }.value != null
        environment.getVariablesList().find { it.name == 'variable_one' }.value == 'variable_value'
    }
}
