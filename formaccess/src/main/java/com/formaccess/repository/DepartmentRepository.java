package com.formaccess.repository;

import com.formaccess.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data writes the SQL for us: we only declare method names.
 * JpaRepository already gives findAll(), findById(), save(), delete(), count() ...
 */
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    List<Department> findAllByOrderByCodeAsc();
}
