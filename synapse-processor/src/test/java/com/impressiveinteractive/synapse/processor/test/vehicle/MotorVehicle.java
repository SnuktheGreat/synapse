package com.impressiveinteractive.synapse.processor.test.vehicle;

import java.util.Optional;

public abstract class MotorVehicle implements Vehicle {

    private Fuel fuel;

    public Fuel getFuel() {
        return fuel;
    }

    public void setFuel(Fuel fuel) {
        this.fuel = fuel;
    }

    @Override
    public String getPropulsion() {
        return Optional.of(fuel).map(Fuel::getPropulsion).orElse(null);
    }

    @Override
    public String toString() {
        return "MotorVehicle with " + getPropulsion() + ".";
    }

    public enum Fuel {
        PETROL("combustion engine (petrol)"),
        DIESEL("combustion engine (diesel)"),
        ELECTRICITY("electric engine");

        private final String propulsion;

        Fuel(String propulsion) {
            this.propulsion = propulsion;
        }

        public String getPropulsion() {
            return propulsion;
        }
    }
}
