package com.jeffmaury.moviebuddy.tests

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.HeaderNames._
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import io.gatling.core.session.Expression

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
  def rnd = ThreadLocalRandom.current
  
  def incrementCounter(name:String): Expression[Session] = session =>
    session(name)
    .validate[Int]
    .map(value => session.set(name, value + 1))
    .recover(session.set(name, 0))

  val voteBody: Expression[String] = session =>
  	for {
  		userindex <- session("userindex").validate[Int]
  		movieindex <- session("movieindex").validate[Int]
  		userid = users(userindex % users.length)
  		movieid = movies(movieindex % users.length)
  		rate = rnd.nextInt(11)
  	} yield s"""{"userId":$userid,"movieId":$movieid,"rate":$rate}"""
    
  val scn = scenario(s"Vote ($totalUsers users/$loops loops)")
    .repeat(loops) {
      exec(incrementCounter("genreindex")).
      exec(
        http("Search Movies by genre (1)")
          .get(session => session("genreindex").validate[Int].map(i => s"$server/movies/search/genre/${i % genres.length}/10"))
          .check(status.is(200))).
      exec(incrementCounter("genreindex")).
      exec(
        http("Search Movies by genre (2)")
          .get(session => session("genreindex").validate[Int].map(i => s"$server/movies/search/genre/${i % genres.length}/10"))
          .check(status.is(200))).
      exec(incrementCounter("userindex")).
      exec(incrementCounter("movieindex")).
      exec(
        http("Vote")
          .post(server + "/rates")
          .body(StringBody(voteBody))
          .asJSON
          .check(status.is(301)))
    }

  setUp(scn
    .inject(rampUsers(totalUsers) over (totalUsers seconds)).protocols(protocol))
}
