package com.example.demowithtests.service;

import com.example.demowithtests.domain.SecurityUser;
import com.example.demowithtests.repository.SecurityUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class SecurityUserServiceBean implements SecurityUserService {

    private final SecurityUserRepository securityUserRepository;
    @Override
    public List<SecurityUser> getAll() {
        return securityUserRepository.findAll();
    }
}