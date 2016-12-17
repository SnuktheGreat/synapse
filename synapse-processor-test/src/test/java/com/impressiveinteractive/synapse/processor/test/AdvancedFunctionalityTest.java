package com.impressiveinteractive.synapse.processor.test;

import com.google.common.collect.ImmutableMap;
import com.impressiveinteractive.synapse.processor.BuildMatcher;
import com.impressiveinteractive.synapse.processor.BuildMatchers;
import com.impressiveinteractive.synapse.processor.matchers.ClassMatcher;
import com.impressiveinteractive.synapse.processor.matchers.MapMatcher;
import com.impressiveinteractive.synapse.processor.matchers.MotorVehicleMatcher;
import com.impressiveinteractive.synapse.processor.matchers.VehicleCarMatcher;
import com.impressiveinteractive.synapse.processor.matchers.VehicleMatcher;
import com.impressiveinteractive.synapse.processor.test.vehicle.Car;
import com.impressiveinteractive.synapse.processor.test.vehicle.MotorVehicle;
import com.impressiveinteractive.synapse.processor.test.vehicle.ParkingLot;
import com.impressiveinteractive.synapse.processor.test.vehicle.Vehicle;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

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
                skipObjectMethods = true),
        @BuildMatcher(
                pojo = ParkingLot.class,
                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers",
                skipObjectMethods = true),
        @BuildMatcher(
                pojo = Class.class,
                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers",
                utilities = Methods.class,
                staticMethodName = "cls"),
        @BuildMatcher(
                pojo = Map.class,
                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers"),
        @BuildMatcher(
                pojo = AdvancedFunctionalityTest.Thingy.class,
                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers")})
public class AdvancedFunctionalityTest {

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
    public void testInterfaceVehicle() throws Exception {
        assertThat(car, is(vehicle()
                .withPropulsion(is("straight six " + PETROL.getPropulsion()))
                .withMaxSpeed(is(9000.0))));
    }

    @Test
    public void testAbstractClassMotorVehicle() throws Exception {
        assertThat(car, is(motorVehicle()
                .withPropulsion(is("straight six " + PETROL.getPropulsion()))
                .withMaxSpeed(is(9000.0))
                .withFuel(is(PETROL))));
    }

    @Test
    public void testConcreteClassCar() throws Exception {
        assertThat(car, is(car()
                .withPropulsion(is("straight six " + PETROL.getPropulsion()))
                .withMaxSpeed(is(9000.0))
                .withFuel(is(PETROL))
                .withBrand(is("Mercedes"))));
    }

    @Test
    public void testMap() throws Exception {
        Map<Integer, String> map = ImmutableMap.of(
                1, "One",
                2, "Two",
                3, "Three");

        assertThat(map, is(MapMatcher.<Integer, String>map()
                .withEmpty(is(false))
                .withSize(is(3))));
    }

    @Test
    public void skipObjectMethods() throws Exception {
        assertThat(VehicleMatcher.class, is(vehicleMatcherClass()
                .withMethodNames(containsInAnyOrder(
                        "withPropulsion",
                        "withMaxSpeed"
                        // No methods from Object because of BuildMatcher.skipObjects = true
                ))));

        assertThat(MotorVehicleMatcher.class, is(motorVehicleMatcherClass()
                .withMethodNames(containsInAnyOrder(
                        "withPropulsion",
                        "withMaxSpeed",
                        "withFuel",
                        "withToString")))); // Only adds toString because VehicleMatcher overrides it

        assertThat(VehicleCarMatcher.class, is(vehicleCarMatcherClass()
                .withMethodNames(containsInAnyOrder(
                        "withPropulsion",
                        "withMaxSpeed",
                        "withFuel",
                        "withBrand",
                        "withToString", // Adds both toString and hashCode, because both are overridden
                        "withHashCode"))));
    }

    /**
     * Used to test generic method parameters and internal types.
     */
    public interface Thingy {
        <T> T get();

        <T extends Vehicle> T getVehicle();
    }

    /*
     * Methods below are used to avoid having to do this: ClassMatcher.<VehicleMatcher>cls()
     */

    private ClassMatcher<VehicleMatcher> vehicleMatcherClass() {
        return ClassMatcher.cls();
    }

    private ClassMatcher<MotorVehicleMatcher> motorVehicleMatcherClass() {
        return ClassMatcher.cls();
    }

    private ClassMatcher<VehicleCarMatcher> vehicleCarMatcherClass() {
        return ClassMatcher.cls();
    }
}
