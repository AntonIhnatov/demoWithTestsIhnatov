package com.example.demowithtests;

import com.example.demowithtests.domain.Employee;
import com.example.demowithtests.domain.Gender;
import com.example.demowithtests.repository.EmployeeRepository;
import com.example.demowithtests.service.EmployeeServiceBean;
import com.example.demowithtests.util.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Tests")
public class ServiceTests {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceBean service;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee
                .builder()
                .id(1)
                .name("Mark")
                .country("UK")
                .email("test@mail.com")
                .gender(Gender.M)
                .isDeleted(Boolean.FALSE)
                .build();
        System.out.println(employee.getName());

    }

    @Test
    @DisplayName("Save employee test")
    public void whenSaveEmployee_shouldReturnEmployee() {

        when(employeeRepository.save(ArgumentMatchers.any(Employee.class))).thenReturn(employee);
        var created = service.create(employee);

        assertNotNull(created, "Expected a non-null employee to be returned");

        assertThat(created).isEqualTo(employee);

        assertThat(created.getName()).isSameAs(employee.getName());

        // Проверяем, что метод save был вызван ровно один раз
        verify(employeeRepository, times(1)).save(employee);

        verify(employeeRepository).save(employee);

        // Проверяем, что метод save был вызван с правильным аргументом
        var captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(employee);
    }



    @Test
    @DisplayName("Get employee by exist id test")
    public void whenGivenId_shouldReturnEmployee_ifFound() {
        int employeeId = 88;

        // Настраиваем мок, чтобы возвращал тестового сотрудника при вызове findById с любым int аргументом
        when(employeeRepository.findById(ArgumentMatchers.anyInt())).thenReturn(Optional.of(employee));

        // Вызываем метод getById с указанным ID
        Employee expected = service.getById(employeeId);

        // Проверяем, что метод getById возвращает сотрудника
        assertNotNull(expected, "Expected a non-null employee to be returned");

        // Проверяем, что возвращенный сотрудник совпадает с тестовым сотрудником
        assertThat(expected).isSameAs(employee);

        // Проверяем, что метод findById был вызван ровно один раз
        verify(employeeRepository, times(1)).findById(anyInt());

        // Проверяем, что метод findById был вызван с правильным аргументом
        var captor = ArgumentCaptor.forClass(Integer.class);
        verify(employeeRepository).findById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(employeeId);
    }

    @Test
    @DisplayName("Throw exception when employee not found test")
    public void should_throw_exception_when_employee_doesnt_exist() {

        when(employeeRepository.findById(anyInt())).thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> employeeRepository.findById(anyInt()));

        assertThrows(ResourceNotFoundException.class, () -> {
            employeeRepository.findById(42); // Произвольное значение аргумента
        });

    }


    @Test
    @DisplayName("Read employee by id test")
    public void readEmployeeByIdTest() {
        // Генерируем случайный идентификатор сотрудника
        int randomId = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);

        // Настраиваем мок, чтобы возвращал заданный сотрудник при вызове findById с заданным идентификатором
        when(employeeRepository.findById(randomId)).thenReturn(Optional.of(employee));

        // Вызываем метод сервиса для чтения сотрудника
        Employee expected = service.getById(randomId);

        // Проверяем, что вернулся ожидаемый сотрудник
        assertThat(expected).isSameAs(employee);

        // Проверяем, что метод findById был вызван с ожидаемым идентификатором
        verify(employeeRepository).findById(randomId);
    }

    @Test
    @DisplayName("Read employee by id with non-existent id test")
    public void readEmployeeByIdWithNonExistentIdTest() {
        // Настраиваем мок, чтобы возвращал Optional.empty() при вызове findById с любым аргументом
        when(employeeRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Проверяем, что метод getById выбрасывает исключение ResourceNotFoundException при несуществующем сотруднике
        assertThrows(ResourceNotFoundException.class, () -> service.getById(42)); // Произвольное значение аргумента
    }


    @Test
    @DisplayName("Read all employees test")
    void readAllEmployeesTest() {
        // Настраиваем мок, чтобы возвращал список сотрудников с одним элементом при вызове findAll
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        // Вызываем метод findAll
        List<Employee> list = employeeRepository.findAll();

        // Проверяем, что список не пустой
        assertThat(list).isNotEmpty();

        // Проверяем, что размер списка равен ожидаемому
        assertThat(list).hasSize(1);

        // Проверяем, что элемент списка соответствует ожидаемому сотруднику
        assertThat(list.get(0)).isSameAs(employee);

        // Проверяем, что метод findAll был вызван
        verify(employeeRepository).findAll();
    }

    @Test
    @DisplayName("Read all employees with empty list test")
    void readAllEmployeesWithEmptyListTest() {
        // Настраиваем мок, чтобы возвращал пустой список при вызове findAll
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        // Вызываем метод findAll
        List<Employee> list = employeeRepository.findAll();

        // Проверяем, что список пустой
        assertThat(list).isEmpty();

        // Проверяем, что метод findAll был вызван
        verify(employeeRepository).findAll();
    }

    @Test
    @DisplayName("Soft delete employee test")
    public void softDeleteEmployeeTest() {
        // Настраиваем мок, чтобы возвращал тестового сотрудника при вызове findById с аргументом 1
        when(employeeRepository.findById(1)).thenReturn(Optional.of(employee));

        // Вызываем метод сервиса, который использует findById и save
        service.softRemoveById(1);

        // Проверяем, что метод findById был вызван с аргументом 1
        var captor = ArgumentCaptor.forClass(Integer.class);
        verify(employeeRepository).findById(captor.capture());
        assertEquals(1, captor.getValue());

        // Проверяем, что метод save был вызван с тем же объектом, который вернул findById
        verify(employeeRepository).save(employee);

        // Проверяем, что у сотрудника установлен флаг isDeleted в true
        assertTrue(employee.getIsDeleted(), "Expected isDeleted to be true");
    }

    private Employee createRussianEmployee(int id) {
        return Employee.builder()
                .id(id)
                .name("RussianEmployee" + id)
                .country("Russia")
                .isDeleted(false)
                .build();
    }
    @Test
    @DisplayName("Soft remove employees by country test")
    void softRemoveByCountryTest() {
        // Создаем несколько тестовых сотрудников из России
        Employee russianEmployee1 = createRussianEmployee(1);
        Employee russianEmployee2 = createRussianEmployee(2);

        List<Employee> russianEmployees = Arrays.asList(russianEmployee1, russianEmployee2);

        // Настраиваем мок репозитория
        when(employeeRepository.findAllRussian()).thenReturn(Optional.of(russianEmployees));

        // Вызываем метод softRemoveByCountry
        List<Employee> removedEmployees = service.softRemoveByCountry();

        // Проверяем, что метод findAllRussian был вызван
        verify(employeeRepository).findAllRussian();

        // Проверяем, что список удаленных сотрудников не пустой
        assertThat(removedEmployees).isNotEmpty();

        // Проверяем, что удаленные сотрудники помечены как удаленные
        for (Employee removedEmployee : removedEmployees) {
            assertThat(removedEmployee.getIsDeleted()).isTrue();
        }

        // Проверяем, что метод save был вызван для каждого удаленного сотрудника
        verify(employeeRepository, times(removedEmployees.size())).save(any(Employee.class));
    }

}
