package com.ssh.backend.security;

import com.ssh.backend.entity.User;
import com.ssh.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServiceException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        // email 만들어주기
        if (email == null && oAuth2User.getAttribute("login") != null) {
            email = oAuth2User.getAttribute("login") + "@github.local";
            avatarUrl = oAuth2User.getAttribute("avatar_url");
        }

        final String finalEmail = email;
        final String finalName = name != null ? name : "User";
        final String finalAvatarUrl = avatarUrl;

        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setUsername(generateUsername(finalEmail));
                    newUser.setFullName(finalName);
                    newUser.setProfileImageUrl(finalAvatarUrl);
                    newUser.setPassword("");
                    newUser.setEnabled(true);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        if (finalAvatarUrl != null && !finalAvatarUrl.equals(user.getProfileImageUrl())) {
            user.setProfileImageUrl(finalAvatarUrl);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/callback")
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    //        ssh2061@naver.com 있으면 @기준으로 0번 인덱스에 대해
//                ^a-z0-9 : 알파벳이나 숫자가 아닌 문자에 대해서 "" : 빈문자 : 삭제한다.  ss!h$2061 => ssh2061
    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}