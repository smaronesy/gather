package cs428.project.gather.data.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

@Entity
public class Category {
	private @Id @GeneratedValue Long id;
	private String name;
	private String description;
	

	//@OneToMany(mappedBy="category")
	//private Set<Event> events = new HashSet<Event>();
	
	@ManyToMany(mappedBy="preferences")
	private Set<Registrant> userPreferences = new HashSet<Registrant>();

	private Category() {}

	public Category(String name, String description) {
		this.setName(name);
		this.setDescription(description);
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
