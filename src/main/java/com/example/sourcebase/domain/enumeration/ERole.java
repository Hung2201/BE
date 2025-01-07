package com.example.sourcebase.domain.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ERole {
    ADMIN("ADMIN", 1),
    EMPLOYEE("EMPLOYEE", 2),
    MANAGER("MANAGER", 3),
    ;

    private final String name;
    private final int value;
}
