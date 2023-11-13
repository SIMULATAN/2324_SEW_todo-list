package com.github.simulatan.todo;


import com.github.simulatan.todo.model.Todo;
import com.github.simulatan.todo.repo.TodoRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.time.LocalDate;

@ApplicationScoped
@Alternative
@Priority(999)
public class MockTodosRepository extends TodoRepository {
	@PostConstruct
	public void init() {
		Todo todoCookies = new Todo();
		todoCookies.setDescription("Bake christmas cookies.");
		todoCookies.setDeadline(LocalDate.of(2023, 12, 24));
		todoCookies.setPriority(1);

		this.addTodo(todoCookies);

		Todo todoCats = new Todo();
		todoCats.setDescription("Feed cats.");
		todoCats.setDeadline(LocalDate.of(2023, 11, 3));
		todoCats.setPriority(3);

		this.addTodo(todoCats);

		Todo todoExams = new Todo();
		todoExams.setDescription("Prepare exam questions.");
		todoExams.setDeadline(LocalDate.of(2024, 6, 5));
		todoExams.setPriority(3);

		this.addTodo(todoExams);

		Todo todoTires = new Todo();
		todoTires.setDescription("Change tires.");
		todoTires.setDeadline(LocalDate.of(2023, 10, 31));
		todoTires.setPriority(1);

		this.addTodo(todoTires);

		Todo todoLaundry = new Todo();
		todoLaundry.setDescription("Do laundry.");
		todoLaundry.setDeadline(LocalDate.of(2023, 11, 5));
		todoLaundry.setPriority(2);

		this.addTodo(todoLaundry);
	}
}
