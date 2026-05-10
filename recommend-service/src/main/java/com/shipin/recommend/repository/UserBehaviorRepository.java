package com.shipin.recommend.repository;

import com.shipin.recommend.entity.UserBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    List<UserBehavior> findTop10ByUserIdAndActionOrderByTimestampDesc(Integer userId, String action);
}