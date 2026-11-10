package com.oms.core.config;

import com.oms.core.constants.AppRole;
import com.oms.core.entity.Role;
import com.oms.core.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Automatically check and seeds missing system architecture roles
        if (!roleRepository.findByName(AppRole.ROLE_CUSTOMER).isPresent()) {
            Role customerRole = new Role();
            customerRole.setName(AppRole.ROLE_CUSTOMER);
            roleRepository.save(customerRole);
        }
        
        if (!roleRepository.findByName(AppRole.ROLE_ADMIN).isPresent()) {
            Role adminRole = new Role();
            adminRole.setName(AppRole.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }

        if (!roleRepository.findByName(AppRole.ROLE_DELIVERY).isPresent()) {
            Role deliveryRole = new Role();
            deliveryRole.setName(AppRole.ROLE_DELIVERY);
            roleRepository.save(deliveryRole);
        }
    }
}