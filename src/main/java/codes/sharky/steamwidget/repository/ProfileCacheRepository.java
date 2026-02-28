package codes.sharky.steamwidget.repository;

import codes.sharky.steamwidget.entity.ProfileCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfileCacheRepository extends JpaRepository<ProfileCache, String>, JpaSpecificationExecutor<ProfileCache>  {

    Optional<ProfileCache> findBySteam64id(String steam64id);

    List<ProfileCache> findBySteam64idIn(Collection<String> steam64ids);

    List<ProfileCache> findByLastrequestBefore (LocalDateTime lastrequest);

}
