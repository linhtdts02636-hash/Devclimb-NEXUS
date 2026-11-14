package com.devclimb.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByFirebaseUid(String firebaseUid);

	boolean existsByNameIgnoreCase(String name);
}
