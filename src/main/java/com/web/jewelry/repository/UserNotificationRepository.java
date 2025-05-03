package com.web.jewelry.repository;

import com.web.jewelry.model.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Page<UserNotification> findAllByCustomerId(Long customerId, Pageable pageable);
    Page<UserNotification> findAllByStaffId(Long staffId, Pageable pageable);
    Page<UserNotification> findAllByManagerId(Long managerId, Pageable pageable);

    @Query("SELECT un FROM UserNotification un WHERE un.customer.id = ?1 AND un.status = 'UNREAD'")
    List<UserNotification> findUnreadByCustomerId(Long customerId);
    @Query("SELECT un FROM UserNotification un WHERE un.staff.id = ?1 AND un.status = 'UNREAD'")
    List<UserNotification> findUnreadByStaffId(Long staffId);
    @Query("SELECT un FROM UserNotification un WHERE un.manager.id = ?1 AND un.status = 'UNREAD'")
    List<UserNotification> findUnreadByManagerId(Long managerId);

    @Query("SELECT COUNT(nd) FROM UserNotification nd WHERE nd.customer.id = ?1 AND nd.status = 'UNREAD'")
    Long countUnreadByCustomer(Long id);

    @Query("SELECT COUNT(nd) FROM UserNotification nd WHERE nd.staff.id = ?1 AND nd.status = 'UNREAD'")
    Long countUnreadByStaff(Long id);

    @Query("SELECT COUNT(nd) FROM UserNotification nd WHERE nd.manager.id = ?1 AND nd.status = 'UNREAD'")
    Long countUnreadByManager(Long id);

    @Modifying
    @Query("DELETE FROM UserNotification un WHERE un.customer.id = ?1")
    void deleteAllByCustomerId(Long id);

    @Modifying
    @Query("DELETE FROM UserNotification un WHERE un.staff.id = ?1")
    void deleteAllByStaffId(Long id);

    @Modifying
    @Query("DELETE FROM UserNotification un WHERE un.manager.id = ?1")
    void deleteAllByManagerId(Long id);
}
