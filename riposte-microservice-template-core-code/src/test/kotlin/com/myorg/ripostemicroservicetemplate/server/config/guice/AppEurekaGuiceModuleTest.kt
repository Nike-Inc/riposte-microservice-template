package com.myorg.ripostemicroservicetemplate.server.config.guice

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions
import org.junit.Test

/**
 * Tests the functionality of [AppEurekaGuiceModule].
 */
class AppEurekaGuiceModuleTest {

    @Test
    fun eurekaServerHooks_returns_object_with_null_hooks() {
        // given
        val module = AppEurekaGuiceModule(mock())

        // when
        val result = module.eurekaServerHooks()

        // then
        Assertions.assertThat(result).isNotNull
        Assertions.assertThat(result.eurekaStartupHook).isNull()
        Assertions.assertThat(result.eurekaShutdownHook).isNull()
    }

    // TODO: EXAMPLE CLEANUP - If you need Eureka, then uncomment the tests below and delete the test above (after
    //      enabling the riposte-service-registration-eureka dependency in build.gradle and following the enabling
    //      instructions in AppEurekaGuiceModule).
    //      If you don't need Eureka then you can leave this as-is, or if you delete AppEurekaGuiceModule entirely
    //      then you can delete this test class.

//    @Test
//    fun eurekaServerHooks_returns_object_that_uses_config_for_suppliers() {
//        // given
//        val configMock: Config = mock()
//        val module = AppEurekaGuiceModule(configMock)
//
//        // when
//        val result = module.eurekaServerHooks()
//
//        // then
//        // The startup and shutdown hooks should be the same thing.
//        Assertions.assertThat(result.eurekaStartupHook)
//            .isNotNull
//            .isInstanceOf(com.nike.riposte.serviceregistration.eureka.EurekaServerHook::class.java)
//            .isSameAs(result.eurekaShutdownHook)
//
//        // and given
//        // The EurekaHandler inside the EurekaServerHook should use the config for its property suppliers.
//        val eurekaHandler: com.nike.riposte.serviceregistration.eureka.EurekaHandler =
//            (result.eurekaStartupHook as com.nike.riposte.serviceregistration.eureka.EurekaServerHook).eurekaHandler
//        val eurekaIsDisabledPropertySupplier = getInternalState(
//            eurekaHandler,
//            "eurekaIsDisabledPropertySupplier"
//        ) as Supplier<Boolean>
//        val datacenterTypePropertySupplier = getInternalState(
//            eurekaHandler,
//            "datacenterTypePropertySupplier"
//        ) as Supplier<String>
//
//        // when
//        eurekaIsDisabledPropertySupplier.get()
//
//        // then
//        Mockito.verify(configMock).getBoolean(
//            com.nike.riposte.serviceregistration.eureka.EurekaHandler.DISABLE_EUREKA_INTEGRATION
//        )
//
//        // and when
//        datacenterTypePropertySupplier.get()
//
//        // then
//        Mockito.verify(configMock).getString(
//            com.nike.riposte.serviceregistration.eureka.EurekaHandler.EUREKA_DATACENTER_TYPE_PROP_NAME
//        )
//    }
}
