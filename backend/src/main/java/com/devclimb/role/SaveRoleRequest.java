package com.devclimb.role;

import java.util.List;

public record SaveRoleRequest(String name, String color, List<String> permissions) {
}
