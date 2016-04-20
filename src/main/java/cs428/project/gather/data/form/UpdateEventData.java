package cs428.project.gather.data.form;

import cs428.project.gather.data.model.*;
import cs428.project.gather.validator.*;

import java.util.*;
import java.lang.reflect.Type;
import com.google.gson.*;
import org.springframework.validation.Errors;

public class UpdateEventData extends NewEventData {
	private Long eventId;
	private List<String> ownersToAdd = new ArrayList<String>();
	private List<String> ownersToRemove = new ArrayList<String>();
	private List<String> participantsToAdd = new ArrayList<String>();
	private List<String> participantsToRemove = new ArrayList<String>();

	public static UpdateEventData parseIn(String rawData, AbstractValidator validator, Errors errors) {
		System.out.println("rawData: " + rawData);
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			// Register an adapter to manage the date types as long values
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return new Date(json.getAsJsonPrimitive().getAsLong());
			}
		});
		Gson gson = builder.create();
		UpdateEventData updateEventData = gson.fromJson(rawData, UpdateEventData.class);
		updateEventData.validate(validator, errors);
		return updateEventData;
	}

	public void validate(AbstractValidator validator, Errors errors) {
		validator.validate(this, errors);
	}

	public List<String> getOwnersToAdd() {
		return ownersToAdd;
	}

	public List<String> getOwnersToRemove() {
		return ownersToRemove;
	}

	public List<String> getParticipantsToAdd() {
		return participantsToAdd;
	}

	public List<String> getParticipantsToRemove() {
		return participantsToRemove;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long id) {
		this.eventId = id;
	}
}
