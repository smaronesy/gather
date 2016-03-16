package cs428.project.gather.data.repo;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import cs428.project.gather.data.model.Event;

public interface EventRepository  extends CrudRepository<Event, Long> {

	/**
	 * Returns the {@link Event} with the given identifier.
	 * 
	 * @param id the id to search for.
	 * @return
	 */
	Event findOne(Long id);
	
	List<Event> findByName(String name);
	List<Event> findByDescription(String description);
	
	@Query("SELECT DISTINCT e FROM Event e INNER JOIN e.location l WHERE l.latitude BETWEEN ?1 AND ?2 AND l.longitude BETWEEN ?3 AND ?4")
	List<Event> findByLocationWithin(double lowerLat, double uppLat, double lowerLon, double upperLon);
	
	@Query("SELECT DISTINCT e FROM Event e INNER JOIN e.occurrences o WHERE o.timestamp > CURRENT_TIMESTAMP AND o.timestamp < ?1")
	List<Event> findByOccurrenceTimeWithin(Timestamp upperBound);
	
	@Query("SELECT DISTINCT e FROM Event e INNER JOIN e.occurrences o INNER JOIN e.location l WHERE o.timestamp > CURRENT_TIMESTAMP AND o.timestamp < ?5 AND l.latitude BETWEEN ?1 AND ?2 AND l.longitude BETWEEN ?3 AND ?4")
	List<Event> findByLocationAndOccurrenceTimeWithin(double lowerLat, double uppLat, double lowerLon, double upperLon, Timestamp upperTime);

    @Query("SELECT DISTINCT e FROM Event e INNER JOIN e.location l WHERE SQRT(POWER((l.latitude - ?1)/(0.014554*1.60934), 2.0) + POWER((l.longitude - ?2)/(0.014457*1.60934), 2.0)) < ?3")
    List<Event> findByLocationWithinKmRadius(double latitude, double longitude, double radius_km);

    @Query("SELECT DISTINCT e FROM Event e INNER JOIN e.occurrences o INNER JOIN e.location l WHERE e.name = ?1 AND l.latitude = ?2 AND l.longitude = ?3 AND o.timestamp = time")
    List<Event> findByNameAndLocationAndTime(String name, double latitude, double longitude, Timestamp time);
}
