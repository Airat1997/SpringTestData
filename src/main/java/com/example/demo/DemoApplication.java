package com.example.demo;

import jakarta.persistence.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

@RestController
@RequestMapping("/topic")
class RestApiDemoController {
	private final TopicRepository topicRepository;
	private final MessageRepository messageRepository;

	public RestApiDemoController(TopicRepository topicRepository, MessageRepository messageRepository) {
		this.topicRepository = topicRepository;
		this.messageRepository = messageRepository;
	}

	@GetMapping
	Iterable<Topic> getTopics() {
		return topicRepository.findAll();
	}
	@GetMapping("/{topicId}")
	public ResponseEntity<TopicWithMessages> getAllMessagesTopicById(@PathVariable String topicId) {
		Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
		List<Message> messages = messageRepository.findByTopicId(topicId);
		TopicWithMessages topicWithMessages = new TopicWithMessages();
		topicWithMessages.setCreated(topic.getCreated());
		topicWithMessages.setId(topic.getId());
		topicWithMessages.setName(topic.getName());
		topicWithMessages.setMessages(messages);
		return ResponseEntity.ok(topicWithMessages);
	}
	@PostMapping
	Topic postTopic(@RequestBody TopicWithMessageRequest request) {
		Topic topic = new Topic(request.getTopicName());
		topic = topicRepository.save(topic);
		Message message = new Message(request.getMessage().getId(), request.getMessage().getText(), request.getMessage().getAuthor(), request.getMessage().getCreated());
		message.setTopic(topic);
		messageRepository.save(message);
		return topic;
	}

	@PutMapping
	public ResponseEntity<Topic> updateTopic(@RequestBody TopicRequest request) {
		// TODO check client data
		Optional<Topic> existingTopicOptional = topicRepository.findById(request.getId());
		if (existingTopicOptional.isPresent()) {
			Topic newTopic = new Topic(request.getId(), request.getName(), request.getCreated());
//			if (request.getTopicName() != null) {
//				existingTopic.setName(request.getTopicName());
//			}
			Topic updatedTopic = topicRepository.save(newTopic);
			return ResponseEntity.ok(updatedTopic);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/{topicId}/message")
	public ResponseEntity<TopicWithMessages> postMessage(@PathVariable String topicId, @RequestBody MessageRequest request){
		Topic topic = topicRepository.findById(topicId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
		Message message = new Message(request.getId(), request.getText(), request.getAuthor(), request.getCreated());
		message.setTopic(topic);
		messageRepository.save(message);
		List<Message> messages = messageRepository.findByTopicId(topicId);
		TopicWithMessages topicWithMessages = new TopicWithMessages();
		topicWithMessages.setCreated(topic.getCreated());
		topicWithMessages.setId(topic.getId());
		topicWithMessages.setName(topic.getName());
		topicWithMessages.setMessages(messages);
		return ResponseEntity.ok(topicWithMessages);
	}
	@PutMapping("/{topicId}/message")
	public ResponseEntity<TopicWithMessages> putMessage(@PathVariable String topicId, @RequestBody MessageRequest request){
		return postMessage(topicId, request);
	}








	@DeleteMapping("/{id}")
	void deleteTopic(@PathVariable String id) {
		topicRepository.deleteById(id);
	}



}

interface TopicRepository extends CrudRepository<Topic, String> {}
interface MessageRepository extends CrudRepository<Message, String> {
	List<Message> findByTopicId(String topicId);
}

@Entity
class Topic {
	@Id
	private String id;
	@Column(name = "name")
	private String name;

	@Column(name = "created")
	private String created;

	public Topic() {
	}

	@OneToMany(mappedBy = "topic")
	private List<Message> messages;
	public Topic(String id, String name, String created) {
		this.id = id;
		this.name = name;
		this.created = created;
	}

	public Topic(String name) {
		this(UUID.randomUUID().toString(), name, LocalDateTime.now().toString());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreated() {
		return created;
	}
}

@Entity
class Message {
	@Id
	private String id;
	@Column
	private String text;
	@Column
	private String author;
	@Column
	private String created;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "topic_id")
	private Topic topic;
	public Message(String id, String text, String author, String created){
		this.id = id;
		this.text = text;
		this.author = author;
		this.created = created;
	}
	public Message() {
	}
	public String getId() {
		return id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCreated() {
		return created;
	}

	public Topic getTopic() {
		return topic;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}
}

class TopicWithMessages {
	private String id;
	private String name;

	private String created;
	private List<Message> messages;

	public TopicWithMessages(){
	};

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

}

class TopicWithMessageRequest {

	private String id;

	private String created;
	private String topicName;
	private Message message;
	public TopicWithMessageRequest() {
	}
	public TopicWithMessageRequest(String id, String created, String topicName, Message message) {
		this.topicName = topicName;
		this.message = message;
		this.id = id;
		this.created = created;
	}
	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}
}

class TopicRequest {
	private String id;
	private String name;
	private String created;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCreated() {
		return created;
	}
}

class MessageRequest {
	private String id;
	private String text;
	private String author;
	private String created;

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getAuthor() {
		return author;
	}

	public String getCreated() {
		return created;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setCreated(String created) {
		this.created = created;
	}
}
