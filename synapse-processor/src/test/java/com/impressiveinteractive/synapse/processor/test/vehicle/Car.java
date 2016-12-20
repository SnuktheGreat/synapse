package com.impressiveinteractive.synapse.processor.test.vehicle;

import java.util.Objects;

public class Car extends MotorVehicle {

    private String brand;
    private double maxSpeed;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public String getPropulsion() {
        return "straight six " + super.getPropulsion();
    }

    @Override
    public double getMaxSpeed() {
        return maxSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Double.compare(car.maxSpeed, maxSpeed) == 0 &&
                Objects.equals(brand, car.brand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brand, maxSpeed);
    }

    @Override
    public String toString() {
        String propulsion = getPropulsion();
        return propulsion.substring(0, 1).toUpperCase() + propulsion.substring(1) + " " + brand
                + " with a top speed of " + maxSpeed + ".";
    }
}
