package com.myorg.ripostemicroservicetemplate.error

import com.nike.backstopper.apierror.ApiError
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrorsTestBase
import com.nike.internal.util.testing.Glassbox
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests the ProjectApiErrorsImpl class. The real tests live in [ProjectApiErrorsTestBase]
 * (which this test class extends), and are picked up and run during the build process. This test is important
 * in making sure that the error handling system will function properly - do not remove it.
 */
class ProjectApiErrorsImplTest : ProjectApiErrorsTestBase() {

    private val projectApiErrors = ProjectApiErrorsImpl()

    override fun getProjectApiErrors(): ProjectApiErrors {
        return projectApiErrors
    }

    @Test
    fun getMetadata_delegates_to_delegate_ApiError() {
        // This test needed for code coverage if/when all examples have been removed.
        ProjectApiError.values().forEach { pae ->
            // given
            val delegate = Glassbox.getInternalState(pae, "delegate") as ApiError

            // expect
            assertThat(pae.metadata).isSameAs(delegate.metadata)
        }
    }
}
