package com.ccnlthd.taskmanager.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ccnlthd.taskmanager.model.Category;
import com.ccnlthd.taskmanager.model.User;
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrderByIdAsc(Long userId);
    Category findByIdAndUserId(Long id, Long userId);
    List<Category> findByUser(User user);
    long countByUserId(Long userId);

    @Query("select count(distinct c.user.id) from Category c where c.user is not null")
    long countDistinctUserId();

    /**
     * Top danh mục có nhiều task nhất. Dùng LEFT JOIN để tính cả
     * category rỗng (count = 0), sắp xếp giảm dần theo số task.
     */
    @Query("""
            select c
            from Category c
              left join c.tasks t
            group by c
            order by count(t) desc, c.id asc
            """)
    List<Category> findTopCategoriesByTaskCount(Pageable pageable);
}
