package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.SchoolClassDto;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolClassService {
    private final SchoolClassRepository schoolClassRepository;

    public SchoolClassService(SchoolClassRepository schoolClassRepository) {
        this.schoolClassRepository = schoolClassRepository;
    }

    // Finds all school classes
    public List<SchoolClass> findAllSchoolClasses() {
        return schoolClassRepository.findAll();
    }

    // Finds a school class with given ID
    public SchoolClass findSchoolClassById(Long id) {
        return schoolClassRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School class with id = "+id+" not found"));
    }

    // Add a new school class with request DTO
    public void addSchoolClass(SchoolClassDto dto) {
        if(schoolClassRepository.existsByName(dto.getName())) {
            throw new BadRequestException("School class '" + dto.getName() + "' already exists");
        }

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName(dto.getName());

        schoolClassRepository.save(schoolClass);
    }
}
