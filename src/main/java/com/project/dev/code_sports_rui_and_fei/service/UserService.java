package com.project.dev.code_sports_rui_and_fei.service;

import com.project.dev.code_sports_rui_and_fei.model.User;
import com.project.dev.code_sports_rui_and_fei.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }

    public User create(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }
        return userRepository.save(user);
    }

    public User update(Long id, User updated) {
        User user = findById(id);
        user.setNickname(updated.getNickname());
        user.setAvatarEmoji(updated.getAvatarEmoji());
        user.setHeight(updated.getHeight());
        user.setTargetWeight(updated.getTargetWeight());
        return userRepository.save(user);
    }
}
