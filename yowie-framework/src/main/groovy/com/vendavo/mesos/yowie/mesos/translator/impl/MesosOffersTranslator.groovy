package com.vendavo.mesos.yowie.mesos.translator.impl

import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import com.vendavo.mesos.yowie.mesos.ResourcesAvailable
import com.vendavo.mesos.yowie.mesos.translator.OffersTranslator
import org.apache.mesos.Protos
import org.springframework.stereotype.Component

import static java.util.stream.Collectors.toMap

/**
 * Created by vtajzich
 */
@Component
class MesosOffersTranslator implements OffersTranslator {

    @Override
    ResourcesAvailable translate(List<Protos.Offer> offers) {

        List<ResourceOffer> resourceOffers = offers.collect { translate(it) }

        return new ResourcesAvailable(new HashSet<ResourceOffer>(resourceOffers))
    }

    private double getScalarValue(Protos.Resource resource) {
        resource?.scalar?.value ?: 0
    }

    ResourceOffer translate(Protos.Offer offer) {

        Collection<Protos.Resource> resources = offer.collect { it.resourcesList }.flatten()

        double cpus = getScalarValue(resources.find { it.name == 'cpus' })
        double mem = getScalarValue(resources.find { it.name == 'mem' })
        double disk = getScalarValue(resources.find { it.name == 'disk' })
        Collection<Range> ranges = resources.find { it.name == 'ports' }.ranges
                .collect { it.rangeList }.flatten()
                .collect { Protos.Value.Range range -> new IntRange(true, range.begin as int, range.end as int) }

        Map<String, String> attributes = offer.attributesList.stream()
                .filter({ it.type == org.apache.mesos.Protos.Value.Type.TEXT })
                .collect(toMap({ Protos.Attribute a -> a.name }, { Protos.Attribute a -> a.text?.value }))

        return new ResourceOffer(offer, cpus, mem, disk, ranges, attributes)
    }
}
