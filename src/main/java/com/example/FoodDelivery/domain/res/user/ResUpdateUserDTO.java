package com.example.FoodDelivery.domain.res.user;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import com.example.FoodDelivery.util.constant.GenderEnum;

@Getter
@Setter
public class ResUpdateUserDTO {
    private long id;
    private String name;
    private GenderEnum gender;
    private String address;
    private int age;

    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;
    private Role role;

    @Getter
    @Setter
    public static class Role {
        private long id;
        private String name;
    }
}
