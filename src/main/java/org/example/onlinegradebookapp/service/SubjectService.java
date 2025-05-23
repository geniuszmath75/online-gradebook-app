package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    // Find all subjects
    public List<Subject> findAllSubjects() {
        return subjectRepository.findAll();
    }

    // Find a subject with given ID
    public Subject findSubjectById(Long id) {
        return subjectRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with id = "+id+" not found"));
    }

    // Add a new subject with request DTO
    public void addSubject(SubjectDto dto) {
        if(subjectRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Subject '" + dto.getName() + "' already exists");
        }

        Subject subject = new Subject();
        subject.setName(dto.getName());

        subjectRepository.save(subject);
    }

    // Update a subject with given ID
    public void updateSubject(SubjectDto dto, Long id) throws ResourceNotFoundException {
        Optional<Subject> optionalSubject = subjectRepository.findById(id);

        // If subject exists, update it
        if(optionalSubject.isPresent()) {
            Subject subject = new Subject();

            subject.setId(id);
            subject.setCreatedAt(optionalSubject.get().getCreatedAt());
            subject.setName(dto.getName());
            subjectRepository.save(subject);
        } else {
            throw new ResourceNotFoundException("Subject with id=" + id + " not found");
        }
    }

    // Delete a subject with given ID
    public void deleteSubject(Long id) {
        if(subjectRepository.existsById(id)) {
            subjectRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Subject with id=" + id + " not found");
        }
    }
}
