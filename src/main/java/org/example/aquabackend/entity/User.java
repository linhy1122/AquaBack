package org.example.aquabackend.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    /** User role: ADMIN / USER / MANAGER */
    private String role = "USER";

    /** Account enabled status */
    private Boolean enabled = true;

    /** Account locked status */
    private Boolean accountLocked = false;

    /** Account expired status */
    private Boolean accountExpired = false;

    /** Credentials expired status */
    private Boolean credentialsExpired = false;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted = 0;
}
