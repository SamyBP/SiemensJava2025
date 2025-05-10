package com.siemens.internship.controller;

import java.util.Set;

public record ErrorResponse(Set<String> detail) {
}
