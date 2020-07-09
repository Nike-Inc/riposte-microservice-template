package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.myorg.ripostemicroservicetemplate.server.config.guice.AppEurekaGuiceModule.EurekaServerHooks;
import com.typesafe.config.Config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the functionality of {@link AppEurekaGuiceModule}.
 */
public class AppEurekaGuiceModuleTest {

    @Test
    public void eurekaServerHooks_returns_object_with_null_hooks() {
        // given
        AppEurekaGuiceModule module = new AppEurekaGuiceModule(mock(Config.class));

        // when
        EurekaServerHooks result = module.eurekaServerHooks();

        // then
        assertThat(result).isNotNull();
        assertThat(result.eurekaStartupHook).isNull();
        assertThat(result.eurekaShutdownHook).isNull();
    }

    // TODO: EXAMPLE CLEANUP - If you need Eureka, then uncomment the tests below and delete the test above (after
    //      enabling the riposte-service-registration-eureka dependency in build.gradle and following the enabling
    //      instructions in AppEurekaGuiceModule).
    //      If you don't need Eureka then you can leave this as-is, or if you delete AppEurekaGuiceModule entirely
    //      then you can delete this test class.
//    @Test
//    @SuppressWarnings("unchecked")
//    public void eurekaServerHooks_returns_object_that_uses_config_for_suppliers() {
//        // given
//        Config configMock = mock(Config.class);
//        AppEurekaGuiceModule module = new AppEurekaGuiceModule(configMock);
//
//        // when
//        EurekaServerHooks result = module.eurekaServerHooks();
//
//        // then
//        // The startup and shutdown hooks should be the same thing.
//        assertThat(result.eurekaStartupHook)
//            .isNotNull()
//            .isInstanceOf(com.nike.riposte.serviceregistration.eureka.EurekaServerHook.class)
//            .isSameAs(result.eurekaShutdownHook);
//
//        // and given
//        // The EurekaHandler inside the EurekaServerHook should use the config for its property suppliers.
//        com.nike.riposte.serviceregistration.eureka.EurekaHandler eurekaHandler =
//            ((com.nike.riposte.serviceregistration.eureka.EurekaServerHook)result.eurekaStartupHook).eurekaHandler;
//        Supplier<Boolean> eurekaIsDisabledPropertySupplier =
//            (Supplier<Boolean>) Whitebox.getInternalState(eurekaHandler, "eurekaIsDisabledPropertySupplier");
//        Supplier<String> datacenterTypePropertySupplier =
//            (Supplier<String>) Whitebox.getInternalState(eurekaHandler, "datacenterTypePropertySupplier");
//
//        // when
//        eurekaIsDisabledPropertySupplier.get();
//
//        // then
//        verify(configMock)
//            .getBoolean(com.nike.riposte.serviceregistration.eureka.EurekaHandler.DISABLE_EUREKA_INTEGRATION);
//
//        // and when
//        datacenterTypePropertySupplier.get();
//
//        // then
//        verify(configMock)
//            .getString(com.nike.riposte.serviceregistration.eureka.EurekaHandler.EUREKA_DATACENTER_TYPE_PROP_NAME);
//    }
}