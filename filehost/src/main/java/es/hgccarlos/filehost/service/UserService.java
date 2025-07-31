package es.hgccarlos.filehost.service;

import es.hgccarlos.filehost.model.User;

public interface UserService {
    User createUser(String username, String rawPassword, String role);
    User getByUsername(String username);
}
