package com.devclimb.role;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devclimb.common.security.AuthenticatedFirebaseUser;
import com.devclimb.common.security.FirebaseAuthenticationFilter;

@RestController
@RequestMapping("/api/communities/{name}")
public class RoleController {

	private static final String FIREBASE_USER = FirebaseAuthenticationFilter.AUTHENTICATED_USER_ATTRIBUTE;

	private final RoleService roleService;

	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}

	@GetMapping("/roles")
	public List<RoleDTO> list(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name) {
		return roleService.listRoles(firebaseUser, name);
	}

	@PostMapping("/roles")
	@ResponseStatus(HttpStatus.CREATED)
	public RoleDTO create(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name, @RequestBody SaveRoleRequest request) {
		return roleService.createRole(firebaseUser, name, request);
	}

	@PatchMapping("/roles/{roleId}")
	public RoleDTO update(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name, @PathVariable Integer roleId, @RequestBody SaveRoleRequest request) {
		return roleService.updateRole(firebaseUser, name, roleId, request);
	}

	@DeleteMapping("/roles/{roleId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name, @PathVariable Integer roleId) {
		roleService.deleteRole(firebaseUser, name, roleId);
	}

	@PutMapping("/members/{userId}/roles/{roleId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void assign(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name, @PathVariable Integer userId, @PathVariable Integer roleId) {
		roleService.assignRole(firebaseUser, name, userId, roleId);
	}

	@DeleteMapping("/members/{userId}/roles/{roleId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void unassign(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name, @PathVariable Integer userId, @PathVariable Integer roleId) {
		roleService.unassignRole(firebaseUser, name, userId, roleId);
	}

	@DeleteMapping("/members/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeMember(@RequestAttribute(FIREBASE_USER) AuthenticatedFirebaseUser firebaseUser,
			@PathVariable String name, @PathVariable Integer userId) {
		roleService.removeMember(firebaseUser, name, userId);
	}
}
