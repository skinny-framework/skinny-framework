package skinny.oauth2.client

import java.net.URL

import org.scalatest._

class OAuthClientSpec extends FunSpec with Matchers {

  // https://github.com/settings/applications
  describe("GitHub OAuth") {
    it("create authentication url") {
      val request = AuthenticationRequest(OAuth2Provider.GitHub)
        .clientId("xxx") // Client ID
        .state("session_id_hash_value")
        .redirectURI("http://localhost:8080/callback")

      val url = new URL(request.locationURI)
      (url.getProtocol + "://" + url.getHost + url.getPath) should equal("https://github.com/login/oauth/authorize")

      val params: Map[String, String] = url.getQuery.split("&").map { e: String =>
        val kv: Array[String] = e.split("=")
        (kv(0), kv(1))
      }.toMap
      params should equal(Map(
        "state" -> "session_id_hash_value",
        "redirect_uri" -> "http%3A%2F%2Flocalhost%3A8080%2Fcallback",
        "client_id" -> "xxx"
      ))
    }

    /*
    it("create an access token request") {
      val response = OAuthClient.accessToken {
        AccessTokenRequest(OAuthProvider.GitHub)
          .grantType(GrantType.AuthorizationCode)
          .state("session_id_hash_value")
          .clientId("754da4b1e21a67320880")
          .clientSecret("8fee36acd90f2d822b7650de785f5183c1004397")
          .code("7ca198ad5a32b3ce6551")
          .redirectURI("http://localhost:8080/callback")
      }
      println(response.accessToken)
    }
    */
  }

  describe("Facebook OAuth") {
    it("create authentication url") {
      val request = AuthenticationRequest(OAuth2Provider.Facebook)
        .clientId("xxx") // App ID
        .state("session_id_hash_value")
        .redirectURI("http://localhost:8080/callback")

      val url = new URL(request.locationURI)
      (url.getProtocol + "://" + url.getHost + url.getPath) should equal("https://graph.facebook.com/oauth/authorize")

      val params: Map[String, String] = url.getQuery.split("&").map { e: String =>
        val kv: Array[String] = e.split("=")
        (kv(0), kv(1))
      }.toMap
      params should equal(Map(
        "state" -> "session_id_hash_value",
        "redirect_uri" -> "http%3A%2F%2Flocalhost%3A8080%2Fcallback",
        "client_id" -> "xxx"
      ))
    }
  }

  // https://console.developers.google.com/
  describe("Google OAuth") {
    it("create authentication url") {
      val request = AuthenticationRequest(OAuth2Provider.Google)
        .clientId("xxx.apps.googleusercontent.com") // CLIENT ID
        .state("session_id_hash_value")
        .responseType("code")
        .scope("openid email")
        .redirectURI("http://localhost:8080/oauth2callback")

      val url = new URL(request.locationURI)
      (url.getProtocol + "://" + url.getHost + url.getPath) should equal("https://accounts.google.com/o/oauth2/auth")

      val params: Map[String, String] = url.getQuery.split("&").map { e: String =>
        val kv: Array[String] = e.split("=")
        (kv(0), kv(1))
      }.toMap
      params should equal(Map(
        "state" -> "session_id_hash_value",
        "scope" -> "openid+email",
        "redirect_uri" -> "http%3A%2F%2Flocalhost%3A8080%2Foauth2callback",
        "client_id" -> "xxx.apps.googleusercontent.com",
        "response_type" -> "code"
      ))
    }
  }

  // http://instagram.com/developer/clients/manage/
  describe("Instagram OAuth") {
    it("create authentication url") {
      val request = AuthenticationRequest(OAuth2Provider.Instagram)
        .clientId("xxx") // CLIENT ID
        .state("session_id_hash_value")
        .responseType("code")
        .redirectURI("http://localhost:8080/callback")

      val url = new URL(request.locationURI)
      (url.getProtocol + "://" + url.getHost + url.getPath) should equal("https://api.instagram.com/oauth/authorize")

      val params: Map[String, String] = url.getQuery.split("&").map { e: String =>
        val kv: Array[String] = e.split("=")
        (kv(0), kv(1))
      }.toMap
      params should equal(Map(
        "response_type" -> "code",
        "state" -> "session_id_hash_value",
        "redirect_uri" -> "http%3A%2F%2Flocalhost%3A8080%2Fcallback",
        "client_id" -> "xxx"
      ))
    }
  }

  // https://integrate.37signals.com/apps
  describe("Basecamp OAuth") {
    it("create authentication url") {
      val request = AuthenticationRequest(OAuth2Provider.Basecamp)
        .clientId("xxx") // Client ID
        .state("session_id_hash_value")
        .param("type", "web_server")
        .redirectURI("http://localhost:8080/callback")

      val url = new URL(request.locationURI)
      (url.getProtocol + "://" + url.getHost + url.getPath) should equal("https://launchpad.37signals.com/authorization/new")

      val params: Map[String, String] = url.getQuery.split("&").map { e: String =>
        val kv: Array[String] = e.split("=")
        (kv(0), kv(1))
      }.toMap
      params should equal(Map(
        "state" -> "session_id_hash_value",
        "redirect_uri" -> "http%3A%2F%2Flocalhost%3A8080%2Fcallback",
        "type" -> "web_server",
        "client_id" -> "xxx"
      ))
    }
  }

  // https://www.dropbox.com/developers/apps
  describe("Dropbox OAuth") {
    it("create authentication url") {
      val request = AuthenticationRequest(OAuth2Provider.Dropbox)
        .clientId("xxx") // Client ID
        .state("session_id_hash_value")
        .responseType("code")
        .redirectURI("http://localhost:8080/callback")

      val url = new URL(request.locationURI)
      (url.getProtocol + "://" + url.getHost + url.getPath) should equal("https://www.dropbox.com/1/oauth2/authorize")

      val params: Map[String, String] = url.getQuery.split("&").map { e: String =>
        val kv: Array[String] = e.split("=")
        (kv(0), kv(1))
      }.toMap
      params should equal(Map(
        "response_type" -> "code",
        "state" -> "session_id_hash_value",
        "redirect_uri" -> "http%3A%2F%2Flocalhost%3A8080%2Fcallback",
        "client_id" -> "xxx"
      ))
    }
  }

  // https://e.developer.yahoo.co.jp/dashboard/
  describe("YConnect (Yahoo! JAPAN) OAuth") {
    it("create authentication url") {
      val request = AuthenticationRequest(OAuth2Provider.YConnect)
        .clientId("xxx") // Application ID
        .state("session_id_hash_value")
        .responseType("code")
        .scope("openid")
        .redirectURI("http://localhost:8080/callback")

      val url = new URL(request.locationURI)
      (url.getProtocol + "://" + url.getHost + url.getPath) should equal("https://auth.login.yahoo.co.jp/yconnect/v1/authorization")

      val params: Map[String, String] = url.getQuery.split("&").map { e: String =>
        val kv: Array[String] = e.split("=")
        (kv(0), kv(1))
      }.toMap
      params should equal(Map(
        "scope" -> "openid",
        "response_type" -> "code",
        "state" -> "session_id_hash_value",
        "redirect_uri" -> "http%3A%2F%2Flocalhost%3A8080%2Fcallback",
        "client_id" -> "xxx"
      ))
    }
  }

}
