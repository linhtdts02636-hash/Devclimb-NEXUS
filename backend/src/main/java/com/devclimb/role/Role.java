package com.devclimb.role;

import java.util.EnumSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private Integer id;

	@Column(name = "role_name", nullable = false, length = 50)
	private String name;

	@Column(name = "role_color", nullable = false)
	private String color;

	// perm_id do database tự sinh nên chỉ ghi role_id và perm_name.
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "permission", joinColumns = @JoinColumn(name = "role_id"))
	@Column(name = "perm_name", nullable = false)
	@Enumerated(EnumType.STRING)
	private Set<CommunityPermission> permissions = EnumSet.noneOf(CommunityPermission.class);

	protected Role() {
	}

	public Role(String name, String color, Set<CommunityPermission> permissions) {
		update(name, color, permissions);
	}

	public void update(String name, String color, Set<CommunityPermission> permissions) {
		this.name = name;
		this.color = color;
		this.permissions.clear();
		this.permissions.addAll(permissions);
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public Set<CommunityPermission> getPermissions() {
		return permissions;
	}
}
