package cs428.project.gather.data.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class Registrant extends Actor {
	private @Id @Column(name = "ID") @GeneratedValue Long id;
	private String password;
	private @Column(unique = true) String displayName;
	private @Column(unique = true) String email;
	private long reliability;
	private int defaultTimeWindow;
	private int defaultZip;
	private boolean isAdmin;

	@ManyToMany(mappedBy = "subscribers")
	private Set<Event> subscribedEvents = new HashSet<Event>();

	@ManyToMany(mappedBy = "owners")
	private Set<Event> ownedEvents = new HashSet<Event>();

	@ManyToMany(mappedBy = "participants")
	private Set<Event> joinedEvents = new HashSet<Event>();

	@ManyToMany
	private Set<Category> preferences = new HashSet<Category>();

	public Registrant() {
		super(ActorType.REGISTERED_USER);
	}

	public Registrant(String email, String password) {
		super(ActorType.REGISTERED_USER);
		this.email = email;
		this.password = password;
	}
	
	public Registrant(String email, String password, String displayName, long reliability,
			int defaultTimeWindow, int defaultZip) {
		this.password = password;
		this.displayName = displayName;
		this.email = email;
		this.reliability = reliability;
		this.defaultTimeWindow = defaultTimeWindow;
		this.defaultZip = defaultZip;
		this.isAdmin = false;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public long getReliability() {
		return reliability;
	}

	public void setReliability(long reliability) {
		this.reliability = reliability;
	}

	public int getDefaultTimeWindow() {
		return defaultTimeWindow;
	}

	public void setDefaultTimeWindow(int defaultTimeWindow) {
		this.defaultTimeWindow = defaultTimeWindow;
	}

	public int getDefaultZip() {
		return defaultZip;
	}

	public void setDefaultZip(int defaultZip) {
		this.defaultZip = defaultZip;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public boolean joinEvent(Event event) {
		return joinedEvents.add(event);
	}
}
