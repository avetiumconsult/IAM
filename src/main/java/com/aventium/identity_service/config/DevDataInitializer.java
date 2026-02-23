package com.aventium.identity_service.config;

import com.aventium.identity_service.entity.Application;
import com.aventium.identity_service.entity.Permission;
import com.aventium.identity_service.entity.Role;
import com.aventium.identity_service.entity.RolePermission;
import com.aventium.identity_service.repository.ApplicationRepository;
import com.aventium.identity_service.repository.PermissionRepository;
import com.aventium.identity_service.repository.RolePermissionRepository;
import com.aventium.identity_service.repository.RoleRepository;
import com.aventium.identity_service.util.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

//@Configuration
//@Profile("dev")
public class DevDataInitializer {
//    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);
//
//    @Bean
//    CommandLineRunner seedData(
//            ApplicationRepository applicationRepository,
//            RoleRepository roleRepository,
//            PermissionRepository permissionRepository,
//            RolePermissionRepository rolePermissionRepository
//    ) {
//        return args -> seed(applicationRepository, roleRepository, permissionRepository, rolePermissionRepository);
//    }
//
//    @Transactional
//    void seed(ApplicationRepository apps, RoleRepository roles, PermissionRepository perms, RolePermissionRepository rpRepo) {
//        log.info("Seeding baseline applications, roles, and permissions (dev profile)...");
//
//        Map<String, List<String>> appRoles = new LinkedHashMap<>();
//        appRoles.put("POS", List.of("POS_ADMIN","POS_MANAGER","POS_CASHIER","POS_AUDITOR"));
//        appRoles.put("PAYROLL", List.of("PAYROLL_ADMIN","PAYROLL_MANAGER","PAYROLL_ANALYST","PAYROLL_AUDITOR"));
//        appRoles.put("ABAS", List.of("ABAS_ADMIN","ABAS_ACCOUNTANT","ABAS_CONTROLLER","ABAS_AUDITOR"));
//        appRoles.put("PROCUREMENT", List.of("PROC_ADMIN","PROC_MANAGER","PROC_BUYER","PROC_AUDITOR"));
//
//        Map<String, List<String>> appPerms = new LinkedHashMap<>();
//        appPerms.put("POS", List.of("SALES:READ","SALES:WRITE","REFUND:APPROVE","CASHDRAWER:OPEN","INVENTORY:READ","INVENTORY:WRITE","REPORTS:READ","SETTINGS:WRITE"));
//        appPerms.put("PAYROLL", List.of("EMPLOYEE:READ","EMPLOYEE:WRITE","PAYRUN:CREATE","PAYRUN:APPROVE","PAYSLIP:READ","PAYSLIP:ISSUE","TAX:CONFIGURE","REPORTS:READ"));
//        appPerms.put("ABAS", List.of("LEDGER:READ","LEDGER:WRITE","JOURNAL:POST","JOURNAL:APPROVE","BUDGET:READ","BUDGET:WRITE","REPORTS:READ","SETTINGS:WRITE"));
//        appPerms.put("PROCUREMENT", List.of("SUPPLIER:READ","SUPPLIER:WRITE","PO:CREATE","PO:APPROVE","INVOICE:READ","INVOICE:APPROVE","CATALOG:READ","CATALOG:WRITE","REPORTS:READ"));
//
//        Map<String, Application> savedApps = new HashMap<>();
//        appRoles.keySet().forEach(name -> savedApps.put(name, ensureApplication(apps, name)));
//
//        Map<String, Map<String, Role>> savedRoles = new HashMap<>();
//        for (var entry : appRoles.entrySet()) {
//            String appName = entry.getKey();
//            Application app = savedApps.get(appName);
//            Map<String, Role> roleMap = new HashMap<>();
//            for (String r : entry.getValue()) {
//                roleMap.put(r, ensureRole(roles, app, r));
//            }
//            savedRoles.put(appName, roleMap);
//        }
//
//        Map<String, Map<String, Permission>> savedPerms = new HashMap<>();
//        for (var entry : appPerms.entrySet()) {
//            String appName = entry.getKey();
//            Application app = savedApps.get(appName);
//            Map<String, Permission> permMap = new HashMap<>();
//            for (String p : entry.getValue()) {
//                permMap.put(p, ensurePermission(perms, app, p));
//            }
//            savedPerms.put(appName, permMap);
//        }
//
//        // Map roles to a reasonable set of permissions per app (simple strategy)
//        for (String appName : savedApps.keySet()) {
//            Map<String, Role> rmap = savedRoles.get(appName);
//            Map<String, Permission> pmap = savedPerms.get(appName);
//            for (var role : rmap.values()) {
//                for (var perm : pmap.values()) {
//                    ensureRolePermission(rpRepo, role, perm);
//                }
//            }
//        }
//
//        log.info("Seeding done.");
//    }
//
//    private Application ensureApplication(ApplicationRepository repo, String name) {
//        return repo.findByName(name).orElseGet(() -> {
//            Application a = new Application();
//            a.setName(name);
//            a.setDescription(name + " application");
//            a.setActive(true);
//            return repo.save(a);
//        });
//    }
//
//    private Role ensureRole(RoleRepository repo, Application app, String name) {
//        return repo.findByApplicationIdAndName(app.getId(), name).orElseGet(() -> {
//            Role r = new Role();
//            r.setApplication(app);
//            r.setName(name);
//            return repo.save(r);
//        });
//    }
//
//    private Permission ensurePermission(PermissionRepository repo, Application app, String name) {
//        return repo.findByApplicationIdAndName(app.getId(), name).orElseGet(() -> {
//            Permission p = new Permission();
//            p.setApplication(app);
//            p.setName(name);
//            return repo.save(p);
//        });
//    }
//
//    private void ensureRolePermission(RolePermissionRepository repo, Role role, Permission permission) {
//        Id id = new Id();
//        id.setRoleId(role.getId());
//        id.setPermissionId(permission.getId());
//        if (!repo.existsById(id)) {
//            RolePermission rp = new RolePermission();
//            rp.setId(id);
//            rp.setRole(role);
//            rp.setPermission(permission);
//            repo.save(rp);
//        }
//    }
}
