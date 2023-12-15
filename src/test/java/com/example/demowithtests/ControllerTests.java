package com.example.demowithtests;

import com.example.demowithtests.domain.Employee;
import com.example.demowithtests.dto.EmployeeSaveDto;
import com.example.demowithtests.dto.EmployeeReadDto;
import com.example.demowithtests.service.EmployeeService;
import com.example.demowithtests.service.EmployeeServiceEM;
import com.example.demowithtests.util.mappers.EmployeeMapper;
import com.example.demowithtests.web.EmployeeController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = EmployeeController.class)
@DisplayName("Employee Controller Tests")
public class ControllerTests {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    EmployeeService service;

    @MockBean
    EmployeeServiceEM serviceEM;

    @MockBean
    EmployeeMapper employeeMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST API -> /api/users")
    @WithMockUser(roles = "ADMIN")
    public void createPassTest() throws Exception {
        // Arrange
        EmployeeSaveDto requestDto = new EmployeeSaveDto(
                1, "Mike", "England", "mail@mail.com",
                null, null, null, null);

        Employee savedEmployee = Employee.builder()
                .id(1)
                .name("Mike")
                .email("mail@mail.com")
                .build();

        when(employeeMapper.toEmployee(any(EmployeeSaveDto.class))).thenReturn(savedEmployee);
        when(employeeMapper.toEmployeeDto(any(Employee.class))).thenReturn(requestDto);
        when(service.create(any(Employee.class))).thenReturn(savedEmployee);

        // Act
        ResultActions result = mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(requestDto)));

        // Assert
        result.andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mike"));

        verify(service).create(any());
    }

    @Test
    @DisplayName("POST API -> /api/users/jpa")
    @WithMockUser(roles = "USER")
    public void testSaveWithJpa() throws Exception {
        // Arrange
        var employeeToBeReturn = Employee.builder()
                .id(1)
                .name("Mark")
                .country("France")
                .build();

        when(serviceEM.createWithJpa(any(Employee.class))).thenReturn(employeeToBeReturn);

        // Act
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/api/users/jpa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(employeeToBeReturn))
                .with(csrf());

        // Assert
        String responseBody = mockMvc.perform(builder)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Additional assertions if needed

        // Verify
        verify(serviceEM, times(1)).createWithJpa(any(Employee.class));
        verifyNoMoreInteractions(serviceEM);
    }

    @Test
    @DisplayName("GET API -> /api/users/{id}")
    @WithMockUser(roles = "USER")
    public void getPassByIdTest() throws Exception {
        // Arrange
        var expectedResponse = new EmployeeReadDto();
        expectedResponse .id = 1;
        expectedResponse .name = "Mike";

        Employee mockEmployee = Employee.builder()
                .id(1)
                .name("Mike")
                .build();

        when(employeeMapper.toEmployeeReadDto(any(Employee.class))).thenReturn(expectedResponse);
        when(service.getById(1)).thenReturn(mockEmployee);

        // Act
        MockHttpServletRequestBuilder getRequest = get("/api/users/1");
        ResultActions resultActions = mockMvc.perform(getRequest);

        // Assert
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name", is("Mike")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Additional assertions
        EmployeeReadDto actualDto = employeeMapper.toEmployeeReadDto(mockEmployee);
        assertEquals(expectedResponse.id, actualDto.id); // использование поля id
        assertEquals(expectedResponse.name, actualDto.name);
        // Verify
        verify(service).getById(1);
    }

    @Test
    @DisplayName("PUT API -> /api/users/{id}")
    @WithMockUser(roles = "ADMIN")
    public void updatePassByIdTest() throws Exception {
        // Arrange
        var response = new EmployeeReadDto();
        response.id = 1;
        var employee = Employee.builder().id(1).build();

        when(employeeMapper.toEmployee(any(EmployeeSaveDto.class))).thenReturn(employee);
        when(service.updateById(eq(1), any(Employee.class))).thenReturn(employee);
        when(employeeMapper.toEmployeeReadDto(any(Employee.class))).thenReturn(response);

        // Act
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .put("/api/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(employee));

        ResultActions resultActions = mockMvc.perform(mockRequest);

        // Assert
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(content().json(mapper.writeValueAsString(response)));

        // Additional assertions
        EmployeeReadDto actualDto = employeeMapper.toEmployeeReadDto(employee);
        assertEquals(response.id, actualDto.id);

        // Verify
        verify(service).updateById(eq(1), any(Employee.class));
    }


    @Test
    @DisplayName("DELETE API -> /api/users/{id}")
    @WithMockUser(roles = "ADMIN")
    public void deletePassTest() throws Exception {
        // Arrange
        doReturn(null).when(service).removeById(1);

        // Act
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
                .delete("/api/users/1")
                .with(csrf());

        ResultActions resultActions = mockMvc.perform(mockRequest);

        // Assert
        resultActions
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(header().doesNotExist("X-CSRF-TOKEN"));

        // Verify
        verify(service, times(1)).removeById(1);
    }


    @Test
    @DisplayName("GET API -> /api/users/pages")
    @WithMockUser(roles = "USER")
    public void getUsersPageTest() throws Exception {

        var employee = Employee.builder().id(1).name("John").country("US").build();
        var employeeTwo = Employee.builder().id(2).name("Jane").country("UK").build();
        var employeeThree = Employee.builder().id(3).name("Bob").country("US").build();

        List<Employee> list = Arrays.asList(employee, employeeTwo, employeeThree);
        Page<Employee> employeesPage = new PageImpl<>(list);
        Pageable pageable = PageRequest.of(0, 5);

        EmployeeReadDto dto = new EmployeeReadDto();
        EmployeeReadDto dtoTwo = new EmployeeReadDto();
        EmployeeReadDto dtoThree = new EmployeeReadDto();

        when(service.getAllWithPagination(eq(pageable))).thenReturn(employeesPage);
        when(employeeMapper.toEmployeeReadDto(employee)).thenReturn(dto);
        when(employeeMapper.toEmployeeReadDto(employeeTwo)).thenReturn(dtoTwo);
        when(employeeMapper.toEmployeeReadDto(employeeThree)).thenReturn(dtoThree);

        MvcResult result = mockMvc.perform(get("/api/users/pages")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn();

        verify(service).getAllWithPagination(eq(pageable));
        verify(employeeMapper, times(1)).toEmployeeReadDto(employee);
        verify(employeeMapper, times(1)).toEmployeeReadDto(employeeTwo);
        verify(employeeMapper, times(1)).toEmployeeReadDto(employeeThree);

        String contentType = result.getResponse().getContentType();
        assertNotNull(contentType);
        assertTrue(contentType.contains(MediaType.APPLICATION_JSON_VALUE));
        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
    }

    @Test
    @DisplayName("GET API -> /api/users-Oleh/UA")
    @WithMockUser(roles = "USER")
    public void testGetAllOlehFromUkraine() throws Exception {
        // Подготовка данных
        Employee employee1 = Employee.builder().id(1).name("Oleh").country("UA").build();
        Employee employee2 = Employee.builder().id(2).name("AnotherName").country("UA").build();
        List<Employee> employees = Arrays.asList(employee1, employee2);

        // Настройка моков
        when(service.findAllUkrainianOleh()).thenReturn(employees);
        when(employeeMapper.toListEmployeeReadDto(employees)).thenReturn(Arrays.asList(new EmployeeReadDto(), new EmployeeReadDto()));

        // Выполнение запроса и проверки
        mockMvc.perform(get("/api/users-Oleh/UA"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        // Проверка вызовов методов сервиса и маппера
        verify(service).findAllUkrainianOleh();
        verify(employeeMapper).toListEmployeeReadDto(employees);
    }

    @Test
    @DisplayName("GET API -> /api/count/UA-women")
    @WithMockUser(roles = "USER")
    public void testGetAllUkraineWomen() throws Exception {
        // Подготовка данных
        int expectedCount = 10;

        // Настройка мока сервиса
        when(service.countAllUkrainianWomen()).thenReturn(expectedCount);

        // Выполнение запроса и проверки
        mockMvc.perform(get("/api/count/UA-women"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count", is(expectedCount)))
                .andExpect(jsonPath("$.description").doesNotExist())  // Проверка отсутствия поля description
                .andExpect(jsonPath("$.someOtherField").doesNotExist()) // Пример: проверка отсутствия другого поля
                .andReturn();

        // Проверка вызова метода сервиса
        verify(service).countAllUkrainianWomen();
    }

}