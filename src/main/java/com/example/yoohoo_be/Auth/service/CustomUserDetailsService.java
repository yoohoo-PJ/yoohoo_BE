package com.example.yoohoo_be.Auth.service;

import com.example.yoohoo_be.Auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 스프링 시큐리티가 로그인/토큰 검증 시 사용자 정보를 조회할 때 사용하는 서비스.
 * User 엔티티가 이미 UserDetails를 구현하고 있으므로 그대로 반환하면 된다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 아이디의 사용자를 찾을 수 없습니다: " + username));
    }
}
