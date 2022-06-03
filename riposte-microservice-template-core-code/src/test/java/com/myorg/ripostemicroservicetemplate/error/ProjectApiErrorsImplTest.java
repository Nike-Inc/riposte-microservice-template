package com.myorg.ripostemicroservicetemplate.error;

import com.nike.backstopper.apierror.ApiError;
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors;
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrorsTestBase;
import com.nike.internal.util.testing.Glassbox;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests the ProjectApiErrorsImpl class. The real tests live in {@link ProjectApiErrorsTestBase}
 * (which this test class extends), and are picked up and run during the build process. This test is important
 * in making sure that the error handling system will function properly - do not remove it.
 */
public class ProjectApiErrorsImplTest extends ProjectApiErrorsTestBase {

    private static ProjectApiErrorsImpl projectErrors;

    @Override
    protected ProjectApiErrors getProjectApiErrors() {
        if (projectErrors == null) {
            projectErrors = new ProjectApiErrorsImpl();
        }

        return projectErrors;
    }

    @Test
    public void getMetadata_delegates_to_delegate_ApiError() {
        for (ProjectApiError pae : ProjectApiError.values()) {
            // given
            ApiError delegate = (ApiError) Glassbox.getInternalState(pae, "delegate");

            // expect
            assertThat(pae.getMetadata()).isSameAs(delegate.getMetadata());
        }
    }

}
