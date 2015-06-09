package com.vendavo.mesos.yowie.mesos.translator

import com.vendavo.mesos.yowie.mesos.ResourcesAvailable
import org.apache.mesos.Protos

/**
 * Created by vtajzich
 */
interface OffersTranslator {

    ResourcesAvailable translate(List<Protos.Offer> offers)
}
