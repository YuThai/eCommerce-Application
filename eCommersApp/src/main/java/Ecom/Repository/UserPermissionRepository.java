package Ecom.Repository;

import Ecom.Entity.UserPermission;
import Ecom.Entity.UserPermission.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    
    /**
     * Find all permissions for a user
     */
    List<UserPermission> findByUserId(Long userId);
    
    /**
     * Find all active permissions for a user
     */
    List<UserPermission> findByUserIdAndActiveTrue(Long userId);
    
    /**
     * Check if user has specific permission on a resource
     */
    Optional<UserPermission> findByUserIdAndResourceNameAndPermissionType(
            Long userId, String resourceName, PermissionType permissionType);
    
    /**
     * Find all permissions for a specific resource
     */
    List<UserPermission> findByResourceName(String resourceName);
    
    /**
     * Check if user has any permission on resource
     */
    Optional<UserPermission> findByUserIdAndResourceName(Long userId, String resourceName);
    
    /**
     * Find all active permissions for a user on a specific resource
     */
    List<UserPermission> findByUserIdAndResourceNameAndActiveTrue(Long userId, String resourceName);
}
