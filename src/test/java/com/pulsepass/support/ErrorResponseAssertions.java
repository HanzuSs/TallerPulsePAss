package com.pulsepass.support;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el contrato ErrorResponse (PRD Fase 3, seccion 13) de forma uniforme
 * en todas las pruebas de Controller (QT-CTRL-011, NFR-CTRL-003).
 */
public final class ErrorResponseAssertions {

    private ErrorResponseAssertions() {
    }

    /**
     * Comprueba codigo HTTP, Content-Type JSON y los cinco campos de ErrorResponse.
     * Devuelve el mismo ResultActions para que el test encadene comprobaciones extra
     * (por ejemplo, el contenido de details).
     */
    public static ResultActions assertErrorResponse(ResultActions result,
                                                    int httpStatus,
                                                    String reasonPhrase,
                                                    String message) throws Exception {
        return result
                .andExpect(status().is(httpStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(httpStatus))
                .andExpect(jsonPath("$.error").value(reasonPhrase))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.details").exists());
    }
}
