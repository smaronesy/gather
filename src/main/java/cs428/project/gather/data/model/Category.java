package cs428.project.gather.data.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class Category {
	private @Id @GeneratedValue Long id;
	private String name;
	private String description;
	

	@ManyToMany(mappedBy="categories")
	private Set<Event> events = new HashSet<Event>();
	
	@ManyToMany(mappedBy="preferences")
	private Set<Registrant> preferedUsers = new HashSet<Registrant>();

	private Category() {}

	public Category(String name, String description) {
		this.name = name;
		this.description = description;
		
	}
}