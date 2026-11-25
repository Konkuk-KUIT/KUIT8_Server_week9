package kuit8th.springmvc.dto;

public record UserDTO(
        String userId,
        String password,
        String name,
        String email
) {
}
