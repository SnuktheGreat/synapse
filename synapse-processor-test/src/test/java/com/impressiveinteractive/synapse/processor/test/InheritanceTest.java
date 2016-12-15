package com.impressiveinteractive.synapse.processor.test;

import com.impressiveinteractive.synapse.processor.BuildMatcher;
import com.impressiveinteractive.synapse.processor.BuildMatchers;
import com.impressiveinteractive.synapse.processor.matchers.MotorVehicleMatcher;
import com.impressiveinteractive.synapse.processor.matchers.VehicleCarMatcher;
import com.impressiveinteractive.synapse.processor.matchers.VehicleMatcher;
import com.impressiveinteractive.synapse.processor.test.vehicle.Car;
import com.impressiveinteractive.synapse.processor.test.vehicle.MotorVehicle;
import com.impressiveinteractive.synapse.processor.test.vehicle.Vehicle;
import org.junit.Before;
import org.junit.Test;

import static com.impressiveinteractive.synapse.processor.matchers.MotorVehicleMatcher.motorVehicle;
import static com.impressiveinteractive.synapse.processor.matchers.VehicleCarMatcher.car;
import static com.impressiveinteractive.synapse.processor.matchers.VehicleMatcher.vehicle;
import static com.impressiveinteractive.synapse.processor.test.vehicle.MotorVehicle.Fuel.PETROL;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@BuildMatchers({
        @BuildMatcher(
                pojo = Vehicle.class,
                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers",
                skipObjectMethods = true),
        @BuildMatcher(
                pojo = MotorVehicle.class,
                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers",
                skipObjectMethods = true),
        @BuildMatcher(
                pojo = Car.class,
                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers",
                destinationName = "VehicleCarMatcher",
                skipObjectMethods = true)})
public class InheritanceTest {

    private Car car;

    @Before
    public void setUp() throws Exception {
        Car car = new Car();
        car.setBrand("Mercedes");
        car.setMaxSpeed(9000.0);
        car.setFuel(PETROL);

        this.car = car;
    }

    @Test
    public void testVehicle() throws Exception {
        assertThat(car, is(vehicle()
                .withPropulsion(is("straight six " + PETROL.getPropulsion()))
                .withMaxSpeed(is(9000.0))));
    }

    @Test
    public void testMotorVehicle() throws Exception {
        assertThat(car, is(motorVehicle()
                .withPropulsion(is("straight six " + PETROL.getPropulsion()))
                .withMaxSpeed(is(9000.0))
                .withFuel(is(PETROL))));
    }

    @Test
    public void testCar() throws Exception {
        assertThat(car, is(car()
                .withPropulsion(is("straight six " + PETROL.getPropulsion()))
                .withMaxSpeed(is(9000.0))
                .withFuel(is(PETROL))
                .withBrand(is("Mercedes"))));
    }

    @Test
    public void skipObjectMethods() throws Exception {
        assertThat(Methods.getWithMethodNames(VehicleMatcher.class),
                containsInAnyOrder(
                        "withPropulsion",
                        "withMaxSpeed"));

        assertThat(Methods.getWithMethodNames(MotorVehicleMatcher.class),
                containsInAnyOrder(
                        "withPropulsion",
                        "withMaxSpeed",
                        "withFuel",
                        "withToString"));

        assertThat(Methods.getWithMethodNames(VehicleCarMatcher.class),
                containsInAnyOrder(
                        "withPropulsion",
                        "withMaxSpeed",
                        "withFuel",
                        "withBrand",
                        "withToString",
                        "withHashCode"));
    }
}
