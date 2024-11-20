package cap.team3.what.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import cap.team3.what.exception.UserNotFoundException;
import cap.team3.what.model.User;
import cap.team3.what.repository.UserRepsitory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepsitory userRepository;
    
    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("No such user in DB"));

        return user;
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("No such user in DB"));

        userRepository.delete(user);
    }
}
