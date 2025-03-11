package com.web.jewelry.repository;

import com.web.jewelry.enums.ENotificationStatus;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Page<UserNotification> findAllByCustomerId(Long customerId, Pageable pageable);
    Page<UserNotification> findAllByStaffId(Long staffId, Pageable pageable);
    Page<UserNotification> findAllByManagerId(Long managerId, Pageable pageable);
    //Optional<UserNotification> findByNotificationAndUser(Notification notification, User user);

    @Query("SELECT COUNT(nd) FROM UserNotification nd WHERE nd.customer = ?1 AND nd.status = 'UNREAD'")
    Long countUnreadByCustomer(Customer customer);

    Page<UserNotification> findByStatus(ENotificationStatus status, Pageable pageable);
}
