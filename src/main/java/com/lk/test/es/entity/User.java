package com.lk.test.es.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author like
 * @since 2020-10-23 13:34
 */
@Data
@Builder
@AllArgsConstructor
public class User {
    private String name;
    private int age;
}
