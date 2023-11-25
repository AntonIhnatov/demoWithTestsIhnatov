package com.example.demowithtests.util.mappers;

import com.example.demowithtests.domain.Employee;
import com.example.demowithtests.dto.*;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeSaveDto toEmployeeDto(Employee employee);

    EmployeeUAWomenDto toEmployeeUAWomenDto(Employee employee);

    EmployeeDeleteDto toDeleteEmployeeDto(Employee employee);

    EmployeeReadDto toEmployeeReadDto(Employee employee);

    EmployeeUpdateDto toEmployeeUpdateDto(Employee employee);

    List<EmployeeReadDto> toListEmployeeReadDto(List<Employee> employees);

    List<EmployeeUpdateDto> toListEmployeeUpdateDto(List<Employee> employees);

    List<EmployeeSaveDto> toListEmployeeDto(List<Employee> employees);

    List<EmployeeDeleteDto> toListDeleteEmployeeDto(List<Employee> employees);

    Employee toEmployee(EmployeeSaveDto employeeSaveDto);
}