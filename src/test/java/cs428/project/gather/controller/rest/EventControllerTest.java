package cs428.project.gather.controller.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cs428.project.gather.GatherApplication;
import cs428.project.gather.data.Coordinates;
import cs428.project.gather.data.RESTResponseData;
import cs428.project.gather.data.model.Category;
import cs428.project.gather.data.model.Event;
import cs428.project.gather.data.model.Occurrence;
import cs428.project.gather.data.model.Registrant;
import cs428.project.gather.data.repo.CategoryRepository;
import cs428.project.gather.data.repo.EventRepository;
import cs428.project.gather.data.repo.RegistrantRepository;
import cs428.project.gather.validator.EventsQueryDataValidator;
import cs428.project.gather.validator.NewEventDataValidator;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(GatherApplication.class)
@WebIntegrationTest
public class EventControllerTest {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private RestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    EventRepository eventRepo;

    @Autowired
    RegistrantRepository regRepo;
    
    @Autowired
    CategoryRepository categoryRepo;

    @Autowired
    private EventsQueryDataValidator eventsQueryDataValidator;

    @Autowired
    private NewEventDataValidator newEventDataValidator;

	@Before
	public void setUp() {
		eventRepo.deleteAll();
		assertEquals(this.eventRepo.count(), 0);
		regRepo.deleteAll();
		assertEquals(this.regRepo.count(), 0);
		Registrant aUser = new Registrant("existed@email.com", "password", "existedName", 10L, 3, 10000);
		Registrant participant = new Registrant("participant@email.com", "password", "participantName", 10L, 3, 10000);
		Registrant newOwner = new Registrant("newOwner@email.com", "password", "newOwner", 10L, 3, 10000);
		this.regRepo.save(participant);
		this.regRepo.save(aUser);
		this.regRepo.save(newOwner);
		assertEquals(this.regRepo.count(), 3);
		this.categoryRepo.deleteAll();
		Category swim= new Category("Swim");
		this.categoryRepo.save(swim);
		Category soccer= new Category("Soccer");
		this.categoryRepo.save(soccer);
		
	}
	
	@Test
	public void testGetEvent() throws JsonProcessingException {
		
		Coordinates eCoor = new Coordinates();
		eCoor.setLatitude(12.342);
		eCoor.setLongitude(111.232);
		
		ResponseEntity<RESTResponseData> apiResponse = attemptGetEvent(eCoor.getLatitude(), eCoor.getLongitude(), 10, 500);
		
		assertTrue(apiResponse.getStatusCode().equals(HttpStatus.OK));
	
	}

	@Test
	public void testGetEventWrongRadius() throws JsonProcessingException {
		
		Coordinates eCoor = new Coordinates();
		eCoor.setLatitude(12.341);
		eCoor.setLongitude(111.231);

		ResponseEntity<RESTResponseData> apiResponse = attemptGetEvent(eCoor.getLatitude(), eCoor.getLongitude(), 25, 500);
		
		assertTrue(apiResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST));

	}
	
	@Test
	public void testAddNewEvent() throws JsonProcessingException {
		ResponseEntity<RESTResponseData> signInResponse = authenticateUser("existed@email.com", "password");
		List<String> cookies = signInResponse.getHeaders().get("Set-Cookie");

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Cookie",StringUtils.join(cookies,';'));
		HttpEntity<String> requestEntity = new HttpEntity<String>(requestHeaders);
		
		// Invoking the API
		
		ResponseEntity<RESTResponseData> response = checkSession(requestEntity);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));

		RESTResponseData responseData = response.getBody();
		assertTrue(responseData.getMessage().equals("Session Found"));
		
		Coordinates eCoor = new Coordinates();
		eCoor.setLatitude(12.342);
		eCoor.setLongitude(111.232);
		
		Coordinates uCoor = new Coordinates();
		uCoor.setLatitude(12.33);
		uCoor.setLongitude(111.24);

		attemptAddEvent("EventOne", eCoor, "DescOne", "Swim", System.nanoTime()+10000L, uCoor, StringUtils.join(cookies,';'));

		List<Event> listEvents = this.eventRepo.findByName("EventOne");
		assertEquals(1, listEvents.size());
		Event anEvent = listEvents.get(0);
		assertEquals("EventOne", anEvent.getName());
		assertEquals("DescOne", anEvent.getDescription());

	}
	
	private ResponseEntity<RESTResponseData> authenticateUser(String email, String password) throws JsonProcessingException {
		// Building the Request body data
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("email", email);
		requestBody.put("password", password);
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		// Creating http entity object with request body and headers
		HttpEntity<String> httpEntity = new HttpEntity<String>(OBJECT_MAPPER.writeValueAsString(requestBody),
				requestHeaders);

		@SuppressWarnings("unchecked")
		ResponseEntity<RESTResponseData> result = restTemplate.exchange("http://localhost:8888/rest/registrants/signin", HttpMethod.POST, httpEntity,
				Map.class, Collections.EMPTY_MAP);
		
		assertNotNull(result);
		// Asserting the response of the API.
		//return apiResponse;
		return result;

	}
	
	private ResponseEntity<RESTResponseData> checkSession(HttpEntity<String> requestEntity) throws JsonProcessingException {

		// Invoking the API
		
		ResponseEntity<RESTResponseData> response = restTemplate.exchange("http://localhost:8888/rest/session", HttpMethod.GET, requestEntity, RESTResponseData.class);

		assertNotNull(response);
		
		// Asserting the response of the API.
		return response;

	}
	
	private Map<String, Object> attemptAddEvent(String name, Coordinates eCoor, String description, String category, long time, Coordinates uCoor, String session) throws JsonProcessingException {
		// Building the Request body data
		
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("eventName", name);
		requestBody.put("eventCoordinates", eCoor);
		requestBody.put("eventDescription", description);
		requestBody.put("eventCategory", category);
		requestBody.put("eventTime", time);
		requestBody.put("callerCoordinates", uCoor);
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Cookie", session);
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);

		// Creating http entity object with request body and headers
		HttpEntity<String> httpEntity = new HttpEntity<String>(OBJECT_MAPPER.writeValueAsString(requestBody),
				requestHeaders);

		// Invoking the API
		@SuppressWarnings("unchecked")
		Map<String, Object> apiResponse = restTemplate.postForObject("http://localhost:8888/rest/events", httpEntity,
				Map.class, Collections.EMPTY_MAP);


		assertNotNull(apiResponse);

		// Asserting the response of the API.
		return apiResponse;

	}
	
	private ResponseEntity<RESTResponseData> attemptGetEvent(double lat, double lon, float radius, int hour) throws JsonProcessingException {
		// Building the Request body data
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("latitude", lat);
		requestBody.put("longitude", lon);
		requestBody.put("radiusMi", radius);
		requestBody.put("hour", hour);
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);

		// Creating http entity object with request body and headers
		HttpEntity<String> httpEntity = new HttpEntity<String>(OBJECT_MAPPER.writeValueAsString(requestBody),
				requestHeaders);

		// Invoking the API
		@SuppressWarnings("unchecked")
		ResponseEntity<RESTResponseData> apiResponse = restTemplate.exchange("http://localhost:8888/rest/events", HttpMethod.PUT, httpEntity,
				RESTResponseData.class);
		
//		@SuppressWarnings("unchecked")
//		Map<String, Object> apiResponse2 = (Map<String, Object>) restTemplate.exchange("http://localhost:8888/rest/events", HttpMethod.PUT, httpEntity,
//				Map.class, Collections.EMPTY_MAP);

		assertNotNull(apiResponse);

		//Asserting the response of the API.
		return apiResponse;

	}
	
	@Test
	public void testJoinEvent() throws JsonProcessingException {
		ResponseEntity<RESTResponseData> signInResponse = authenticateUser("existed@email.com", "password");
		List<String> cookies = signInResponse.getHeaders().get("Set-Cookie");

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Cookie",StringUtils.join(cookies,';'));
		HttpEntity<String> requestEntity = new HttpEntity<String>(requestHeaders);
		
		// Invoking the API
		
		ResponseEntity<RESTResponseData> response = checkSession(requestEntity);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));

		RESTResponseData responseData = response.getBody();
		assertTrue(responseData.getMessage().equals("Session Found"));
		
		Coordinates eCoor = new Coordinates();
		eCoor.setLatitude(12.34);
		eCoor.setLongitude(111.23);
		
		Coordinates uCoor = new Coordinates();
		uCoor.setLatitude(12.33);
		uCoor.setLongitude(111.24);

		Map<String, Object> apiResponse = attemptAddEvent("EventOne", eCoor, "DescOne", "Swim", System.nanoTime()+10000L, uCoor, StringUtils.join(cookies,';'));
		Object events = apiResponse.get("events");
		List<Event> listEvents = this.eventRepo.findByName("EventOne");
		assertEquals(1, listEvents.size());
		Event anEvent = listEvents.get(0);
		assertEquals("EventOne", anEvent.getName());
		assertEquals("DescOne", anEvent.getDescription());
		
		Long eventId = anEvent.getId();
		Map<String, Object> apiResponse2 = attemptJoinEvent(eventId, StringUtils.join(cookies,';'));
		Set<Registrant> listParticipant = anEvent.getParticipants();
		Registrant participant = null;		
		
		for(Registrant partic: listParticipant){
			if (partic.getEmail().toString() == "existed@email.com"){
				participant = partic;
			}
		}
		assertEquals(true, listParticipant.contains(participant));
	}
	
	private Map<String, Object> attemptJoinEvent(Long Id, String session) throws JsonProcessingException {
		// Building the Request body data
		
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("eventId", Id);
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Cookie", session);
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);

		// Creating http entity object with request body and headers
		HttpEntity<String> httpEntity = new HttpEntity<String>(OBJECT_MAPPER.writeValueAsString(requestBody),
				requestHeaders);

		// Invoking the API
		@SuppressWarnings("unchecked")
		Map<String, Object> apiResponse = restTemplate.postForObject("http://localhost:8888/rest/events/join", httpEntity,
				Map.class, Collections.EMPTY_MAP);


		assertNotNull(apiResponse);

		// Asserting the response of the API.
		return apiResponse;

	}
	
	@Test
	public void testUpdateEventBasic() throws JsonProcessingException {
		ResponseEntity<RESTResponseData> signInResponse = authenticateUser("existed@email.com", "password");
		List<String> cookies = signInResponse.getHeaders().get("Set-Cookie");

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Cookie",StringUtils.join(cookies,';'));
		HttpEntity<String> requestEntity = new HttpEntity<String>(requestHeaders);
		
		// Invoking the API
		
		ResponseEntity<RESTResponseData> response = checkSession(requestEntity);
		assertTrue(response.getStatusCode().equals(HttpStatus.OK));

		RESTResponseData responseData = response.getBody();
		assertTrue(responseData.getMessage().equals("Session Found"));
		
		Coordinates eCoor = new Coordinates();
		eCoor.setLatitude(12.34);
		eCoor.setLongitude(111.23);
		
		Coordinates uCoor = new Coordinates();
		uCoor.setLatitude(12.33);
		uCoor.setLongitude(111.24);

		attemptAddEvent("EventOne", eCoor, "DescOne", "Swim", System.nanoTime()+10000L, uCoor, StringUtils.join(cookies,';'));

		List<Event> listEvents = this.eventRepo.findByName("EventOne");
		assertEquals(1, listEvents.size());
		Event eventOne = listEvents.get(0);
		assertEquals("EventOne", eventOne.getName());
		assertEquals("DescOne", eventOne.getDescription());
		
		
		Long eventOneId = eventOne.getId();

		attemptJoinEvent(eventOneId, StringUtils.join(cookies,';'));
		Set<Registrant> listParticipant = eventOne.getParticipants();
		Registrant participant = null;		
		
		for(Registrant partic: listParticipant){
			if (partic.getEmail().toString() == "existed@email.com"){
				participant = partic;
			}
		}
		assertEquals(true, listParticipant.contains(participant));
		
		//Add the other participant
		Registrant newParticipant = regRepo.findOneByEmail("participant@email.com");
		eventOne.addParticipant(newParticipant);
		eventRepo.save(eventOne);
		assertEquals(2,eventOne.getParticipants().size());
		Registrant newOwner = regRepo.findOneByEmail("newOwner@email.com");
		Registrant currentOwner = regRepo.findOneByEmail("existed@email.com");
		
		//After set up an event and join, now, modify it
		attemptUpdateEvent(eventOneId,"EventOneUpdated", eCoor, "DescOneUpdated", "Soccer", System.nanoTime()+20000L, uCoor, StringUtils.join(cookies,';'), participant, newOwner);
		
		//Verify the event got updated
		Event afterUpdate = eventRepo.findOne(eventOneId);
		assertEquals("EventOneUpdated",afterUpdate.getName());
		assertEquals("DescOneUpdated",afterUpdate.getDescription());
		assertEquals("Soccer",afterUpdate.getCategory().getName());
		assertEquals(2,afterUpdate.getOccurrences().size());
		assertEquals(2,afterUpdate.getOwners().size());
		assertEquals(1,afterUpdate.getParticipants().size());
		assertTrue(afterUpdate.getParticipants().contains(newParticipant));
		assertTrue(afterUpdate.getOwners().contains(newOwner));
		assertTrue(afterUpdate.getOwners().contains(currentOwner));
	}

	private Map<String, Object> attemptUpdateEvent(Long eventId, String name, Coordinates eCoor, String description, String category, long time, Coordinates uCoor, String session, Registrant participantToRemove, Registrant ownerToAdd) throws JsonProcessingException {
		List<Occurrence> occurrencesToAdd =  new ArrayList<Occurrence>();
		List<Occurrence> occurrencesToRemove =  new ArrayList<Occurrence>();
		List<Registrant> ownersToAdd = new ArrayList<Registrant>();
		List<Registrant> ownersToRemove = new ArrayList<Registrant>();
		List<Registrant> participantsToAdd = new ArrayList<Registrant>();
		List<Registrant> participantsToRemove = new ArrayList<Registrant>();
		ownersToAdd.add(ownerToAdd);
		participantsToRemove.add(participantToRemove);
		occurrencesToAdd.add(new Occurrence("",new Timestamp(time)));
		// Building the Request body data		
		Map<String, Object> requestBody = new HashMap<String, Object>();
		requestBody.put("eventId", eventId);
		requestBody.put("eventName", name);
		requestBody.put("eventCoordinates", eCoor);
		requestBody.put("eventDescription", description);
		requestBody.put("eventCategory", category);
		requestBody.put("eventTime", time);
		requestBody.put("callerCoordinates", uCoor);
		requestBody.put("occurrencesToAdd", occurrencesToAdd);
		requestBody.put("occurrencesToRemove", occurrencesToRemove);
		requestBody.put("ownersToAdd", ownersToAdd);
		requestBody.put("ownersToRemove", ownersToRemove);
		requestBody.put("participantsToAdd", participantsToAdd);
		requestBody.put("participantsToRemove", participantsToRemove);
		
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set("Cookie", session);
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);

		// Creating http entity object with request body and headers
		HttpEntity<String> httpEntity = new HttpEntity<String>(OBJECT_MAPPER.writeValueAsString(requestBody),
				requestHeaders);

		// Invoking the API
		@SuppressWarnings("unchecked")
		Map<String, Object> apiResponse = restTemplate.postForObject("http://localhost:8888/rest/events/update", httpEntity,
				Map.class, Collections.EMPTY_MAP);


		assertNotNull(apiResponse);

		// Asserting the response of the API.
		return apiResponse;

	}
}
