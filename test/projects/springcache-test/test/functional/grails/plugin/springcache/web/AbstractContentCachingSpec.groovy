package grails.plugin.springcache.web

import grails.plugin.geb.GebSpec
import grails.plugin.springcache.SpringcacheService
import musicstore.Album
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.grails.plugins.springsecurity.service.AuthenticateService
import spock.lang.Shared
import musicstore.auth.*
import musicstore.pages.*
import org.grails.rateable.*

abstract class AbstractContentCachingSpec extends GebSpec {

	@Shared SpringcacheService springcacheService = ApplicationHolder.application.mainContext.springcacheService
	@Shared AuthenticateService authenticateService = ApplicationHolder.application.mainContext.authenticateService

	def cleanup() {
		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}

	protected void setUpAlbumRating(Album album, User rater, double stars) {
		Rating.withNewSession {
			def rating = new Rating(stars: stars, raterId: rater.id, raterClass: User.name)
			rating.save(failOnError: true)
			def link = new RatingLink(rating: rating, ratingRef: album.id, type: "album")
			link.save(failOnError: true)
		}
	}

	protected User setUpUser(username, userRealName) {
		User.withTransaction {tx ->
			def userRole = Role.findByAuthority("ROLE_USER")
			def user = new User(username: username, userRealName: userRealName, email: "$username@energizedwork.com", enabled: true)
			user.passwd = authenticateService.encodePassword("password")
			user.save(failOnError: true)

			userRole.addToPeople user
			userRole.save(failOnError: true)

			return user
		}
	}

	protected void tearDownUsers() {
		def userRole = Role.findByAuthority("ROLE_USER")
		User.withTransaction {tx ->
			User.list().each {
				userRole.removeFromPeople(it)
			}
		}
	}

	void logout() {
		go "/logout"
		page HomePage
	}

}