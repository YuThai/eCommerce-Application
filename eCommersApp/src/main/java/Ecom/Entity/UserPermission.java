package Ecom.Entity;

import Ecom.Model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Permission Entity
 * Manages granular table-level access control for users
 * Supports CRUD operations on specific tables
 */
@Entity
@Table(name = "user_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "resource_name", "permission_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Resource name: PRODUCTS, ORDERS, USERS, PAYMENTS, etc.
     */
    @Column(name = "resource_name", nullable = false)
    private String resourceName;

    /**
     * Permission type: CREATE, READ, UPDATE, DELETE, EXPORT
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permissionType;

    /**
     * Whether this permission is active
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Notes about this permission (for audit purposes)
     */
    @Column(name = "notes")
    private String notes;

    /**
     * When this permission was granted
     */
    @Column(name = "created_at", nullable = false)
    private Long createdAt = System.currentTimeMillis();

    /**
     * Last modified timestamp
     */
    @Column(name = "updated_at")
    private Long updatedAt;
    
    public enum PermissionType {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        EXPORT,
        IMPORT,
        ADMIN
    }
}
