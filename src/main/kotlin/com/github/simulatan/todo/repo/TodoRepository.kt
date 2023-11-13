package com.github.simulatan.todo.repo

import com.github.simulatan.todo.model.Todo
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validator
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
open class TodoRepository {
	@Inject
	private lateinit var entityManager: EntityManager

	@Inject
	private lateinit var validator: Validator

	fun tryValidation(item: Any) {
		validator.validate(item).let {
			if (it.isNotEmpty()) {
				throw ConstraintViolationException(it)
			}
		}
	}

	fun addTodo(todo: Todo) = insert(todo)

	@Transactional
	fun insert(todo: Todo): Todo {
		tryValidation(todo)
		entityManager.persist(todo)
		return todo
	}

	fun getTodo(id: Int) = entityManager.find(Todo::class.java, id) ?: throw NotFoundException()

	fun getAllTodos(): List<Todo> =
		entityManager.createNamedQuery(Todo.QUERY_GET_ALL, Todo::class.java).resultList

	fun `getAllTodos by priority`(priority: Int)
		= entityManager.createNamedQuery(Todo.QUERY_GET_ALL_BY_PRIORITY, Todo::class.java)
			.apply {
				setParameter("priority", priority)
			}.resultList

	@Transactional
	fun delete(id: Int) {
		entityManager.remove(getTodo(id))
	}

	@Transactional
	fun update(id: Int, changes: Todo) {
		val existing = getTodo(id)
		entityManager.merge(existing)
		existing.updateWith(changes)
		tryValidation(existing)
	}
}
