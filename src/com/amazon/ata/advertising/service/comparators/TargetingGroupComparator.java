package com.amazon.ata.advertising.service.comparators;

import com.amazon.ata.advertising.service.targeting.TargetingGroup;

import java.util.Comparator;

public class TargetingGroupComparator implements Comparator<TargetingGroup> {

    @Override
    public int compare(TargetingGroup firstTargetingGroup, TargetingGroup secondTargetingGroup) {
        return Double.compare(firstTargetingGroup.getClickThroughRate(),
                secondTargetingGroup.getClickThroughRate());
    }

}
