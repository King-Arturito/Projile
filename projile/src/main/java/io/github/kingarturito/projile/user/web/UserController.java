package io.github.kingarturito.projile.user.web;

import io.github.kingarturito.projile.user.UserService;
import io.github.kingarturito.projile.user.dtos.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("")
    public UserDto findUserByUsername(@RequestParam String username) {
        return userService.findByUsername(username);
    }
}
