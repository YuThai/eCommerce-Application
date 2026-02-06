package Ecom.Repository;

import Ecom.Entity.UserPermission;
import Ecom.Entity.UserPermission.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    
    /**
     * Find all permissions for a user
     */
    @Query("SELECT up FROM UserPermission up WHERE up.user.userId = :userId")
    List<UserPermission> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find all active permissions for a user
     */
    @Query("SELECT up FROM UserPermission up WHERE up.user.userId = :userId AND up.active = true")
    List<UserPermission> findByUserIdAndActiveTrue(@Param("userId") Long userId);
    
    /**
     * Check if user has specific permission on a resource
     */
    @Query("SELECT up FROM UserPermission up WHERE up.user.userId = :userId AND up.resourceName = :resourceName AND up.permissionType = :permissionType")
    Optional<UserPermission> findByUserIdAndResourceNameAndPermissionType(
            @Param("userId") Long userId, 
            @Param("resourceName") String resourceName, 
            @Param("permissionType") PermissionType permissionType);
    
    /**
     * Find all permissions for a specific resource
     */
    List<UserPermission> findByResourceName(String resourceName);
    
    /**
     * Check if user has any permission on resource
     */
    @Query("SELECT up FROM UserPermission up WHERE up.user.userId = :userId AND up.resourceName = :resourceName")
    Optional<UserPermission> findByUserIdAndResourceName(
            @Param("userId") Long userId, 
            @Param("resourceName") String resourceName);
    
    /**
     * Find all active permissions for a user on a specific resource
     */
    @Query("SELECT up FROM UserPermission up WHERE up.user.userId = :userId AND up.resourceName = :resourceName AND up.active = true")
    List<UserPermission> findByUserIdAndResourceNameAndActiveTrue(
            @Param("userId") Long userId, 
            @Param("resourceName") String resourceName);
}
