package codes.sharky.steamwidget.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

@Getter
@Entity
@Immutable
@Table(name = "tracking_profile_insights_playtime")
public class TrackingProfileInsightsPlaytime {
	@Id
	@Column(name = "steam64id")
	private String steam64id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "alltime_hours")
	private Long alltimeHours;
	
	@Column(name = "alltime_minutes")
	private Long alltimeMinutes;
	
	@Column(name = "year_hours")
	private Long yearHours;
	
	@Column(name = "year_minutes")
	private Long yearMinutes;
	
	@Column(name = "avg_daily_hours")
	private Long avgDailyHours;
	
	@Column(name = "avg_daily_minutes")
	private Long avgDailyMinutes;
	
	@Column(name = "best_day_date")
	private LocalDate bestDayDate;
	
	@Column(name = "best_day_hours")
	private Long bestDayHours;
	
	@Column(name = "best_day_minutes")
	private Long bestDayMinutes;
	
	@Column(name = "unique_games_this_year")
	private Long uniqueGamesThisYear;
	
	@Column(name = "unique_games_alltime")
	private Long uniqueGamesAlltime;
	
	
}