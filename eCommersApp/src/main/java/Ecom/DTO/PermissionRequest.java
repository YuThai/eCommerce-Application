package Ecom.DTO;

import Ecom.Entity.UserPermission.PermissionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission Assignment Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Resource name is required")
    private String resourceName;
    
    @NotNull(message = "Permission type is required")
    private PermissionType permissionType;
    
    private String notes;
}
