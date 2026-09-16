package com.example._0260811.controller;

import com.example._0260811.advice.GlobalExceptionHandler;
import com.example._0260811.model.MysqlClient;
import com.example._0260811.service.MysqlService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MysqlControllerTest {
    @Mock
    private MysqlService mysqlService;

    @InjectMocks
    private MysqlController mysqlController;

    MysqlClient mysqlClient = MysqlClient.builder().id(1L).telephone("1234567890").username("testuser").email("aaa@cc.com").build();
    @BeforeAll
    static void setUp() {
    }
    @Test
    void getMysqlClientByIdTest_OK() {
        when(mysqlService.getMysqlClientById(1L)).thenReturn(mysqlClient);
        MysqlClient mysqlClientById = mysqlController.getMysqlClientById(1L);

        assertEquals(mysqlClient, mysqlClientById);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(mysqlService).getMysqlClientById(idCaptor.capture());
        assertEquals(1L, idCaptor.getValue());
        verifyNoMoreInteractions(mysqlService);
    }

    @Test
    void getMysqlClientByIdTest_NOTFOUND() {
        when(mysqlService.getMysqlClientById(1L))
                .thenThrow(new RuntimeException("Mysql client not found with id: 1"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> mysqlController.getMysqlClientById(1L));

        assertEquals("Mysql client not found with id: 1", ex.getMessage());
        verify(mysqlService).getMysqlClientById(1L);
        verifyNoMoreInteractions(mysqlService);
    }

    @Test
    void getMysqlClientByIdTest_NOTFOUND_UsesGlobalExceptionHandler() throws Exception {
        long missingId = 300L;
        RuntimeException exception = new RuntimeException("Mysql client not found with id: " + missingId);
        when(mysqlService.getMysqlClientById(missingId)).thenThrow(exception);

        GlobalExceptionHandler globalExceptionHandler = spy(new GlobalExceptionHandler());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(mysqlController)
                .setControllerAdvice(globalExceptionHandler)
                .build();

        mockMvc.perform(get("/mysql/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Data not found"))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()));

        verify(mysqlService).getMysqlClientById(missingId);
        verify(globalExceptionHandler).handleDataNotFoundException(same(exception), any(HttpServletRequest.class));
        verifyNoMoreInteractions(mysqlService);
    }
}
