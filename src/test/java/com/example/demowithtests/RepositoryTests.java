package com.example.demowithtests;

import com.example.demowithtests.domain.Address;
import com.example.demowithtests.domain.Employee;
import com.example.demowithtests.domain.Gender;
import com.example.demowithtests.repository.EmployeeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Employee Repository Tests")
public class RepositoryTests {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @Order(1)
    @Rollback(value = false)
    @DisplayName("Save employee test")
    public void saveEmployeeTest() {

        var employee = Employee.builder()
                .name("Mark")
                .country("England")
                .addresses(new HashSet<>(Set.of(
                        Address
                                .builder()
                                .country("UK")
                                .build())))
                .gender(Gender.M)
                .build();

        employeeRepository.save(employee);

        assertThat(employee.getId()).isGreaterThan(0);

        assertThat(employee.getId()).isEqualTo(1);

        assertThat(employee.getName()).isEqualTo("Mark");

        assertThat(employee.getCountry()).isEqualTo("England");

        assertThat(employee.getGender()).isEqualTo(Gender.M);

        assertThat(employee.getAddresses()).isNotEmpty();

        Address address = employee.getAddresses().iterator().next();

        assertThat(address.getCountry()).isEqualTo("UK");

        Optional<Employee> savedEmployee = employeeRepository.findById(employee.getId());

        assertThat(savedEmployee).isPresent();

        assertThat(employeeRepository.findById(1)
                        .map(Employee::getAddresses)
                        .orElse(Collections.emptySet()))
                .isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("Get employee by id test")
    public void getEmployeeTest() {

        var employee = employeeRepository.findById(1).orElseThrow();

        assertThat(employee.getId()).isEqualTo(1);

        assertThat(employee.getName()).isEqualTo("Mark");

        assertThat(employee.getAddresses()).isNotEmpty();

        assertThat(employee.getGender()).isEqualTo(Gender.M);

        assertThat(employee.getCountry()).isEqualTo("England");

        assertThat(employeeRepository.existsById(1)).isTrue();

        assertThat(employeeRepository.findById(1)
                        .map(Employee::getAddresses)
                        .orElse(Collections.emptySet()))
                .isNotEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("Get employees test")
    public void getListOfEmployeeTest() {

        var employeesList = employeeRepository.findAll();

        assertThat(employeesList.size()).isGreaterThan(0);

        assertThat(employeesList).isNotEmpty();

        assertThat(employeesList.size()).isEqualTo(1);

        assertThat(employeesList)
                .extracting(Employee::getId)
                .doesNotHaveDuplicates();

        List<Integer> ids = employeesList.stream().map(Employee::getId).collect(Collectors.toList());
        assertThat(employeeRepository.findAllById(ids)).hasSameSizeAs(ids);
    }

    @Test
    @Order(4)
    @Rollback(value = false)
    @DisplayName("Update employee test")
    public void updateEmployeeTest() {

        var employee = employeeRepository.findById(1).orElseThrow();

        employee.setName("Martin");
        var employeeUpdated = employeeRepository.save(employee);

        assertThat(employeeUpdated.getName()).isEqualTo("Martin");

        //Проверка, что остальные поля сотрудника не изменились
        assertThat(employeeUpdated)
                .usingRecursiveComparison()
                .ignoringFields("name")
                .isEqualTo(employee);

    }

    @Test
    @Order(5)
    @DisplayName("Find employee by gender test")
    public void findByGenderTest() {

        var employees = employeeRepository.findByGender(Gender.M.toString(), "UK");

        assertThat(employees).isNotNull().isNotEmpty();

        assertThat(employees.get(0).getGender()).isEqualTo(Gender.M);

        assertThat(employees.get(0).getGender()).isNotEqualTo(Gender.F);

        assertThat(employees)
                .extracting(Employee::getGender)
                .containsOnly(Gender.M);


    }

    @Test
    @Order(6)
    @Rollback(value = false)
    @DisplayName("Delete employee test")
    public void deleteEmployeeTest() {

        var employee = employeeRepository.findById(1).orElseThrow();

        employeeRepository.delete(employee);

        Employee employeeNull = null;

        var optionalEmployee = Optional.ofNullable(employeeRepository.findByName("Martin"));

        if (optionalEmployee.isPresent()) {
            employeeNull = optionalEmployee.orElseThrow();
        }

        assertThat(employeeNull).isNull();

        assertThat(optionalEmployee).isEmpty();

        assertThat(optionalEmployee).isNotPresent();

    }

    @Test
    @Order(7)
    @Rollback(value = false)
    @DirtiesContext
    @DisplayName("Count all Ukrainian women-employees test")
    public void testCountAllUkrainianWomen(){
        var employee1 = Employee.builder()
                .name("Olga")
                .country("Ukraine")
                .addresses(new HashSet<>(Set.of(
                        Address.builder().country("UA").build())))
                .gender(Gender.F)
                .build();

        var employee2 = Employee.builder()
                .name("Natalia")
                .country("Ukraine")
                .addresses(new HashSet<>(Set.of(
                        Address.builder().country("UA").build())))
                .gender(Gender.F)
                .build();

        employeeRepository.saveAll(List.of(employee1, employee2));

        int count = employeeRepository.countAllUkrainianWomen();

        assertThat(count).isGreaterThanOrEqualTo(0);
        assertThat(count).isEqualTo(2);
        assertThat(count).isEven();
        assertThat(count).isLessThanOrEqualTo(1000);

    }

    @Test
    @Order(8)
    @Rollback(value = false)
    @DisplayName("Find all employees from Italy with name Mario test")
    public void testFindAllItalyByNameMario() {

        var employee1 = Employee.builder()
                .name("Mario")
                .country("Italy")
                .build();

        var employee2 = Employee.builder()
                .name("Mario")
                .country("Italy")
                .build();

        employeeRepository.saveAll(List.of(employee1, employee2));

        Optional<List<Employee>> employeesOptional = employeeRepository.findAllItalyByNameMario();

        assertThat(employeesOptional).isPresent();

        List<Employee> employees = employeesOptional.orElseThrow();

        assertThat(employees).hasSize(2);

        assertThat(employees).allMatch(employee -> employee.getName().equals("Mario")
                &&
                employee.getCountry().equals("Italy"));

    }

}
