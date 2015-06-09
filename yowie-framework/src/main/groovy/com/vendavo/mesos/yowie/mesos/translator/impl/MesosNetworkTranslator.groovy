package com.vendavo.mesos.yowie.mesos.translator.impl

import com.vendavo.mesos.yowie.api.domain.Network
import com.vendavo.mesos.yowie.mesos.translator.NetworkTranslator
import groovy.transform.CompileStatic
import org.apache.mesos.Protos
import org.springframework.stereotype.Component

/**
 * Created by vtajzich
 */
@Component
@CompileStatic
class MesosNetworkTranslator implements NetworkTranslator {

    @Override
    Protos.ContainerInfo.DockerInfo.Network translate(Network network) {
        
        switch (network) {

            case Network.NONE:
                return Protos.ContainerInfo.DockerInfo.Network.NONE
            case Network.CONTAINER:
            case Network.MANAGING:
            case Network.BRIDGE:
                return Protos.ContainerInfo.DockerInfo.Network.BRIDGE
            case Network.HOST:
                return Protos.ContainerInfo.DockerInfo.Network.HOST
        }
    }
}
