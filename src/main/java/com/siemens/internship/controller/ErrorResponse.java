package com.siemens.internship.controller;

import java.util.List;

public record ErrorResponse(List<String> detail) {
}
