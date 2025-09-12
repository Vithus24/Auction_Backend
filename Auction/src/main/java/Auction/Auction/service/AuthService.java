package Auction.Auction.service;

import Auction.Auction.dto.RegisterRequest;
import Auction.Auction.entity.User;
import Auction.Auction.exception.EmailAlreadyUsedException;
import Auction.Auction.mapper.UserMapper;
import Auction.Auction.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new EmailAlreadyUsedException(registerRequest.email() + " This email already is used, please try with different email.");
        }
        User user = userMapper.RegisterRequestToUser(registerRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
}
