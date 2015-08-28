package skinny.controller.feature

import skinny.micro.contrib.JSONParamsAutoBinderSupport

/**
 * Merging JSON request body into Scalatra params.
 *
 * When you'd like to avoid merging JSON request body into params in some actions, please separate controllers.
 */
trait JSONParamsAutoBinderFeature
    extends JSONParamsAutoBinderSupport {

}
