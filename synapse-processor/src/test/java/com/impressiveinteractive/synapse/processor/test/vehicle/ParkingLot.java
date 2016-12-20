package com.impressiveinteractive.synapse.processor.test.vehicle;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot<T1 extends Vehicle, T2 extends T1> {
    private final List<T2> vehicles = new ArrayList<>();

    public List<T2> getVehicles() {
        return vehicles;
    }
}
