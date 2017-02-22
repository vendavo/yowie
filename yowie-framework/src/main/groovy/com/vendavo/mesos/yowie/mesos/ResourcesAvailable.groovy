package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import com.vendavo.mesos.yowie.exception.NoResourcesAvailableException
import groovy.transform.CompileStatic

/**
 * Created by vtajzich
 */
@CompileStatic
class ResourcesAvailable {

    Set<ResourceOffer> offers = new HashSet<>()

    ResourcesAvailable(Set<ResourceOffer> offers) {
        this.offers = new HashSet<>(offers)
    }
    
    ResourceOffer allocate(Task task) {

        List<ResourceOffer> available = offers
                .findAll { it.hasCapacity(task) }
                .sort { ResourceOffer l, ResourceOffer r ->

            int cpusResult = ((Double) l.cpus).compareTo(r.cpus)

            if (cpusResult != 0) {
                return cpusResult
            }

            return ((Double) l.mem).compareTo(r.mem)
        }

        if (!available) {
            throw new NoResourcesAvailableException(""" taskId="$task.id" task="$task" Cannot allocate resources for task! """)
        }

        ResourceOffer offer = available.first()

        offer.allocate(task)

        return offer
    }

    double getCpus() {
        return offers.sum { ResourceOffer offer -> offer.cpus } as Double
    }

    double getMem() {
        return offers.sum { ResourceOffer offer -> offer.mem } as Double
    }

    double getDisk() {
        return offers.sum { ResourceOffer offer -> offer.disk } as Double
    }

    boolean hasCapacity(Task task) {
        return offers.find { it.hasCapacity(task) }
    }

    @Override
    public String toString() {
        return """ $offers """
    }

    void offerRescinded(String id) {
        def offerRescinded = offers.find { it.offer.id.value == id }
        offers.remove(offerRescinded)
    }
}
