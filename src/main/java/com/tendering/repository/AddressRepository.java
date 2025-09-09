package com.tendering.repository;

import com.tendering.model.Address;
import com.tendering.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser(User user);

    List<Address> findByUserPublicId(UUID userPublicId);

    Optional<Address> findByPublicId(UUID publicId);

    Optional<Address> findByUserAndIsDefault(User user, Boolean isDefault);

    Optional<Address> findByUserAndIsInvoiceAddress(User user, Boolean isInvoiceAddress);

    void deleteByPublicId(UUID publicId);
}