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
@Table(name = "tracking_profile_insights_games")
public class TrackingProfileInsightsGame {
	@Id
	@Column(name = "steam64id")
	private String steam64id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "most_played_alltime_game")
	private String mostPlayedAlltimeGame;
	
	@Column(name = "most_played_alltime_hours")
	private Long mostPlayedAlltimeHours;
	
	@Column(name = "most_played_alltime_minutes")
	private Long mostPlayedAlltimeMinutes;
	
	@Column(name = "most_played_year_game")
	private String mostPlayedYearGame;
	
	@Column(name = "most_played_year_hours")
	private Long mostPlayedYearHours;
	
	@Column(name = "most_played_year_minutes")
	private Long mostPlayedYearMinutes;
	
	@Column(name = "last_played_game")
	private String lastPlayedGame;
	
	@Column(name = "last_played_date")
	private LocalDate lastPlayedDate;
	
	@Column(name = "longest_streak_alltime_game")
	private String longestStreakAlltimeGame;
	
	@Column(name = "longest_streak_alltime_days")
	private Long longestStreakAlltimeDays;
	
	@Column(name = "longest_streak_alltime_start")
	private LocalDate longestStreakAlltimeStart;
	
	@Column(name = "longest_streak_alltime_end")
	private LocalDate longestStreakAlltimeEnd;
	
	@Column(name = "longest_streak_year_game")
	private String longestStreakYearGame;
	
	@Column(name = "longest_streak_year_days")
	private Long longestStreakYearDays;
	
	@Column(name = "longest_streak_year_start")
	private LocalDate longestStreakYearStart;
	
	@Column(name = "longest_streak_year_end")
	private LocalDate longestStreakYearEnd;
	
	
}