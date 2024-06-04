package com.kcs3.auction.repository;

import com.kcs3.auction.entity.ItemDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemDetailRepository extends JpaRepository<ItemDetail, Long> {

    @Query("SELECT i FROM ItemDetail i WHERE i.item.itemId=:itemId")
    Optional<ItemDetail> findByItemId(@Param("itemId")Long ItemId);
}
