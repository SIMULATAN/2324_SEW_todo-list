package com.github.simulatan.todo.model

import jakarta.persistence.*
import org.hibernate.validator.constraints.Range
import java.time.LocalDate

@Entity
@NamedQueries(
	NamedQuery(name= Todo.QUERY_GET_ALL, query="SELECT t FROM Todo t ORDER BY t.deadline"),
	NamedQuery(name= Todo.QUERY_GET_ALL_BY_PRIORITY, query="SELECT t FROM Todo t WHERE t.priority = :priority ORDER BY t.deadline")
)
class Todo {
	fun updateWith(todo: Todo) {
		todo.description?.apply { description = this }
		todo.deadline?.apply { deadline = this }
		todo.priority?.apply { priority = this }
	}

	@get:Id
	@get:GeneratedValue(strategy = GenerationType.SEQUENCE)
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	var id: Int = 0
	var description: String? = null
	var deadline: LocalDate? = null
	// fuck you quarkus
	@Range(min = 1, max = 3, message = "Priority {org.hibernate.validator.constraints.Range.message}!")
	var priority: Int = 0

	companion object {
		const val QUERY_GET_ALL: String = "Todo.getAll"
		const val QUERY_GET_ALL_BY_PRIORITY: String = "Todo.getAllByPriority"
	}
}
