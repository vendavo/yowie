package com.vendavo.mesos.yowie.mesos

import groovy.transform.CompileStatic
import org.apache.mesos.Protos

/**
 * Created by vtajzich
 */
@CompileStatic
class RunUnit {

    Collection<Protos.TaskInfo> tasks = []
    Collection<Protos.OfferID> ids = []
    Protos.Filters filters = Protos.Filters.newBuilder().build()


    RunUnit(List<Protos.OfferID> ids) {
        this.ids = ids
    }
    
    RunUnit(Protos.OfferID id) {
        this.ids = [id]
    }

    RunUnit(Protos.OfferID id, Collection<Protos.TaskInfo> tasks) {
        this.tasks = tasks
        this.ids = [id]
    }

    boolean hasTaskFor(Protos.OfferID id) {
        return ids.contains(id)
    }
}
