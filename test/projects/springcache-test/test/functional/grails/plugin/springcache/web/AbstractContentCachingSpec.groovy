package grails.plugin.springcache.web

import auth.User
import geb.spock.GebSpec
import grails.plugin.springcache.SpringcacheService
import musicstore.Album
import musicstore.pages.HomePage
import org.apache.shiro.crypto.hash.Sha256Hash
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.grails.rateable.*
import spock.lang.*

abstract class AbstractContentCachingSpec extends GebSpec {

	@Shared SpringcacheService springcacheService = ApplicationHolder.application.mainContext.springcacheService

	def cleanup() {
		if (!isStepwise()) {
			clearAllCaches()
		}
	}

	def cleanupSpec() {
		clearAllCaches()
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
		User.withTransaction { tx ->
			def user = new User(username: username, passwordHash: new Sha256Hash("password").toHex(), name: userRealName)
			user.addToPermissions("*:*")
			user.save(failOnError: true)
		}
	}

	protected void tearDownUsers() {
		User.withNewSession { session ->
			User.list()*.delete()
			session.flush()
		}
	}

	void logout() {
		go "/auth/signOut"
		page HomePage
	}

	private void clearAllCaches() {
		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}

	private boolean isStepwise() {
		getClass().getAnnotation(Stepwise)
	}

}