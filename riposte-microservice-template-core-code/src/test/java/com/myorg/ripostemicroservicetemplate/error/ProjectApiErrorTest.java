package com.myorg.ripostemicroservicetemplate.error;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the functionality of {@link ProjectApiError}
 *
 * @author Nic Munroe
 */
public class ProjectApiErrorTest {

    @Test
    public void make_code_coverage_happy() {
        // Some code coverage tools force you to exercise valueOf() (for example) or you get uncovered lines.
        for (ProjectApiError error : ProjectApiError.values()) {
            assertThat(ProjectApiError.valueOf(error.getName())).isEqualTo(error);
        }
    }

}