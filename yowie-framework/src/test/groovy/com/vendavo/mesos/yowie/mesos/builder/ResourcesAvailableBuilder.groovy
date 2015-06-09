package com.vendavo.mesos.yowie.mesos.builder

import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import com.vendavo.mesos.yowie.mesos.ResourcesAvailable
import groovy.transform.CompileStatic

/**
 * Created by vtajzich
 */
@CompileStatic
class ResourcesAvailableBuilder {

    List<ResourceOfferBuilder> builders = []
    List<ResourceOffer> offers = []

    ResourcesAvailableBuilder withResource(ResourceOffer offer) {
        offers << offer
        return this
    }
    
    ResourcesAvailableBuilder withResource(String id, double cpus, double mem) {
        builders << new ResourceOfferBuilder().withId(id).withCpus(cpus).withMem(mem)
        return this
    }

    ResourcesAvailableBuilder withResource(Map<String, String> attrs, String id, double cpus, double mem) {
        builders << new ResourceOfferBuilder().withId(id).withCpus(cpus).withMem(mem).withAttrs(attrs)
        return this
    }

    ResourcesAvailable build() {
        
        offers.addAll(builders.collect { it.build() })
        
        return new ResourcesAvailable(offers)
    }
}
