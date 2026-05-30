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
@Table(name = "tracking_profile_insights_activity")
public class TrackingProfileInsightsActivity {
	@Id
	@Column(name = "steam64id")
	private String steam64id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "current_streak_days")
	private Long currentStreakDays;
	
	@Column(name = "current_streak_start")
	private LocalDate currentStreakStart;
	
	@Column(name = "current_streak_end")
	private LocalDate currentStreakEnd;
	
	@Column(name = "longest_streak_year_days")
	private Long longestStreakYearDays;
	
	@Column(name = "longest_streak_year_start")
	private LocalDate longestStreakYearStart;
	
	@Column(name = "longest_streak_year_end")
	private LocalDate longestStreakYearEnd;
	
	@Column(name = "longest_streak_alltime_days")
	private Long longestStreakAlltimeDays;
	
	@Column(name = "longest_streak_alltime_start")
	private LocalDate longestStreakAlltimeStart;
	
	@Column(name = "longest_streak_alltime_end")
	private LocalDate longestStreakAlltimeEnd;
	
	@Column(name = "most_active_dow")
	private String mostActiveDow;
	
	@Column(name = "most_active_dow_count")
	private Long mostActiveDowCount;
	
	@Column(name = "most_active_month")
	private String mostActiveMonth;
	
	@Column(name = "most_active_month_days")
	private Long mostActiveMonthDays;
	
	
}