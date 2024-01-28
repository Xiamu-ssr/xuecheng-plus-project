package com.xuecheng.base.exception;

import jakarta.validation.groups.Default;

public class ValidationGroups {
    public interface Insert extends Default{};
    public interface Update extends Default{};
    public interface Delete extends Default{};
}
