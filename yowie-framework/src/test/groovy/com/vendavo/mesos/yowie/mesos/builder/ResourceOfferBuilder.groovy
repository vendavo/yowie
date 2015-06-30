package com.vendavo.mesos.yowie.mesos.builder

import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import org.apache.mesos.Protos

/**
 * Created by vtajzich
 */
@Newify(ResourceOfferBuilder)
class ResourceOfferBuilder {

    private String id = 'anyId'
    private double cpus
    private double mem
    private double hdd
    private Map<String, String> attrs = [:]
    private IntRange range

    ResourceOfferBuilder withId(String id) {
        this.id = id
        return this
    }

    ResourceOfferBuilder withCpus(double cpus) {
        this.cpus = cpus
        return this
    }

    ResourceOfferBuilder withMem(double mem) {
        this.mem = mem
        return this
    }

    ResourceOfferBuilder withHDD(double hdd) {
        this.hdd = hdd
        return this
    }

    ResourceOfferBuilder withAttr(String key, String value) {
        attrs.put(key, value)
        return this
    }

    ResourceOfferBuilder withAttrs(Map<String, String> attrs) {
        this.attrs = attrs
        return this
    }

    ResourceOfferBuilder withPorts(IntRange range) {
        this.range = range
        return this
    }

    ResourceOffer build() {

        Protos.Offer.Builder builder = Protos.Offer.newBuilder().setId(Protos.OfferID.newBuilder().setValue(id).build())
        builder.setFrameworkId(Protos.FrameworkID.newBuilder().setValue('framework').build())
        builder.setSlaveId(Protos.SlaveID.newBuilder().setValue('slaveId').build())
        builder.setHostname('localhost')

        Protos.Offer offer = builder.build()

        return new ResourceOffer(offer, cpus, mem, hdd, range ? [range] : [], attrs)
    }
}
