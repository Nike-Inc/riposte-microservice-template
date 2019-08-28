package com.myorg.ripostemicroservicetemplate.error

import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.Whitebox
import com.nike.backstopper.apierror.ApiError
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrorsTestBase
import org.assertj.core.api.Assertions
import org.junit.Test

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
            val delegate = Whitebox.getInternalState(pae, "delegate") as ApiError

            // expect
            Assertions.assertThat(pae.metadata).isSameAs(delegate.metadata)
        }
    }
}
