package cs428.project.gather.data.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import cs428.project.gather.data.model.Event;
import cs428.project.gather.data.model.Registered;

public interface RegisteredRepository extends CrudRepository<Registered, Long> {
	

}