package com.impressiveinteractive.synapse.processor.test;

import com.impressiveinteractive.synapse.processor.BuildMatcher;
import com.impressiveinteractive.synapse.processor.BuildMatchers;
import com.impressiveinteractive.synapse.processor.matchers.test.java.util.ClassMatcher;
import com.impressiveinteractive.synapse.processor.matchers.test.java.util.MapMatcher;
import com.impressiveinteractive.synapse.processor.matchers.test.MotorVehicleMatcher;
import com.impressiveinteractive.synapse.processor.matchers.test.ThingyMatcher;
import com.impressiveinteractive.synapse.processor.matchers.test.VehicleCarMatcher;
import com.impressiveinteractive.synapse.processor.matchers.test.VehicleMatcher;
import com.impressiveinteractive.synapse.processor.test.vehicle.Car;
import com.impressiveinteractive.synapse.processor.test.vehicle.MotorVehicle;
import com.impressiveinteractive.synapse.processor.test.vehicle.ParkingLot;
import com.impressiveinteractive.synapse.processor.test.vehicle.Vehicle;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.impressiveinteractive.synapse.processor.test.vehicle.MotorVehicle.Fuel.PETROL;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@BuildMatchers(
        defaultDestinationPackage = "com.impressiveinteractive.synapse.processor.matchers.test",
        matchers = {
                @BuildMatcher(pojo = Vehicle.class, includeObjectMethods = false),
                @BuildMatcher(pojo = MotorVehicle.class, includeObjectMethods = false),
                @BuildMatcher(
                        pojo = Car.class,
                        destinationName = "VehicleCarMatcher",
                        includeObjectMethods = false),
                @BuildMatcher(pojo = ParkingLot.class, includeObjectMethods = false),
                @BuildMatcher(
                        pojo = Class.class,
                        destinationPackage = "com.impressiveinteractive.synapse.processor.matchers.test.java.util",
                        utilities = Methods.class,
                        staticMethodName = "cls"),
                @BuildMatcher(
                        pojo = Map.class,
                        destinationPackage = "com.impressiveinteractive.synapse.processor.matchers.test.java.util"),
                @BuildMatcher(AdvancedFunctionalityTest.Thingy.class),
                @BuildMatcher(
                        pojo = AdvancedFunctionalityTest.NameCollision.class,
                        shortenGetterNames = false)})
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
        assertThat(car, Matchers.is(VehicleMatcher.vehicle()
                .withPropulsion(is("straight six " + PETROL.getPropulsion()))
                .withMaxSpeed(is(9000.0))));
    }

    @Test
    public void testAbstractClassMotorVehicle() throws Exception {
        assertThat(car, Matchers.is(MotorVehicleMatcher.motorVehicle()
                .withPropulsion(is("straight six " + PETROL.getPropulsion()))
                .withMaxSpeed(is(9000.0))
                .withFuel(is(PETROL))));
    }

    @Test
    public void testConcreteClassCar() throws Exception {
        assertThat(car, Matchers.is(VehicleCarMatcher.car()
                .withPropulsion(is("straight six " + PETROL.getPropulsion()))
                .withMaxSpeed(is(9000.0))
                .withFuel(is(PETROL))
                .withBrand(is("Mercedes"))));
    }

    @Test
    public void testMap() throws Exception {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "One");
        map.put(2, "Two");
        map.put(3, "Three");

        assertThat(map, Matchers.is(MapMatcher.<Integer, String>map()
                .withEmpty(is(false))
                .withSize(is(3))));
    }

    @Test
    public void includeObjectMethods() throws Exception {
        // @BuildMatcher(includeObjectMethods = true) - The default
        assertThat(ThingyMatcher.class, Matchers.is(thingyMatcherClass()
                .withMethodNames(containsInAnyOrder(
                        "withGet",
                        "withVehicle",
                        "withClass",
                        "withToString", // Adds both toString and hashCode
                        "withHashCode"))));

        // @BuildMatcher(includeObjectMethods = false)
        assertThat(VehicleMatcher.class, Matchers.is(vehicleMatcherClass()
                .withMethodNames(containsInAnyOrder(
                        "withPropulsion",
                        "withMaxSpeed"
                        // No methods from Object because of BuildMatcher.skipObjects = true
                ))));

        // @BuildMatcher(includeObjectMethods = false)
        assertThat(MotorVehicleMatcher.class, Matchers.is(motorVehicleMatcherClass()
                .withMethodNames(containsInAnyOrder(
                        "withPropulsion",
                        "withMaxSpeed",
                        "withFuel",
                        "withToString")))); // Only adds toString because VehicleMatcher overrides it

        // @BuildMatcher(includeObjectMethods = false)
        assertThat(VehicleCarMatcher.class, Matchers.is(vehicleCarMatcherClass()
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

    /**
     * Used to demonstrate {@link BuildMatcher#shortenGetterNames()} set to false.
     */
    public interface NameCollision {
        Vehicle vehicle();

        Vehicle getVehicle();
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

    private ClassMatcher<ThingyMatcher> thingyMatcherClass() {
        return ClassMatcher.cls();
    }
}
