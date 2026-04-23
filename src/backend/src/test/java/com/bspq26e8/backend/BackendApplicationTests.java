package com.bspq26e8.backend;

import com.bspq26e8.backend.problem.repository.LanguageRepository;
import com.bspq26e8.backend.problem.repository.ProblemLanguageRepository;
import com.bspq26e8.backend.problem.repository.ProblemRepository;
import com.bspq26e8.backend.submission.repository.SubmissionRepository;
import com.bspq26e8.backend.user.repository.RefreshTokenRepository;
import com.bspq26e8.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
    properties = {
        "spring.autoconfigure.exclude="
            + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
    }
)
class BackendApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private SubmissionRepository submissionRepository;

    @MockBean
    private ProblemRepository problemRepository;

    @MockBean
    private LanguageRepository languageRepository;

    @MockBean
    private ProblemLanguageRepository problemLanguageRepository;

    @Test
    void contextLoads() {
    }
}
