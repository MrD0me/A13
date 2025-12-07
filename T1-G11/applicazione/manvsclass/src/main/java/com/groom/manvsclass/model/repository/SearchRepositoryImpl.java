package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Admin;
import com.groom.manvsclass.model.ClassUT;
import com.groom.manvsclass.model.interaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SearchRepositoryImpl {

    private final ClassRepository classRepository;
    private final InteractionRepository interactionRepository;
    private final AdminRepository adminRepository;

    public SearchRepositoryImpl(ClassRepository classRepository,
                                InteractionRepository interactionRepository,
                                AdminRepository adminRepository) {
        this.classRepository = classRepository;
        this.interactionRepository = interactionRepository;
        this.adminRepository = adminRepository;
    }

    public long getLikes(String name) {
        return interactionRepository.countByNameAndType(name, 1);
    }

    public List<interaction> findReport() {
        return interactionRepository.findByType(0);
    }

    public List<ClassUT> findByText(String text) {
        return classRepository.searchByName(text);
    }

    public Admin findAdminByUsername(String username) {
        Optional<Admin> result = adminRepository.findByUsername(username);
        return result.orElse(null);
    }

    public Admin findAdminByEmail(String email) {
        Optional<Admin> result = adminRepository.findByEmail(email);
        return result.orElse(null);
    }

    public Admin findAdminByResetToken(String resetToken) {
        Optional<Admin> result = adminRepository.findByResetToken(resetToken);
        return result.orElse(null);
    }

    public Admin findAdminByInvitationToken(String invitationToken) {
        Optional<Admin> result = adminRepository.findByInvitationToken(invitationToken);
        return result.orElse(null);
    }

    public List<ClassUT> searchAndFilter(String text, String category) {
        return classRepository.searchByTextAndCategory(text, category);
    }

    public List<ClassUT> filterByCategory(String category) {
        return classRepository.findByCategoryIgnoreCase(category);
    }

    public List<ClassUT> orderByDate() {
        return classRepository.findAllByOrderByDateAsc();
    }

    public List<ClassUT> orderByName() {
        return classRepository.findAllByOrderByNameAsc();
    }

    public List<ClassUT> filterByDifficulty(String difficulty) {
        return classRepository.findByDifficulty(difficulty);
    }

    public List<ClassUT> searchAndDFilter(String text, String difficulty) {
        return classRepository.searchByTextAndDifficulty(text, difficulty);
    }

}
