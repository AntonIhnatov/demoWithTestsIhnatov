package com.example.demowithtests.service;

import com.example.demowithtests.domain.ActionType;
import com.example.demowithtests.domain.Document;
import com.example.demowithtests.domain.DocumentHistory;
import com.example.demowithtests.domain.Employee;
import com.example.demowithtests.repository.DocumentRepository;
import com.example.demowithtests.repository.EmployeeRepository;
import com.example.demowithtests.service.emailService.EmailSenderService;
import com.example.demowithtests.util.annotations.entity.ActivateCustomAnnotations;
import com.example.demowithtests.util.annotations.entity.Name;
import com.example.demowithtests.util.annotations.entity.ToLowerCase;
import com.example.demowithtests.util.exception.CountryNotSpecifiedException;
import com.example.demowithtests.util.exception.GenderNotFoundException;
import com.example.demowithtests.util.exception.ResourceNotFoundException;
import com.example.demowithtests.util.exception.ResourceWasDeletedException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
@Service
public class EmployeeServiceBean implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmailSenderService emailSenderService;
    private final DocumentRepository documentRepository;

    @Override
    public void addDocumentAndHistory(Employee employee, Document document) {

        Document savedDocument = documentRepository.save(document);

        DocumentHistory historyEntry = new DocumentHistory();
        historyEntry.setTimestamp(LocalDateTime.now());
        historyEntry.setActionType(ActionType.ADDED);
        historyEntry.setDocument(savedDocument);
        savedDocument.getHistory().add(historyEntry);

        employee.setDocument(savedDocument);
    }

    //    @Override
//    public Employee deleteDocumentByUserId(Integer id) {
//        return employeeRepository.findById(id).map(entity ->{
//            entity.getDocument().setIsDeleted(Boolean.TRUE);
//            entity.getDocument().setDeleteDate(LocalDateTime.now());
//            return employeeRepository.save(entity);
//        }).orElseThrow(() ->new EntityNotFoundException("Employee not found with id " + id));
//    }
    @Override
    public Employee deleteDocumentByUserId(Integer id) {
        return employeeRepository.findById(id).map(entity -> {
            Document document = entity.getDocument();
            if (document != null) {
                // Создаем новую запись в истории
                DocumentHistory historyEntry = new DocumentHistory();
                historyEntry.setTimestamp(LocalDateTime.now());
                historyEntry.setActionType(ActionType.REMOVED);
                historyEntry.setDocument(document);
                document.getHistory().add(historyEntry);

                // Отмечаем документ как удаленный
                document.setIsDeleted(Boolean.TRUE);
            }

            // Сохраняем изменения
            return employeeRepository.save(entity);
        }).orElseThrow(() -> new EntityNotFoundException("Employee not found with id " + id));
    }

    @Override
    public Employee restoreDocumentByUserId(Integer id) {
        return employeeRepository.findById(id).map(entity -> {
            Document document = entity.getDocument();
            if (document != null) {
                // Создаем новую запись в истории
                DocumentHistory historyEntry = new DocumentHistory();
                historyEntry.setTimestamp(LocalDateTime.now());
                historyEntry.setActionType(ActionType.RESTORED);
                historyEntry.setDocument(document);
                document.getHistory().add(historyEntry);

                // Отмечаем документ как восстановленный
                document.setIsDeleted(Boolean.TRUE);

                // Отмечаем документ как не удаленный
                document.setIsDeleted(Boolean.FALSE);

            }

            // Сохраняем изменения
            return employeeRepository.save(entity);
        }).orElseThrow(() -> new EntityNotFoundException("Employee not found with id " + id));
    }


    @Override
    @ActivateCustomAnnotations({Name.class, ToLowerCase.class})

    // @Transactional(propagation = Propagation.MANDATORY)
    public Employee create(Employee employee) {
        if (employee.getGender() == null) {
            throw new GenderNotFoundException("Gender is required for creating an employee. ");
        } else if (employee.getCountry() == null || employee.getCountry().isEmpty()) {
            throw new CountryNotSpecifiedException("You must specify an existing country. ");
        }
        return employeeRepository.save(employee);
//        return employeeRepository.saveAndFlush(employee);
    }

    /**
     * @param employee
     * @return
     */
    @Override
    public void createAndSave(Employee employee) {
        employeeRepository.saveEmployee(employee.getName(), employee.getEmail(), employee.getCountry(), String.valueOf(employee.getGender()));
    }

    @Override
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Page<Employee> getAllWithPagination(Pageable pageable) {
        log.debug("getAllWithPagination() - start: pageable = {}", pageable);
        Page<Employee> list = employeeRepository.findAll(pageable);
        log.debug("getAllWithPagination() - end: list = {}", list);
        return list;
    }

    @Override
    public Employee getById(Integer id) {
        var employee = employeeRepository.findById(id)
                // .orElseThrow(() -> new EntityNotFoundException("Employee not found with id = " + id));
                .orElseThrow(ResourceNotFoundException::new);
        if (employee.getIsDeleted()) {
            throw new EntityNotFoundException("Employee was deleted with id = " + id);
        }
        return employee;
    }

    @Override
    public Employee updateById(Integer id, Employee employee) {
        return employeeRepository.findById(id)
                .map(entity -> {
                    entity.setName(employee.getName());
                    entity.setEmail(employee.getEmail());
                    entity.setCountry(employee.getCountry());
                    return employeeRepository.save(entity);
                })
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id = " + id));
    }

    @Override
    public Employee removeById(Integer id) {
        //repository.deleteById(id);
        var employee = employeeRepository.findById(id)
//                 .orElseThrow(() -> new EntityNotFoundException("Employee not found with id = " + id));
                .orElseThrow(ResourceWasDeletedException::new);
        employee.setIsDeleted(Boolean.TRUE);
//        employeeRepository.delete(employee);
        employeeRepository.save(employee);
        return employee;
    }

    @Override
    public Employee softRemoveById(Integer id) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(ResourceWasDeletedException::new);
        if (Boolean.TRUE.equals(employee.getIsDeleted())) {
            throw new EntityNotFoundException("Employee not found with id = " + id);
        } else {
            employee.setIsDeleted(true);
            employeeRepository.save(employee);
        }
        return employee;
    }

    @Override
    public List<Employee> softRemoveByCountry() {
        var russians = employeeRepository.findAllRussian()
                .orElseThrow(() -> new EntityNotFoundException("Employees from Russia not found!"));

        for (Employee employee : russians) {
            if (Boolean.TRUE.equals(employee.getIsDeleted())) {
                throw new EntityNotFoundException("Employee not found ");
            } else {
                employee.setIsDeleted(true);
                employeeRepository.save(employee);
            }
        }

        return russians;
    }

    @Override
    public List<Employee> cancelSoftDeleteByCountry() {
        var russians = employeeRepository.findAllRussian()
                .orElseThrow(() -> new EntityNotFoundException("Employees from Russia not found!"));

        for (Employee employee : russians) {
            if (!Boolean.TRUE.equals(employee.getIsDeleted())) {
                throw new EntityNotFoundException("Employee not found ");
            } else {
                employee.setIsDeleted(false);
                employeeRepository.save(employee);
            }
        }

        return russians;
    }

    @Override
    public void removeAll() {
        employeeRepository.deleteAll();
    }

    /*@Override
    public Page<Employee> findByCountryContaining(String country, Pageable pageable) {
        return employeeRepository.findByCountryContaining(country, pageable);
    }*/

    @Override
    public Page<Employee> findByCountryContaining(String country, int page, int size, List<String> sortList, String sortOrder) {
        // create Pageable object using the page, size and sort details
        Pageable pageable = PageRequest.of(page, size, Sort.by(createSortOrder(sortList, sortOrder)));
        // fetch the page object by additionally passing pageable with the filters
        return employeeRepository.findByCountryContaining(country, pageable);
    }

    private List<Sort.Order> createSortOrder(List<String> sortList, String sortDirection) {
        List<Sort.Order> sorts = new ArrayList<>();
        Sort.Direction direction;
        for (String sort : sortList) {
            if (sortDirection != null) {
                direction = Sort.Direction.fromString(sortDirection);
            } else {
                direction = Sort.Direction.DESC;
            }
            sorts.add(new Sort.Order(direction, sort));
        }
        return sorts;
    }

    @Override
    public List<String> getAllEmployeeCountry() {
        log.info("getAllEmployeeCountry() - start:");
        List<Employee> employeeList = employeeRepository.findAll();
        List<String> countries = employeeList.stream()
                .map(country -> country.getCountry())
                .collect(Collectors.toList());
        /*List<String> countries = employeeList.stream()
                .map(Employee::getCountry)
                //.sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());*/

        log.info("getAllEmployeeCountry() - end: countries = {}", countries);
        return countries;
    }

    @Override
    public List<String> getSortCountry() {
        List<Employee> employeeList = employeeRepository.findAll();
        return employeeList.stream()
                .map(Employee::getCountry)
                .filter(c -> c.startsWith("U"))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<String> findEmails() {
        var employeeList = employeeRepository.findAll();

        var emails = employeeList.stream()
                .map(Employee::getEmail)
                .collect(Collectors.toList());

        var opt = emails.stream()
                .filter(s -> s.endsWith(".com"))
                .findFirst()
                .orElse("error?");
        return Optional.ofNullable(opt);
    }

    @Override
    public List<Employee> filterByCountry(String country) {
        return employeeRepository.findEmployeesByCountry(country);
    }

    @Override
    public Set<String> sendEmailsAllUkrainian() {
        var ukrainians = employeeRepository.findAllUkrainian()
                .orElseThrow(() -> new EntityNotFoundException("Employees from Ukraine not found!"));
        var emails = new HashSet<String>();
        ukrainians.forEach(employee -> {
            emailSenderService.sendEmail(
                    /*employee.getEmail(),*/
                    "kaluzny.oleg@gmail.com", //для тесту
                    "Need to update your information",
                    String.format(
                            "Dear " + employee.getName() + "!\n" +
                                    "\n" +
                                    "The expiration date of your information is coming up soon. \n" +
                                    "Please. Don't delay in updating it. \n" +
                                    "\n" +
                                    "Best regards,\n" +
                                    "Ukrainian Info Service.")
            );
            emails.add(employee.getEmail());
        });

        return emails;
    }

    /**
     * @param name
     * @return
     */
    @Override
    public List<Employee> findByNameContaining(String name) {
        return employeeRepository.findByNameContaining(name);
    }

    /**
     * @param name
     * @param id
     * @return
     */
//    @Override
//    public Employee updateEmployeeByName(String name, Integer id) {
//        employeeRepository.updateEmployeeByName(name, id);
//        return employeeRepository.findById(id).orElse(null);
//
//    }
    @Override
    public void updateEmployeeByName(String name, Integer id) {
        employeeRepository.updateEmployeeByName(name, id);
    }

    @Override
    public void updateRussianEmployeeByName(List<Employee> employee) {
        employeeRepository.updateByCountryRussia();
    }

    @Override
    public List<Employee> findAllUkrainianOleh() {
        var employeesOlehUA = employeeRepository.findAllUkrainianByNameOleh()
                .orElseThrow(() -> new EntityNotFoundException("Employees from Ukraine with name Oleh not found!"));

        return employeesOlehUA;

    }

    @Override
    public int countAllUkrainianWomen() {
        return employeeRepository.countAllUkrainianWomen();
    }

    @Override
    public List<Employee> findAllItalyMario() {
        var employeesMarioIT = employeeRepository.findAllItalyByNameMario()
                .orElseThrow(() -> new EntityNotFoundException("Employees from Italy with name Mario not found!"));

        return employeesMarioIT;

    }

    @Override
    public List<Employee> findAllDeletedBelarus() {
        var employeesDeletedBelarus = employeeRepository.findAllDeletedBelarus()
                .orElseThrow(() -> new EntityNotFoundException("Deleted employees from Belarus not found!"));
        return employeesDeletedBelarus;
    }
}
