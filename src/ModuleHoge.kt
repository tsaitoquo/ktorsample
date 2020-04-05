package com.example

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.content.TextContent
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing

data class IndexData(val items: List<Int>)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            // ここに設定を書ける
        }
    }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Authentication) {
        basic {
            validate { if (it.name == "user" && it.password == "password") UserIdPrincipal("user") else null }
        }
    }
    install(StatusPages) {
        exception<IllegalArgumentException> { cause ->
            call.respond(HttpStatusCode.BadRequest)
        }
        status(HttpStatusCode.BadRequest) {
            call.respond(TextContent("${it.value} ${it.description} ですよー", ContentType.Text.Plain.withCharset(Charsets.UTF_8), it))
        }
    }
    routing {
        route("/hoge1") {
            get("/") {
                call.respondText("Hello hoge/")
            }
            get("/hello") {
                call.respondText("Hello hoge/hello")
            }
        }

        get("/json") {
            call.respond(mapOf("hoge" to "fuag"))
        }
        post ("/json") {
            val request = call.receive<SampleRequest>()
            val response = SampleResponse(request.id, "OK gorugo")
            call.respond(response)
        }
        get("/exception") {
            throw IllegalArgumentException("exception example.")
        }
        authenticate {
            get("/auth") {
                val user = call.authentication.principal<UserIdPrincipal>()
                call.respondText("userName = ${user!!.name} is Authenticated!!")
            }
        }
        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3))), ""))
        }
        get("/{...}") {
            call.respondText("hi there")
        }

        route("/login") {
            get {
                call.respond(FreeMarkerContent("login.ftl", null))
            }
            post {
                val post = call.receiveParameters()
                if (post["username"] != null && post["username"] == post["password"]) {
                    call.respondText("OK")
                } else {
                    call.respond(FreeMarkerContent("login.ftl", mapOf("error" to "Invalid login")))
                }
            }
        }
    }

}

data class SampleRequest(val id: Int)
data class SampleResponse(val id: Int, val message: String)
