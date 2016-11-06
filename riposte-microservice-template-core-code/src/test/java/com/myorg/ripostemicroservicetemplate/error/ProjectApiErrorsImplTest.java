package com.myorg.ripostemicroservicetemplate.error;

import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors;
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrorsTestBase;

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

}
