package com.impressiveinteractive.synapse.processor.test;

import com.impressiveinteractive.synapse.processor.BuildMatcher;
import com.impressiveinteractive.synapse.processor.BuildMatchers;
import com.impressiveinteractive.synapse.processor.test.vehicle.Car;
import org.junit.Test;

// TODO: Work in progress.
@BuildMatchers({
//        TODO: Currently abstract types can not be used to create matchers. This should change.
//        @BuildMatcher(
//                pojo = Vehicle.class,
//                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers"),
//        @BuildMatcher(
//                pojo = MotorVehicle.class,
//                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers"),
        @BuildMatcher(
                pojo = Car.class,
                destinationPackage = "com.impressiveinteractive.synapse.processor.matchers",
                includeObjectMethods = true)
})
public class InheritanceTest {
    @Test
    public void testInterface() throws Exception {

    }
}
