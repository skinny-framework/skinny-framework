package skinny.activeimplicits

/**
  * ActiveSupport-ish implicit conversions.
  */
object AllImplicits extends AllImplicits

trait AllImplicits extends NumberImplicits with StringImplicits with InflectorImplicits
