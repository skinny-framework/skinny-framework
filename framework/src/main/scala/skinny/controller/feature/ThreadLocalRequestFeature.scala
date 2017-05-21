package skinny.controller.feature

import skinny.controller.ThreadLocalRequest

trait ThreadLocalRequestFeature { self: BeforeAfterActionFeature =>

  /**
    * Stores request as a thread-local value.
    */
  beforeAction() {
    ThreadLocalRequest.save(request(context))
  }

}
