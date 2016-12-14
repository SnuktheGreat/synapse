package com.impressiveinteractive.synapse.processor.test.vehicle;

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
}
