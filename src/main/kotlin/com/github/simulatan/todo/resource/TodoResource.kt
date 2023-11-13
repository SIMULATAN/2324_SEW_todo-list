package com.github.simulatan.todo.resource

import com.github.simulatan.todo.model.Todo
import com.github.simulatan.todo.repo.TodoRepository
import com.github.simulatan.todo.util.mapValidatorException
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo

@Path("/api/todos")
class TodoResource {

	@Inject
	private lateinit var repository: TodoRepository

	@Context
	private lateinit var uriInfo: UriInfo

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.MEDIA_TYPE_WILDCARD)
	fun create(todo: Todo): Response {
		val created = mapValidatorException { return@mapValidatorException repository.insert(todo) }
		val uriBuilder = uriInfo.absolutePathBuilder.path(created.id.toString())
		return Response.created(uriBuilder.build()).build()
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	fun get(@PathParam("id") id: Int): Todo = repository.getTodo(id)

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	fun getAll(): List<Todo> = repository.getAllTodos()

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list/{priority}")
	fun getAllByPriority(@PathParam("priority") priority: Int): List<Todo> =
		repository.`getAllTodos by priority`(priority)

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	fun update(@PathParam("id") id: Int, todo: Todo): Response {
		val existing = get(id)
		mapValidatorException { repository.update(id, todo) }
		return Response.noContent().build()
	}

	@DELETE
	@Path("/{id}")
	fun delete(@PathParam("id") id: Int) = repository.delete(id)
}
