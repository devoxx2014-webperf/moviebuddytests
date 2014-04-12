package com.jeffmaury.moviebuddy.tests

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.HeaderNames._
import io.gatling.http.request._
import scala.concurrent.duration._

class AllMoviesLoadingScenario extends Simulation {
  val server = System.getProperty("buddyserver", "http://localhost:8080")
  val totalUsers = Integer.getInteger("gatling.users", 100).toInt
  val loops = Integer.getInteger("gatling.loops", 1000).toInt
  val scn = scenario(s"Loading all movies ($totalUsers users/$loops loops)")
    .repeat(loops) {
      exec(
        http("Loading all movies")
          .get(server + "/movies")
          .check(status.is(200)))
    }

  setUp(scn
    .inject(rampUsers(totalUsers) over (totalUsers seconds)))
}
