package com.jeffmaury.moviebuddy.tests

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.HeaderNames._
import scala.concurrent.duration._
import scala.util.Random

class VoteScenario extends Simulation {
  val server = System.getProperty("buddyserver", "http://localhost:8080")
  val totalUsers = Integer.getInteger("gatling.users", 100).toInt
  val loops = Integer.getInteger("gatling.loops", 1000).toInt
  val protocol = http.disableCaching.disableFollowRedirect
  val genres = Array("Documentary", "Drama", "History", "War", "Comedy", "Action", "Family", "Thriller", "Biography", "Animation", "Adventure", "Talk-Show", "Crime", "News", "Horror", "Fantasy", "Mystery")
  /*
   * List of 10 users ids participating to the vote randomly generated as code time
   */
  val users = Array(1084, 7655, 428, 4359, 569, 6947, 272, 1512, 8023, 7556)
   /*
   * Liste of 10 movies id being voted to randomly generated as code time
   */
  val movies = Array(593, 264, 582, 564, 724, 403, 653, 334, 287, 771)
  val rnd = new Random
  
  def incrementLoop(session:Session, name:String):Session = {
    if (session.contains(name)) {
      session.set(name, session(name).as[Int] + 1)
    } else {
      session.set(name, 0);
    }
  }
  val scn = scenario(s"Vote ($totalUsers users/$loops loops)")
    .repeat(loops) {
      exec(session => incrementLoop(session, "genreindex")).
      exec(
        http("Search Movies by genre (1)")
          .get(session => server + "/movies/search/genre/" + genres(session("genreindex").as[Int] % genres.length) + "/10")
          .check(status.is(200))).
      exec(session => incrementLoop(session, "genreindex")).
      exec(
        http("Search Movies by genre (2)")
          .get(session => server + "/movies/search/genre/" + genres(session("genreindex").as[Int] % genres.length) + "/10")
          .check(status.is(200))).
      exec(session => incrementLoop(session, "userindex")).
      exec(session => session.set("userid", users(session("userindex").as[Int] % users.length))).
      exec(session => incrementLoop(session, "movieindex")).
      exec(session => session.set("movieid", movies(session("movieindex").as[Int] % movies.length))).
      exec(session => session.set("rate", rnd.nextInt(11))).
      exec(
        http("Vote")
          .post(session => server + "/rates")
          .body(StringBody("{\"userId\":${userid},\"movieId\":${movieid},\"rate\":${rate}}"))
          .asJSON
          .check(status.is(301)))
    }

  setUp(scn
    .inject(rampUsers(totalUsers) over (totalUsers seconds)).protocols(protocol))
}
