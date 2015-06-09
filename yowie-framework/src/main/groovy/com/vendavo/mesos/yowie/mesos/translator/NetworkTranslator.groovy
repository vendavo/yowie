package com.vendavo.mesos.yowie.mesos.translator

import com.vendavo.mesos.yowie.api.domain.Network
import org.apache.mesos.Protos

/**
 * Created by vtajzich
 */
interface NetworkTranslator {
    
    Protos.ContainerInfo.DockerInfo.Network translate(Network network)
}
