package com.vendavo.mesos.yowie.mesos.translator.impl

import com.vendavo.mesos.yowie.api.domain.Container
import com.vendavo.mesos.yowie.api.domain.ResourceType
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.mesos.TaskDescription
import com.vendavo.mesos.yowie.mesos.translator.NetworkTranslator
import com.vendavo.mesos.yowie.mesos.translator.TaskTranslator
import groovy.transform.CompileStatic
import org.apache.mesos.Protos
import org.apache.mesos.Protos.Volume.Mode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import static org.apache.mesos.Protos.Value.*

/**
 * Created by vtajzich
 */
@Component
class MesosTaskTranslator implements TaskTranslator {

    @Autowired
    NetworkTranslator networkTranslator

    @Value('${yowie.framework.externalUrl}')
    String externalUrl

    @CompileStatic
    @Override
    Protos.TaskInfo translate(TaskDescription description) {

        def environment = translateEnvVariables(description.task)
        def commandInfo = createCommand(environment)
        def containerInfo = Protos.ContainerInfo.newBuilder()
        def dockerInfo = translateDocker(description.task)

        translateContainerInfo(description.task, containerInfo, dockerInfo)

        Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(description.task.id).build()

        return translateTask(taskId, description.resource.offer, commandInfo, containerInfo, description.task)
    }

    @CompileStatic
    private Protos.TaskInfo translateTask(Protos.TaskID taskId, Protos.Offer offer, Protos.CommandInfo.Builder commandInfo, Protos.ContainerInfo.Builder containerInfo, Task task) {

        Protos.TaskInfo.Builder taskInfo = Protos.TaskInfo.newBuilder()
                .setName(taskId.getValue())
                .setTaskId(taskId)
                .setSlaveId(offer.getSlaveId())
                .setCommand(commandInfo)
                .setContainer(containerInfo)

                .addResources(Protos.Resource.newBuilder()
                .setName(ResourceType.CPUS.value)
                .setType(Type.SCALAR)
                .setScalar(Scalar.newBuilder().setValue(task.cpus)))

                .addResources(Protos.Resource.newBuilder()
                .setName(ResourceType.MEM.value)
                .setType(Type.SCALAR)
                .setScalar(Scalar.newBuilder().setValue(task.mem)))

        addPortMappingResources(task, taskInfo)

        return taskInfo.build()
    }

    protected void addPortMappingResources(Task task, Protos.TaskInfo.Builder taskInfo) {

        if (task.container.portMappings) {

            Optional<List<Range>> ranges = task.container.portMappings.stream()
                    .sorted()
                    .map({ [Range.newBuilder().setBegin(it.hostPort).setEnd(it.hostPort).build()] })
                    .reduce(this.&reduceRanges)

            taskInfo.addResources(Protos.Resource.newBuilder()
                    .setName(ResourceType.PORTS.value)
                    .setType(Type.RANGES)
                    .setRanges(Ranges.newBuilder()
                    .addAllRange(ranges.orElse([]))
                    .build()))
        }
    }

    @CompileStatic
    protected void translateContainerInfo(Task task, Protos.ContainerInfo.Builder containerInfo, Protos.ContainerInfo.DockerInfo.Builder dockerInfo) {

        containerInfo.setDocker(dockerInfo)
        containerInfo.setType(Protos.ContainerInfo.Type.DOCKER)

        Collection<Protos.Volume> volumes = task.container.volumes.collect {

            Protos.Volume.newBuilder()
                    .setContainerPath(it.containerPath)
                    .setHostPath(it.hostPath)
                    .setMode(translateMode(it.mode))
                    .build()
        }

        containerInfo.addAllVolumes(volumes)
    }

    @CompileStatic
    protected Protos.ContainerInfo.DockerInfo.Builder translateDocker(Task task) {

        Container container = task.container

        def dockerInfo = Protos.ContainerInfo.DockerInfo.newBuilder()
                .setImage(container.image)
                .setNetwork(networkTranslator.translate(container.network))
                .setPrivileged(container.privileged)
                .setForcePullImage(task.container.forcePull)

        List<Protos.ContainerInfo.DockerInfo.PortMapping> mappings = container.portMappings.collect {

            return Protos.ContainerInfo.DockerInfo.PortMapping.newBuilder()
                    .setContainerPort(it.containerPort)
                    .setHostPort(it.hostPort)
                    .setProtocol(it.protocol.name())
                    .build()
        }

        dockerInfo.addAllPortMappings(mappings)

        List<Protos.Parameter> parameters = container.parameters.collect {
            return Protos.Parameter.newBuilder().setKey(it.key).setValue(it.value).build()
        }

        dockerInfo.addAllParameters(parameters)

        return dockerInfo
    }

    @CompileStatic
    protected Protos.CommandInfo.Builder createCommand(Protos.Environment.Builder environment) {
        return Protos.CommandInfo.newBuilder().setEnvironment(environment).setShell(false)
    }

    @CompileStatic
    protected Protos.Environment.Builder translateEnvVariables(Task task) {

        def environment = Protos.Environment.newBuilder()

        task.env.collect { Protos.Environment.Variable.newBuilder().setName(it.key).setValue(it.value) }
                .each { environment.addVariables(it) }

        environment.addVariables(Protos.Environment.Variable.newBuilder().setName("YOWIE_TASK_ID").setValue(task.id))
        environment.addVariables(Protos.Environment.Variable.newBuilder().setName("YOWIE_URL").setValue(externalUrl))
        environment.addVariables(Protos.Environment.Variable.newBuilder().setName("YOWIE_STATUS_UPDATE_URL").setValue("${externalUrl}/tasks/${task.id}/"))

        return environment
    }

    @CompileStatic
    protected Mode translateMode(com.vendavo.mesos.yowie.api.domain.Mode mode) {

        switch (mode) {

            case com.vendavo.mesos.yowie.api.domain.Mode.R:
                return Mode.RO
            case com.vendavo.mesos.yowie.api.domain.Mode.RW:
                return Mode.RW
        }
    }

    @CompileStatic
    private List<Range> reduceRanges(List<Range> ranges, List<Range> rangeList) {

        def range = rangeList.first()
        def rangeToAdd = range

        if (!ranges.empty) {

            Range previous = ranges.last()

            if (previous.end + 1 >= range.begin) {

                ranges.remove(previous)
                rangeToAdd = Range.newBuilder().setBegin(previous.begin).setEnd(range.end).build()
            }
        }

        ranges.add(rangeToAdd)

        return ranges
    }
}
