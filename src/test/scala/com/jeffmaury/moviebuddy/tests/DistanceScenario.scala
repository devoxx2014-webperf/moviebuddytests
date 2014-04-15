package com.jeffmaury.moviebuddy.tests

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.HeaderNames._
import scala.concurrent.duration._
import scala.util.Random

class DistanceScenario extends Simulation {
  val server = System.getProperty("buddyserver", "http://localhost:8080")
  val totalUsers = Integer.getInteger("gatling.users", 100).toInt
  val loops = Integer.getInteger("gatling.loops", 1000).toInt
  val protocol = http.disableCaching.disableFollowRedirect

  val scn = scenario(s"Distance ($totalUsers users/$loops loops)")
    .repeat(loops) {
      exec(
        http("Distance 3022 <-> 9649")
          .get(server + "/users/distance/3022/9649")
          .check(status.is(200))).
      exec(
        http("Distance 2349 <-> 496")
          .get(server + "/users/distance/2349/496")
          .check(status.is(200)))
    }

  setUp(scn
    .inject(rampUsers(totalUsers) over (totalUsers seconds)).protocols(protocol))
}
