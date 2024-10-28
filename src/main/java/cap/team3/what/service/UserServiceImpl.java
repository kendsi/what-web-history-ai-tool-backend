package cap.team3.what.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cap.team3.what.exception.UserNotFoundException;
import cap.team3.what.model.User;
import cap.team3.what.repository.UserRepsitory;
import cap.team3.what.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepsitory userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public String handleGoogleLogin(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            User newUser = new User(email);
            userRepository.save(newUser);
            return jwtTokenProvider.createToken(email);
        }
        
        return jwtTokenProvider.createToken(user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("No such user in DB"));

        return user;
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("No such user in DB"));

        userRepository.delete(user);
    }
}
