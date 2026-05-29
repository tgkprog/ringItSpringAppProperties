package sel2in.settings;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {

    @Query("select distinct s.appName from AppSetting s order by s.appName")
    List<String> findDistinctAppNames();

    @Query("select distinct s.category from AppSetting s where s.appName = :appName order by s.category")
    List<String> findDistinctCategories(@Param("appName") String appName);

    @Query("select distinct s.propertyName from AppSetting s where s.appName = :appName and s.category = :category order by s.propertyName")
    List<String> findDistinctPropertyNames(@Param("appName") String appName, @Param("category") String category);

    Optional<AppSetting> findByAppNameAndCategoryAndPropertyName(String appName, String category, String propertyName);

    List<AppSetting> findAllByOrderByAppNameAscCategoryAscPropertyNameAsc();
}