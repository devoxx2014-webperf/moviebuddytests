package com.jeffmaury.moviebuddy.tests

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import io.gatling.http.request._
import scala.concurrent.duration._
import bootstrap._
import assertions._

class AllMoviesLoadingScenario extends Simulation {
  val server = System.getProperty("buddyserver", "http://localhost:8080");
  val scn = scenario("Loading all movies").repeat(50) {
    exec(
      http("Loading all movies")
        .get(server + "/movies")
        .check(status.is(200)))
  }

  setUp(scn
    .inject(ramp(10 users) over (10 seconds)))
}

