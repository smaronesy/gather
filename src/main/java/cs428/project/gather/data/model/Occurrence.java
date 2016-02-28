package cs428.project.gather.data.model;


import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.util.Assert;

@Entity
public class Occurrence {
	private @Id @GeneratedValue Long id;
	private String description;
	private Timestamp datetime;

	@ManyToOne
	private Location location;

	private Occurrence() {}

	public Occurrence(String description, Timestamp datetime) {
		this.description = description;
		this.datetime = datetime;
	}
	
	public Occurrence(String description, Timestamp datetime, Location location){
		this.description = description;
		this.datetime = datetime;
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		Assert.hasText(description);
		this.description = description;
	}

	public Timestamp getDatetime() {
		return datetime;
	}

	public void setDatetime(Timestamp datetime) {
		Assert.notNull(datetime);
		this.datetime = datetime;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		Assert.notNull(location);
		this.location = location;
	}
}