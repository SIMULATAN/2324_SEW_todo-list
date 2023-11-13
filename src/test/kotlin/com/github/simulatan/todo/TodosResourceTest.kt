package com.github.simulatan.todo

import com.github.simulatan.todo.model.Todo
import com.github.simulatan.todo.resource.TodoResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.json.Json
import jakarta.ws.rs.core.MediaType
import org.hamcrest.Matchers
import org.hamcrest.core.Is
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalDate
import java.util.*

@QuarkusTest
@TestHTTPEndpoint(TodoResource::class)
@TestMethodOrder(
	MethodOrderer.OrderAnnotation::class
)
internal class TodosResourceTest {
	@Test
	@Order(0)
	fun testListReturnsTodosInCorrectOrder() {
		RestAssured.given()
			.`when`().get("list")
			.then()
			.body("size()", Is.`is`(5))
			.body("description[0]", Is.`is`("Change tires."))
			.body("deadline[0]", Is.`is`("2023-10-31"))
			.body("priority[0]", Is.`is`(1))
			.body("description[1]", Is.`is`("Feed cats."))
			.body("deadline[1]", Is.`is`("2023-11-03"))
			.body("priority[1]", Is.`is`(3))
			.body("description[2]", Is.`is`("Do laundry."))
			.body("deadline[2]", Is.`is`("2023-11-05"))
			.body("priority[2]", Is.`is`(2))
			.body("description[3]", Is.`is`("Bake christmas cookies."))
			.body("deadline[3]", Is.`is`("2023-12-24"))
			.body("priority[3]", Is.`is`(1))
			.body("description[4]", Is.`is`("Prepare exam questions."))
			.body("deadline[4]", Is.`is`("2024-06-05"))
			.body("priority[4]", Is.`is`(3))
	}

	@Test
	@Order(0)
	fun testListFilteredByPriorityReturnsCorrectTodos() {
		RestAssured.given()
			.`when`().get("list/1")
			.then()
			.body("size()", Is.`is`(2))
			.body("description", Matchers.hasItems("Bake christmas cookies.", "Change tires."))
		RestAssured.given()
			.`when`().get("list/2")
			.then()
			.body("size()", Is.`is`(1))
			.body("description", Matchers.hasItems("Do laundry."))
		RestAssured.given()
			.`when`().get("list/3")
			.then()
			.body("size()", Is.`is`(2))
			.body("description", Matchers.hasItems("Feed cats.", "Prepare exam questions."))
	}

	@Test
	@Order(0)
	fun testGetTodoByNonExistingIdReturnsNotFound() {
		var idMax: Int = RestAssured.given()
			.`when`().get("list")
			.then()
			.extract()
			.path("id.max()")
		idMax++
		RestAssured.given()
			.`when`().get(idMax.toString())
			.then()
			.statusCode(404)
	}

	@Test
	@Order(0)
	fun testGetTodoByIdReturnsCorrectTodo() {
		val todosExpected: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		for (currTodoExpected in todosExpected) {
			val todoActual: Todo = RestAssured.given()
				.`when`().get("" + currTodoExpected.id)
				.then()
				.extract()
				.`as`(Todo::class.java)
			assertEquals(currTodoExpected.description, todoActual.description)
			assertEquals(currTodoExpected.deadline, todoActual.deadline)
			assertEquals(currTodoExpected.priority, todoActual.priority)
		}
	}

	@Test
	@Order(1)
	fun testAddTodoWithPriorityTooLowReturnsError() {
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Wash car.")
			.add("deadline", "2023-11-04")
			.add("priority", 0)
			.build()
		val body: String = RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
			.then()
			.statusCode(400)
			.extract()
			.body()
			.asString()
		assertEquals(true, body.contains("Priority must be between 1 and 3!"))
	}

	@Test
	@Order(1)
	fun testAddTodoWithPriorityTooLowDoesNotChangeList() {
		val todosExpected: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Wash car.")
			.add("deadline", "2023-11-04")
			.add("priority", 0)
			.build()
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
		val todosActual: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(todosExpected.size, todosActual.size)
		assertEquals(
			false,
			todosActual.stream().anyMatch { t: Todo -> t.description.equals("Wash car.") }
		)
	}

	@Test
	@Order(1)
	fun testAddTodoWithPriorityTooHighReturnsError() {
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Buy christmas presents.")
			.add("deadline", "2023-12-23")
			.add("priority", 4)
			.build()
		val body: String = RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
			.then()
			.statusCode(400)
			.extract()
			.body()
			.asString()
		assertEquals(true, body.contains("Priority must be between 1 and 3!"))
	}

	@Test
	@Order(1)
	fun testAddTodoWithPriorityTooHighDoesNotChangeList() {
		val todosExpected: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Buy christmas presents.")
			.add("deadline", "2023-12-23")
			.add("priority", 4)
			.build()
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
		val todosActual: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(todosExpected.size, todosActual.size)
		assertEquals(
			false,
			todosActual.stream()
				.anyMatch { t: Todo -> t.description.equals("Buy christmas presents.") }
		)
	}

	@Test
	@Order(1)
	fun testAddTodoReturnsCorrectStatusCodeAndUrl() {
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Buy football tickets.")
			.add("deadline", "2023-11-12")
			.add("priority", 3)
			.build()
		val headerLocation: String = RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
			.then()
			.statusCode(201)
			.extract()
			.header("Location")
		val todo: Todo = RestAssured.given()
			.`when`().get(headerLocation)
			.then()
			.extract()
			.`as`(Todo::class.java)
		assertEquals("Buy football tickets.", todo.description)
		assertEquals(LocalDate.of(2023, 11, 12), todo.deadline)
		assertEquals(3, todo.priority)
	}

	@Test
	@Order(1)
	fun testAddTodoChangesList() {
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Return empty bottles.")
			.add("deadline", "2023-11-03")
			.add("priority", 1)
			.build()
		val todosExpected: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(
			false,
			todosExpected.stream()
				.anyMatch { t: Todo -> t.description.equals("Return empty bottles.") }
		)
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
			.then()
			.statusCode(201)
		val todosActual: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(todosExpected.size + 1, todosActual.size)
		assertEquals(
			true,
			todosActual.stream()
				.anyMatch { t: Todo -> t.description.equals("Return empty bottles.") }
		)
	}

	@Test
	@Order(1)
	fun testAddTodoChangesFilteredList() {
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Prepare Quarkus demo.")
			.add("deadline", "2023-11-06")
			.add("priority", 2)
			.build()
		val todosLowBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/1")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosMediumBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/2")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosHighBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/3")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(
			false,
			todosLowBefore.stream()
				.anyMatch { t: Todo -> t.description.equals("Prepare Quarkus demo.") }
		)
		assertEquals(
			false,
			todosMediumBefore.stream()
				.anyMatch { t: Todo -> t.description.equals("Prepare Quarkus demo.") }
		)
		assertEquals(
			false,
			todosHighBefore.stream()
				.anyMatch { t: Todo -> t.description.equals("Prepare Quarkus demo.") }
		)
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
			.then()
			.statusCode(201)
		val todosLowAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/1")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosMediumAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/2")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosHighAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/3")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(todosLowBefore.size, todosLowAfter.size)
		assertEquals(todosMediumBefore.size + 1, todosMediumAfter.size)
		assertEquals(todosHighBefore.size, todosHighAfter.size)
		assertEquals(
			false,
			todosLowAfter.stream()
				.anyMatch { t: Todo -> t.description.equals("Prepare Quarkus demo.") }
		)
		assertEquals(
			true,
			todosMediumAfter.stream()
				.anyMatch { t: Todo -> t.description.equals("Prepare Quarkus demo.") }
		)
		assertEquals(
			false,
			todosHighAfter.stream()
				.anyMatch { t: Todo -> t.description.equals("Prepare Quarkus demo.") }
		)
	}

	@Test
	@Order(1)
	fun testAddTodosListInCorrectOrder() {
		var todoRaw = Json.createObjectBuilder()
			.add("description", "Book a hotel.")
			.add("deadline", "2024-07-10")
			.add("priority", 2)
			.build()
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
			.then()
			.statusCode(201)
		todoRaw = Json.createObjectBuilder()
			.add("description", "Cook delicious bigos.")
			.add("deadline", "2022-12-23")
			.add("priority", 3)
			.build()
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
			.then()
			.statusCode(201)
		todoRaw = Json.createObjectBuilder()
			.add("description", "Buy new wok.")
			.add("deadline", "2022-11-30")
			.add("priority", 1)
			.build()
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().post()
			.then()
			.statusCode(201)
		val todos: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		var deadlineLast: LocalDate = todos[0].deadline!!
		for (i in 1 until todos.size) {
			val currDeadline: LocalDate = todos[i].deadline!!
			assertEquals(true, deadlineLast.isBefore(currDeadline) || deadlineLast.isEqual(currDeadline))
			deadlineLast = currDeadline
		}
	}

	@Test
	@Order(1)
	fun testUpdateTodoByNonExistingIdReturnsNotFound() {
		var idMax: Int = RestAssured.given()
			.`when`().get("list")
			.then()
			.extract()
			.path("id.max()")
		idMax++
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Cut toenails.")
			.add("deadline", "2023-11-07")
			.add("priority", 3)
			.build()
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().put("" + idMax)
			.then()
			.statusCode(404)
	}

	@Test
	@Order(1)
	fun testUpdateTodoChangesTodo() {
		val todos: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		var todo: Todo = RestAssured.given()
			.`when`().get("" + todos[1].id)
			.then()
			.extract()
			.`as`(Todo::class.java)
		assertEquals(false, todo.description.equals("Iron suit."))
		assertEquals(false, todo.deadline!!.isEqual(LocalDate.of(2023, 10, 16)))
		assertEquals(false, todo.priority == 2)
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Iron suit.")
			.add("deadline", "2023-10-16")
			.add("priority", 2)
			.build()
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().put("" + todo.id)
			.then()
			.statusCode(204)
		todo = RestAssured.given()
			.`when`().get("" + todo.id)
			.then()
			.extract()
			.`as`(Todo::class.java)
		assertEquals(true, todo.description.equals("Iron suit."))
		assertEquals(true, todo.deadline!!.isEqual(LocalDate.of(2023, 10, 16)))
		assertEquals(true, todo.priority == 2)
	}

	@Test
	@Order(1)
	fun testUpdateTodoChangesList() {
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Cuddle cats.")
			.add("deadline", "2023-11-04")
			.add("priority", 3)
			.build()
		val todosBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(
			false,
			todosBefore.stream().anyMatch { t: Todo -> t.description.equals("Cuddle cats.") }
		)
		val todo: Todo = todosBefore[2]
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().put("" + todo.id)
			.then()
			.statusCode(204)
		val todosAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(todosBefore.size, todosAfter.size)
		assertEquals(
			true,
			todosAfter.stream().anyMatch { t: Todo -> t.description.equals("Cuddle cats.") }
		)
	}

	@Test
	@Order(1)
	fun testUpdateTodoChangesFilteredList() {
		val todoRaw = Json.createObjectBuilder()
			.add("description", "Repair vacuum.")
			.add("deadline", "2023-11-10")
			.add("priority", 2)
			.build()
		val todosLowBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/1")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosMediumBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/2")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosHighBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/3")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(
			false,
			todosLowBefore.stream().anyMatch { t: Todo -> t.description.equals("Repair vacuum.") }
		)
		assertEquals(
			false,
			todosMediumBefore.stream()
				.anyMatch { t: Todo -> t.description.equals("Repair vacuum.") }
		)
		assertEquals(
			false,
			todosHighBefore.stream()
				.anyMatch { t: Todo -> t.description.equals("Repair vacuum.") }
		)
		val todo: Todo = todosLowBefore[1]
		RestAssured.given()
			.contentType(MediaType.APPLICATION_JSON)
			.body(todoRaw.toString())
			.`when`().put("" + todo.id)
			.then()
			.statusCode(204)
		val todosLowAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/1")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosMediumAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/2")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosHighAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/3")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(todosLowBefore.size - 1, todosLowAfter.size)
		assertEquals(todosMediumBefore.size + 1, todosMediumAfter.size)
		assertEquals(todosHighBefore.size, todosHighAfter.size)
		assertEquals(
			false,
			todosLowAfter.stream().anyMatch { t: Todo -> t.description.equals("Repair vacuum.") }
		)
		assertEquals(
			true,
			todosMediumAfter.stream()
				.anyMatch { t: Todo -> t.description.equals("Repair vacuum.") }
		)
		assertEquals(
			false,
			todosHighAfter.stream().anyMatch { t: Todo -> t.description.equals("Repair vacuum.") }
		)
	}

	@Test
	@Order(2)
	fun testDeleteTodoDeletesTodo() {
		val todosBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todo: Todo = todosBefore[3]
		RestAssured.given()
			.`when`().delete("" + todo.id)
			.then()
			.statusCode(204)
		RestAssured.given()
			.`when`().get("" + todo.id)
			.then()
			.statusCode(404)
	}

	@Test
	@Order(2)
	fun testDeleteTodoByNonExistingIdReturnsNotFound() {
		var idMax: Int = RestAssured.given()
			.`when`().get("list")
			.then()
			.extract()
			.path("id.max()")
		idMax++
		RestAssured.given()
			.`when`().delete(idMax.toString())
			.then()
			.statusCode(404)
	}

	@Test
	@Order(2)
	fun testDeleteTodoChangesList() {
		val todosBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todo: Todo = todosBefore[2]
		RestAssured.given()
			.`when`().delete("" + todo.id)
			.then()
			.statusCode(204)
		val todosAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(todosBefore.size - 1, todosAfter.size)
		assertEquals(
			false,
			todosAfter.stream()
				.anyMatch { t: Todo -> t.description.equals(todo.description) }
		)
	}

	@Test
	@Order(2)
	fun testDeleteTodoChangesFilteredList() {
		val todosLowBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/1")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosMediumBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/2")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosHighBefore: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/3")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todo: Todo = todosHighBefore[2]
		assertEquals(
			false,
			todosLowBefore.stream()
				.anyMatch { t: Todo -> t.description.equals(todo.description) }
		)
		assertEquals(
			false,
			todosMediumBefore.stream()
				.anyMatch { t: Todo -> t.description.equals(todo.description) }
		)
		assertEquals(
			true,
			todosHighBefore.stream()
				.anyMatch { t: Todo -> t.description.equals(todo.description) }
		)
		RestAssured.given()
			.`when`().delete("" + todo.id)
			.then()
			.statusCode(204)
		val todosLowAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/1")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosMediumAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/2")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		val todosHighAfter: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list/3")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		assertEquals(todosLowBefore.size, todosLowAfter.size)
		assertEquals(todosMediumBefore.size, todosMediumAfter.size)
		assertEquals(todosHighBefore.size - 1, todosHighAfter.size)
		assertEquals(
			false,
			todosLowAfter.stream()
				.anyMatch { t: Todo -> t.description.equals(todo.description) }
		)
		assertEquals(
			false,
			todosMediumAfter.stream()
				.anyMatch { t: Todo -> t.description.equals(todo.description) }
		)
		assertEquals(
			false,
			todosHighAfter.stream()
				.anyMatch { t: Todo -> t.description.equals(todo.description) }
		)
	}

	@Test
	@Order(99)
	fun testListInCorrectOrderAfterAllTests() {
		val todos: List<Todo> = listOf(
			*RestAssured.given()
				.`when`().get("list")
				.then()
				.extract()
				.response()
				.`as`(Array<Todo>::class.java)
		)
		var deadlineLast: LocalDate = todos[0].deadline!!
		for (i in 1 until todos.size) {
			val currDeadline: LocalDate = todos[i].deadline!!
			assertEquals(true, deadlineLast.isBefore(currDeadline) || deadlineLast.isEqual(currDeadline))
			deadlineLast = currDeadline
		}
	}
}
