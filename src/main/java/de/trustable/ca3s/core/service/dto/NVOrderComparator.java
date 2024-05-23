package de.trustable.ca3s.core.service.dto;

import java.util.Comparator;
import java.util.Map;

public class NVOrderComparator implements Comparator {

    final private Map<String, Integer> orderAttributMap;

    NVOrderComparator(Map<String, Integer> orderAttributMap) {
        this.orderAttributMap = orderAttributMap;
    }

    @Override
    public int compare(Object o1, Object o2) {
        NamedValue nv1 = (NamedValue) o1;
        NamedValue nv2 = (NamedValue) o2;
        int pos1 = 999;
        int pos2 = 999;

        if (orderAttributMap.containsKey(nv1.getName())) {
            pos1 = orderAttributMap.get(nv1.getName());
        }
        if (orderAttributMap.containsKey(nv2.getName())) {
            pos2 = orderAttributMap.get(nv2.getName());
        }

        return pos1 - pos2;
    }

}
